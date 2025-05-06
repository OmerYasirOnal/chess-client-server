package com.chess.common;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        CONNECT, DISCONNECT, MOVE, READY, GAME_START, GAME_END, CHAT, 
        CREATE_GAME, JOIN_GAME, GAME_LIST, GAME_LIST_RESPONSE, DELETE_GAME, ERROR
    }
    
    private MessageType type;
    private String content;
    private String sender;
    private ChessMove move;
    private PlayerInfo playerInfo;
    private String gameId;
    private String gameType;
    private List<GameInfo> games;
    private GameInfo gameInfo;
    
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
    
    public String getGameType() {
        return gameType;
    }
    
    public void setGameType(String gameType) {
        this.gameType = gameType;
    }
    
    public List<GameInfo> getGames() {
        return games;
    }
    
    public void setGames(List<GameInfo> games) {
        this.games = games;
    }
    
    public GameInfo getGameInfo() {
        return gameInfo;
    }
    
    public void setGameInfo(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
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
        private String gameType;
        
        public GameInfo() {
            // Default constructor
        }
        
        public GameInfo(String id, String hostName, String gameType) {
            this.id = id;
            this.hostName = hostName;
            this.gameType = gameType;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getHostName() {
            return hostName;
        }
        
        public void setHostName(String hostName) {
            this.hostName = hostName;
        }
        
        public String getGameType() {
            return gameType;
        }
        
        public void setGameType(String gameType) {
            this.gameType = gameType;
        }
    }
} 