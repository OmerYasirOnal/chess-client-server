package com.chess.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

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
        
        // Initially empty, will be filled when player info is updated
        JLabel waitingLabel = new JLabel("Waiting for players...");
        waitingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        playersPanel.add(waitingLabel);
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
        
        // Add info message to chat
        addChatMessage("System", message);
    }
    
    public void showGameEndMessage(String message) {
        gameStatusLabel.setText("Game Over");
        turnLabel.setText(message);
        addChatMessage("System", "Game over: " + message);
        
        // Lock the board, no more moves allowed
        boardPanel.setLocked(true);
        
        // Show options to the user
        Object[] options = {"New Game", "Main Menu", "Exit"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Game over: " + message + "\nWhat would you like to do?",
            "Game Ended",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        switch (choice) {
            case 0: // New game
                resetGame();
                client.sendReadyMessage();
                break;
            case 1: // Main menu
                ((MainFrame) SwingUtilities.getWindowAncestor(this)).showLoginPanel();
                break;
            case 2: // Exit
                client.disconnect();
                System.exit(0);
                break;
            default:
                break;
        }
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
        if (message.getMove() != null) {
            // Process the move
            boardPanel.makeMove(message.getMove());
            
            // Add to move history
            String moveStr = message.getSender() + ": " + message.getMove().toString();
            addMoveToHistory(moveStr);
            
            // Update turn status
            boolean isWhiteTurn = true; // Default to white's turn if can't determine
            boolean isMyTurn = (isWhiteTurn && playerColor == ChessPiece.PieceColor.WHITE) ||
                             (!isWhiteTurn && playerColor == ChessPiece.PieceColor.BLACK);
            
            turnLabel.setText(isMyTurn ? "Your turn" : "Opponent's turn");
        } else if (message.getContent() != null) {
            // Show info message
            JOptionPane.showMessageDialog(
                this,
                message.getContent(),
                "Move Information",
                JOptionPane.INFORMATION_MESSAGE
            );
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
} 