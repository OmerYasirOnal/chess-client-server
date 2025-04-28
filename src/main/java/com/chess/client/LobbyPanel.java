package com.chess.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.chess.common.Message;

public class LobbyPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private ChessClient client;
    private DefaultListModel<GameInfo> gameListModel;
    private JList<GameInfo> gameList;
    private JButton createGameButton;
    private JButton joinGameButton;
    private JButton refreshButton;
    private JLabel userInfoLabel;
    private JLabel statusLabel;
    
    private LobbyListener lobbyListener;
    
    private List<GameInfo> availableGames = new ArrayList<>();
    
    public LobbyPanel(ChessClient client) {
        this.client = client;
        setLayout(new BorderLayout());
        initializeUI();
    }
    
    private void initializeUI() {
        // Panel header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(40, 40, 40));
        headerPanel.setPreferredSize(new Dimension(800, 60));
        
        JLabel titleLabel = new JLabel("Chess Lobby", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // User info
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setBackground(new Color(40, 40, 40));
        userInfoLabel = new JLabel("Welcome, " + client.getUsername());
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userInfoLabel.setForeground(Color.WHITE);
        userPanel.add(userInfoLabel);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(200, 50, 50));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> handleLogout());
        userPanel.add(logoutButton);
        
        headerPanel.add(userPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(550);
        splitPane.setEnabled(false);
        
        // Left panel - game list
        JPanel gameListPanel = new JPanel(new BorderLayout());
        gameListPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), 
                "Available Games", 
                TitledBorder.LEFT, 
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)));
        
        gameListModel = new DefaultListModel<>();
        gameList = new JList<>(gameListModel);
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameList.setCellRenderer(new GameListCellRenderer());
        gameList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    joinSelectedGame();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(gameList);
        gameListPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons below game list
        JPanel gameListButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshButton.setBackground(new Color(240, 240, 240));
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setFocusPainted(false);
        refreshButton.setPreferredSize(new Dimension(100, 30));
        refreshButton.addActionListener(e -> refreshGameList());
        gameListButtonPanel.add(refreshButton);
        
        joinGameButton = new JButton("Join");
        joinGameButton.setFont(new Font("Arial", Font.BOLD, 14));
        joinGameButton.setBackground(new Color(70, 130, 180));
        joinGameButton.setForeground(Color.WHITE);
        joinGameButton.setFocusPainted(false);
        joinGameButton.setPreferredSize(new Dimension(100, 30));
        joinGameButton.addActionListener(e -> joinSelectedGame());
        joinGameButton.setEnabled(false);
        gameListButtonPanel.add(joinGameButton);
        
        gameListPanel.add(gameListButtonPanel, BorderLayout.SOUTH);
        splitPane.setLeftComponent(gameListPanel);
        
        // Right panel - create game
        JPanel createGamePanel = new JPanel(new GridBagLayout());
        createGamePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), 
                "New Game", 
                TitledBorder.LEFT, 
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JLabel infoLabel = new JLabel("<html>Click the button below to start a new chess game. " +
                "After creating the game, you will need to wait for an opponent to join.</html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        createGamePanel.add(infoLabel, gbc);
        
        createGameButton = new JButton("Create New Game");
        createGameButton.setFont(new Font("Arial", Font.BOLD, 16));
        createGameButton.setBackground(new Color(70, 130, 180));
        createGameButton.setForeground(Color.WHITE);
        createGameButton.setFocusPainted(false);
        createGameButton.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        createGameButton.addActionListener(e -> createNewGame());
        
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        createGamePanel.add(createGameButton, gbc);
        
        splitPane.setRightComponent(createGamePanel);
        add(splitPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(230, 230, 230));
        statusPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        statusLabel = new JLabel("Connected to server. Loading games...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        add(statusPanel, BorderLayout.SOUTH);
        
        // Load game list initially
        refreshGameList();
        
        // Update button state when selected game changes
        gameList.addListSelectionListener(e -> {
            joinGameButton.setEnabled(gameList.getSelectedIndex() != -1);
        });
    }
    
    private void refreshGameList() {
        // Sunucudan oyun listesini iste
        statusLabel.setText("Refreshing game list...");
        
        // Listeyi temizle
        gameListModel.clear();
        availableGames.clear();
        
        // Sunucudan oyun listesini iste
        requestGameList();
    }
    
    private void requestGameList() {
        if (client != null && client.isConnected()) {
            Message requestMessage = new Message(Message.MessageType.GAME_LIST);
            client.sendMessage(requestMessage);
        } else {
            statusLabel.setText("Not connected to server! Please log in first.");
        }
    }
    
    public void updateGameList(List<GameInfo> games) {
        gameListModel.clear();
        availableGames = games;
        
        for (GameInfo game : games) {
            gameListModel.addElement(game);
        }
        
        statusLabel.setText(games.size() + " active games found. Last refresh: " + 
                java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
    
    private void joinSelectedGame() {
        int selectedIndex = gameList.getSelectedIndex();
        if (selectedIndex != -1) {
            GameInfo game = gameListModel.getElementAt(selectedIndex);
            if (lobbyListener != null) {
                lobbyListener.onJoinGame(game);
            }
        }
    }
    
    private void createNewGame() {
        // Show game creation dialog
        if (lobbyListener != null) {
            lobbyListener.onCreateGame();
        }
    }
    
    private void handleLogout() {
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION && lobbyListener != null) {
            lobbyListener.onLogout();
        }
    }
    
    public void setLobbyListener(LobbyListener listener) {
        this.lobbyListener = listener;
    }
    
    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }
    
    public interface LobbyListener {
        void onCreateGame();
        void onJoinGame(GameInfo game);
        void onLogout();
    }
    
    // Game info model
    public static class GameInfo {
        private String id;
        private String hostName;
        private String timeControl;
        private long creationTime;
        
        public GameInfo(String id, String hostName, String timeControl) {
            this.id = id;
            this.hostName = hostName;
            this.timeControl = timeControl;
            this.creationTime = System.currentTimeMillis();
        }
        
        public String getId() {
            return id;
        }
        
        public String getHostName() {
            return hostName;
        }
        
        public String getTimeControl() {
            return timeControl;
        }
        
        public long getCreationTime() {
            return creationTime;
        }
        
        @Override
        public String toString() {
            return hostName + " - " + timeControl;
        }
    }
    
    // Game list cell renderer
    private class GameListCellRenderer extends javax.swing.DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public java.awt.Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof GameInfo) {
                GameInfo game = (GameInfo) value;
                
                // Time format
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
                String timeStr = sdf.format(new java.util.Date(game.getCreationTime()));
                
                label.setText("<html><b>" + game.getHostName() + "</b> - " + 
                        game.getTimeControl() + " <i>(Created: " + timeStr + ")</i></html>");
                label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                label.setFont(new Font("Arial", Font.PLAIN, 14));
            }
            
            return label;
        }
    }
} 