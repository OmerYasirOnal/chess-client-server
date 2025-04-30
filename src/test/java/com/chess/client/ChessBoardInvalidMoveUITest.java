package com.chess.client;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.chess.common.ChessBoard;
import com.chess.common.ChessPiece;

/**
 * UI Tests for the "Invalid Move → Freeze" bug.
 * 
 * These tests verify that the UI properly handles the sequence of:
 * 1. Invalid move attempt (that doesn't resolve check)
 * 2. UI state reset
 * 3. Valid move execution
 */
public class ChessBoardInvalidMoveUITest {
    
    private JFrame frame;
    private ChessBoardPanel boardPanel;
    private static final int SQUARE_SIZE = 60; // Must match ChessBoardPanel.SQUARE_SIZE
    private Robot robot;
    
    @Before
    public void setUp() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Create frame
                frame = new JFrame("Chess Board Test");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                
                // Create a dummy client
                ChessClient client = new ChessClient("localhost", 8080, "TestUser");
                
                // Create the board panel
                boardPanel = new ChessBoardPanel(client);
                
                // Add panel to frame
                frame.getContentPane().add(boardPanel);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        latch.await(); // Wait for Swing setup to complete
        
        // Create AWT Robot for UI automation
        robot = new Robot();
        robot.setAutoDelay(200); // Increase delay between robot actions
        robot.delay(1000); // Longer initial delay to ensure window is fully visible
    }
    
    @After
    public void tearDown() {
        if (frame != null) {
            SwingUtilities.invokeLater(() -> {
                frame.dispose();
            });
        }
    }
    
    /**
     * Creates a test scenario with a white pawn checking the black king.
     * - White pawn at d2 checking black king at e1
     * - Black knight at b8
     * - Black queen at d8
     * - It's Black's turn to move
     */
    private void setupCheckScenario() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Reset board
                boardPanel.resetBoard();
                ChessBoard board = boardPanel.getBoard();
                
                // Clear the board first
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        board.setPiece(row, col, null);
                    }
                }
                
                // White pawn checking black king
                board.setPiece(1, 3, new ChessPiece(ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.WHITE));
                
                // Black king in check
                board.setPiece(0, 4, new ChessPiece(ChessPiece.PieceType.KING, ChessPiece.PieceColor.BLACK));
                
                // Other black pieces
                board.setPiece(7, 1, new ChessPiece(ChessPiece.PieceType.KNIGHT, ChessPiece.PieceColor.BLACK));
                board.setPiece(7, 3, new ChessPiece(ChessPiece.PieceType.QUEEN, ChessPiece.PieceColor.BLACK));
                
                // Set current turn to BLACK
                board.setCurrentTurn(ChessPiece.PieceColor.BLACK);
                
                // Set player color to BLACK
                boardPanel.setPlayerColor(ChessPiece.PieceColor.BLACK);
                
                boardPanel.repaint();
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
                latch.countDown();
            }
        });
        
        latch.await();
        robot.delay(1000); // Longer delay for UI update
    }
    
    /**
     * Helper method to click on a square of the chessboard
     */
    private void clickSquare(int row, int col) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isBlackView = new boolean[1];
        
        SwingUtilities.invokeLater(() -> {
            isBlackView[0] = boardPanel.getPlayerColor() == ChessPiece.PieceColor.BLACK;
            latch.countDown();
        });
        
        latch.await();
        
        // Adjust coordinates if the board is flipped for black player
        if (isBlackView[0]) {
            row = 7 - row;
            col = 7 - col;
        }
        
        // Get frame location and size
        java.awt.Point frameLocation = frame.getLocationOnScreen();
        java.awt.Insets insets = frame.getInsets();
        
        // Calculate absolute screen position
        int screenX = frameLocation.x + insets.left + col * SQUARE_SIZE + SQUARE_SIZE / 2;
        int screenY = frameLocation.y + insets.top + row * SQUARE_SIZE + SQUARE_SIZE / 2;
        
        // Move mouse and click
        robot.mouseMove(screenX, screenY);
        robot.delay(300); // Delay after moving mouse
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(200); // Longer press
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(500); // Longer delay after click
    }
    
    /**
     * Checks if a piece is visually present at the specified board position
     */
    private boolean isPieceVisibleAt(int row, int col) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = new boolean[1];
        
        SwingUtilities.invokeLater(() -> {
            ChessPiece piece = boardPanel.getBoard().getPiece(row, col);
            result[0] = piece != null;
            latch.countDown();
        });
        
        latch.await();
        return result[0];
    }
    
    /**
     * Gets the piece type at the specified position
     */
    private ChessPiece.PieceType getPieceTypeAt(int row, int col) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final ChessPiece.PieceType[] result = new ChessPiece.PieceType[1];
        
        SwingUtilities.invokeLater(() -> {
            ChessPiece piece = boardPanel.getBoard().getPiece(row, col);
            result[0] = piece != null ? piece.getType() : null;
            latch.countDown();
        });
        
        latch.await();
        return result[0];
    }
    
    /**
     * Gets the current turn
     */
    private ChessPiece.PieceColor getCurrentTurn() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final ChessPiece.PieceColor[] result = new ChessPiece.PieceColor[1];
        
        SwingUtilities.invokeLater(() -> {
            result[0] = boardPanel.getBoard().getCurrentTurn();
            latch.countDown();
        });
        
        latch.await();
        return result[0];
    }
    
    /**
     * Test case 1: Valid Move After Invalid Move
     */
    @Test
    public void testInvalidMoveThenValidMove() throws Exception {
        // 1. Setup check scenario
        setupCheckScenario();
        
        // 2. Attempt an invalid move with knight
        clickSquare(7, 1); // Select knight at b8
        robot.delay(1000);
        
        clickSquare(5, 2); // Attempt to move to c6 (doesn't resolve check)
        robot.delay(1500);
        
        // 3. Verify that the knight didn't move
        assertTrue("Knight should still be at b8", isPieceVisibleAt(7, 1));
        assertFalse("No piece should be at c6", isPieceVisibleAt(5, 2));
        assertEquals("Knight should be at b8", ChessPiece.PieceType.KNIGHT, getPieceTypeAt(7, 1));
        
        // 4. Click elsewhere to clear highlights
        clickSquare(4, 4); // Click empty square
        robot.delay(1000);
        
        // 5. Make a valid move with the queen to capture the checking pawn
        clickSquare(7, 3); // Select queen at d8
        robot.delay(1000);
        
        clickSquare(1, 3); // Capture pawn at d2
        robot.delay(1500);
        
        // 6. Verify the board updates correctly
        assertFalse("Queen should no longer be at d8", isPieceVisibleAt(7, 3));
        assertTrue("Queen should now be at d2", isPieceVisibleAt(1, 3));
        assertEquals("Queen should be at d2", ChessPiece.PieceType.QUEEN, getPieceTypeAt(1, 3));
        
        // Verify turn has switched to White
        assertEquals("Turn should now be WHITE", ChessPiece.PieceColor.WHITE, getCurrentTurn());
    }
    
    /**
     * Test case 2: New Selection After Invalid Move
     */
    @Test
    public void testInvalidMoveThenNewSelection() throws Exception {
        // 1. Setup check scenario
        setupCheckScenario();
        
        // 2. Attempt an invalid move with knight
        clickSquare(7, 1); // Select knight at b8
        robot.delay(1000);
        
        clickSquare(5, 2); // Attempt to move to c6 (illegal move)
        robot.delay(1500);
        
        // Verify knight is still at original position
        assertTrue("Knight should still be at b8", isPieceVisibleAt(7, 1));
        
        // 3. Immediately select a different piece (queen)
        clickSquare(7, 3); // Select queen at d8
        robot.delay(1000);
        
        // 4. Make a valid move with queen
        clickSquare(1, 3); // Move queen to d2 (capturing pawn)
        robot.delay(1500);
        
        // Queen should now be at d2
        assertFalse("Queen should no longer be at d8", isPieceVisibleAt(7, 3));
        assertTrue("Queen should now be at d2", isPieceVisibleAt(1, 3));
        assertEquals("Queen should be at d2", ChessPiece.PieceType.QUEEN, getPieceTypeAt(1, 3));
    }
    
    /**
     * Test case 3: Valid Move After Multiple Invalid Moves
     */
    @Test
    public void testMultipleInvalidMovesThenValid() throws Exception {
        // 1. Setup check scenario
        setupCheckScenario();
        
        // 2. Attempt first invalid move with knight
        clickSquare(7, 1); // Select knight at b8
        robot.delay(1000);
        
        clickSquare(5, 2); // Move to c6 (illegal)
        robot.delay(1500);
        
        // Verify knight is still at original position
        assertTrue("Knight should still be at b8", isPieceVisibleAt(7, 1));
        
        // Attempt second invalid move
        clickSquare(7, 1); // Select knight again
        robot.delay(1000);
        
        clickSquare(5, 0); // Move to a6 (illegal)
        robot.delay(1500);
        
        // Attempt third invalid move
        clickSquare(7, 1); // Select knight again
        robot.delay(1000);
        
        clickSquare(6, 3); // Move to d7 (illegal)
        robot.delay(1500);
        
        // 3. Make a valid move (queen captures checking pawn)
        clickSquare(7, 3); // Select queen
        robot.delay(1000);
        
        clickSquare(1, 3); // Capture pawn at d2
        robot.delay(1500);
        
        // 4. Verify board updates
        assertFalse("Queen should no longer be at d8", isPieceVisibleAt(7, 3));
        assertTrue("Queen should now be at d2", isPieceVisibleAt(1, 3));
        assertEquals("Queen should be at d2", ChessPiece.PieceType.QUEEN, getPieceTypeAt(1, 3));
        
        // Verify turn switched to White
        assertEquals("Turn should now be WHITE", ChessPiece.PieceColor.WHITE, getCurrentTurn());
    }
} 