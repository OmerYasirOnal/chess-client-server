package com.chess.client;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import com.chess.common.ChessBoard;
import com.chess.common.ChessPiece;

/**
 * Manual test class to visualize and manually reproduce the "Invalid Move → Freeze" bug.
 * 
 * This test sets up a scenario with a white pawn checking a black king, where:
 * 1. Black can try to make an invalid move with a knight (not resolving check)
 * 2. Black can then try to make a valid move with the queen (capturing the checking pawn)
 * 
 * In the buggy version, the UI would freeze after the invalid move.
 * In the fixed version, the selection clears properly and the UI remains responsive.
 */
public class ChessBoardInvalidMoveBugTest {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }
    
    private static void createAndShowGUI() {
        // Create frame
        JFrame frame = new JFrame("Invalid Move → Freeze Bug Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create a panel with instructions
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setLayout(new BorderLayout());
        instructionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        instructionsPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Test for 'Invalid Move → Freeze' Bug");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        instructionsPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel stepsPanel = new JPanel();
        stepsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        stepsPanel.setBackground(Color.WHITE);
        
        JLabel stepsLabel = new JLabel("<html>" +
                "<b>Reproduction Steps:</b><br>" +
                "1. White pawn on d2 is checking Black king on e1<br>" +
                "2. Try an invalid move: click Black knight on b8, then click c6 (should show purple highlight)<br>" +
                "3. Try a valid move: click Black queen on d8, then click d2 to capture the checking pawn<br><br>" +
                "With the bug fixed, the UI should remain responsive and the queen should capture the pawn.<br>" +
                "In the buggy version, the queen would not visibly move after step 3." +
                "</html>");
        stepsLabel.setPreferredSize(new Dimension(600, 120));
        stepsPanel.add(stepsLabel);
        
        instructionsPanel.add(stepsPanel, BorderLayout.CENTER);
        
        // Create a dummy client for the board panel
        ChessClient client = new ChessClient("localhost", 8080, "TestUser");
        
        // Create board panel
        ChessBoardPanel boardPanel = new ChessBoardPanel(client);
        
        // Set up the test scenario
        ChessBoard board = boardPanel.getBoard();
        board.reset(); // Reset first to clear any existing setup
        
        // Clear the board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board.setPiece(row, col, null);
            }
        }
        
        // Set up the pieces for the test scenario
        // White pawn checking black king
        board.setPiece(1, 3, new ChessPiece(ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.WHITE));
        
        // Black king in check
        board.setPiece(0, 4, new ChessPiece(ChessPiece.PieceType.KING, ChessPiece.PieceColor.BLACK));
        
        // Black pieces for testing
        board.setPiece(7, 1, new ChessPiece(ChessPiece.PieceType.KNIGHT, ChessPiece.PieceColor.BLACK)); // Knight at b8
        board.setPiece(7, 3, new ChessPiece(ChessPiece.PieceType.QUEEN, ChessPiece.PieceColor.BLACK));  // Queen at d8
        
        // Set current turn to BLACK
        board.setCurrentTurn(ChessPiece.PieceColor.BLACK);
        
        // Set player color to BLACK
        boardPanel.setPlayerColor(ChessPiece.PieceColor.BLACK);
        
        // Prepare the frame
        frame.getContentPane().add(instructionsPanel, BorderLayout.NORTH);
        frame.getContentPane().add(boardPanel, BorderLayout.CENTER);
        
        // Add status label at the bottom
        JLabel statusLabel = new JLabel("It's Black's turn. Try to make an invalid move with the knight first.");
        statusLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.getContentPane().add(statusLabel, BorderLayout.SOUTH);
        
        // Display the frame
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        System.out.println("Test opened - Black's turn.");
        System.out.println("Test scenario: White pawn on d2 checking Black king on e1.");
        System.out.println("Instructions: Try to make an invalid move with the knight, then try to capture the pawn with the queen.");
    }
} 