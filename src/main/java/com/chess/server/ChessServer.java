package com.chess.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.chess.common.ChessBoard;
import com.chess.common.ChessMove;
import com.chess.common.ChessPiece;
import com.chess.common.Message;
import com.chess.common.PlayerInfo;
import com.google.gson.Gson;

public class ChessServer {
    private static final int PORT = 9999;
    private final ExecutorService pool = Executors.newFixedThreadPool(10);
    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final List<GameSession> gameSessions = new ArrayList<>();
    private final Gson gson = new Gson();
    
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chess server started on port " + PORT + "...");
            System.out.println("Waiting for connections...");
            
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
            Message disconnectMessage = new Message(Message.MessageType.DISCONNECT);
            disconnectMessage.setContent(client.getUsername() + " left the game.");
            broadcast(disconnectMessage, null);
            
            // If the game is still in progress, notify the other player about the win
            if (!gameSession.getChessBoard().isGameOver()) {
                ClientHandler opponent = gameSession.getOpponent(client);
                if (opponent != null) {
                    gameSession.getChessBoard().setGameResult(opponent.getUsername() + " won. (Opponent left)");
                    
                    Message gameEndMessage = new Message(Message.MessageType.GAME_END);
                    gameEndMessage.setContent(gameSession.getChessBoard().getGameResult());
                    opponent.sendMessage(gameEndMessage);
                }
            }
            
            // Remove session
            gameSessions.remove(gameSession);
        }
    }
    
    public void handleMessage(Message message, ClientHandler sender) {
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
            default:
                System.out.println("Unknown message type: " + message.getType());
        }
    }
    
    private void handleConnect(Message message, ClientHandler sender) {
        String username = message.getContent();
        sender.setUsername(username);
        
        // Set username
        PlayerInfo playerInfo = new PlayerInfo(username);
        sender.setPlayerInfo(playerInfo);
        
        // If there's a waiting player, match them
        if (clients.size() >= 2) {
            boolean matched = false;
            for (ClientHandler client : clients) {
                if (client != sender && !isClientInGame(client)) {
                    createGameSession(sender, client);
                    matched = true;
                    break;
                }
            }
            
            if (!matched) {
                Message waitingMessage = new Message(Message.MessageType.CONNECT);
                waitingMessage.setContent("Waiting for a match...");
                sender.sendMessage(waitingMessage);
            }
        } else {
            Message waitingMessage = new Message(Message.MessageType.CONNECT);
            waitingMessage.setContent("Waiting for an opponent...");
            sender.sendMessage(waitingMessage);
        }
        
        // Notify other users
        Message broadcastMessage = new Message(Message.MessageType.CONNECT);
        broadcastMessage.setContent(username + " connected.");
        broadcast(broadcastMessage, sender);
        
        System.out.println(username + " connected. Active connections: " + clients.size());
    }
    
    private void handleReady(Message message, ClientHandler sender) {
        GameSession gameSession = findGameSessionByClient(sender);
        if (gameSession != null) {
            sender.getPlayerInfo().setReady(true);
            
            // Notify the other player
            Message readyMessage = new Message(Message.MessageType.READY);
            readyMessage.setContent(sender.getUsername() + " is ready.");
            ClientHandler opponent = gameSession.getOpponent(sender);
            if (opponent != null) {
                opponent.sendMessage(readyMessage);
            }
            
            // If both players are ready, start the game
            if (gameSession.isAllPlayersReady()) {
                startGame(gameSession);
            }
        }
    }
    
    private void handleMove(Message message, ClientHandler sender) {
        GameSession gameSession = findGameSessionByClient(sender);
        if (gameSession != null) {
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
        // Create a new game session
        GameSession gameSession = new GameSession(player1, player2);
        gameSessions.add(gameSession);
        
        // Assign colors to players
        player1.setPlayerInfo(new PlayerInfo(player1.getUsername(), ChessPiece.PieceColor.WHITE));
        player2.setPlayerInfo(new PlayerInfo(player2.getUsername(), ChessPiece.PieceColor.BLACK));
        
        // Assign player names to ChessBoard
        ChessBoard board = gameSession.getChessBoard();
        board.setWhitePlayerName(player1.getUsername());
        board.setBlackPlayerName(player2.getUsername());
        
        // Notify players about the match
        Message matchMessage1 = new Message(Message.MessageType.GAME_START);
        matchMessage1.setContent("Your opponent: " + player2.getUsername() + ". Your color: White");
        
        // Create a Message.PlayerInfo from PlayerInfo
        Message.PlayerInfo messagePlayerInfo1 = new Message.PlayerInfo(player1.getUsername(), player1.getPlayerInfo().getColor());
        matchMessage1.setPlayerInfo(messagePlayerInfo1);
        
        player1.sendMessage(matchMessage1);
        
        Message matchMessage2 = new Message(Message.MessageType.GAME_START);
        matchMessage2.setContent("Your opponent: " + player1.getUsername() + ". Your color: Black");
        
        // Create a Message.PlayerInfo from PlayerInfo
        Message.PlayerInfo messagePlayerInfo2 = new Message.PlayerInfo(player2.getUsername(), player2.getPlayerInfo().getColor());
        matchMessage2.setPlayerInfo(messagePlayerInfo2);
        
        player2.sendMessage(matchMessage2);
        
        System.out.println("New game started: " + player1.getUsername() + " vs " + player2.getUsername());
    }
    
    private void startGame(GameSession gameSession) {
        // Initialize the game board
        ChessBoard board = gameSession.getChessBoard();
        
        // Set the first move to white pieces
        board.setCurrentTurn(ChessPiece.PieceColor.WHITE);
        
        // Notify players that the game has started
        Message gameStartMessage = new Message(Message.MessageType.GAME_START);
        gameStartMessage.setContent("Game started! Turn: White");
        
        gameSession.getPlayer1().sendMessage(gameStartMessage);
        gameSession.getPlayer2().sendMessage(gameStartMessage);
    }
    
    private boolean isValidMove(ChessMove move, ChessBoard board, ClientHandler player) {
        // Check if it's the player's turn
        ChessPiece.PieceColor playerColor = player.getPlayerInfo().getColor();
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
    
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final ChessServer server;
        private PrintWriter writer;
        private BufferedReader reader;
        private String username;
        private PlayerInfo playerInfo;
        private final Gson gson = new Gson();
        
        public ClientHandler(Socket socket, ChessServer server) {
            this.clientSocket = socket;
            this.server = server;
        }
        
        @Override
        public void run() {
            try {
                writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                
                String line;
                while ((line = reader.readLine()) != null) {
                    Message message = gson.fromJson(line, Message.class);
                    server.handleMessage(message, this);
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
                server.removeClient(this);
            }
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
        
        public PlayerInfo getPlayerInfo() {
            return playerInfo;
        }
        
        public void setPlayerInfo(PlayerInfo playerInfo) {
            this.playerInfo = playerInfo;
        }
    }
    
    private static class GameSession {
        private final ClientHandler player1;
        private final ClientHandler player2;
        private final ChessBoard chessBoard;
        
        public GameSession(ClientHandler player1, ClientHandler player2) {
            this.player1 = player1;
            this.player2 = player2;
            this.chessBoard = new ChessBoard();
        }
        
        public ClientHandler getPlayer1() {
            return player1;
        }
        
        public ClientHandler getPlayer2() {
            return player2;
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
            return player1.getPlayerInfo().isReady() && 
                   player2.getPlayerInfo().isReady();
        }
    }
} 