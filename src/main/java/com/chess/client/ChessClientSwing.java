package com.chess.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.chess.common.ChessMove;
import com.chess.common.ChessPiece;
import com.chess.common.Message;
import com.chess.common.Message.GameInfo;
import com.chess.client.util.UIUtils;

public class ChessClientSwing {
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    
    // Panels
    private LoginPanel loginPanel;
    private LobbyPanel lobbyPanel;
    private CreateGamePanel createGamePanel;
    private JPanel gamePanel;
    
    // Game components
    private JLabel statusLabel;
    private JTextArea chatArea;
    private JTextField chatInput;
    private ChessBoardPanel chessBoardPanel;
    
    private ChessClient client;
    private boolean isInGame = false;
    
    public ChessClientSwing() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Chess Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 650);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        frame.setContentPane(contentPanel);
        
        // Create login panel
        createLoginPanel();
        
        frame.setVisible(true);
    }
    
    private void createLoginPanel() {
        loginPanel = new LoginPanel();
        loginPanel.setLoginListener(new LoginPanel.LoginListener() {
            @Override
            public void onLogin(String username, String server, int port) {
                connectToServer(username, server, port);
            }
        });
        
        contentPanel.add(loginPanel, "login");
        cardLayout.show(contentPanel, "login");
    }
    
    private void createLobbyPanel() {
        lobbyPanel = new LobbyPanel(client);
        lobbyPanel.setLobbyListener(new LobbyPanel.LobbyListener() {
            @Override
            public void onCreateGame(String gameType) {
                createGame(gameType);
            }
            
            @Override
            public void onJoinGame(com.chess.client.LobbyPanel.GameInfo game) {
                joinGame(game.getId());
            }
            
            @Override
            public void onLogout() {
                logout();
            }
        });
        
        contentPanel.add(lobbyPanel, "lobby");
        cardLayout.show(contentPanel, "lobby");
        
        // Oyun listesini sunucudan iste
        if (client != null && client.isConnected()) {
            Message gameListRequest = new Message(Message.MessageType.GAME_LIST);
            client.sendMessage(gameListRequest);
        }
    }
    
    private void showCreateGamePanel() {
        if (createGamePanel == null) {
            createGamePanel = new CreateGamePanel();
            createGamePanel.setCreateGameListener(new CreateGamePanel.CreateGameListener() {
                @Override
                public void onCreateGame(String gameType) {
                    createGame(gameType);
                }
                
                @Override
                public void onCancel() {
                    cardLayout.show(contentPanel, "lobby");
                }
            });
            
            contentPanel.add(createGamePanel, "createGame");
        }
        
        cardLayout.show(contentPanel, "createGame");
    }
    
    private void createGamePanel() {
        gamePanel = new JPanel(new BorderLayout());

        // Chess board
        chessBoardPanel = new ChessBoardPanel(client);
        // Başlangıçta tahtayı kilitli tut (oyuncular hazır olana kadar)
        chessBoardPanel.setLocked(true);
        gamePanel.add(chessBoardPanel, BorderLayout.CENTER);

        // Right panel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        rightPanel.setPreferredSize(new Dimension(250, 600));

        // Status label
        statusLabel = new JLabel("Opponent connecting...");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        rightPanel.add(statusLabel);
        rightPanel.add(Box.createVerticalStrut(10));

        // Chat area
        JLabel chatLabel = new JLabel("Chat");
        chatLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        rightPanel.add(chatLabel);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setPreferredSize(new Dimension(230, 300));
        chatScrollPane.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        rightPanel.add(chatScrollPane);
        rightPanel.add(Box.createVerticalStrut(10));

        // Chat input
        chatInput = new JTextField();
        chatInput.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        chatInput.addActionListener(e -> {
            String message = chatInput.getText().trim();
            if (!message.isEmpty()) {
                client.sendChatMessage(message);
                chatArea.append("You: " + message + "\n");
                chatInput.setText("");
            }
        });
        rightPanel.add(chatInput);
        rightPanel.add(Box.createVerticalStrut(20));

        // Leave game button
        JButton leaveButton = new JButton("Leave Game");
        UIUtils.setDangerButtonStyle(leaveButton);
        leaveButton.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        UIUtils.setButtonSize(leaveButton, 230, 40);
        leaveButton.addActionListener(e -> leaveGame());
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(leaveButton);

        gamePanel.add(rightPanel, BorderLayout.EAST);
        
        contentPanel.add(gamePanel, "game");
    }

    private void connectToServer(String username, String serverAddress, int port) {
        try {
            client = new ChessClient(serverAddress, port, username);
            
            // Add message listener
            client.setMessageListener(message -> {
                SwingUtilities.invokeLater(() -> handleMessage(message));
            });
            
            // Connect to server
            client.connect();
            
            // Create and show lobby screen
            createLobbyPanel();
            
            // Create game panel as well (will be shown when needed)
            createGamePanel();
            
        } catch (IOException e) {
            loginPanel.setErrorMessage("Error connecting to server: " + e.getMessage());
        }
    }
    
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case CONNECT:
                updateStatus(message.getContent());
                break;
            case ERROR:
                // Handle error messages (like username already in use)
                if (message.getContent() != null && message.getContent().contains("Username already in use")) {
                    // Show error in login panel
                    if (loginPanel != null) {
                        loginPanel.setErrorMessage(message.getContent());
                        // Make sure we're showing the login panel
                        cardLayout.show(contentPanel, "login");
                    }
                } else if (loginPanel != null) {
                    loginPanel.setErrorMessage(message.getContent());
                } else {
                    updateStatus("Error: " + message.getContent());
                }
                break;
            case GAME_START:
                if (message.getPlayerInfo() != null) {
                    chessBoardPanel.setPlayerColor(message.getPlayerInfo().getColor());
                }
                updateStatus(message.getContent());
                showGamePanel();
                
                // Ensure gameId is set when joining a game
                if (message.getGameId() != null && !message.getGameId().isEmpty()) {
                    client.setCurrentGameId(message.getGameId());
                    System.out.println("Game started with ID: " + message.getGameId());
                }
                
                // Oyun başladıysa tahtanın kilidini aç (eğer hazırsa ve sırası geldiyse)
                if (message.getContent().contains("Game started")) {
                    ChessPiece.PieceColor turn = chessBoardPanel.getBoard().getCurrentTurn();
                    if (turn == chessBoardPanel.getBoard().getCurrentTurn()) {
                        chessBoardPanel.setLocked(false);
                    }
                }
                break;
            case MOVE:
                if (message.getMove() != null) {
                    chessBoardPanel.makeMove(message.getMove());
                    // Hamleden sonra, oyuncunun sırası geldiyse tahtanın kilidini aç
                    if (chessBoardPanel.getBoard().getCurrentTurn() == chessBoardPanel.getPlayerColor()) {
                        chessBoardPanel.setLocked(false);
                        updateStatus("Your turn");
                    } else {
                        chessBoardPanel.setLocked(true);
                        updateStatus("Opponent's turn");
                    }
                } else {
                    updateStatus(message.getContent());
                }
                break;
            case GAME_END:
                updateStatus(message.getContent());
                chessBoardPanel.setLocked(true); // Oyun bittiğinde tahtayı kilitle
                showGameEndDialog(message.getContent());
                break;
            case CHAT:
                chatArea.append(message.getSender() + ": " + message.getContent() + "\n");
                break;
            case DISCONNECT:
                updateStatus(message.getContent());
                if (isInGame) {
                    chessBoardPanel.setLocked(true); // Rakip ayrıldığında tahtayı kilitle
                    showGameEndDialog("Opponent left the game. Game over.");
                }
                break;
            case DELETE_GAME:
                // This case handles confirmation that a game was deleted
                if (message.getContent() != null) {
                    updateStatus(message.getContent());
                    System.out.println("DELETE_GAME confirmation received: " + message.getContent());
                }
                
                // Request an updated game list to refresh the lobby
                if (lobbyPanel != null && !isInGame) {
                    System.out.println("Requesting updated game list after DELETE_GAME");
                    Message gameListRequest = new Message(Message.MessageType.GAME_LIST);
                    client.sendMessage(gameListRequest);
                }
                break;
            case GAME_LIST_RESPONSE:
                if (message.getGames() != null && lobbyPanel != null) {
                    List<com.chess.client.LobbyPanel.GameInfo> gameInfos = new ArrayList<>();
                    for (GameInfo game : message.getGames()) {
                        gameInfos.add(new com.chess.client.LobbyPanel.GameInfo(
                                game.getId(), 
                                game.getHostName(), 
                                game.getGameType()));
                    }
                    lobbyPanel.updateGameList(gameInfos);
                    System.out.println("Updated game list with " + 
                                      (message.getGames() != null ? message.getGames().size() : 0) + 
                                      " games");
                }
                break;
            default:
                System.out.println("Unknown message type: " + message.getType());
                break;
        }
    }
    
    private void createGame(String gameType) {
        if (client != null && client.isConnected()) {
            Message createGameMessage = new Message(Message.MessageType.CREATE_GAME);
            String gameId = UUID.randomUUID().toString();
            
            // Oyun bilgilerini hazırla
            Message.GameInfo gameInfo = new Message.GameInfo(
                gameId,
                client.getUsername(),
                gameType
            );
            createGameMessage.setGameInfo(gameInfo);
            
            client.sendMessage(createGameMessage);
            
            // Oyun paneline geçiş yap ve bekleme diyalogunu göster
            showGamePanel();
            resetGamePanel();
            
            // Bekleme durumunu ayarla
            chessBoardPanel.setLocked(true);
            statusLabel.setText("Waiting for an opponent to join your game...");
            statusLabel.setForeground(UIUtils.WAITING_COLOR);
            client.setCurrentGameId(gameId);
        }
    }
    
    private void joinGame(String gameId) {
        if (client != null && client.isConnected()) {
            Message joinMessage = new Message(Message.MessageType.JOIN_GAME);
            joinMessage.setGameId(gameId);
            client.sendMessage(joinMessage);
            
            // We stay in the lobby screen, will switch to game screen when GAME_START message is received
            lobbyPanel.setStatusMessage("Joining game...");
        }
    }
    
    private void leaveGame() {
        if (isInGame && client != null && client.isConnected()) {
            int confirm = JOptionPane.showConfirmDialog(frame, 
                    "Are you sure you want to leave the game?", 
                    "Leave Game", 
                    JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Ensure we have a valid game ID
                String gameId = client.getCurrentGameId();
                if (gameId == null || gameId.isEmpty()) {
                    System.out.println("Warning: Attempting to leave game with null or empty gameId");
                    
                    // Sadece lobby'e dön ve oyundan çık
                    isInGame = false;
                    cardLayout.show(contentPanel, "lobby");
                    lobbyPanel.setStatusMessage("Left the game.");
                    resetGamePanel();
                    return;
                }
                
                System.out.println("Leaving game with ID: " + gameId);
                
                // Always send delete game message to end the game and remove from lobby
                Message deleteMessage = new Message(Message.MessageType.DELETE_GAME);
                deleteMessage.setGameId(gameId);
                deleteMessage.setContent(client.getUsername() + " left the game");
                client.sendMessage(deleteMessage);
                
                // Return to lobby screen
                isInGame = false;
                cardLayout.show(contentPanel, "lobby");
                lobbyPanel.setStatusMessage("Left the game.");
                
                // Reset game panel
                resetGamePanel();
            }
        }
    }
    
    private void logout() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
        
        // Return to login screen
        isInGame = false;
        cardLayout.show(contentPanel, "login");
    }
    
    private void showGamePanel() {
        isInGame = true;
        cardLayout.show(contentPanel, "game");
    }
    
    private void resetGamePanel() {
        // Reset game panel
        if (chessBoardPanel != null) {
            chessBoardPanel.resetBoard();
        }
        
        // Clear game ID in client
        if (client != null) {
            client.setCurrentGameId(null);
            client.setCurrentGameType(null);
        }
        
        // Reset status label
        if (statusLabel != null) {
            statusLabel.setText("Game connected. Waiting for the game to start...");
            statusLabel.setForeground(Color.BLACK);
        }
    }

    private void updateStatus(String status) {
        if (statusLabel != null) {
            statusLabel.setText(status);
        }
    }

    private void showGameEndDialog(String result) {
        // Format the result to look nicer
        String formattedResult = formatGameResult(result);
        String title = getGameEndTitle(result);
        
        SwingUtilities.invokeLater(() -> {
            Object[] options = {"New Game", "Return to Lobby"};
            int choice = JOptionPane.showOptionDialog(frame,
                    formattedResult,
                    title,
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);
            
            isInGame = false;
            
            if (choice == 0) {
                // Go to lobby screen and open panel for new game
                cardLayout.show(contentPanel, "lobby");
                SwingUtilities.invokeLater(() -> showCreateGamePanel());
            } else {
                // Return to lobby screen
                cardLayout.show(contentPanel, "lobby");
            }
            
            // Reset game panel
            resetGamePanel();
        });
    }
    
    private String formatGameResult(String result) {
        if (result.contains("won")) {
            String winner = result.substring(0, result.indexOf(" "));
            if (winner.equals(client.getUsername())) {
                return "🏆 Congratulations, you won! 🏆\n\n" + result;
            } else {
                return "😔 Sorry, you lost. 😔\n\n" + result;
            }
        } else if (result.contains("draw")) {
            return "🤝 The game ended in a draw! 🤝\n\n" + result;
        } else {
            return result;
        }
    }
    
    private String getGameEndTitle(String result) {
        if (result.contains("won")) {
            String winner = result.substring(0, result.indexOf(" "));
            if (winner.equals(client.getUsername())) {
                return "Victory!";
            } else {
                return "Defeat";
            }
        } else if (result.contains("draw")) {
            return "Draw";
        } else {
            return "Game Over";
        }
    }

    public JFrame getFrame() {
        return frame;
    }

    /**
     * Expose the showGamePanel method for testing
     */
    public void showGamePanelForTesting() {
        showGamePanel();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChessClientSwing();
        });
    }
} 