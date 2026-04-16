package com.wujie.im.websocket;

import org.springframework.stereotype.Component;
import jakarta.websocket.server.ServerEndpoint;

@Component
@ServerEndpoint("/ws/server")
public class WsServerEndpoint {
    // Simple WebSocket endpoint placeholder for STOMP or other advanced features
}
