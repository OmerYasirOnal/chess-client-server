package com.chess.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JTextField serverField;
    private JTextField portField;
    private JButton connectButton;
    private JLabel statusLabel;
    
    private final MainFrame mainFrame;
    
    public LoginPanel(MainFrame parent) {
        this.mainFrame = parent;
        
        // Panel layout
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create components
        createHeaderPanel();
        createFormPanel();
        createFooterPanel();
        
        // Default values for testing
        usernameField.setText("Player1");
        serverField.setText("localhost");
        portField.setText("8888");
    }
    
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        
        JLabel titleLabel = new JLabel("Chess Game");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel subtitleLabel = new JLabel("Multiplayer Online Chess");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        subtitleLabel.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createFormPanel() {
        // Panel düzeni
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(50, 100, 50, 100));
        formPanel.setBackground(new Color(240, 240, 240));
        
        // Kullanıcı adı alanı
        JPanel usernamePanel = createFieldPanel("Username:");
        usernameField = new JTextField(20);
        usernameField.setMaximumSize(new Dimension(300, 30));
        usernamePanel.add(usernameField);
        
        // Sunucu alanı
        JPanel serverPanel = createFieldPanel("Server Address:");
        serverField = new JTextField("localhost", 20);
        serverField.setMaximumSize(new Dimension(300, 30));
        serverPanel.add(serverField);
        
        // Port alanı
        JPanel portPanel = createFieldPanel("Port:");
        portField = new JTextField("8888", 5);
        portField.setMaximumSize(new Dimension(100, 30));
        portPanel.add(portField);
        
        // Durum etiketi
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Bağlan butonu
        connectButton = new JButton("Connect to Game");
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.setMaximumSize(new Dimension(200, 40));
        connectButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToGame();
            }
        });
        
        // Form paneline elemanları ekle
        formPanel.add(Box.createVerticalGlue());
        formPanel.add(usernamePanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(serverPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(portPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        formPanel.add(statusLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(connectButton);
        formPanel.add(Box.createVerticalGlue());
        
        add(formPanel, BorderLayout.CENTER);
    }
    
    private void createFooterPanel() {
        // Alt bilgi paneli
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(200, 200, 200));
        footerPanel.setPreferredSize(new Dimension(800, 40));
        
        JLabel versionLabel = new JLabel("v1.0 - Network Project");
        versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerPanel.add(versionLabel, BorderLayout.CENTER);
        
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createFieldPanel(String labelText) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setBackground(new Color(240, 240, 240));
        
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(120, 30));
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        
        return panel;
    }
    
    private void connectToGame() {
        String username = usernameField.getText().trim();
        String server = serverField.getText().trim();
        String portText = portField.getText().trim();
        
        // Validation
        if (username.isEmpty()) {
            statusLabel.setText("Please enter a username.");
            return;
        }
        
        if (server.isEmpty()) {
            statusLabel.setText("Please enter a server address.");
            return;
        }
        
        int port;
        try {
            port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                statusLabel.setText("Port must be between 1-65535.");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid port number.");
            return;
        }
        
        // Connection attempt
        connectButton.setEnabled(false);
        statusLabel.setText("Connecting...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    ChessClient client = new ChessClient(server, port, username);
                    client.connect();
                    mainFrame.startGame(client);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    
                    if (!success) {
                        connectButton.setEnabled(true);
                        statusLabel.setText("Connection failed. Please try again.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    connectButton.setEnabled(true);
                    statusLabel.setText("Connection error: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
} 