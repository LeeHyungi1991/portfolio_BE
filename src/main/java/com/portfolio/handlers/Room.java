package com.portfolio.handlers;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class Room {
    List<CustomWebSocketSession> sessions = new ArrayList<>();
    String roomNumber;
    String roomName;
    int startTurn;
    int nextTurn;
    GameStatus roomStatus;
    int roomSize;
    Room(String roomName, String roomNumber, int roomSize) {
        this.roomNumber = roomNumber;
        this.roomName = roomName;
        this.startTurn = 0;
        this.nextTurn = 0;
        this.roomStatus = GameStatus.WAITING;
        this.roomSize = roomSize;
    }
    public void startToPrev() {
        this.startTurn--;
        if (this.startTurn < 0) {
            this.startTurn = this.sessions.size() - 1;
        }
    }
    public void startToNext() {
        this.startTurn++;
        if (this.startTurn >= this.sessions.size()) {
            this.startTurn = 0;
        }
    }
    public void turnToPrev() {
        this.nextTurn--;
        if (this.nextTurn < 0) {
            this.nextTurn = this.sessions.size() - 1;
        }
    }
    public void turnToNext() {
        this.nextTurn++;
        if (this.nextTurn >= this.sessions.size()) {
            this.nextTurn = 0;
        }
    }

    public List<Map<String, Object>> getUsers() {
        List<Map<String, Object>> users = new ArrayList<>();
        for (int i = 0; i < this.sessions.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            CustomWebSocketSession session = this.sessions.get(i);
            map.put("seq", i);
            map.put("name", session.getName());
            map.put("email", session.getEmail());
            map.put("isOwner", i == 0);
            users.add(map);
        }
        return users;
    }
}
