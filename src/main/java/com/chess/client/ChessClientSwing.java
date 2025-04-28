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
    private JButton readyButton;
    
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
            public void onCreateGame() {
                showCreateGamePanel();
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
                public void onCreateGame(String timeControl) {
                    createGame(timeControl);
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
        gamePanel.add(chessBoardPanel, BorderLayout.CENTER);

        // Right panel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        rightPanel.setPreferredSize(new Dimension(250, 600));

        // Status label
        statusLabel = new JLabel("Waiting for opponent...");
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

        // Buttons
        readyButton = new JButton("Ready");
        readyButton.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        readyButton.addActionListener(e -> {
            client.sendReadyMessage();
            readyButton.setEnabled(false);
            statusLabel.setText("You are ready, waiting for your opponent to be ready...");
        });
        rightPanel.add(readyButton);
        
        // Leave game button
        JButton leaveButton = new JButton("Leave Game");
        leaveButton.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        leaveButton.setBackground(new Color(200, 50, 50));
        leaveButton.setForeground(Color.WHITE);
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
            case GAME_START:
                if (message.getPlayerInfo() != null) {
                    chessBoardPanel.setPlayerColor(message.getPlayerInfo().getColor());
                }
                updateStatus(message.getContent());
                showGamePanel();
                break;
            case MOVE:
                if (message.getMove() != null) {
                    chessBoardPanel.makeMove(message.getMove());
                    updateStatus("Your turn");
                } else {
                    updateStatus(message.getContent());
                }
                break;
            case GAME_END:
                updateStatus(message.getContent());
                showGameEndDialog(message.getContent());
                break;
            case CHAT:
                chatArea.append(message.getSender() + ": " + message.getContent() + "\n");
                break;
            case READY:
                updateStatus(message.getContent());
                break;
            case DISCONNECT:
                updateStatus(message.getContent());
                if (isInGame) {
                    showGameEndDialog("Opponent left the game. Game over.");
                }
                break;
            case GAME_LIST_RESPONSE:
                if (message.getGames() != null && lobbyPanel != null) {
                    List<com.chess.client.LobbyPanel.GameInfo> gameInfos = new ArrayList<>();
                    for (GameInfo game : message.getGames()) {
                        gameInfos.add(new com.chess.client.LobbyPanel.GameInfo(
                                game.getId(), 
                                game.getHostName(), 
                                game.getTimeControl()));
                    }
                    lobbyPanel.updateGameList(gameInfos);
                }
                break;
            default:
                break;
        }
    }
    
    private void createGame(String timeControl) {
        if (client != null && client.isConnected()) {
            String gameId = UUID.randomUUID().toString();
            
            Message createMessage = new Message(Message.MessageType.CREATE_GAME);
            createMessage.setGameId(gameId);
            createMessage.setTimeControl(timeControl);
            client.sendMessage(createMessage);
            
            // Return to lobby screen
            cardLayout.show(contentPanel, "lobby");
            lobbyPanel.setStatusMessage("Game created. Waiting for an opponent...");
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
                // Send leave game message (using DISCONNECT in this example)
                Message leaveMessage = new Message(Message.MessageType.DISCONNECT);
                leaveMessage.setContent(client.getUsername() + " left the game.");
                client.sendMessage(leaveMessage);
                
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
        
        // Enable ready button
        if (readyButton != null) {
            readyButton.setEnabled(true);
        }
        
        // Clear chat area
        if (chatArea != null) {
            chatArea.setText("");
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChessClientSwing();
        });
    }
} 