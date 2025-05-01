package com.chess.client;

import javax.swing.SwingUtilities;

/**
 * Simple launcher class to start the Chess Client
 * with the remote server configuration.
 */
public class ChessClientLauncher {
    public static void main(String[] args) {
        // Set the default IP and port for the remote server
        final String defaultIp = "141.147.25.123";
        final int defaultPort = 9999;
        
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
        
        System.out.println("Chess Client launched");
        System.out.println("Default server: " + defaultIp + ":" + defaultPort);
        System.out.println("Enter a username and connect to play!");
    }
} 