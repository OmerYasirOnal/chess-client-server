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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.chess.client.util.UIUtils;
import com.chess.common.Message;

public class LobbyPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private ChessClient client;
    private JTabbedPane tabbedPane;
    private JTable gameTable;
    private DefaultTableModel gameTableModel;
    private JButton refreshButton;
    private JButton joinButton;
    private JButton createButton;
    private JLabel userInfoLabel;
    private JLabel statusLabel;
    private JPanel emptyGamesPanel;
    
    private LobbyListener lobbyListener;
    
    private List<GameInfo> availableGames = new ArrayList<>();
    
    public LobbyPanel(ChessClient client) {
        this.client = client;
        setLayout(new BorderLayout());
        initializeUI();
    }
    
    private void initializeUI() {
        // Header panel with user info and logout button
        createHeaderPanel();
        
        // Main content - tabbed pane with Join and Create tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setName("lobbyTabbedPane");
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Join Game tab
        JPanel joinGamePanel = createJoinGameTab();
        tabbedPane.addTab("Join Game", joinGamePanel);
        
        // Create Game tab
        JPanel createGamePanel = createCreateGameTab();
        tabbedPane.addTab("Create Game", createGamePanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
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
    }
    
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(40, 40, 40));
        headerPanel.setPreferredSize(new Dimension(800, 60));
        
        JLabel titleLabel = new JLabel("Chess Lobby", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // User info and logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setBackground(new Color(40, 40, 40));
        userInfoLabel = new JLabel("Welcome, " + client.getUsername());
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userInfoLabel.setForeground(Color.WHITE);
        userPanel.add(userInfoLabel);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setName("logoutButton");
        UIUtils.setDangerButtonStyle(logoutButton);
        logoutButton.addActionListener(e -> handleLogout());
        userPanel.add(logoutButton);
        
        headerPanel.add(userPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private JPanel createJoinGameTab() {
        JPanel joinGamePanel = new JPanel(new BorderLayout());
        joinGamePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Game table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), 
                "Available Games", 
                TitledBorder.LEFT, 
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)));
        
        // Create the table model with column names
        String[] columnNames = {"Host", "Game Type", "Created At"};
        gameTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        // Create the table
        gameTable = new JTable(gameTableModel);
        gameTable.setName("gamesTable");
        gameTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameTable.setRowHeight(30);
        gameTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        gameTable.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Center text in cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < gameTable.getColumnCount(); i++) {
            gameTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Double-click to join game
        gameTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    joinSelectedGame();
                }
            }
        });
        
        // Set table selection listener
        gameTable.getSelectionModel().addListSelectionListener(e -> {
            joinButton.setEnabled(gameTable.getSelectedRow() != -1);
        });
        
        // Empty state panel (shown when no games are available)
        emptyGamesPanel = new JPanel(new GridBagLayout());
        emptyGamesPanel.setBackground(Color.WHITE);
        
        JLabel emptyLabel = new JLabel("No open games—create one!");
        emptyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        emptyLabel.setForeground(new Color(120, 120, 120));
        
        JButton createNewButton = new JButton("Create Game");
        createNewButton.setFont(new Font("Arial", Font.BOLD, 14));
        UIUtils.setPrimaryButtonStyle(createNewButton);
        createNewButton.addActionListener(e -> {
            tabbedPane.setSelectedIndex(1); // Switch to Create Game tab
        });
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(5, 5, 5, 5);
        emptyGamesPanel.add(emptyLabel, gbc);
        gbc.insets = new Insets(15, 5, 5, 5);
        emptyGamesPanel.add(createNewButton, gbc);
        
        // Create a panel that can switch between table and empty state
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JScrollPane(gameTable), BorderLayout.CENTER);
        
        tablePanel.add(contentPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        refreshButton = new JButton("Refresh");
        refreshButton.setName("refreshButton");
        UIUtils.setNeutralButtonStyle(refreshButton);
        UIUtils.setButtonSize(refreshButton, 120, 30);
        refreshButton.addActionListener(e -> refreshGameList());
        buttonPanel.add(refreshButton);
        
        joinButton = new JButton("Join Game");
        joinButton.setName("joinGameButton");
        UIUtils.setPrimaryButtonStyle(joinButton);
        UIUtils.setButtonSize(joinButton, 120, 30);
        joinButton.addActionListener(e -> joinSelectedGame());
        joinButton.setEnabled(false); // Initially disabled until a game is selected
        buttonPanel.add(joinButton);
        
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);
        joinGamePanel.add(tablePanel, BorderLayout.CENTER);
        
        return joinGamePanel;
    }
    
    private JPanel createCreateGameTab() {
        JPanel createGamePanel = new JPanel(new BorderLayout());
        createGamePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Simple panel with a central "Create Game" button
        JPanel centerPanel = new JPanel(new GridBagLayout());
        
        JLabel instructionLabel = new JLabel("Click the button below to create a new standard chess game");
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton createGameButton = new JButton("Create Game");
        createGameButton.setName("createStandardGameButton");
        UIUtils.setPrimaryButtonStyle(createGameButton);
        createGameButton.setFont(new Font("Arial", Font.BOLD, 18));
        UIUtils.setButtonSize(createGameButton, 200, 50);
        createGameButton.addActionListener(e -> createGameWithTimeControl("standard"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 0, 30, 0);
        
        centerPanel.add(instructionLabel, gbc);
        
        gbc.insets = new Insets(30, 0, 10, 0);
        centerPanel.add(createGameButton, gbc);
        
        createGamePanel.add(centerPanel, BorderLayout.CENTER);
        
        return createGamePanel;
    }
    
    private void refreshGameList() {
        statusLabel.setText("Refreshing game list...");
        
        // Clear the table
        gameTableModel.setRowCount(0);
        availableGames.clear();
        
        // Request game list from server
        if (client != null && client.isConnected()) {
            Message requestMessage = new Message(Message.MessageType.GAME_LIST);
            client.sendMessage(requestMessage);
        } else {
            statusLabel.setText("Not connected to server! Please log in first.");
        }
    }
    
    /**
     * Get the list of available games
     */
    public List<GameInfo> getAvailableGames() {
        return availableGames;
    }
    
    public void updateGameList(List<GameInfo> games) {
        availableGames = games;
        
        // Clear existing table data
        gameTableModel.setRowCount(0);
        
        if (games.isEmpty()) {
            // Show empty state if no games available
            if (gameTable.getParent() instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) gameTable.getParent().getParent();
                scrollPane.getParent().add(emptyGamesPanel, BorderLayout.CENTER);
                scrollPane.setVisible(false);
                emptyGamesPanel.setVisible(true);
            }
        } else {
            // Populate table with game data
            for (GameInfo game : games) {
                String host = game.getHostName();
                String gameType = "standard";
                
                // Format creation time
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String creationTime = sdf.format(new Date(game.getCreationTime()));
                
                // Add row to table
                gameTableModel.addRow(new Object[]{
                    host,
                    gameType,
                    creationTime
                });
            }
            
            // Show table and hide empty state
            if (gameTable.getParent() instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) gameTable.getParent().getParent();
                if (!scrollPane.isVisible()) {
                    scrollPane.getParent().remove(emptyGamesPanel);
                    scrollPane.setVisible(true);
                }
            }
        }
        
        statusLabel.setText(games.size() + " active games found. Last refresh: " + 
                java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
    
    // Helper method to determine the game type from time control
    private String determineGameType(String timeControl) {
        // Always return standard type
        return "standard";
    }
    
    private void joinSelectedGame() {
        int selectedRow = gameTable.getSelectedRow();
        if (selectedRow != -1 && selectedRow < availableGames.size()) {
            GameInfo game = availableGames.get(selectedRow);
            if (lobbyListener != null) {
                lobbyListener.onJoinGame(game);
            }
        }
    }
    
    /**
     * Create game with specified time control - this now ignores the timeControl parameter
     * and always creates a standard chess game
     */
    private void createGameWithTimeControl(String timeControl) {
        // Always create a standard game regardless of the timeControl parameter
        if (lobbyListener != null) {
            lobbyListener.onCreateGame("standard");
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
    
    public LobbyListener getLobbyListener() {
        return lobbyListener;
    }
    
    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }
    
    public interface LobbyListener {
        void onCreateGame(String gameType);
        void onJoinGame(GameInfo game);
        void onLogout();
    }
    
    /**
     * Game information container class
     */
    public static class GameInfo {
        private String id;
        private String hostName;
        private String gameType;
        private long creationTime;
        
        public GameInfo(String id, String hostName, String gameType) {
            this.id = id;
            this.hostName = hostName;
            this.gameType = gameType;
            this.creationTime = System.currentTimeMillis();
        }
        
        public String getId() {
            return id;
        }
        
        public String getHostName() {
            return hostName;
        }
        
        public String getGameType() {
            return gameType;
        }
        
        public long getCreationTime() {
            return creationTime;
        }
        
        @Override
        public String toString() {
            return "Game hosted by: " + hostName;
        }
    }
} 