package com.chess.common;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        CONNECT, DISCONNECT, MOVE, GAME_START, GAME_END, CHAT, READY, PLAYER_INFO
    }
    
    private MessageType type;
    private String content;
    private String sender;
    private ChessMove move;
    private PlayerInfo playerInfo;
    private List<PlayerInfo> playerInfos;
    
    public Message() {
    }
    
    public Message(MessageType type) {
        this.type = type;
    }
    
    public Message(MessageType type, String content) {
        this.type = type;
        this.content = content;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public ChessMove getMove() {
        return move;
    }
    
    public void setMove(ChessMove move) {
        this.move = move;
    }
    
    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }
    
    public void setPlayerInfo(PlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }
    
    public List<PlayerInfo> getPlayerInfos() {
        return playerInfos;
    }
    
    public void setPlayerInfos(List<PlayerInfo> playerInfos) {
        this.playerInfos = playerInfos;
    }
} 