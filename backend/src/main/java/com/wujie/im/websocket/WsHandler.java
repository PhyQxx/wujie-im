package com.wujie.im.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wujie.im.common.JwtUtil;
import com.wujie.im.entity.Conversation;
import com.wujie.im.entity.Message;
import com.wujie.im.service.ConversationService;
import com.wujie.im.service.GroupService;
import com.wujie.im.service.MessageService;
import com.wujie.im.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WsHandler implements WebSocketHandler {
    private final Map<Long, WebSocketSession> onlineUsers = new ConcurrentHashMap<>();

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private MessageService messageService;
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private UserService userService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket连接建立: sessionId={}", session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = message.getPayload().toString();
        log.info("WS收到原始消息 sessionId={}: {}", session.getId(), payload);
        JSONObject json = JSONUtil.parseObj(payload);
        String type = json.getStr("type");
        log.info("WS消息类型: {}, sessionId={}, onlineUsers={}", type, session.getId(), onlineUsers.keySet());

        switch (type) {
            case "auth":
                handleAuth(session, json);
                break;
            case "heartbeat":
                session.sendMessage(new TextMessage("{\"type\":\"heartbeat\",\"data\":{\"ts\":" + System.currentTimeMillis() + "}}"));
                break;
            case "send_message":
                handleSendMessage(json);
                break;
            case "read_message":
                handleReadMessage(json);
                break;
            case "recall_message":
                handleRecallMessage(json);
                break;
            default:
                break;
        }
    }

    private void handleAuth(WebSocketSession session, JSONObject json) {
        String payload = json.toString();
        String token = json.getStr("token");
        log.info("WebSocket原始消息: {}", payload);
        log.info("WebSocket提取的token: {}", token);
        if (token == null || token.isBlank()) {
            log.error("WebSocket认证失败: token为空或null");
            try {
                session.sendMessage(new TextMessage("{\"type\":\"auth\",\"data\":{\"code\":401,\"msg\":\"token为空\"}}"));
            } catch (IOException ex) { throw new RuntimeException(ex); }
            return;
        }
        log.info("WebSocket认证 token长度={}", token.length());
        try {
            if (!jwtUtil.validateToken(token)) {
                // 尝试解析token以获取更多错误信息
                try {
                    jwtUtil.parseToken(token);
                    log.error("WebSocket认证失败: token验证失败 (parse成功但validate失败)");
                } catch (Exception e) {
                    log.error("WebSocket认证失败: token验证失败, parse异常: {} - {}", e.getClass().getName(), e.getMessage());
                }
                session.sendMessage(new TextMessage("{\"type\":\"auth\",\"data\":{\"code\":401,\"msg\":\"token无效\"}}"));
                return;
            }
            Long userId = jwtUtil.getUserId(token);
            log.info("WebSocket认证成功: userId={}, userIdType={}, sessionId={}, onlineUsers.size={}, onlineUsers={}", userId, userId != null ? userId.getClass().getName() : "null", session.getId(), onlineUsers.size(), onlineUsers.keySet());
            onlineUsers.put(userId, session);
            session.getAttributes().put("userId", userId);
            userService.updateStatus(userId, "ONLINE");
            log.info("WebSocket认证后 onlineUsers: {}", onlineUsers.keySet());
            session.sendMessage(new TextMessage("{\"type\":\"auth\",\"data\":{\"code\":200,\"msg\":\"认证成功\"}}"));
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("WebSocket认证失败: 签名错误 - {}", e.getMessage());
            try { session.sendMessage(new TextMessage("{\"type\":\"auth\",\"data\":{\"code\":401,\"msg\":\"签名错误\"}}")); } catch (IOException ex) { throw new RuntimeException(ex); }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("WebSocket认证失败: token已过期 - {}", e.getMessage());
            try { session.sendMessage(new TextMessage("{\"type\":\"auth\",\"data\":{\"code\":401,\"msg\":\"token已过期\"}}")); } catch (IOException ex) { throw new RuntimeException(ex); }
        } catch (Exception e) {
            log.error("WebSocket认证失败: error={}, type={}", e.getMessage(), e.getClass().getName(), e);
            try { session.sendMessage(new TextMessage("{\"type\":\"auth\",\"data\":{\"code\":401,\"msg\":\"认证失败: " + e.getMessage() + "\"}}")); } catch (IOException ex) { throw new RuntimeException(ex); }
        }
    }

    private void handleSendMessage(JSONObject json) {
        Long senderId = json.getLong("senderId");
        Long conversationId = json.getLong("conversationId");
        String content = json.getStr("content");
        String contentType = json.getStr("contentType", "TEXT");
        String metaStr = json.getStr("meta");

        log.info("handleSendMessage: senderId={}, conversationId={}, content={}", senderId, conversationId, content);

        // 保存消息（meta中可能包含atAll字段），推送由MessageService内部处理
        Message msg = messageService.sendMessage(senderId, conversationId, content, contentType, metaStr);
        log.info("handleSendMessage: saved msgId={}, senderId={}, conversationId={}", msg.getId(), msg.getSenderId(), msg.getConversationId());
    }

    private void handleReadMessage(JSONObject json) {
        Long userId = json.getLong("userId");
        Long conversationId = json.getLong("conversationId");
        Long messageId = json.getLong("messageId");
        messageService.markAsRead(userId, conversationId, messageId);
    }

    private void handleRecallMessage(JSONObject json) {
        Long userId = json.getLong("userId");
        Long messageId = json.getLong("messageId");
        messageService.recallMessage(userId, messageId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: {}", exception.getMessage());
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            WebSocketSession currentSession = onlineUsers.get(userId);
            // 只有当前session关闭时才更新状态，新session已连接则不更新
            if (session.equals(currentSession)) {
                onlineUsers.remove(userId);
                userService.updateStatus(userId, "OFFLINE");
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSocket连接关闭: sessionId={}, status={}, onlineUsers.size={}", session.getId(), closeStatus, onlineUsers.size());
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            WebSocketSession currentSession = onlineUsers.get(userId);
            // 只有当前session关闭时才更新状态，新session已连接则不更新
            if (session.equals(currentSession)) {
                onlineUsers.remove(userId);
                userService.updateStatus(userId, "OFFLINE");
            }
        }
        log.info("WebSocket连接关闭后 onlineUsers: {}", onlineUsers);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void sendToUser(Long userId, String message) {
        WebSocketSession session = onlineUsers.get(userId);
        log.info("sendToUser: userId={}, session={}, isOpen={}", userId, session != null, session != null && session.isOpen());
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.info("sendToUser: SUCCESS sent to userId={}", userId);
            } catch (IOException e) {
                log.error("发送WebSocket消息失败: userId={}", userId, e);
            }
        } else {
            log.warn("sendToUser: user not online or session null, userId={}", userId);
        }
    }

    public boolean isOnline(Long userId) {
        WebSocketSession session = onlineUsers.get(userId);
        return session != null && session.isOpen();
    }
}
