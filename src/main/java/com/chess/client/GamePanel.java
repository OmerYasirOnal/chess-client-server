package com.chess.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.chess.common.ChessBoard;
import com.chess.common.ChessMove;
import com.chess.common.ChessPiece;
import com.chess.common.Message;

/**
 * Main game panel for the chess game. Contains the chess board, player information,
 * chat, and other game controls.
 */
public class GamePanel extends JPanel {
    private ChessClient client;
    private ChessBoardPanel boardPanel;
    private JPanel sidePanel;
    private JTextArea chatArea;
    private JTextField chatInput;
    private JButton sendButton;
    private JPanel playersPanel;
    private JLabel gameStatusLabel;
    private JLabel turnLabel;
    private JButton readyButton;
    private JTextArea moveHistoryArea;
    
    // Timer components
    private JLabel whiteTimerLabel;
    private JLabel blackTimerLabel;
    private javax.swing.Timer whiteTimer;
    private javax.swing.Timer blackTimer;
    private int whiteTimeRemaining; // in seconds
    private int blackTimeRemaining; // in seconds
    private int incrementSeconds;
    private boolean timerRunning = false;
    
    private ChessPiece.PieceColor playerColor = ChessPiece.PieceColor.WHITE;
    
    public GamePanel(ChessClient client) {
        this.client = client;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Game board panel
        boardPanel = new ChessBoardPanel(client);
        add(boardPanel, BorderLayout.CENTER);
        
        // Side panel (right side)
        createSidePanel();
        add(sidePanel, BorderLayout.EAST);
        
        // Bottom info panel
        createBottomPanel();
    }
    
    private void createSidePanel() {
        sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(250, 0));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        // Player information
        createPlayersPanel();
        sidePanel.add(playersPanel);
        sidePanel.add(Box.createVerticalStrut(15));
        
        // Game status
        gameStatusLabel = new JLabel("Game Not Started");
        gameStatusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gameStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidePanel.add(gameStatusLabel);
        
        turnLabel = new JLabel("Waiting...");
        turnLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        turnLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidePanel.add(turnLabel);
        sidePanel.add(Box.createVerticalStrut(15));
        
        // Ready button
        readyButton = new JButton("Ready");
        readyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        readyButton.addActionListener(e -> {
            client.sendReadyMessage();
            readyButton.setEnabled(false);
        });
        sidePanel.add(readyButton);
        sidePanel.add(Box.createVerticalStrut(15));
        
        // Move history
        JLabel historyLabel = new JLabel("Move History");
        historyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidePanel.add(historyLabel);
        
        moveHistoryArea = new JTextArea();
        moveHistoryArea.setEditable(false);
        moveHistoryArea.setLineWrap(true);
        moveHistoryArea.setWrapStyleWord(true);
        moveHistoryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane historyScroll = new JScrollPane(moveHistoryArea);
        historyScroll.setPreferredSize(new Dimension(250, 150));
        historyScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidePanel.add(historyScroll);
        sidePanel.add(Box.createVerticalStrut(15));
        
        // Chat Area
        JLabel chatLabel = new JLabel("Chat");
        chatLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidePanel.add(chatLabel);
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setPreferredSize(new Dimension(250, 200));
        chatScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidePanel.add(chatScroll);
        
        // Message sending area
        JPanel chatInputPanel = new JPanel();
        chatInputPanel.setLayout(new BoxLayout(chatInputPanel, BoxLayout.X_AXIS));
        chatInputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatInputPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        
        chatInput = new JTextField();
        chatInput.addActionListener(e -> sendChatMessage());
        
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendChatMessage());
        
        chatInputPanel.add(chatInput);
        chatInputPanel.add(Box.createHorizontalStrut(5));
        chatInputPanel.add(sendButton);
        
        sidePanel.add(Box.createVerticalStrut(5));
        sidePanel.add(chatInputPanel);
    }
    
    private void createPlayersPanel() {
        playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setBorder(BorderFactory.createTitledBorder("Players"));
        playersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // White player panel
        JPanel whitePlayerPanel = createPlayerPanel("White", "...");
        JPanel blackPlayerPanel = createPlayerPanel("Black", "...");
        
        // Add timer labels
        whiteTimerLabel = new JLabel("--:--");
        whiteTimerLabel.setName("whiteTimerLabel");
        whiteTimerLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        whiteTimerLabel.setHorizontalAlignment(JLabel.CENTER);
        whiteTimerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        whiteTimerLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        blackTimerLabel = new JLabel("--:--");
        blackTimerLabel.setName("blackTimerLabel");
        blackTimerLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        blackTimerLabel.setHorizontalAlignment(JLabel.CENTER);
        blackTimerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        blackTimerLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        whitePlayerPanel.add(whiteTimerLabel);
        blackPlayerPanel.add(blackTimerLabel);
        
        playersPanel.add(whitePlayerPanel);
        playersPanel.add(Box.createVerticalStrut(10));
        playersPanel.add(blackPlayerPanel);
    }
    
    private JPanel createPlayerPanel(String colorName, String playerName) {
        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
        playerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel colorLabel = new JLabel(colorName);
        colorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        colorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel nameLabel = new JLabel(playerName);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        playerPanel.add(colorLabel);
        playerPanel.add(nameLabel);
        
        return playerPanel;
    }
    
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton resignButton = new JButton("Resign");
        resignButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this, 
                "Are you sure you want to resign?", 
                "Confirm Resignation", 
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                client.sendResignMessage();
            }
        });
        
        JButton drawButton = new JButton("Offer Draw");
        drawButton.addActionListener(e -> {
            client.sendDrawOfferMessage();
        });
        
        bottomPanel.add(drawButton);
        bottomPanel.add(resignButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void sendChatMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            client.sendChatMessage(message);
            chatInput.setText("");
        }
    }
    
    public void addChatMessage(String sender, String message) {
        chatArea.append(sender + ": " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    public void showGameStartMessage(String message) {
        gameStatusLabel.setText("Game Started");
        turnLabel.setText(playerColor == ChessPiece.PieceColor.WHITE ? 
                "White (You) to move" : "Black (Opponent) to move");
        
        // Reset the board to the starting position
        boardPanel.resetBoard();
        
        // Initialize and start the chess timer
        initializeChessTimer();
        
        // Add info message to chat
        addChatMessage("System", message);
    }
    
    /**
     * Initialize and start the chess timer based on time control
     */
    private void initializeChessTimer() {
        // Parse time control (format: minutes+increment)
        String timeControlStr = client.getCurrentTimeControl();
        if (timeControlStr == null || timeControlStr.isEmpty()) {
            timeControlStr = "5+0"; // Default 5 minutes, no increment
        }
        
        String[] parts = timeControlStr.split("\\+");
        int minutes = Integer.parseInt(parts[0]);
        incrementSeconds = (parts.length > 1) ? Integer.parseInt(parts[1]) : 0;
        
        // Set initial times
        whiteTimeRemaining = minutes * 60;
        blackTimeRemaining = minutes * 60;
        
        // Update timer displays
        updateTimerDisplay(whiteTimerLabel, whiteTimeRemaining);
        updateTimerDisplay(blackTimerLabel, blackTimeRemaining);
        
        // Create white player timer
        whiteTimer = new javax.swing.Timer(1000, e -> {
            whiteTimeRemaining--;
            updateTimerDisplay(whiteTimerLabel, whiteTimeRemaining);
            
            if (whiteTimeRemaining <= 0) {
                whiteTimer.stop();
                handleTimeUp(ChessPiece.PieceColor.WHITE);
            }
        });
        
        // Create black player timer
        blackTimer = new javax.swing.Timer(1000, e -> {
            blackTimeRemaining--;
            updateTimerDisplay(blackTimerLabel, blackTimeRemaining);
            
            if (blackTimeRemaining <= 0) {
                blackTimer.stop();
                handleTimeUp(ChessPiece.PieceColor.BLACK);
            }
        });
        
        // Start the white timer (white moves first)
        startPlayerTimer(ChessPiece.PieceColor.WHITE);
        timerRunning = true;
    }
    
    /**
     * Update the timer display with the given time in seconds
     */
    private void updateTimerDisplay(JLabel timerLabel, int timeInSeconds) {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        timerLabel.setText(String.format("%d:%02d", minutes, seconds));
        
        // Change color to red if less than 30 seconds
        if (timeInSeconds < 30) {
            timerLabel.setForeground(Color.RED);
        } else {
            timerLabel.setForeground(Color.BLACK);
        }
    }
    
    /**
     * Start the timer for the specified player and stop the opponent's timer
     */
    private void startPlayerTimer(ChessPiece.PieceColor color) {
        if (!timerRunning) return;
        
        if (color == ChessPiece.PieceColor.WHITE) {
            blackTimer.stop();
            whiteTimer.start();
        } else {
            whiteTimer.stop();
            blackTimer.start();
        }
    }
    
    /**
     * Handle when a player's time runs out
     */
    private void handleTimeUp(ChessPiece.PieceColor color) {
        String message = color == ChessPiece.PieceColor.WHITE 
                ? "Time's up! White loses."
                : "Time's up! Black loses.";
        
        boolean playerLost = color == playerColor;
        message = playerLost ? "Time's up! You lost" : "Time's up! You won";
        
        showGameEndMessage(message);
    }
    
    /**
     * Apply increment after a player makes a move
     */
    private void applyIncrement(ChessPiece.PieceColor color) {
        if (incrementSeconds > 0) {
            if (color == ChessPiece.PieceColor.WHITE) {
                whiteTimeRemaining += incrementSeconds;
                updateTimerDisplay(whiteTimerLabel, whiteTimeRemaining);
            } else {
                blackTimeRemaining += incrementSeconds;
                updateTimerDisplay(blackTimerLabel, blackTimeRemaining);
            }
        }
    }
    
    public void showGameEndMessage(String message) {
        gameStatusLabel.setText("Game Over");
        
        // Add to chat history
        addChatMessage("System", "Game over: " + message);
        
        // Show game end dialog
        String dialogMessage = "Game over: " + message + "\nWhat would you like to do?";
        
        // Stop the timers if they're running
        if (whiteTimer != null) whiteTimer.stop();
        if (blackTimer != null) blackTimer.stop();
        
        // Show game end dialog with options
        SwingUtilities.invokeLater(() -> {
            Object[] options = {"Play Again", "Return to Lobby", "Exit"};
            int choice = JOptionPane.showOptionDialog(
                this,
                dialogMessage,
                "Game Ended",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
            );
            
            // Handle user choice
            if (choice == 0) {
                // Play Again - Create a new game with the same time control
                if (client != null) {
                    String timeControl = client.getCurrentTimeControl();
                    if (timeControl == null || timeControl.isEmpty()) {
                        timeControl = "5+0"; // Default
                    }
                    
                    // Get the parent MainFrame
                    MainFrame mainFrame = (MainFrame) SwingUtilities.getWindowAncestor(this);
                    if (mainFrame != null) {
                        mainFrame.showLobbyPanel();
                        // Use reflection to access the private createGame method
                        try {
                            Method createGameMethod = MainFrame.class.getDeclaredMethod("createGame", String.class);
                            createGameMethod.setAccessible(true);
                            createGameMethod.invoke(mainFrame, timeControl);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (choice == 1) {
                // Return to Lobby
                MainFrame mainFrame = (MainFrame) SwingUtilities.getWindowAncestor(this);
                if (mainFrame != null) {
                    mainFrame.showLobbyPanel();
                }
            } else {
                // Exit the application
                System.exit(0);
            }
        });
    }
    
    public void showDisconnectMessage(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Disconnection",
            JOptionPane.WARNING_MESSAGE
        );
        ((MainFrame) SwingUtilities.getWindowAncestor(this)).showLoginPanel();
    }
    
    public void handleMoveMessage(Message message) {
        // Extract move info from the message
        String moveStr = message.getContent();
        if (moveStr == null || moveStr.isEmpty()) {
            return;
        }
        
        // Apply the move to the board
        try {
            String[] parts = moveStr.split("->");
            if (parts.length == 2) {
                String fromStr = parts[0].trim();
                String toStr = parts[1].trim();
                
                int fromRow = Character.getNumericValue(fromStr.charAt(0));
                int fromCol = Character.getNumericValue(fromStr.charAt(1));
                int toRow = Character.getNumericValue(toStr.charAt(0));
                int toCol = Character.getNumericValue(toStr.charAt(1));
                
                // Make the move on the board
                ChessMove move = new ChessMove(fromRow, fromCol, toRow, toCol);
                boardPanel.makeMove(move);
                
                // Update move history
                addMoveToHistory(moveStr);
                
                // Update whose turn it is
                ChessPiece.PieceColor moveMadeBy = 
                        message.getSender().equals(client.getUsername()) ? 
                        playerColor : getOpponentColor();
                
                // Apply increment to the player who just moved
                applyIncrement(moveMadeBy);
                
                // Switch timer to the other player
                startPlayerTimer(getOpponentColor(moveMadeBy));
                
                // Update turn label
                boolean isPlayerTurn = getCurrentTurnColor() == playerColor;
                turnLabel.setText(isPlayerTurn ? "Your turn" : "Opponent's turn");
            }
        } catch (Exception e) {
            System.err.println("Error processing move: " + e.getMessage());
        }
    }
    
    public void handleInfoMessage(Message message) {
        switch (message.getType()) {
            case CONNECT:
                addChatMessage("System", message.getContent());
                break;
                
            case READY:
                addChatMessage("System", message.getContent());
                break;
                
            default:
                // If we get here with a playerInfo, update the player information
                if (message.getPlayerInfo() != null) {
                    updatePlayersInfo(message);
                }
                break;
        }
    }
    
    private void updatePlayersInfo(Message message) {
        if (message.getPlayerInfo() != null) {
            // Update player information
            Message.PlayerInfo playerInfo = message.getPlayerInfo();
            playerColor = playerInfo.getColor();
            
            // Update UI elements
            boardPanel.setPlayerColor(playerColor);
            
            // Update players panel
            playersPanel.removeAll();
            
            JLabel whitePlayerLabel = new JLabel("White: " + 
                    (playerColor == ChessPiece.PieceColor.WHITE ? 
                            client.getUsername() + " (You)" : "Opponent"));
            whitePlayerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            playersPanel.add(whitePlayerLabel);
            
            JLabel blackPlayerLabel = new JLabel("Black: " + 
                    (playerColor == ChessPiece.PieceColor.BLACK ? 
                            client.getUsername() + " (You)" : "Opponent"));
            blackPlayerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            playersPanel.add(blackPlayerLabel);
            
            playersPanel.revalidate();
            playersPanel.repaint();
        }
    }
    
    private void addMoveToHistory(String moveStr) {
        moveHistoryArea.append(moveStr + "\n");
        moveHistoryArea.setCaretPosition(moveHistoryArea.getDocument().getLength());
    }
    
    public void resetGame() {
        // Reset board
        boardPanel.resetBoard();
        boardPanel.setLocked(false);
        
        // Reset game status
        gameStatusLabel.setText("Waiting for Players");
        turnLabel.setText("Waiting...");
        
        // Enable ready button
        readyButton.setEnabled(true);
    }
    
    public void setPlayerColor(ChessPiece.PieceColor color) {
        this.playerColor = color;
        boardPanel.setPlayerColor(color);
    }
    
    private ChessPiece.PieceColor getCurrentTurnColor() {
        // Determine current turn color based on the board state
        ChessBoard board = boardPanel.getBoard();
        return board.isWhiteTurn() ? ChessPiece.PieceColor.WHITE : ChessPiece.PieceColor.BLACK;
    }
    
    private ChessPiece.PieceColor getOpponentColor() {
        return playerColor == ChessPiece.PieceColor.WHITE ? 
                ChessPiece.PieceColor.BLACK : ChessPiece.PieceColor.WHITE;
    }
    
    private ChessPiece.PieceColor getOpponentColor(ChessPiece.PieceColor color) {
        return color == ChessPiece.PieceColor.WHITE ? 
                ChessPiece.PieceColor.BLACK : ChessPiece.PieceColor.WHITE;
    }
} 