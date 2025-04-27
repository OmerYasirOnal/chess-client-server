package com.chess.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

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

public class ChessClientSwing {
    private JFrame frame;
    private JTextField usernameField;
    private JTextField serverField;
    private JTextField portField;
    private JLabel statusLabel;
    private JTextArea chatArea;
    private JTextField chatInput;
    private ChessClient client;
    private ChessBoardPanel chessBoardPanel;
    private JButton readyButton;

    public ChessClientSwing() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Chess Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        // Create login panel
        createLoginPanel();
    }

    private void createLoginPanel() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Chess Game");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        loginPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        loginPanel.add(usernameField, gbc);

        JLabel serverLabel = new JLabel("Server Address:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(serverLabel, gbc);

        serverField = new JTextField("localhost");
        gbc.gridx = 1;
        gbc.gridy = 2;
        loginPanel.add(serverField, gbc);

        JLabel portLabel = new JLabel("Port:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        loginPanel.add(portLabel, gbc);

        portField = new JTextField("8888");
        gbc.gridx = 1;
        gbc.gridy = 3;
        loginPanel.add(portField, gbc);

        JButton connectButton = new JButton("Connect");
        gbc.gridx = 1;
        gbc.gridy = 4;
        loginPanel.add(connectButton, gbc);

        JLabel messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        loginPanel.add(messageLabel, gbc);

        connectButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String serverAddress = serverField.getText().trim();
            String portText = portField.getText().trim();

            if (username.isEmpty()) {
                messageLabel.setText("Please enter a username!");
                return;
            }

            if (serverAddress.isEmpty()) {
                messageLabel.setText("Please enter a server address!");
                return;
            }

            int port;
            try {
                port = Integer.parseInt(portText);
            } catch (NumberFormatException ex) {
                messageLabel.setText("Please enter a valid port number!");
                return;
            }

            connectToServer(username, serverAddress, port);
        });

        frame.setContentPane(loginPanel);
        frame.setVisible(true);
    }

    private void createGamePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Chess board
        chessBoardPanel = new ChessBoardPanel(client);
        mainPanel.add(chessBoardPanel, BorderLayout.CENTER);

        // Right panel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        rightPanel.setPreferredSize(new Dimension(250, 600));

        // Status label
        statusLabel = new JLabel("Waiting for opponent...");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(statusLabel);
        rightPanel.add(Box.createVerticalStrut(10));

        // Chat area
        JLabel chatLabel = new JLabel("Chat");
        chatLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(chatLabel);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setPreferredSize(new Dimension(230, 300));
        chatScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(chatScrollPane);
        rightPanel.add(Box.createVerticalStrut(10));

        // Chat input
        chatInput = new JTextField();
        chatInput.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        readyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        readyButton.addActionListener(e -> {
            client.sendReadyMessage();
            readyButton.setEnabled(false);
        });
        rightPanel.add(readyButton);

        mainPanel.add(rightPanel, BorderLayout.EAST);
        frame.setContentPane(mainPanel);
    }

    private void connectToServer(String username, String serverAddress, int port) {
        try {
            client = new ChessClient(serverAddress, port, username);
            
            // Add event listeners
            client.setMessageListener(message -> {
                SwingUtilities.invokeLater(() -> {
                    switch (message.getType()) {
                        case CONNECT:
                            updateStatus(message.getContent());
                            break;
                        case GAME_START:
                            if (message.getPlayerInfo() != null) {
                                chessBoardPanel.setPlayerColor(message.getPlayerInfo().getColor());
                            }
                            updateStatus(message.getContent());
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
                            break;
                    }
                });
            });
            
            client.connect();
            
            // Create and show game screen
            createGamePanel();
            
        } catch (Exception e) {
            showErrorDialog("Connection Error", "Error connecting to server: " + e.getMessage());
        }
    }

    private void updateStatus(String status) {
        statusLabel.setText(status);
    }

    private void resetGame() {
        if (client != null) {
            client.disconnect();
            String username = client.getUsername();
            String host = client.getHost();
            int port = client.getPort();
            
            connectToServer(username, host, port);
        }
    }

    private void showErrorDialog(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
        });
    }

    private void showGameEndDialog(String result) {
        SwingUtilities.invokeLater(() -> {
            Object[] options = {"New Game", "Exit"};
            int choice = JOptionPane.showOptionDialog(frame,
                    result,
                    "Game Over",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);
            
            if (choice == 0) {
                resetGame();
            } else {
                client.disconnect();
                createLoginPanel();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChessClientSwing();
        });
    }
} 