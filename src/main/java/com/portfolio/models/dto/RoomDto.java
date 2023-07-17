package com.portfolio.models.dto;

import com.portfolio.handlers.GameStatus;
import com.portfolio.handlers.Room;
import lombok.Data;

@Data
public class RoomDto {
    String roomName;
    long roomNumber;
    int startTurn;
    int nextTurn;
    GameStatus roomStatus;
    int roomSize;
    int roomCount;
    public RoomDto(Room room) {
        this.roomName = room.getRoomName();
        this.startTurn = room.getStartTurn();
        this.nextTurn = room.getNextTurn();
        this.roomStatus = room.getRoomStatus();
        this.roomNumber = Long.parseLong(room.getRoomNumber());
        this.roomSize = room.getRoomSize();
        this.roomCount = room.getSessions().size();
    }
}
