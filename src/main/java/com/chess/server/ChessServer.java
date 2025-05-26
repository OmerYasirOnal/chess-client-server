package com.chess.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.chess.common.ChessBoard;
import com.chess.common.ChessMove;
import com.chess.common.ChessPiece;
import com.chess.common.Message;
import com.google.gson.Gson;

public class ChessServer {
    private static final int PORT = 9999;
    private final ExecutorService pool = Executors.newFixedThreadPool(10);
    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final List<GameSession> gameSessions = new ArrayList<>();
    private final Gson gson = new Gson();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chess server started on port " + PORT + "...");
            System.out.println("Waiting for connections...");
            
            // Start the client checker that runs every 60 seconds
            startClientChecker();
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection accepted: " + clientSocket.getInetAddress().getHostAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            pool.shutdown();
            scheduler.shutdown();
        }
    }
    
    private void startClientChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndCleanupClients();
            } catch (Exception e) {
                System.err.println("Error in client checker: " + e.getMessage());
                e.printStackTrace();
            }
        }, 60, 60, TimeUnit.SECONDS);
        
        System.out.println("Client checker scheduled to run every 60 seconds");
    }
    
    private void checkAndCleanupClients() {
        int disconnectedCount = 0;
        
        for (Iterator<ClientHandler> it = clients.iterator(); it.hasNext();) {
            ClientHandler client = it.next();
            if (!client.isConnected()) {
                System.out.println("Client checker removing disconnected client: " + 
                    (client.getUsername() != null ? client.getUsername() : "unknown"));
                
                removeClient(client);
                disconnectedCount++;
            }
        }
        
        if (disconnectedCount > 0) {
            System.out.println("Client checker removed " + disconnectedCount + " disconnected clients");
        }
    }
    
    public void broadcast(Message message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
    
    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client disconnected. Active connections: " + clients.size());
        
        // Check client's game session
        GameSession gameSession = findGameSessionByClient(client);
        if (gameSession != null) {
            // Get the opponent before removing the session
            ClientHandler opponent = gameSession.getOpponent(client);
            
            // If the game is still in progress, notify the other player about the disconnection
            if (gameSession.getStatus() == GameStatus.IN_PROGRESS) {
                // Send disconnect message to opponent
                Message disconnectMessage = new Message(Message.MessageType.DISCONNECT);
                disconnectMessage.setContent(client.getUsername() + " disconnected from the game.");
                if (opponent != null) {
                    opponent.sendMessage(disconnectMessage);
                }
                
                // Update game status to WAITING_FOR_OPPONENT
                gameSession.setStatus(GameStatus.WAITING_FOR_OPPONENT);
                
                // Don't remove the session immediately, wait for reconnection
                return;
            }
            
            // If the game was waiting for opponent, just remove it
            if (gameSession.getStatus() == GameStatus.WAITING_FOR_OPPONENT) {
                gameSessions.remove(gameSession);
                return;
            }
            
            // If the game was completed, remove it
            if (gameSession.getStatus() == GameStatus.COMPLETED) {
                gameSessions.remove(gameSession);
                return;
            }
        }
    }
    
    public void handleMessage(Message message, ClientHandler sender) {
        if (message == null || message.getType() == null) {
            System.err.println("Null mesaj veya mesaj tipi");
            return;
        }
        
        switch (message.getType()) {
            case CONNECT:
                handleConnect(message, sender);
                break;
            case READY:
                handleReady(message, sender);
                break;
            case MOVE:
                handleMove(message, sender);
                break;
            case CHAT:
                handleChat(message, sender);
                break;
            case CREATE_GAME:
                handleCreateGame(message, sender);
                break;
            case JOIN_GAME:
                handleJoinGame(message, sender);
                break;
            case GAME_LIST:
                handleGameList(sender);
                break;
            case DELETE_GAME:
                handleDeleteGame(message, sender);
                break;
            case DISCONNECT:
                // Disconnect is handled by the removeClient method
                break;
            case GAME_STATE:
                handleGameState(message, sender);
                break;
            default:
                System.out.println("Unknown message type: " + message.getType());
        }
    }
    
    private void handleConnect(Message message, ClientHandler sender) {
        // Kullanıcı adını al ve istemciye atama
        String username = message.getContent();
        if (username == null || username.trim().isEmpty()) {
            Message errorMsg = new Message(Message.MessageType.ERROR, "Username cannot be empty");
            sender.sendMessage(errorMsg);
            return;
        }

        // Check if username is already in use by an active client
        boolean usernameInUse = false;
        for (ClientHandler client : clients) {
            if (client != sender && 
                username.equals(client.getUsername()) && 
                client.isConnected()) {
                usernameInUse = true;
                break;
            }
        }

        if (usernameInUse) {
            Message errorMsg = new Message(Message.MessageType.ERROR, "Username already in use");
            sender.sendMessage(errorMsg);
            return;
        }

        // Set username for this client
        sender.setUsername(username);
        
        // Create player info object - null için null renk kullanıyoruz
        Message.PlayerInfo playerInfo = new Message.PlayerInfo(username, null);
        sender.setPlayerInfo(playerInfo);
        
        // Create confirmation message
        Message confirmMessage = new Message(Message.MessageType.CONNECT);
        confirmMessage.setContent("Connected as " + username);
        sender.sendMessage(confirmMessage);
        
        // Broadcast to other clients
        Message broadcastMessage = new Message(Message.MessageType.CONNECT);
        broadcastMessage.setContent(username + " has joined");
        broadcastMessage.setSender("Server");
        broadcast(broadcastMessage, sender);
        
        System.out.println("New client connected: " + username);
    }
    
    private void handleReady(Message message, ClientHandler sender) {
        // This method is now a no-op since we're removing the ready button functionality
        // Games will automatically start when a player joins
    }
    
    private void handleMove(Message message, ClientHandler sender) {
        GameSession gameSession = findGameSessionByClient(sender);
        if (gameSession != null) {
            // Check if the game is in progress
            if (gameSession.getStatus() != GameStatus.IN_PROGRESS) {
                // Game is not in progress, reject move
                Message waitingMessage = new Message(Message.MessageType.MOVE);
                waitingMessage.setContent("Cannot make moves - waiting for opponent to join.");
                sender.sendMessage(waitingMessage);
                return;
            }
            
            ChessBoard board = gameSession.getChessBoard();
            ChessMove move = message.getMove();
            
            // Check if the move is valid
            if (move != null && isValidMove(move, board, sender)) {
                // Make the move
                board.makeMove(move);
                
                // Update game state
                board.updateGameState();
                
                // Notify the other player about the move
                Message moveMessage = new Message(Message.MessageType.MOVE);
                moveMessage.setMove(move);
                moveMessage.setSender(sender.getUsername());
                
                ClientHandler opponent = gameSession.getOpponent(sender);
                if (opponent != null) {
                    opponent.sendMessage(moveMessage);
                }
                
                // Check if the game has ended
                checkGameEnd(gameSession);
            } else {
                // Invalid move notification
                Message invalidMoveMessage = new Message(Message.MessageType.MOVE);
                invalidMoveMessage.setContent("Invalid move!");
                sender.sendMessage(invalidMoveMessage);
            }
        }
    }
    
    private void handleChat(Message message, ClientHandler sender) {
        // Only send to players in the same game session
        GameSession gameSession = findGameSessionByClient(sender);
        if (gameSession != null) {
            ClientHandler opponent = gameSession.getOpponent(sender);
            if (opponent != null) {
                message.setSender(sender.getUsername());
                opponent.sendMessage(message);
            }
        }
    }
    
    private void createGameSession(ClientHandler player1, ClientHandler player2) {
        GameSession gameSession = new GameSession(player1, player2);
        gameSessions.add(gameSession);
        
        // Set player colors
        player1.setPlayerInfo(new Message.PlayerInfo(player1.getUsername(), ChessPiece.PieceColor.WHITE));
        player2.setPlayerInfo(new Message.PlayerInfo(player2.getUsername(), ChessPiece.PieceColor.BLACK));
        
        // Create and send game start message
        // For player 1
        Message matchMessage1 = new Message(Message.MessageType.GAME_START);
        matchMessage1.setContent("Game started! Your opponent: " + player2.getUsername() + ". You play as White.");
        
        // Create a Message.PlayerInfo
        Message.PlayerInfo messagePlayerInfo1 = new Message.PlayerInfo(player1.getUsername(), player1.getPlayerInfo().getColor());
        matchMessage1.setPlayerInfo(messagePlayerInfo1);
        
        // Set the game ID
        matchMessage1.setGameId(gameSession.getSessionId());
        
        player1.sendMessage(matchMessage1);
        
        // For player 2
        Message matchMessage2 = new Message(Message.MessageType.GAME_START);
        matchMessage2.setContent("Game started! Your opponent: " + player1.getUsername() + ". You play as Black.");
        
        // Create a Message.PlayerInfo
        Message.PlayerInfo messagePlayerInfo2 = new Message.PlayerInfo(player2.getUsername(), player2.getPlayerInfo().getColor());
        matchMessage2.setPlayerInfo(messagePlayerInfo2);
        
        // Set the game ID
        matchMessage2.setGameId(gameSession.getSessionId());
        
        player2.sendMessage(matchMessage2);
        
        // Start the game logic
        startGame(gameSession);
    }
    
    private void startGame(GameSession gameSession) {
        // Initialize the game board
        ChessBoard board = gameSession.getChessBoard();
        
        // Set the first move to white pieces
        board.setCurrentTurn(ChessPiece.PieceColor.WHITE);
        
        // Update game status to IN_PROGRESS
        gameSession.setStatus(GameStatus.IN_PROGRESS);
        
        // Notify players that the game has started
        Message gameStartMessage = new Message(Message.MessageType.GAME_START);
        gameStartMessage.setContent("Game started! Turn: White");
        
        gameSession.getPlayer1().sendMessage(gameStartMessage);
        gameSession.getPlayer2().sendMessage(gameStartMessage);
        
        System.out.println("Game started between " + gameSession.getPlayer1().getUsername() + 
                          " and " + gameSession.getPlayer2().getUsername());
    }
    
    private boolean isValidMove(ChessMove move, ChessBoard board, ClientHandler player) {
        // First, check if it's the player's turn
        Message.PlayerInfo playerInfo = player.getPlayerInfo();
        ChessPiece.PieceColor playerColor = playerInfo.getColor();
        ChessPiece.PieceColor currentTurn = board.getCurrentTurn();
        
        if (playerColor != currentTurn) {
            return false;
        }
        
        // Check if the move is valid according to the game rules
        return board.isValidPosition(move.getStartRow(), move.getStartCol()) &&
               board.isValidPosition(move.getEndRow(), move.getEndCol()) &&
               board.getPiece(move.getStartRow(), move.getStartCol()) != null &&
               board.getPiece(move.getStartRow(), move.getStartCol()).getColor() == playerColor;
    }
    
    private void checkGameEnd(GameSession gameSession) {
        ChessBoard board = gameSession.getChessBoard();
        
        // Check for checkmate or draw
        if (board.isCheckmate(ChessPiece.PieceColor.WHITE)) {
            board.setGameResult(board.getBlackPlayerName() + " won by checkmate!");
            sendGameEndMessage(gameSession);
        } else if (board.isCheckmate(ChessPiece.PieceColor.BLACK)) {
            board.setGameResult(board.getWhitePlayerName() + " won by checkmate!");
            sendGameEndMessage(gameSession);
        } else if (board.isStalemate(board.getCurrentTurn())) {
            board.setGameResult("Draw by stalemate!");
            sendGameEndMessage(gameSession);
        }
        // Add more end game conditions as needed
    }
    
    private void sendGameEndMessage(GameSession gameSession) {
        ChessBoard board = gameSession.getChessBoard();
        String result = board.getGameResult();
        
        // Update game status to COMPLETED
        gameSession.setStatus(GameStatus.COMPLETED);
        
        Message gameEndMessage = new Message(Message.MessageType.GAME_END);
        gameEndMessage.setContent(result);
        
        ClientHandler player1 = gameSession.getPlayer1();
        ClientHandler player2 = gameSession.getPlayer2();
        
        player1.sendMessage(gameEndMessage);
        player2.sendMessage(gameEndMessage);
        
        System.out.println("Game ended: " + player1.getUsername() + " vs " + player2.getUsername() + " - " + result);
        
        // Remove the game session
        gameSessions.remove(gameSession);
    }
    
    private GameSession findGameSessionByClient(ClientHandler client) {
        for (GameSession session : gameSessions) {
            if (session.hasPlayer(client)) {
                return session;
            }
        }
        return null;
    }
    
    private boolean isClientInGame(ClientHandler client) {
        return findGameSessionByClient(client) != null;
    }
    
    public static void main(String[] args) {
        ChessServer server = new ChessServer();
        server.start();
    }
    
    /**
     * Enum representing the current status of a game session
     */
    private enum GameStatus {
        WAITING_FOR_OPPONENT,  // Game created but waiting for second player
        IN_PROGRESS,           // Game has two players and is active
        COMPLETED              // Game has ended
    }
    
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final ChessServer server;
        private PrintWriter writer;
        private BufferedReader reader;
        private String username;
        private Message.PlayerInfo playerInfo;
        private final Gson gson = new Gson();
        private boolean connected = true;
        private long lastActiveTime;
        private static final long IDLE_TIMEOUT = 300000; // 5 minutes in milliseconds
        
        public ClientHandler(Socket socket, ChessServer server) {
            this.clientSocket = socket;
            this.server = server;
            this.lastActiveTime = System.currentTimeMillis();
            try {
                // Set a socket timeout to detect disconnected clients faster
                this.clientSocket.setSoTimeout(30000); // 30 seconds timeout
            } catch (IOException e) {
                System.err.println("Error setting socket timeout: " + e.getMessage());
            }
        }
        
        @Override
        public void run() {
            try {
                writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                
                // Start a ping thread
                startPingThread();
                
                String line;
                while (connected && (line = reader.readLine()) != null) {
                    // Update last active time on any message received
                    updateLastActiveTime();
                    
                    try {
                        Message message = gson.fromJson(line, Message.class);
                        // Null mesaj veya mesaj tipi kontrolü
                        if (message == null || message.getType() == null) {
                            System.err.println("Geçersiz mesaj alındı: " + line);
                            continue;
                        }
                        
                        // Handle pong message
                        if (message.getType() == Message.MessageType.PONG) {
                            // Do nothing, just update the last active time
                            continue;
                        }
                        
                        server.handleMessage(message, this);
                    } catch (Exception e) {
                        System.err.println("Mesaj işlenirken hata oluştu: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                System.err.println("Connection error with client " + 
                    (username != null ? username : "unknown") + ": " + e.getMessage());
            } finally {
                cleanup();
            }
        }
        
        private void startPingThread() {
            Thread pingThread = new Thread(() -> {
                while (connected) {
                    try {
                        // Sleep for 25 seconds
                        Thread.sleep(25000);
                        
                        // Check if client is idle
                        if (System.currentTimeMillis() - lastActiveTime > IDLE_TIMEOUT) {
                            System.out.println("Client " + (username != null ? username : "unknown") + 
                                " has been idle for too long. Disconnecting.");
                            break;
                        }
                        
                        // Send ping message
                        if (connected) {
                            Message pingMessage = new Message(Message.MessageType.PING);
                            sendMessage(pingMessage);
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        System.err.println("Error in ping thread: " + e.getMessage());
                        break;
                    }
                }
                
                // If we exit the loop, disconnect the client
                if (connected) {
                    cleanup();
                }
            });
            
            pingThread.setDaemon(true);
            pingThread.start();
        }
        
        private void updateLastActiveTime() {
            this.lastActiveTime = System.currentTimeMillis();
        }
        
        private void cleanup() {
            connected = false;
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
            
            // Ensure client is removed from the server
            server.removeClient(this);
            
            // Log the cleanup
            System.out.println("Client cleanup completed for: " + 
                (username != null ? username : "unknown"));
        }
        
        public void sendMessage(Message message) {
            writer.println(gson.toJson(message));
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public Message.PlayerInfo getPlayerInfo() {
            return playerInfo;
        }
        
        public void setPlayerInfo(Message.PlayerInfo playerInfo) {
            this.playerInfo = playerInfo;
        }
        
        // Method to disconnect the client
        public void disconnect() {
            this.connected = false;
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket during disconnect: " + e.getMessage());
            }
        }
        
        public boolean isConnected() {
            return connected && System.currentTimeMillis() - lastActiveTime < IDLE_TIMEOUT;
        }
    }
    
    private static class GameSession {
        private final ClientHandler player1;
        private ClientHandler player2;
        private final ChessBoard chessBoard;
        private String sessionId;
        private String gameType;
        private GameStatus status;
        
        public GameSession(ClientHandler player1, ClientHandler player2) {
            this.player1 = player1;
            this.player2 = player2;
            this.chessBoard = new ChessBoard();
            this.status = GameStatus.WAITING_FOR_OPPONENT;
        }
        
        public ClientHandler getPlayer1() {
            return player1;
        }
        
        public ClientHandler getPlayer2() {
            return player2;
        }
        
        public void setPlayer2(ClientHandler player2) {
            this.player2 = player2;
        }
        
        public ChessBoard getChessBoard() {
            return chessBoard;
        }
        
        public boolean hasPlayer(ClientHandler player) {
            return player1 == player || player2 == player;
        }
        
        public ClientHandler getOpponent(ClientHandler player) {
            if (player1 == player) {
                return player2;
            } else if (player2 == player) {
                return player1;
            }
            return null;
        }
        
        public boolean isAllPlayersReady() {
            // Both players must be present and ready
            return player1 != null && player2 != null &&
                   player1.getPlayerInfo() != null && player2.getPlayerInfo() != null &&
                   player1.getPlayerInfo().isReady() && player2.getPlayerInfo().isReady();
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
        
        public String getGameType() {
            return gameType;
        }
        
        public void setGameType(String gameType) {
            this.gameType = gameType;
        }
        
        public GameStatus getStatus() {
            return status;
        }
        
        public void setStatus(GameStatus status) {
            this.status = status;
        }
    }
    
    // Mevcut oyunların listesini istemciye gönder
    private void handleGameList(ClientHandler sender) {
        List<Message.GameInfo> gameInfos = new ArrayList<>();
        
        // Find game sessions that only have one player (waiting for opponent)
        for (GameSession session : gameSessions) {
            if (session.getPlayer2() == null) {
                Message.GameInfo gameInfo = new Message.GameInfo(
                        session.getSessionId(),
                        session.getPlayer1().getUsername(),
                        session.getGameType()
                );
                gameInfos.add(gameInfo);
            }
        }
        
        // Send game list to client
        Message response = new Message(Message.MessageType.GAME_LIST_RESPONSE);
        response.setGames(gameInfos);
        sender.sendMessage(response);
    }
    
    // Yeni oyun oluştur
    private void handleCreateGame(Message message, ClientHandler sender) {
        // GameInfo nesnesinden bilgileri al
        Message.GameInfo gameInfo = null;
        String gameId = null;
        String gameType = null;
        
        if (message.getGameInfo() != null) {
            gameInfo = message.getGameInfo();
            gameId = gameInfo.getId();
            gameType = gameInfo.getGameType();
        } else {
            // Eski yöntem (geriye dönük uyumluluk için)
            gameId = message.getGameId();
            gameType = message.getGameType();
        }
        
        // ID null ise, UUID oluştur
        if (gameId == null) {
            gameId = java.util.UUID.randomUUID().toString();
        }
        
        // Eğer kullanıcı zaten bir oyun içindeyse, önce o oyunu sonlandır
        GameSession existingSession = findGameSessionByClient(sender);
        if (existingSession != null) {
            gameSessions.remove(existingSession);
        }
        
        // Yeni oyun oturumu oluştur (sadece bir oyuncu ile)
        GameSession gameSession = new GameSession(sender, null);
        gameSession.setSessionId(gameId);
        gameSession.setGameType(gameType);
        gameSession.setStatus(GameStatus.WAITING_FOR_OPPONENT);  // Explicitly set the status
        gameSessions.add(gameSession);
        
        // Oyuncu rengi atama
        sender.setPlayerInfo(new Message.PlayerInfo(sender.getUsername(), ChessPiece.PieceColor.WHITE));
        
        // Oyun tahtasına oyuncu adlarını atama
        ChessBoard board = gameSession.getChessBoard();
        board.setWhitePlayerName(sender.getUsername());
        
        // Oyuncuya bilgi mesajı gönder
        Message confirmMessage = new Message(Message.MessageType.GAME_START);
        confirmMessage.setContent("Waiting for an opponent to join your game.");
        
        // PlayerInfo mesajı oluştur
        Message.PlayerInfo messagePlayerInfo = new Message.PlayerInfo(
                sender.getUsername(), 
                sender.getPlayerInfo().getColor()
        );
        confirmMessage.setPlayerInfo(messagePlayerInfo);
        
        // Oyun ID'sini mesaja ekle (client'ın hatırlaması için)
        confirmMessage.setGameId(gameId);
        
        sender.sendMessage(confirmMessage);
        
        // Tüm kullanıcılara oyun listesinin güncellendiğini bildir
        broadcast(createLobbyUpdateMessage(), null);
        
        System.out.println("New game created by: " + sender.getUsername() + ", ID: " + gameId);
    }
    
    // Var olan bir oyuna katıl
    private void handleJoinGame(Message message, ClientHandler sender) {
        // Check if client is already in a game
        if (isClientInGame(sender)) {
            sendJoinGameFailedMessage(sender, "You are already in a game.");
            return;
        }
        
        // Find the game session
        String gameId = message.getGameId();
        GameSession session = findGameSessionById(gameId);
        
        if (session == null) {
            sendJoinGameFailedMessage(sender, "Game not found.");
            return;
        }
        
        // Check if the game already has two players
        if (session.getPlayer2() != null) {
            sendJoinGameFailedMessage(sender, "Game is full.");
            return;
        }
        
        // Join the game
        session.setPlayer2(sender);
        
        // Create and send the game board
        createGameSession(session.getPlayer1(), sender);
        
        // Set the game ID and type in the session
        GameSession updatedSession = findGameSessionByClient(sender);
        if (updatedSession != null) {
            updatedSession.setSessionId(gameId);
            updatedSession.setGameType(session.getGameType());
            
            // Send a game start message to both players with the game ID
            Message player1Message = new Message(Message.MessageType.GAME_START);
            player1Message.setContent("Game started with " + sender.getUsername());
            player1Message.setGameId(gameId);
            player1Message.setGameType(session.getGameType());
            
            Message player2Message = new Message(Message.MessageType.GAME_START);
            player2Message.setContent("Game started with " + session.getPlayer1().getUsername());
            player2Message.setGameId(gameId);
            player2Message.setGameType(session.getGameType());
            
            // Hazır durumunu güncelle
            session.getPlayer1().getPlayerInfo().setReady(true);
            sender.getPlayerInfo().setReady(true);
            
            // Start the game immediately
            startGame(updatedSession);
            
            updatedSession.getPlayer1().sendMessage(player1Message);
            updatedSession.getPlayer2().sendMessage(player2Message);
        }
        
        // Update the game list for all clients
        broadcastGameList();
    }
    
    // Oyun ID'sine göre oyun oturumunu bul
    private GameSession findGameSessionById(String gameId) {
        for (GameSession session : gameSessions) {
            if (session.getSessionId() != null && session.getSessionId().equals(gameId)) {
                return session;
            }
        }
        return null;
    }
    
    private Message createLobbyUpdateMessage() {
        Message lobbyUpdateMessage = new Message(Message.MessageType.GAME_LIST_RESPONSE);
        
        // Create a list of available games (without player2)
        List<Message.GameInfo> availableGames = new ArrayList<>();
        
        for (GameSession session : gameSessions) {
            if (session.getPlayer2() == null) {
                // This game is still available
                Message.GameInfo gameInfo = new Message.GameInfo(
                    session.getSessionId(),
                    session.getPlayer1().getUsername(),
                    session.getGameType()
                );
                availableGames.add(gameInfo);
            }
        }
        
        lobbyUpdateMessage.setGames(availableGames);
        return lobbyUpdateMessage;
    }
    
    /**
     * Send a failure message when joining a game fails
     */
    private void sendJoinGameFailedMessage(ClientHandler client, String reason) {
        Message errorMessage = new Message(Message.MessageType.JOIN_GAME);
        errorMessage.setContent(reason);
        client.sendMessage(errorMessage);
    }
    
    /**
     * Broadcast the updated game list to all clients
     */
    private void broadcastGameList() {
        broadcast(createLobbyUpdateMessage(), null);
    }
    
    // Bu metod DELETE_GAME mesajını işleyecek
    private void handleDeleteGame(Message message, ClientHandler sender) {
        if (message.getGameId() != null) {
            System.out.println("DELETE_GAME request received for gameId: " + message.getGameId());
            
            // Oyunu ID'ye göre bul
            GameSession gameSession = findGameSessionById(message.getGameId());
            
            if (gameSession != null) {
                System.out.println("Game found: " + gameSession.getSessionId() + ", initiating deletion...");
                
                // Oyundaki diğer oyuncuya bilgi ver (eğer varsa)
                ClientHandler opponent = gameSession.getOpponent(sender);
                if (opponent != null) {
                    Message gameEndMessage = new Message(Message.MessageType.GAME_END);
                    gameEndMessage.setContent(sender.getUsername() + " left the game. Game over.");
                    opponent.sendMessage(gameEndMessage);
                }
                
                // Oyunu listeden kaldır
                gameSessions.remove(gameSession);
                System.out.println("Game session removed. Remaining sessions: " + gameSessions.size());
                
                // Oynamakta olan oyuncuları bilgilendir
                Message confirmationMessage = new Message(Message.MessageType.DELETE_GAME);
                confirmationMessage.setContent("Game has been deleted");
                sender.sendMessage(confirmationMessage);
                
                // Tüm istemcilere güncellenmiş oyun listesini gönder
                broadcastGameList();
            } else {
                System.out.println("Game not found with ID: " + message.getGameId());
                
                // İstemciye oyunun bulunamadığını bildir
                Message notFoundMessage = new Message(Message.MessageType.DELETE_GAME);
                notFoundMessage.setContent("Game not found with ID: " + message.getGameId());
                sender.sendMessage(notFoundMessage);
            }
        } else {
            System.out.println("DELETE_GAME request without gameId");
        }
    }
    
    // Add new method to handle game state changes
    private void handleGameState(Message message, ClientHandler sender) {
        GameSession gameSession = findGameSessionByClient(sender);
        if (gameSession != null) {
            ClientHandler opponent = gameSession.getOpponent(sender);
            if (opponent != null) {
                // Forward the game state message to the opponent
                opponent.sendMessage(message);
            }
        }
    }
} 