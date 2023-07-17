package com.portfolio.handlers;

public enum GameStatus {
    WAITING("대기중"),
    PLAYING("게임중");

    String status;
    GameStatus(String status) {
        this.status = status;
    }
}
