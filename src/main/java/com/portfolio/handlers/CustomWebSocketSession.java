package com.portfolio.handlers;

import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

@Data
public class CustomWebSocketSession {
    private final WebSocketSession session;
    private String email;
    private String name;

    CustomWebSocketSession(WebSocketSession session, String email, String name) {
        this.session = session;
        this.email = email;
        this.name = name;
    }
}
