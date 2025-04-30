package com.chess.client;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.chess.common.ChessPiece;
import com.chess.common.Message;

public class MainFrame extends JFrame {
    private static final String TITLE = "Chess Game";
    private static final int DEFAULT_WIDTH = 1000;
    private static final int DEFAULT_HEIGHT = 700;
    
    // Panel names for CardLayout
    private static final String LOGIN_PANEL = "login";
    private static final String LOBBY_PANEL = "lobby";
    private static final String WAITING_ROOM_PANEL = "waitingRoom";
    private static final String GAME_PANEL = "game";
    
    private CardLayout cardLayout;
    private JPanel contentPanel;
    
    private LoginPanel loginPanel;
    private LobbyPanel lobbyPanel;
    private WaitingRoomPanel waitingRoomPanel;
    private GamePanel gamePanel;
    private ChessClient client;
    
    public MainFrame() {
        setTitle(TITLE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Close the connection when the frame is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client != null && client.isConnected()) {
                    client.disconnect();
                }
            }
        });
        
        // Set up CardLayout for panel switching
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        setContentPane(contentPanel);
        
        // Create and show the login panel
        showLoginPanel();
    }
    
    public void showLoginPanel() {
        if (loginPanel == null) {
            loginPanel = new LoginPanel();
            
            // Set login listener
            loginPanel.setLoginListener((username, server, port) -> {
                try {
                    client = new ChessClient(server, port, username);
                    client.connect();
                    // After successful connection, show the lobby
                    showLobbyPanel();
                } catch (Exception e) {
                    loginPanel.setErrorMessage("Error connecting: " + e.getMessage());
                }
            });
            
            contentPanel.add(loginPanel, LOGIN_PANEL);
        }
        
        cardLayout.show(contentPanel, LOGIN_PANEL);
    }
    
    public void showLobbyPanel() {
        if (lobbyPanel == null) {
            lobbyPanel = new LobbyPanel(client);
            
            lobbyPanel.setLobbyListener(new LobbyPanel.LobbyListener() {
                @Override
                public void onCreateGame(String timeControl) {
                    createGame(timeControl);
                }
                
                @Override
                public void onJoinGame(LobbyPanel.GameInfo game) {
                    joinGame(game.getId());
                }
                
                @Override
                public void onLogout() {
                    if (client != null) {
                        client.disconnect();
                    }
                    showLoginPanel();
                }
            });
            
            contentPanel.add(lobbyPanel, LOBBY_PANEL);
        }
        
        // Request game list from server
        if (client != null && client.isConnected()) {
            client.setMessageListener(this::handleLobbyMessage);
            
            Message gameListRequest = new Message(Message.MessageType.GAME_LIST);
            client.sendMessage(gameListRequest);
        }
        
        cardLayout.show(contentPanel, LOBBY_PANEL);
    }
    
    public void showWaitingRoomPanel(String gameId, String timeControl) {
        if (waitingRoomPanel == null) {
            waitingRoomPanel = new WaitingRoomPanel();
            
            waitingRoomPanel.setWaitingRoomListener(() -> {
                // Leave game and return to lobby
                leaveGame();
                showLobbyPanel();
            });
            
            contentPanel.add(waitingRoomPanel, WAITING_ROOM_PANEL);
        }
        
        // Update waiting room info
        waitingRoomPanel.setGameInfo(gameId, timeControl);
        
        // Set message listener for waiting room
        client.setMessageListener(this::handleWaitingRoomMessage);
        
        cardLayout.show(contentPanel, WAITING_ROOM_PANEL);
    }
    
    public void startGame(String gameId) {
        // Create and show the game panel when an opponent joins
        if (gamePanel == null) {
            gamePanel = new GamePanel(client);
            contentPanel.add(gamePanel, GAME_PANEL);
        }
        
        // Set the message listener for game messages
        client.setMessageListener(this::handleGameMessage);
        
        cardLayout.show(contentPanel, GAME_PANEL);
    }
    
    private void handleLobbyMessage(Message message) {
        if (lobbyPanel == null) {
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case GAME_LIST_RESPONSE:
                    // Convert from Message.GameInfo to LobbyPanel.GameInfo
                    List<LobbyPanel.GameInfo> gameInfoList = new ArrayList<>();
                    if (message.getGames() != null) {
                        for (Message.GameInfo gameInfo : message.getGames()) {
                            LobbyPanel.GameInfo lobbyGameInfo = new LobbyPanel.GameInfo(
                                    gameInfo.getId(),
                                    gameInfo.getHostName(),
                                    gameInfo.getTimeControl());
                            gameInfoList.add(lobbyGameInfo);
                        }
                    }
                    lobbyPanel.updateGameList(gameInfoList);
                    break;
                    
                case DISCONNECT:
                    if (client.isConnected()) {
                        lobbyPanel.setStatusMessage("Disconnected from server: " + message.getContent());
                    }
                    break;
                    
                default:
                    // Ignore other messages
                    break;
            }
        });
    }
    
    private void handleWaitingRoomMessage(Message message) {
        if (waitingRoomPanel == null) {
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case GAME_START:
                    String content = message.getContent();
                    ChessPiece.PieceColor playerColor = content.contains("white") ? 
                            ChessPiece.PieceColor.WHITE : ChessPiece.PieceColor.BLACK;
                    
                    // Start the game
                    if (gamePanel != null) {
                        gamePanel.setPlayerColor(playerColor);
                    }
                    
                    String gameId = client.getCurrentGameId();
                    startGame(gameId);
                    break;
                    
                case DISCONNECT:
                    if (client.isConnected()) {
                        waitingRoomPanel.setStatusMessage("Disconnected from server: " + message.getContent());
                    }
                    break;
                    
                case CONNECT:
                    waitingRoomPanel.setStatusMessage("Opponent connected!");
                    break;
                    
                default:
                    // Ignore other messages
                    break;
            }
        });
    }
    
    private void handleGameMessage(Message message) {
        if (gamePanel == null) {
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case GAME_START:
                    String content = message.getContent();
                    ChessPiece.PieceColor playerColor = content.contains("white") ? 
                            ChessPiece.PieceColor.WHITE : ChessPiece.PieceColor.BLACK;
                    gamePanel.setPlayerColor(playerColor);
                    gamePanel.showGameStartMessage(message.getContent());
                    break;
                    
                case MOVE:
                    gamePanel.handleMoveMessage(message);
                    break;
                    
                case GAME_END:
                    gamePanel.showGameEndMessage(message.getContent());
                    // After game ends, allow returning to lobby
                    SwingUtilities.invokeLater(() -> {
                        int response = javax.swing.JOptionPane.showConfirmDialog(
                            this,
                            "Return to lobby?",
                            "Game Ended",
                            javax.swing.JOptionPane.YES_NO_OPTION);
                        
                        if (response == javax.swing.JOptionPane.YES_OPTION) {
                            showLobbyPanel();
                        }
                    });
                    break;
                    
                case CHAT:
                    gamePanel.addChatMessage(message.getSender(), message.getContent());
                    break;
                    
                case DISCONNECT:
                    if (client.isConnected()) {
                        gamePanel.showDisconnectMessage(message.getContent());
                    }
                    break;
                    
                case CONNECT:
                case READY:
                    // These messages can be handled by GamePanel
                    gamePanel.handleInfoMessage(message);
                    break;
                
                default:
                    // Ignore other messages
                    break;
            }
        });
    }
    
    private void createGame(String timeControl) {
        if (client != null && client.isConnected()) {
            // Generate a unique game ID
            String gameId = java.util.UUID.randomUUID().toString();
            
            // Send create game message to server
            Message createGameMessage = new Message(Message.MessageType.CREATE_GAME);
            createGameMessage.setContent(timeControl);
            createGameMessage.setSender(client.getUsername());
            createGameMessage.setGameId(gameId);
            client.sendMessage(createGameMessage);
            
            // Store the game ID and time control
            client.setCurrentGameId(gameId);
            client.setCurrentTimeControl(timeControl);
            
            // Show waiting room
            showWaitingRoomPanel(gameId, timeControl);
        }
    }
    
    private void joinGame(String gameId) {
        if (client != null && client.isConnected()) {
            // Find the game in the lobby's list to get the time control
            LobbyPanel.GameInfo gameInfo = null;
            if (lobbyPanel != null) {
                for (LobbyPanel.GameInfo game : lobbyPanel.getAvailableGames()) {
                    if (gameId.equals(game.getId())) {
                        gameInfo = game;
                        break;
                    }
                }
            }
            
            // Send join game message to server
            Message joinGameMessage = new Message(Message.MessageType.JOIN_GAME);
            joinGameMessage.setGameId(gameId);
            joinGameMessage.setSender(client.getUsername());
            client.sendMessage(joinGameMessage);
            
            // Store the game ID
            client.setCurrentGameId(gameId);
            
            // Store the time control if found
            if (gameInfo != null) {
                client.setCurrentTimeControl(gameInfo.getTimeControl());
            }
            
            // Show waiting room - we'll transition to game when we get the GAME_START message
            String timeControl = (gameInfo != null) ? gameInfo.getTimeControl() : "";
            showWaitingRoomPanel(gameId, timeControl);
        }
    }
    
    private void leaveGame() {
        if (client != null && client.isConnected() && client.getCurrentGameId() != null) {
            Message.MessageType leaveGameType = null;
            
            // Determine the appropriate message type for leaving a game
            try {
                // First try to use LEAVE_GAME if it exists
                leaveGameType = Message.MessageType.valueOf("LEAVE_GAME");
            } catch (IllegalArgumentException e) {
                // If LEAVE_GAME doesn't exist, use DISCONNECT
                leaveGameType = Message.MessageType.DISCONNECT;
            }
            
            // Send leave game message to server
            Message leaveGameMessage = new Message(leaveGameType);
            leaveGameMessage.setGameId(client.getCurrentGameId());
            leaveGameMessage.setSender(client.getUsername());
            leaveGameMessage.setContent("Player left the game");
            client.sendMessage(leaveGameMessage);
            
            // Clear the game ID
            client.setCurrentGameId(null);
        }
    }
    
    public static void main(String[] args) {
        // Use system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create and display the main frame
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
} 