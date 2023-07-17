package com.portfolio.handlers;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.portfolio.models.dto.RoomDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Clock;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.portfolio.handlers.WebsocketHandler.getOmokRooms;

@Component
@RequiredArgsConstructor
public class WebsocketHandlerBoard extends TextWebSocketHandler {
    @Value("${spring.jwt.secret.access}")
    private String accessSecretKey;
    private final Gson gson;
    private Clock clock = () -> new Date(0);
    private JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private static Logger logger = LoggerFactory.getLogger(WebsocketHandlerBoard.class);
    private static final List<CustomWebSocketSession> waiters = new ArrayList<>();

    public static void sendMessageToAll(String message) throws IOException {
        for (CustomWebSocketSession session : waiters) {
            synchronized (session.getSession()) {
                session.getSession().sendMessage(new TextMessage(message));
            }
        }
    }
    public static void sendMessage(String account, String message) throws IOException {
        Optional<WebSocketSession> session = Optional.empty();
        for (CustomWebSocketSession webSocketSession : waiters) {
            if (webSocketSession.getEmail().equals(account)) {
                session = Optional.of(webSocketSession.getSession());
                break;
            }
        }
        if (session.isPresent()) {
            session.get().sendMessage(new TextMessage(message));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            for (CustomWebSocketSession webSocketSession : waiters) {
                synchronized (webSocketSession.getSession()) {
                    webSocketSession.getSession().sendMessage(new TextMessage(jsonFactory.toString(getOmokRooms())));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            String[] query = session.getUri().getQuery().split("&");
            String token = query[0].split("=")[1];
            Jws<Claims> jws = Jwts.parser().setSigningKey(accessSecretKey.getBytes(StandardCharsets.UTF_8)).setClock(clock).parseClaimsJws(token);
            String name = jws.getBody().get("name").toString();
            String email = jws.getBody().getSubject();
            waiters.add(new CustomWebSocketSession(session, email, name));
            for (CustomWebSocketSession webSocketSession : waiters) {
                synchronized (webSocketSession.getSession()) {
                    webSocketSession.getSession().sendMessage(new TextMessage(gson.toJson(getOmokRooms(), new TypeToken<List<RoomDto>>(){}.getType())));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            String[] query = session.getUri().getQuery().split("&");
            String token = query[0].split("=")[1];
            Jws<Claims> jws = Jwts.parser().setSigningKey(accessSecretKey.getBytes(StandardCharsets.UTF_8)).setClock(clock).parseClaimsJws(token);
            String email = jws.getBody().getSubject();
            waiters.removeIf(webSocketSession -> webSocketSession.getEmail().equals(email));
            for (CustomWebSocketSession webSocketSession : waiters) {
                synchronized (webSocketSession.getSession()) {
                    webSocketSession.getSession().sendMessage(new TextMessage(gson.toJson(getOmokRooms(), new TypeToken<List<RoomDto>>(){}.getType())));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
