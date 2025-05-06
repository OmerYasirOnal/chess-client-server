package com.chess.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.chess.client.util.UIUtils;
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
        sidePanel.setLayout(new BorderLayout(0, 10));
        sidePanel.setPreferredSize(new Dimension(300, 600));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        // Players panel at the top of side panel
        createPlayersPanel();
        sidePanel.add(playersPanel, BorderLayout.NORTH);
        
        // Chat panel in the middle of side panel
        JPanel chatPanel = new JPanel(new BorderLayout(0, 5));
        chatPanel.setBorder(BorderFactory.createTitledBorder("Chat"));
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setPreferredSize(new Dimension(250, 250));
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        
        JPanel chatInputPanel = new JPanel(new BorderLayout(5, 0));
        chatInput = new JTextField();
        chatInput.addActionListener(e -> sendChatMessage());
        chatInputPanel.add(chatInput, BorderLayout.CENTER);
        
        sendButton = new JButton("Send");
        UIUtils.setPrimaryButtonStyle(sendButton);
        sendButton.addActionListener(e -> sendChatMessage());
        chatInputPanel.add(sendButton, BorderLayout.EAST);
        
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);
        sidePanel.add(chatPanel, BorderLayout.CENTER);
        
        // Move history panel at the bottom of side panel
        JPanel moveHistoryPanel = new JPanel(new BorderLayout());
        moveHistoryPanel.setBorder(BorderFactory.createTitledBorder("Move History"));
        
        moveHistoryArea = new JTextArea();
        moveHistoryArea.setEditable(false);
        moveHistoryArea.setLineWrap(true);
        moveHistoryArea.setWrapStyleWord(true);
        JScrollPane moveHistoryScrollPane = new JScrollPane(moveHistoryArea);
        moveHistoryScrollPane.setPreferredSize(new Dimension(250, 150));
        moveHistoryPanel.add(moveHistoryScrollPane, BorderLayout.CENTER);
        
        sidePanel.add(moveHistoryPanel, BorderLayout.SOUTH);
    }
    
    private void createPlayersPanel() {
        playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setBorder(BorderFactory.createTitledBorder("Game Information"));
        
        // Game status
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Status:"));
        gameStatusLabel = new JLabel("Waiting for game to start...");
        statusPanel.add(gameStatusLabel);
        playersPanel.add(statusPanel);
        
        // Turn information
        JPanel turnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        turnPanel.add(new JLabel("Turn:"));
        turnLabel = new JLabel("Waiting for game to start...");
        turnPanel.add(turnLabel);
        playersPanel.add(turnPanel);
        
        // Player information
        JPanel whitePlayerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        whitePlayerPanel.add(new JLabel("White:"));
        JLabel whitePlayerLabel = new JLabel(client.getUsername());
        whitePlayerPanel.add(whitePlayerLabel);
        playersPanel.add(whitePlayerPanel);
        
        JPanel blackPlayerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        blackPlayerPanel.add(new JLabel("Black:"));
        JLabel blackPlayerLabel = new JLabel("Opponent");
        blackPlayerPanel.add(blackPlayerLabel);
        playersPanel.add(blackPlayerPanel);
    }
    
    private void sendChatMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            client.sendChatMessage(message);
            addChatMessage("You", message);
            chatInput.setText("");
        }
    }
    
    public void addChatMessage(String sender, String message) {
        chatArea.append(sender + ": " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    public void showGameStartMessage(String message) {
        gameStatusLabel.setText("Game Started");
        String turnText = playerColor == ChessPiece.PieceColor.WHITE ? 
                "White (You) to move" : "Black (Opponent) to move";
        turnLabel.setText(turnText);
        
        // Update the board panel with the game status
        if (message.contains("Waiting for an opponent")) {
            boardPanel.setStatusMessage("waiting for opponent");
        } else if (message.contains("Game started") || message.contains("Your opponent:")) {
            // İkinci oyuncu katıldığında
            boardPanel.setStatusMessage("game in progress");
            // Oyunu başlat mesajı
            addChatMessage("System", "Game has started! Both players are ready.");
        } else {
            boardPanel.setStatusMessage("game in progress");
        }
        
        // Reset the board to the starting position
        boardPanel.resetBoard();
        
        // Add info message to chat
        addChatMessage("System", message);
    }
    
    public void updateTurnDisplay(ChessPiece.PieceColor currentTurn) {
        boolean isPlayerTurn = (currentTurn == playerColor);
        String turnText = isPlayerTurn ? 
                (playerColor == ChessPiece.PieceColor.WHITE ? "White (You)" : "Black (You)") :
                (playerColor == ChessPiece.PieceColor.WHITE ? "Black (Opponent)" : "White (Opponent)");
        
        turnLabel.setText(turnText + " to move");
    }
    
    public void setPlayerColor(ChessPiece.PieceColor color) {
        this.playerColor = color;
        boardPanel.setPlayerColor(color);
    }
    
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton resignButton = new JButton("Resign");
        UIUtils.setDangerButtonStyle(resignButton);
        resignButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to resign?",
                    "Resign Confirmation",
                    JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                client.sendResignMessage();
                showGameEndMessage("You resigned. Game over.");
            }
        });
        bottomPanel.add(resignButton);
        
        JButton drawButton = new JButton("Offer Draw");
        UIUtils.setNeutralButtonStyle(drawButton);
        drawButton.addActionListener(e -> {
            client.sendDrawOfferMessage();
            addChatMessage("System", "⌛ You offered a draw. Waiting for opponent to accept or decline.");
        });
        bottomPanel.add(drawButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    public void showGameEndMessage(String message) {
        gameStatusLabel.setText("Game Over");
        
        // Disable move making
        boardPanel.setLocked(true);
        
        // Add message to chat
        addChatMessage("System", message);
        
        // Show dialog
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void addMoveToHistory(String move) {
        moveHistoryArea.append(move + "\n");
        moveHistoryArea.setCaretPosition(moveHistoryArea.getDocument().getLength());
    }
    
    public ChessBoardPanel getBoardPanel() {
        return boardPanel;
    }
    
    public void handleMoveMessage(Message message) {
        // Check if this is an error message about waiting for opponent
        if (message.getContent() != null && message.getContent().contains("waiting for opponent")) {
            addChatMessage("System", message.getContent());
            boardPanel.setStatusMessage("waiting for opponent");
            return;
        }
        
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
                
                ChessMove move = new ChessMove(fromRow, fromCol, toRow, toCol);
                boardPanel.makeRemoteMove(move);
                
                // Update the turn display
                updateTurnDisplay(boardPanel.getBoard().getCurrentTurn());
                
                // Add the move to history
                addMoveToHistory(message.getSender() + ": " + moveStr);
            }
        } catch (Exception e) {
            System.out.println("Invalid move format: " + moveStr);
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
    
    private ChessPiece.PieceColor getOpponentColor() {
        return playerColor == ChessPiece.PieceColor.WHITE ? 
                ChessPiece.PieceColor.BLACK : ChessPiece.PieceColor.WHITE;
    }
} 