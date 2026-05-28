package com.wujie.im.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wujie.im.common.JwtUtil;
import com.wujie.im.entity.Conversation;
import com.wujie.im.entity.Message;
import com.wujie.im.event.MessageRecalledEvent;
import com.wujie.im.event.MessageReadEvent;
import com.wujie.im.event.MessageSentEvent;
import com.wujie.im.service.ConversationService;
import com.wujie.im.service.GroupService;
import com.wujie.im.service.OnlineUserService;
import com.wujie.im.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WsHandler implements WebSocketHandler {
    private static final Map<Long, Set<WebSocketSession>> onlineUsers = new ConcurrentHashMap<>();


    @Value("${spring.application.name:im-server}")
    private String serverId;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private UserService userService;
    @Autowired
    private WsCryptoService wsCryptoService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private OnlineUserService onlineUserService;
    @Autowired
    @org.springframework.context.annotation.Lazy
    private com.wujie.im.service.FriendService friendService;

    public static int getOnlineUserCount() {
        return onlineUsers.size();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.debug("WebSocket连接建立: sessionId={}", session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = message.getPayload().toString();
        log.debug("WS收到消息 sessionId={}: {}", session.getId(), payload);
        JSONObject json = JSONUtil.parseObj(payload);
        String type = json.getStr("type");
        log.debug("WS消息类型: {}, sessionId={}", type, session.getId());

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
        String token = json.getStr("token");
        log.debug("WebSocket认证 token长度={}", token != null ? token.length() : 0);
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
            log.debug("WebSocket认证成功: userId={}, sessionId={}", userId, session.getId());
            onlineUsers.computeIfAbsent(userId, k -> java.util.Collections.synchronizedSet(new java.util.HashSet<>())).add(session);
            session.getAttributes().put("userId", userId);
            onlineUserService.setOnline(userId, serverId);
            userService.updateStatus(userId, "ONLINE");
            notifyFriendsStatusChange(userId, "ONLINE");
            session.sendMessage(new TextMessage("{\"type\":\"auth\",\"data\":{\"code\":200,\"msg\":\"认证成功\"}}"));
            // 认证成功后，发送未读会话同步消息
            syncUnreadConversations(userId);
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
        Long senderId;
        Long conversationId;
        String content;
        String contentType;
        String metaStr;

        if (json.containsKey("data")) {
            String encryptedData = json.getStr("data");
            String decryptedJson = wsCryptoService.decryptData(encryptedData);
            if (decryptedJson == null) {
                log.error("[WS] send_message 解密 data 失败");
                return;
            }
            JSONObject dataObj = JSONUtil.parseObj(decryptedJson);
            senderId = dataObj.getLong("senderId");
            conversationId = dataObj.getLong("conversationId");
            content = dataObj.getStr("content");
            contentType = dataObj.getStr("contentType", "TEXT");
            metaStr = dataObj.getStr("meta");
        } else {
            senderId = json.getLong("senderId");
            conversationId = json.getLong("conversationId");
            content = json.getStr("content");
            contentType = json.getStr("contentType", "TEXT");
            metaStr = json.getStr("meta");
        }

        log.debug("handleSendMessage: senderId={}, conversationId={}", senderId, conversationId);

        Conversation conv = conversationService.getOrCreateSingleConversation(senderId, conversationId);
        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setContentType(contentType);
        msg.setMeta(metaStr);
        msg.setStatus("SENT");

        eventPublisher.publishEvent(new MessageSentEvent(this, msg, conv, senderId));
    }

    private void handleReadMessage(JSONObject json) {
        Long userId = json.getLong("userId");
        Long conversationId = json.getLong("conversationId");
        Long messageId = json.getLong("messageId");
        eventPublisher.publishEvent(new MessageReadEvent(this, userId, conversationId, messageId, userId));
    }

    private void handleRecallMessage(JSONObject json) {
        Long userId = json.getLong("userId");
        Long messageId = json.getLong("messageId");
        Conversation conv = conversationService.getConversationByTypeId(userId);
        if (conv != null) {
            eventPublisher.publishEvent(new MessageRecalledEvent(this, userId, messageId, conv));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: sessionId={}, error={}", session.getId(), exception.getMessage());
        removeSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.debug("WebSocket连接关闭: sessionId={}, status={}", session.getId(), closeStatus);
        removeSession(session);
    }

    private void removeSession(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            Set<WebSocketSession> sessions = onlineUsers.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    onlineUsers.remove(userId);
                    onlineUserService.setOffline(userId);
                    userService.updateStatus(userId, "OFFLINE");
                    notifyFriendsStatusChange(userId, "OFFLINE");
                }
            }
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void notifyFriendsStatusChange(Long userId, String status) {
        try {
            List<Long> friendIds = friendService.getFriendIds(userId);
            String message = JSONUtil.toJsonStr(Map.of(
                    "type", "user_status",
                    "data", Map.of("userId", userId, "status", status)
            ));
            for (Long friendId : friendIds) {
                sendToUser(friendId, message);
            }
        } catch (Exception e) {
            log.error("通知好友状态变更失败: userId={}", userId, e);
        }
    }

    public void sendToUser(Long userId, String message) {
        Set<WebSocketSession> sessions = onlineUsers.get(userId);
        if (sessions != null && !sessions.isEmpty()) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                        log.debug("sendToUser: SUCCESS sent to userId={}, sessionId={}", userId, session.getId());
                    } catch (IOException e) {
                        log.error("发送WebSocket消息失败: userId={}, sessionId={}", userId, session.getId(), e);
                    }
                }
            }
        } else {
            log.debug("sendToUser: user not online or sessions empty, userId={}", userId);
        }
    }

    public void sendToUser(Long userId, String type, Object data) {
        String message;
        if ("message".equals(type) && data != null) {
            String encryptedData = wsCryptoService.encryptData(data);
            if (encryptedData == null) {
                log.error("[WS] 加密 message data 失败，不发送");
                return;
            }
            message = JSONUtil.toJsonStr(Map.of("type", type, "data", encryptedData));
        } else {
            message = JSONUtil.toJsonStr(Map.of("type", type, "data", data));
        }
        sendToUser(userId, message);
    }

    public boolean isOnline(Long userId) {
        return onlineUserService.isOnline(userId);
    }

    /**
     * 同步用户的未读会话列表（用户上线时主动推送）
     */
    private void syncUnreadConversations(Long userId) {
        List<Conversation> unreadConvs = conversationService.getConversations(userId).stream()
                .filter(c -> c.getUnreadCount() != null && c.getUnreadCount() > 0)
                .toList();
        if (!unreadConvs.isEmpty()) {
            String syncMessage = cn.hutool.json.JSONUtil.toJsonStr(
                    java.util.Map.of("type", "unread_sync", "data", unreadConvs)
            );
            sendToUser(userId, syncMessage);
            log.info("syncUnreadConversations: 已推送 {} 个未读会话给用户 {}", unreadConvs.size(), userId);
        }
    }
}
