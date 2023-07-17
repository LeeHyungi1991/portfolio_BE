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
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WebsocketHandler extends TextWebSocketHandler {
    @Value("${spring.jwt.secret.access}")
    private String accessSecretKey;
    private final Gson gson;
    private Clock clock = () -> new Date(0);
    private JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private static Logger logger = LoggerFactory.getLogger(WebsocketHandler.class);
    private static final Map<String, Room> rooms = new HashMap<>();

    public static void sendMessageToAll(String message) throws IOException {
        for (Room room : rooms.values()) {
            for (CustomWebSocketSession session : room.getSessions()) {
                session.getSession().sendMessage(new TextMessage(message));
            }
        }
    }
    public static void sendMessage(String account, String message) throws IOException {
        Optional<org.springframework.web.socket.WebSocketSession> session = Optional.empty();
        for (Room value : rooms.values()) {
            for (CustomWebSocketSession webSocketSession : value.getSessions()) {
                if (webSocketSession.getEmail().equals(account)) {
                    session = Optional.of(webSocketSession.getSession());
                    break;
                }
            }
        }
        if (session.isPresent()) {
            session.get().sendMessage(new TextMessage(message));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            Map<String, Object> payload = jsonFactory.fromString(message.getPayload(), Map.class);
            String[] query = session.getUri().getQuery().split("&");
            String token = query[0].split("=")[1];
            String roomNumber = query[1].split("=")[1];
            Jws<Claims> jws = Jwts.parser().setSigningKey(accessSecretKey.getBytes(StandardCharsets.UTF_8)).setClock(clock).parseClaimsJws(token);
            String email = jws.getBody().getSubject();
            String name = jws.getBody().get("name").toString();
            payload.put("name", name);
            String type = payload.get("type").toString();
            Room room = rooms.get(roomNumber);
            int startTurn = room.getStartTurn();
            if (type.equals("omok")) {
                if (room.getRoomStatus() == GameStatus.WAITING) {
                    session.sendMessage(new TextMessage("{\"type\":\"warn\",\"message\":\"게임이 아직 시작되지 않았습니다.\",\"name\":\"system\"}"));
                    return;
                }
                if (!room.sessions.get(room.getNextTurn()).getEmail().equals(email)) {
                    session.sendMessage(new TextMessage("{\"type\":\"warn\",\"message\":\"상대방의 턴입니다.\",\"name\":\"system\"}"));
                    return;
                }
                room.turnToNext();
            }
            if (type.equals("start")) {
                if (!room.getSessions().get(0).getEmail().equals(email)) {
                    session.sendMessage(new TextMessage("{\"type\":\"warn\",\"message\":\"방장만 게임을 시작할 수 있습니다.\",\"name\":\"system\"}"));
                    return;
                }
                if (room.getSessions().size() == 1) {
                    session.sendMessage(new TextMessage("{\"type\":\"warn\",\"message\":\"상대방이 없습니다.\",\"name\":\"system\"}"));
                    return;
                }
                room.setNextTurn(room.getStartTurn());
                room.setRoomStatus(GameStatus.PLAYING);
                room.startToNext();
                payload.put("name", "system");
                payload.put("message", "방장 " + name + "님이 게임을 시작했습니다.");
            }
            if (type.equals("end")) {
                payload.put("name", "system");
                room.setRoomStatus(GameStatus.WAITING);
                for (int i = 0; i < room.getSessions().size(); i++) {
                    CustomWebSocketSession vo = room.getSessions().get(i);
                    synchronized (vo.getSession()) {
                        payload.put("owner", i == 0);
                        if (i == 0 && !payload.get("name").equals("system")) {
                            payload.put("name", payload.get("name") + "(방장)");
                        }
                        vo.getSession().sendMessage(new TextMessage(jsonFactory.toString(payload)));
                    }
                }
                return;
            }
            payload.put("turn", room.nextTurn);
            for (int i = 0; i < room.getSessions().size(); i++) {
                CustomWebSocketSession vo = room.getSessions().get(i);
                synchronized (vo.getSession()) {
                    if (!type.equals("chat")) {
                        payload.put("myColor", i == startTurn ? 1 : 2);
                        payload.put("myTurn", i == room.getNextTurn());
                    }
                    payload.put("owner", i == 0);
                    if (i == 0 && !payload.get("name").equals("system")) {
                        payload.put("name", payload.get("name") + "(방장)");
                    }
                    vo.getSession().sendMessage(new TextMessage(jsonFactory.toString(payload)));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void afterConnectionEstablished(org.springframework.web.socket.WebSocketSession session) {
        String[] query = session.getUri().getQuery().split("&");
        String token = query[0].split("=")[1];
        String roomNumber = query[1].split("=")[1];
        String roomName = query[2].split("=")[1];
        try {
            Jws<Claims> jws = Jwts.parser().setSigningKey(accessSecretKey.getBytes(StandardCharsets.UTF_8)).setClock(clock).parseClaimsJws(token);
            String name = jws.getBody().get("name").toString();
            String email = jws.getBody().getSubject();
            if (!rooms.containsKey(roomNumber)) {
                rooms.put(roomNumber, new Room(roomName, roomNumber, 2));
            }
            Room room = rooms.get(roomNumber);
            if (room.sessions.size() == 2) {
                session.sendMessage(new TextMessage("{\"type\":\"warn\",\"message\":\"방이 꽉 찼습니다.\",\"name\":\"system\"}"));
                session.close();
                return;
            }
            room.sessions.add(new CustomWebSocketSession(session, email, name));
            if (email.equals(room.sessions.get(0).getEmail())) {
                room.setStartTurn(0);
                room.setNextTurn(0);
            }
            int size = room.sessions.size();
            for (int i = 0; i < size; i++) {
                CustomWebSocketSession vo = room.sessions.get(i);
                String passName = name;
                if (i == 0 && email.equals(vo.getEmail())) {
                    passName = passName + "(방장)";
                }
                synchronized (vo.getSession()) {
                    vo.getSession().sendMessage(new TextMessage("{\"name\":\"system\",\"type\":\"chat\",\"message\":\"" + passName + "님이 입장하셨습니다.\",\"turn\":" + room.nextTurn + ", \"owner\":" + (i == 0) + ", \"users\":" + jsonFactory.toString(room.getUsers()) + "}"));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                WebsocketHandlerBoard.sendMessageToAll(gson.toJson(getOmokRooms(), new TypeToken<List<RoomDto>>(){}.getType()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void afterConnectionClosed(org.springframework.web.socket.WebSocketSession session, CloseStatus status) {
        String[] query = session.getUri().getQuery().split("&");
        String token = query[0].split("=")[1];
        String roomNumber = query[1].split("=")[1];
        try {
            Jws<Claims> jws = Jwts.parser().setSigningKey(accessSecretKey.getBytes(StandardCharsets.UTF_8)).setClock(clock).parseClaimsJws(token);
            String name = jws.getBody().get("name").toString();
            String email = jws.getBody().getSubject();
            Room room = rooms.get(roomNumber);
            boolean wasOwner = room.sessions.get(0).getEmail().equals(email);
            room.sessions.removeIf(vo -> vo.getEmail().equals(email));
            String type = "chat";
            String message = name + "님이 퇴장하셨습니다.";
            if (room.sessions.size() == 1) {
                if (room.getRoomStatus() == GameStatus.PLAYING) {
                    type = "exit";
                    message += " 이에 따라 상대방이 없어서 게임이 자동으로 종료됩니다.";
                    room.setRoomStatus(GameStatus.WAITING);
                }
                if (wasOwner) {
                    if (type.equals("exit")) {
                        message += " 또한";
                    }
                    message += " 방장이 퇴장함에 따라 방장이 " + room.sessions.get(0).getName() + "님으로 변경되었습니다.";
                }
            }
            room.startToPrev();
            room.turnToPrev();
            if (room.sessions.size() == 0) {
                rooms.remove(roomNumber);
            }
            int size = room.sessions.size();
            for (int i = 0; i < size; i++) {
                CustomWebSocketSession vo = room.sessions.get(i);
                synchronized (vo.getSession()) {
                    vo.getSession().sendMessage(new TextMessage("{\"name\":\"system\",\"type\":\"" + type + "\",\"message\":\"" + message + "\",\"turn\":" + room.nextTurn + ", \"owner\":" + (i == 0) + ", \"users\":" + jsonFactory.toString(room.getUsers()) + "}"));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                WebsocketHandlerBoard.sendMessageToAll(gson.toJson(getOmokRooms(), new TypeToken<List<RoomDto>>(){}.getType()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static List<RoomDto> getOmokRooms() {
        return rooms.values().stream().map(RoomDto::new).collect(Collectors.toList());
    }
    public static List<RoomDto> getOmokRoomsByPage(int page) {
        return rooms.values().stream().map(RoomDto::new).skip(page).limit(10).collect(Collectors.toList());
    }
}
