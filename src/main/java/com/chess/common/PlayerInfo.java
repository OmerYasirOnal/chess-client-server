package com.chess.common;

import java.io.Serializable;

public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private ChessPiece.PieceColor color;
    private boolean isReady;
    
    public PlayerInfo(String username) {
        this.username = username;
        this.isReady = false;
    }
    
    public PlayerInfo(String username, ChessPiece.PieceColor color) {
        this.username = username;
        this.color = color;
        this.isReady = false;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public ChessPiece.PieceColor getColor() {
        return color;
    }
    
    public void setColor(ChessPiece.PieceColor color) {
        this.color = color;
    }
    
    public boolean isReady() {
        return isReady;
    }
    
    public void setReady(boolean ready) {
        isReady = ready;
    }
} 