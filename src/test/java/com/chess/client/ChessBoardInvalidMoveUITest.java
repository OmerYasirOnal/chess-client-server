package com.chess.client;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Point;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.swing.core.ComponentLookupScope;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Pause;
import org.junit.Test;

import com.chess.common.ChessPiece;
import com.chess.common.Message;

/**
 * Test class for the chess board to ensure basic moves work properly
 */
public class ChessBoardInvalidMoveUITest extends AssertJSwingJUnitTestCase {
    
    private FrameFixture window;
    private ChessClientSwing client;
    private Frame frame;
    
    @Override
    protected void onSetUp() {
        // Launch the application on EDT and wrap it in a FrameFixture
        client = GuiActionRunner.execute(() -> new ChessClientSwing());
        frame = client.getFrame();
        window = new FrameFixture(robot(), frame);
        window.show();
        window.robot().settings().componentLookupScope(ComponentLookupScope.ALL);
        
        // Log in and start a game
        loginAndStartGame();
    }
    
    @Override
    protected void onTearDown() {
        // Clean up the fixture
        window.cleanUp();
    }
    
    /**
     * Test 1: Verify that the board initializes correctly with pieces
     */
    @Test
    public void boardInitializesWithPieces() {
        // Find the chess board panel
        ChessBoardPanel boardPanel = findChessBoardPanel(frame);
        assertThat(boardPanel).isNotNull();
        
        // The board should have 32 pieces at the start (16 white + 16 black)
        // We'll verify this by checking a sample of key positions
        
        // Check white pawns
        for (int col = 0; col < 8; col++) {
            ChessPiece piece = boardPanel.getBoard().getPiece(6, col);
            assertThat(piece).isNotNull();
            assertThat(piece.getType()).isEqualTo(ChessPiece.PieceType.PAWN);
            assertThat(piece.getColor()).isEqualTo(ChessPiece.PieceColor.WHITE);
        }
        
        // Check white major pieces
        ChessPiece whiteRook1 = boardPanel.getBoard().getPiece(7, 0);
        assertThat(whiteRook1).isNotNull();
        assertThat(whiteRook1.getType()).isEqualTo(ChessPiece.PieceType.ROOK);
        
        ChessPiece whiteKnight1 = boardPanel.getBoard().getPiece(7, 1);
        assertThat(whiteKnight1).isNotNull();
        assertThat(whiteKnight1.getType()).isEqualTo(ChessPiece.PieceType.KNIGHT);
        
        ChessPiece whiteKing = boardPanel.getBoard().getPiece(7, 4);
        assertThat(whiteKing).isNotNull();
        assertThat(whiteKing.getType()).isEqualTo(ChessPiece.PieceType.KING);
        
        // Check black pawns
        for (int col = 0; col < 8; col++) {
            ChessPiece piece = boardPanel.getBoard().getPiece(1, col);
            assertThat(piece).isNotNull();
            assertThat(piece.getType()).isEqualTo(ChessPiece.PieceType.PAWN);
            assertThat(piece.getColor()).isEqualTo(ChessPiece.PieceColor.BLACK);
        }
    }
    
    /**
     * Test 2: Verify that a valid pawn move works
     */
    @Test
    public void validPawnMoveWorks() {
        // Find the chess board panel
        ChessBoardPanel boardPanel = findChessBoardPanel(frame);
        assertThat(boardPanel).isNotNull();
        
        JPanelFixture boardFixture = new JPanelFixture(robot(), boardPanel);
        
        // Get position of e2 pawn (row 6, col 4)
        Point pawnPosition = getSquareCenter(boardPanel, 6, 4);
        Point destination = getSquareCenter(boardPanel, 4, 4); // e4
        
        // Make a move: click pawn, then destination
        robot().click(boardPanel, pawnPosition);
        Pause.pause(300); // Wait for UI to update
        robot().click(boardPanel, destination);
        Pause.pause(300); // Wait for move to complete
        
        // Verify the pawn moved
        ChessPiece movedPawn = boardPanel.getBoard().getPiece(4, 4);
        assertThat(movedPawn).isNotNull();
        assertThat(movedPawn.getType()).isEqualTo(ChessPiece.PieceType.PAWN);
        assertThat(movedPawn.getColor()).isEqualTo(ChessPiece.PieceColor.WHITE);
        
        // Original position should be empty
        assertThat(boardPanel.getBoard().getPiece(6, 4)).isNull();
    }
    
    /**
     * Test 3: Verify that an invalid move is rejected
     */
    @Test
    public void invalidMoveIsRejected() {
        // Find the chess board panel
        ChessBoardPanel boardPanel = findChessBoardPanel(frame);
        assertThat(boardPanel).isNotNull();
        
        JPanelFixture boardFixture = new JPanelFixture(robot(), boardPanel);
        
        // Try to move a pawn incorrectly (diagonally without capture)
        Point pawnPosition = getSquareCenter(boardPanel, 6, 4); // e2
        Point invalidDestination = getSquareCenter(boardPanel, 5, 5); // f3 (invalid diagonal without capture)
        
        // Make the invalid move
        robot().click(boardPanel, pawnPosition);
        Pause.pause(300); // Wait for UI to update
        robot().click(boardPanel, invalidDestination);
        Pause.pause(300); // Wait for move to complete
        
        // Verify the pawn didn't move
        ChessPiece pawn = boardPanel.getBoard().getPiece(6, 4);
        assertThat(pawn).isNotNull();
        assertThat(pawn.getType()).isEqualTo(ChessPiece.PieceType.PAWN);
        
        // Destination should be empty
        assertThat(boardPanel.getBoard().getPiece(5, 5)).isNull();
    }
    
    // --------------------------------
    // Helper methods
    // --------------------------------
    
    /**
     * Helper to log in and start a game
     */
    private void loginAndStartGame() {
        // Login
        window.textBox("usernameField").setText("TestPlayer");
        window.textBox("serverField").setText("localhost");
        window.textBox("portField").setText("9999");
        window.button("connectButton").click();
        robot().waitForIdle();
        
        // Create a game
        window.tabbedPane().selectTab("Create Game");
        window.button(JButtonMatcher.withText("Create Game")).click();
        
        // Wait for dialog to appear
        DialogFixture createGameDialog = WindowFinder.findDialog("Create Game").using(robot());
        
        // Click "Create Game" in the dialog
        createGameDialog.button(JButtonMatcher.withText("Create Game")).click();
        
        // Wait for UI to update
        robot().waitForIdle();
        Pause.pause(500);
        
        // Simulate game start by sending a GAME_START message
        GuiActionRunner.execute(() -> {
            Message gameStartMessage = new Message(Message.MessageType.GAME_START);
            gameStartMessage.setContent("Your opponent: TestOpponent. Your color: White");
            gameStartMessage.setSender("TestOpponent");
            
            WaitingRoomPanel waitingRoom = findWaitingRoomPanel(frame);
            if (waitingRoom != null) {
                waitingRoom.setOpponentName("TestOpponent");
            }
            
            // Force the transition to the game panel
            try {
                client.showGamePanelForTesting();
                
                // Set player color to white
                GamePanel gamePanel = findGamePanel(frame);
                if (gamePanel != null) {
                    gamePanel.setPlayerColor(ChessPiece.PieceColor.WHITE);
                    gamePanel.showGameStartMessage("Game started with opponent");
                }
            } catch (Exception e) {
                System.err.println("Failed to start game: " + e.getMessage());
            }
        });
        
        // Wait for the game panel to appear
        robot().waitForIdle();
        Pause.pause(500);
    }
    
    /**
     * Find the chess board panel
     */
    private ChessBoardPanel findChessBoardPanel(Container container) {
        if (container == null) return null;
        
        for (Component component : container.getComponents()) {
            if (component instanceof ChessBoardPanel) {
                return (ChessBoardPanel) component;
            } else if (component instanceof GamePanel) {
                return ((GamePanel) component).getBoardPanel();
            } else if (component instanceof Container) {
                ChessBoardPanel found = findChessBoardPanel((Container) component);
                if (found != null) {
                    return found;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find the WaitingRoomPanel
     */
    private WaitingRoomPanel findWaitingRoomPanel(Container container) {
        if (container == null) return null;
        
        for (Component component : container.getComponents()) {
            if (component instanceof WaitingRoomPanel) {
                return (WaitingRoomPanel) component;
            }
            
            if (component instanceof Container) {
                WaitingRoomPanel found = findWaitingRoomPanel((Container) component);
                if (found != null) {
                    return found;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find the GamePanel
     */
    private GamePanel findGamePanel(Container container) {
        if (container == null) return null;
        
        for (Component component : container.getComponents()) {
            if (component instanceof GamePanel) {
                return (GamePanel) component;
            }
            
            if (component instanceof Container) {
                GamePanel found = findGamePanel((Container) component);
                if (found != null) {
                    return found;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get the center position of a chess square
     */
    private Point getSquareCenter(ChessBoardPanel boardPanel, int row, int col) {
        int squareSize = boardPanel.getWidth() / 8;
        // Add an offset to make sure we're clicking in the center of the square
        return new Point(col * squareSize + squareSize / 2, row * squareSize + squareSize / 2);
    }
} 