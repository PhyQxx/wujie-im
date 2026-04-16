package com.wujie.im.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wujie.im.common.JwtUtil;
import com.wujie.im.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
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

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket连接建立: sessionId={}", session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = message.getPayload().toString();
        JSONObject json = JSONUtil.parseObj(payload);
        String type = json.getStr("type");

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
        try {
            Long userId = jwtUtil.getUserId(token);
            onlineUsers.put(userId, session);
            session.sendMessage(new TextMessage("{\"type\":\"auth\",\"data\":{\"code\":200,\"msg\":\"认证成功\"}}"));
            log.info("用户认证成功: userId={}", userId);
        } catch (Exception e) {
            try {
                session.sendMessage(new TextMessage("{\"type\":\"auth\",\"data\":{\"code\":401,\"msg\":\"认证失败\"}}"));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void handleSendMessage(JSONObject json) {
        Long senderId = json.getLong("senderId");
        Long conversationId = json.getLong("conversationId");
        String content = json.getStr("content");
        String contentType = json.getStr("contentType", "TEXT");
        messageService.sendMessage(senderId, conversationId, content, contentType);
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
        onlineUsers.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSocket连接关闭: sessionId={}, status={}", session.getId(), closeStatus);
        onlineUsers.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void sendToUser(Long userId, String message) {
        WebSocketSession session = onlineUsers.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送WebSocket消息失败: userId={}", userId, e);
            }
        }
    }

    public boolean isOnline(Long userId) {
        WebSocketSession session = onlineUsers.get(userId);
        return session != null && session.isOpen();
    }
}
