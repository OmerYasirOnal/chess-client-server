package com.chess.common;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        CONNECT, DISCONNECT, MOVE, READY, GAME_START, GAME_END, CHAT, 
        CREATE_GAME, JOIN_GAME, GAME_LIST, GAME_LIST_RESPONSE
    }
    
    private MessageType type;
    private String content;
    private String sender;
    private ChessMove move;
    private PlayerInfo playerInfo;
    private String gameId;
    private String timeControl;
    private List<GameInfo> games;
    
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
    
    public String getGameId() {
        return gameId;
    }
    
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    
    public String getTimeControl() {
        return timeControl;
    }
    
    public void setTimeControl(String timeControl) {
        this.timeControl = timeControl;
    }
    
    public List<GameInfo> getGames() {
        return games;
    }
    
    public void setGames(List<GameInfo> games) {
        this.games = games;
    }
    
    public static class PlayerInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String name;
        private ChessPiece.PieceColor color;

        public PlayerInfo(String name, ChessPiece.PieceColor color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public ChessPiece.PieceColor getColor() {
            return color;
        }
    }
    
    public static class GameInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String id;
        private String hostName;
        private String timeControl;
        
        public GameInfo(String id, String hostName, String timeControl) {
            this.id = id;
            this.hostName = hostName;
            this.timeControl = timeControl;
        }
        
        public String getId() {
            return id;
        }
        
        public String getHostName() {
            return hostName;
        }
        
        public String getTimeControl() {
            return timeControl;
        }
    }
} 