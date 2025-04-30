package com.chess.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

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
        logoutButton.setBackground(new Color(200, 50, 50));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
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
        String[] columnNames = {"Host", "Game Type", "Time Control", "Created At"};
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
        createNewButton.setBackground(new Color(70, 130, 180));
        createNewButton.setForeground(Color.WHITE);
        createNewButton.setFocusPainted(false);
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
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshButton.setBackground(new Color(240, 240, 240));
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setFocusPainted(false);
        refreshButton.setPreferredSize(new Dimension(120, 30));
        refreshButton.addActionListener(e -> refreshGameList());
        buttonPanel.add(refreshButton);
        
        joinButton = new JButton("Join Game");
        joinButton.setName("joinGameButton");
        joinButton.setFont(new Font("Arial", Font.BOLD, 14));
        joinButton.setBackground(new Color(70, 130, 180));
        joinButton.setForeground(Color.WHITE);
        joinButton.setFocusPainted(false);
        joinButton.setPreferredSize(new Dimension(120, 30));
        joinButton.addActionListener(e -> joinSelectedGame());
        joinButton.setEnabled(false); // Initially disabled until a game is selected
        buttonPanel.add(joinButton);
        
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);
        joinGamePanel.add(tablePanel, BorderLayout.CENTER);
        
        return joinGamePanel;
    }
    
    private JPanel createCreateGameTab() {
        JPanel createGamePanel = new JPanel(new BorderLayout());
        createGamePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Presets panel
        JPanel presetsPanel = new JPanel(new BorderLayout());
        presetsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), 
                "Game Presets", 
                TitledBorder.LEFT, 
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)));
        
        JPanel instructionPanel = new JPanel(new BorderLayout());
        instructionPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel instructionLabel = new JLabel("<html>Select a preset or create a custom game with your preferred time control.</html>");
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        instructionPanel.add(instructionLabel, BorderLayout.CENTER);
        
        presetsPanel.add(instructionPanel, BorderLayout.NORTH);
        
        // 3x3 grid of presets
        JPanel gridPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        gridPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Game type labels
        String[] gameTypes = {"Blitz", "Rapid", "Classical"};
        
        // Time control settings (minutes + increment)
        String[][] timeControls = {
            {"3+2", "5+0", "10+5"},     // Blitz options
            {"15+10", "20+0", "30+0"},  // Rapid options
            {"45+45", "60+30", "90+30"} // Classical options
        };
        
        // Create 3x3 grid of preset buttons
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                final String gameType = gameTypes[row];
                final String timeControl = timeControls[row][col];
                
                JPanel presetPanel = new JPanel();
                presetPanel.setLayout(new BoxLayout(presetPanel, BoxLayout.Y_AXIS));
                presetPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
                presetPanel.setBackground(new Color(250, 250, 250));
                
                JLabel typeLabel = new JLabel(gameType);
                typeLabel.setFont(new Font("Arial", Font.BOLD, 16));
                typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                typeLabel.setBorder(new EmptyBorder(10, 5, 5, 5));
                
                JLabel timeLabel = new JLabel(timeControl);
                timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                JButton createButton = new JButton("Create");
                createButton.setName("createButton_" + timeControl.replace('+', '_'));
                createButton.setFont(new Font("Arial", Font.BOLD, 14));
                createButton.setBackground(new Color(70, 130, 180));
                createButton.setForeground(Color.WHITE);
                createButton.setFocusPainted(false);
                createButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                createButton.setMaximumSize(new Dimension(100, 30));
                createButton.setBorder(new EmptyBorder(5, 15, 5, 15));
                createButton.addActionListener(e -> createGameWithTimeControl(timeControl));
                
                presetPanel.add(Box.createVerticalStrut(10));
                presetPanel.add(typeLabel);
                presetPanel.add(Box.createVerticalStrut(5));
                presetPanel.add(timeLabel);
                presetPanel.add(Box.createVerticalStrut(15));
                presetPanel.add(createButton);
                presetPanel.add(Box.createVerticalStrut(10));
                
                gridPanel.add(presetPanel);
            }
        }
        
        presetsPanel.add(gridPanel, BorderLayout.CENTER);
        
        // Custom game button
        JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        customPanel.setBorder(new EmptyBorder(10, 0, 20, 0));
        
        JButton customButton = new JButton("Custom Game...");
        customButton.setName("customGameButton");
        customButton.setFont(new Font("Arial", Font.BOLD, 16));
        customButton.setBackground(new Color(70, 130, 180));
        customButton.setForeground(Color.WHITE);
        customButton.setFocusPainted(false);
        customButton.setPreferredSize(new Dimension(180, 40));
        customButton.addActionListener(e -> showCustomGameDialog());
        
        customPanel.add(customButton);
        presetsPanel.add(customPanel, BorderLayout.SOUTH);
        
        createGamePanel.add(presetsPanel, BorderLayout.CENTER);
        
        return createGamePanel;
    }
    
    private void showCustomGameDialog() {
        // Find the parent frame for the dialog
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog customDialog = new JDialog(parentFrame, "Custom Game", true);
        customDialog.setLayout(new BorderLayout());
        customDialog.setSize(400, 300);
        customDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Title
        JLabel titleLabel = new JLabel("Custom Time Control");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        contentPanel.add(titleLabel, gbc);
        
        // Minutes
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel minutesLabel = new JLabel("Minutes:");
        minutesLabel.setFont(new Font("Arial", Font.BOLD, 14));
        contentPanel.add(minutesLabel, gbc);
        
        gbc.gridx = 1;
        Integer[] minutes = {1, 2, 3, 5, 7, 10, 15, 20, 30, 45, 60, 90, 120};
        javax.swing.JComboBox<Integer> minutesCombo = new javax.swing.JComboBox<>(minutes);
        minutesCombo.setName("minutesComboBox");
        minutesCombo.setSelectedItem(7); // Default 7 minutes
        contentPanel.add(minutesCombo, gbc);
        
        // Increment
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel incrementLabel = new JLabel("Increment (seconds):");
        incrementLabel.setFont(new Font("Arial", Font.BOLD, 14));
        contentPanel.add(incrementLabel, gbc);
        
        gbc.gridx = 1;
        Integer[] increments = {0, 1, 2, 3, 5, 10, 15, 20, 30, 45, 60};
        javax.swing.JComboBox<Integer> incrementCombo = new javax.swing.JComboBox<>(increments);
        incrementCombo.setName("incrementComboBox");
        incrementCombo.setSelectedItem(5); // Default to 5 seconds increment 
        contentPanel.add(incrementCombo, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setName("cancelButton");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelButton.addActionListener(e -> customDialog.dispose());
        buttonPanel.add(cancelButton);
        
        JButton createButton = new JButton("Create Game");
        createButton.setName("createCustomGameButton");
        createButton.setFont(new Font("Arial", Font.BOLD, 14));
        createButton.setBackground(new Color(70, 130, 180));
        createButton.setForeground(Color.WHITE);
        createButton.setFocusPainted(false);
        createButton.addActionListener(e -> {
            int selectedMinutes = (Integer) minutesCombo.getSelectedItem();
            int selectedIncrement = (Integer) incrementCombo.getSelectedItem();
            String timeControl = selectedMinutes + "+" + selectedIncrement;
            customDialog.dispose();
            createGameWithTimeControl(timeControl);
        });
        buttonPanel.add(createButton);
        
        customDialog.add(contentPanel, BorderLayout.CENTER);
        customDialog.add(buttonPanel, BorderLayout.SOUTH);
        customDialog.setVisible(true);
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
        // Clear the table
        gameTableModel.setRowCount(0);
        availableGames = games;
        
        // Check if there are any games
        if (games.isEmpty()) {
            // Show empty state
            if (gameTable.getParent() instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) gameTable.getParent().getParent();
                scrollPane.getParent().add(emptyGamesPanel, BorderLayout.CENTER);
                scrollPane.setVisible(false);
                emptyGamesPanel.setVisible(true);
            }
        } else {
            // Populate the table
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            
            for (GameInfo game : games) {
                String host = game.getHostName();
                // Determine game type based on time control
                String timeControl = game.getTimeControl();
                String gameType = determineGameType(timeControl);
                
                // Format creation time
                Date creationDate = new Date(game.getCreationTime());
                String createdAt = dateFormat.format(creationDate);
                
                // Add to table
                gameTableModel.addRow(new Object[]{host, gameType, timeControl, createdAt});
            }
            
            // Show table
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
    
    private String determineGameType(String timeControl) {
        if (timeControl == null) return "Standard";
        
        String[] parts = timeControl.split("\\+");
        if (parts.length != 2) return "Standard";
        
        try {
            int minutes = Integer.parseInt(parts[0]);
            
            if (minutes <= 5) return "Blitz";
            if (minutes <= 30) return "Rapid";
            return "Classical";
        } catch (NumberFormatException e) {
            return "Standard";
        }
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
    
    private void createGameWithTimeControl(String timeControl) {
        // Call the listener to create a game
        if (lobbyListener != null) {
            lobbyListener.onCreateGame(timeControl);
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
        void onCreateGame(String timeControl);
        void onJoinGame(GameInfo game);
        void onLogout();
    }
    
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
} 