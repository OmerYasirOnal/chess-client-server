package com.chess.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private JTextField usernameField;
    private JTextField serverField;
    private JTextField portField;
    private JLabel errorLabel;
    private JButton loginButton;
    
    private LoginListener loginListener;
    
    public LoginPanel() {
        setLayout(new BorderLayout());
        initializeUI();
    }
    
    private void initializeUI() {
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(40, 40, 40));
        headerPanel.setPreferredSize(new Dimension(800, 80));
        
        JLabel titleLabel = new JLabel("Chess Game", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Login form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 240, 240));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);
        
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), 
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(usernameField, gbc);
        
        // Server address
        JLabel serverLabel = new JLabel("Server Address:");
        serverLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(serverLabel, gbc);
        
        serverField = new JTextField("localhost", 20);
        serverField.setFont(new Font("Arial", Font.PLAIN, 14));
        serverField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), 
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(serverField, gbc);
        
        // Port
        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(portLabel, gbc);
        
        portField = new JTextField("9999", 20);
        portField.setFont(new Font("Arial", Font.PLAIN, 14));
        portField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), 
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(portField, gbc);
        
        // Error message
        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        formPanel.add(errorLabel, gbc);
        
        // Login button
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(loginButton, gbc);
        
        // Login button function
        loginButton.addActionListener(this::handleLogin);
        
        // Enter key function
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin(null);
                }
            }
        });
        
        serverField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin(null);
                }
            }
        });
        
        portField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin(null);
                }
            }
        });
        
        add(formPanel, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(40, 40, 40));
        footerPanel.setPreferredSize(new Dimension(800, 30));
        
        JLabel footerLabel = new JLabel("© 2025 Chess Game - Computer Networks Project", SwingConstants.CENTER);
        footerLabel.setForeground(new Color(200, 200, 200));
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        footerPanel.add(footerLabel, BorderLayout.CENTER);
        
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String server = serverField.getText().trim();
        String portText = portField.getText().trim();
        
        // Validation
        if (username.isEmpty()) {
            errorLabel.setText("Please enter a username!");
            return;
        }
        
        if (server.isEmpty()) {
            errorLabel.setText("Please enter a server address!");
            return;
        }
        
        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException ex) {
            errorLabel.setText("Please enter a valid port number!");
            return;
        }
        
        // Notify login listener
        if (loginListener != null) {
            loginListener.onLogin(username, server, port);
        }
    }
    
    public void setLoginListener(LoginListener listener) {
        this.loginListener = listener;
    }
    
    public void setErrorMessage(String message) {
        errorLabel.setText(message);
    }
    
    // Interface for login process
    public interface LoginListener {
        void onLogin(String username, String server, int port);
    }
} 