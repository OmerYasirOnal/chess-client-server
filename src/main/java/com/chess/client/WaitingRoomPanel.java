package com.chess.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Panel shown while waiting for an opponent to join a game
 */
public class WaitingRoomPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private JLabel statusLabel;
    private JLabel gameIdLabel;
    private JLabel timeControlLabel;
    private JButton leaveButton;
    private JLabel opponentNameLabel;
    
    private WaitingRoomListener listener;
    
    public WaitingRoomPanel() {
        setLayout(new BorderLayout());
        initializeUI();
    }
    
    private void initializeUI() {
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        headerPanel.setBackground(new Color(50, 50, 50));
        
        JLabel titleLabel = new JLabel("Waiting Room");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 10, 0);
        
        // Status section
        JPanel statusPanel = createSectionPanel("Status");
        statusLabel = new JLabel("Waiting for opponent to join...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusPanel.add(statusLabel);
        
        contentPanel.add(statusPanel, gbc);
        
        // Game information section
        JPanel gameInfoPanel = createSectionPanel("Game Information");
        
        JLabel gameIdTitleLabel = new JLabel("Game ID:");
        gameIdTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gameInfoPanel.add(gameIdTitleLabel);
        
        gameIdLabel = new JLabel("...");
        gameIdLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gameInfoPanel.add(gameIdLabel);
        
        JLabel timeControlTitleLabel = new JLabel("Game Type:");
        timeControlTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gameInfoPanel.add(timeControlTitleLabel);
        
        timeControlLabel = new JLabel("Standard");
        timeControlLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gameInfoPanel.add(timeControlLabel);
        
        contentPanel.add(gameInfoPanel, gbc);
        
        // Opponent information section
        JPanel opponentPanel = createSectionPanel("Opponent Information");
        
        JLabel opponentTitleLabel = new JLabel("Opponent:");
        opponentTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        opponentPanel.add(opponentTitleLabel);
        
        opponentNameLabel = new JLabel("Waiting for opponent...");
        opponentNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        opponentPanel.add(opponentNameLabel);
        
        JLabel infoLabel = new JLabel("Game will start automatically when an opponent joins.");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        infoLabel.setForeground(new Color(0, 100, 0));
        opponentPanel.add(infoLabel);
        
        contentPanel.add(opponentPanel, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        leaveButton = new JButton("Leave Game");
        leaveButton.setPreferredSize(new Dimension(120, 40));
        leaveButton.setFont(new Font("Arial", Font.BOLD, 14));
        leaveButton.addActionListener(e -> {
            if (listener != null) {
                listener.onLeaveGame();
            }
        });
        buttonPanel.add(leaveButton);
        
        contentPanel.add(buttonPanel, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Footer panel
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel footerLabel = new JLabel("© 2025 Chess Game - Computer Networks Project");
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerLabel.setForeground(Color.GRAY);
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        footerPanel.add(footerLabel, BorderLayout.CENTER);
        
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 20);
        
        return panel;
    }
    
    public void setGameInfo(String gameId, String timeControl) {
        gameIdLabel.setText(gameId != null ? gameId : "...");
        timeControlLabel.setText("Standard");
    }
    
    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }
    
    public void setWaitingRoomListener(WaitingRoomListener listener) {
        this.listener = listener;
    }
    
    public void setOpponentName(String name) {
        if (name != null && !name.isEmpty()) {
            opponentNameLabel.setText(name);
            // If opponent has joined, update status to indicate game will start shortly
            statusLabel.setText("Opponent joined! Game starting...");
        } else {
            opponentNameLabel.setText("Waiting for opponent...");
        }
    }
    
    /**
     * Interface for waiting room events
     */
    public interface WaitingRoomListener {
        void onLeaveGame();
    }
} 