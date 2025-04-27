package com.chess.client;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.chess.common.ChessPiece;
import com.chess.common.Message;

public class MainFrame extends JFrame {
    private static final String TITLE = "Chess Game";
    private static final int DEFAULT_WIDTH = 1000;
    private static final int DEFAULT_HEIGHT = 700;
    
    private LoginPanel loginPanel;
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
        
        // Create and show the login panel
        showLoginPanel();
    }
    
    public void showLoginPanel() {
        if (loginPanel == null) {
            loginPanel = new LoginPanel(this);
        }
        
        setContentPane(loginPanel);
        validate();
        repaint();
    }
    
    public void startGame(ChessClient client) {
        this.client = client;
        
        // Set the message listener
        client.setMessageListener(this::handleMessage);
        
        // Create and show the game panel
        gamePanel = new GamePanel(client);
        setContentPane(gamePanel);
        validate();
        repaint();
    }
    
    private void handleMessage(Message message) {
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
                case PLAYER_INFO:
                    // These messages can be handled by GamePanel
                    gamePanel.handleInfoMessage(message);
                    break;
            }
        });
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