package com.chess.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Panel for creating a new chess game
 */
public class CreateGamePanel extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private CreateGameListener createGameListener;
    private JButton createButton;
    private JButton cancelButton;
    
    public CreateGamePanel() {
        setTitle("Create Game");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setModal(true);
        setResizable(false);
        
        initializeUI();
    }
    
    private void initializeUI() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 15, 0);
        
        // Title
        JLabel titleLabel = new JLabel("Create a New Game", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        contentPanel.add(titleLabel, gbc);
        
        // Description
        JLabel descriptionLabel = new JLabel("Create a multiplayer game room. Another player will be able to join.", SwingConstants.CENTER);
        gbc.insets = new Insets(0, 0, 20, 0);
        contentPanel.add(descriptionLabel, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            if (createGameListener != null) {
                createGameListener.onCancel();
            }
            dispose();
        });
        buttonPanel.add(cancelButton);
        
        createButton = new JButton("Create Game");
        createButton.addActionListener(e -> {
            if (createGameListener != null) {
                createGameListener.onCreateGame("standard");
            }
            dispose();
        });
        buttonPanel.add(createButton);
        
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPanel.add(buttonPanel, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    public String getSelectedTimeControl() {
        // Always return standard since we removed time controls
        return "standard";
    }
    
    public void setCreateGameListener(CreateGameListener listener) {
        this.createGameListener = listener;
    }
    
    public interface CreateGameListener {
        void onCreateGame(String timeControl);
        void onCancel();
    }
} 