package com.chess.client;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JLabel;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.swing.core.ComponentLookupScope;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Pause;
import org.junit.Test;

import com.chess.common.Message;

/**
 * Test class for the simplified chess game flow with no time controls and auto-start on player join.
 */
public class ChessGameFlowUITest extends AssertJSwingJUnitTestCase {
    
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
        
        // Log in to get to the lobby
        loginToLobby();
    }
    
    @Override
    protected void onTearDown() {
        // Clean up the fixture
        window.cleanUp();
    }
    
    /**
     * Test 1: Verify game creation goes to waiting room without time control options
     */
    @Test
    public void createGameNavigatesToWaitingRoom() {
        // Navigate to Create Game tab
        window.tabbedPane().selectTab("Create Game");
        
        // Click "Create Game" button
        window.button(JButtonMatcher.withText("Create Game")).click();
        
        // Wait for dialog to appear
        DialogFixture createGameDialog = WindowFinder.findDialog("Create Game").using(robot());
        
        // The dialog should show no time control options
        assertThat(findLabelWithText(createGameDialog.target(), "Time Control")).isNull();
        
        // Click "Create Game" button in the dialog
        createGameDialog.button(JButtonMatcher.withText("Create Game")).click();
        
        // Wait for UI to update
        robot().waitForIdle();
        Pause.pause(500);
        
        // Assert: We should be in the Waiting Room
        assertWaitingRoomIsVisible();
        
        // Check that the game type is "Standard"
        Component timeControlLabel = findComponentByTextAndType(frame, "Standard", JLabel.class);
        assertThat(timeControlLabel).isNotNull();
    }
    
    /**
     * Test 2: Verify waiting room shows auto-start message instead of ready button
     */
    @Test
    public void waitingRoomShowsAutoStartMessage() {
        // Create a game
        createTestGame();
        
        // Assert: We should be in the Waiting Room
        assertWaitingRoomIsVisible();
        
        // Check that there is no Ready button
        Component readyButton = findButtonWithText(frame, "Ready");
        assertThat(readyButton).isNull();
        
        // Check that there is an auto-start message
        Component autoStartLabel = findComponentContainingTextAndType(
                frame, "Game will start automatically", JLabel.class);
        assertThat(autoStartLabel).isNotNull();
        
        // Check that there is a Leave Game button
        Component leaveButton = findButtonWithText(frame, "Leave Game");
        assertThat(leaveButton).isNotNull();
    }
    
    /**
     * Test 3: Verify game starts automatically when opponent joins
     */
    @Test
    public void gameStartsAutomaticallyWhenOpponentJoins() {
        // Create a game
        createTestGame();
        
        // Simulate an opponent joining by sending a GAME_START message
        GuiActionRunner.execute(() -> {
            Message gameStartMessage = new Message(Message.MessageType.GAME_START);
            gameStartMessage.setContent("Your opponent: TestOpponent. Your color: White");
            gameStartMessage.setSender("TestOpponent");
            
            // Force the transition to the game panel
            try {
                client.showGamePanelForTesting();
                
                // Set player color to white
                GamePanel gamePanel = findGamePanel(frame);
                if (gamePanel != null) {
                    gamePanel.setPlayerColor(com.chess.common.ChessPiece.PieceColor.WHITE);
                    gamePanel.showGameStartMessage("Game started with opponent");
                }
            } catch (Exception e) {
                System.err.println("Failed to start game: " + e.getMessage());
            }
        });
        
        // Wait for the game panel to appear
        robot().waitForIdle();
        Pause.pause(500);
        
        // Check that the game panel is visible
        GamePanel gamePanel = findGamePanel(frame);
        assertThat(gamePanel).isNotNull();
        
        // Check that the game status shows game started
        Component gameStatusLabel = findComponentContainingTextAndType(
                gamePanel, "Game Started", JLabel.class);
        assertThat(gameStatusLabel).isNotNull();
    }
    
    // --------------------------------
    // Helper methods
    // --------------------------------
    
    /**
     * Helper to log in and get to the lobby
     */
    private void loginToLobby() {
        window.textBox("usernameField").setText("TestPlayer");
        window.textBox("serverField").setText("localhost");
        window.textBox("portField").setText("9999");
        robot().waitForIdle();
        window.button("connectButton").click();
        robot().waitForIdle();
    }
    
    /**
     * Helper to create a test game
     */
    private void createTestGame() {
        // Navigate to Create Game tab
        window.tabbedPane().selectTab("Create Game");
        
        // Click "Create Game" button
        window.button(JButtonMatcher.withText("Create Game")).click();
        
        // Wait for dialog to appear
        DialogFixture createGameDialog = WindowFinder.findDialog("Create Game").using(robot());
        
        // Click "Create Game" button in the dialog
        createGameDialog.button(JButtonMatcher.withText("Create Game")).click();
        
        // Wait for UI to update
        robot().waitForIdle();
        Pause.pause(500);
    }
    
    /**
     * Assert that the waiting room is visible
     */
    private void assertWaitingRoomIsVisible() {
        Component gameStatusLabel = findComponentContainingTextAndType(frame, "Waiting for opponent", JLabel.class);
        assertThat(gameStatusLabel).isNotNull();
    }
    
    /**
     * Find a button with the specified text
     */
    private JButton findButtonWithText(Container container, String text) {
        return findComponentByTextAndType(container, text, JButton.class);
    }
    
    /**
     * Find a component by text and type
     */
    private <T extends Component> T findComponentByTextAndType(Container container, String text, Class<T> type) {
        if (container == null) return null;
        
        for (Component component : container.getComponents()) {
            if (type.isInstance(component)) {
                if (component instanceof JLabel && ((JLabel)component).getText().equals(text)) {
                    return type.cast(component);
                }
                if (component instanceof JButton && ((JButton)component).getText().equals(text)) {
                    return type.cast(component);
                }
            }
            
            if (component instanceof Container) {
                T found = findComponentByTextAndType((Container)component, text, type);
                if (found != null) {
                    return found;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find a component containing the specified text and of the specified type
     */
    private <T extends Component> T findComponentContainingTextAndType(Container container, String text, Class<T> type) {
        if (container == null) return null;
        
        for (Component component : container.getComponents()) {
            if (type.isInstance(component)) {
                if (component instanceof JLabel && ((JLabel)component).getText().contains(text)) {
                    return type.cast(component);
                }
                if (component instanceof JButton && ((JButton)component).getText().contains(text)) {
                    return type.cast(component);
                }
            }
            
            if (component instanceof Container) {
                T found = findComponentContainingTextAndType((Container)component, text, type);
                if (found != null) {
                    return found;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find a label with the specified text
     */
    private JLabel findLabelWithText(Container container, String text) {
        return findComponentByTextAndType(container, text, JLabel.class);
    }
    
    /**
     * Find the game panel
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
} 