package com.portfolio.configs;

import com.portfolio.handlers.WebsocketHandler;
import com.portfolio.handlers.WebsocketHandlerBoard;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@RequiredArgsConstructor
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebsocketHandler websocketHandler;
    private final WebsocketHandlerBoard websocketHandlerBoard;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(websocketHandler, "ws/chat").setAllowedOrigins("*");
        registry.addHandler(websocketHandlerBoard, "ws/board").setAllowedOrigins("*");
    }
}