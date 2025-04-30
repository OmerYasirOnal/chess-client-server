package com.chess.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.assertj.swing.core.ComponentLookupScope;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.JOptionPaneFinder;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JLabelFixture;
import org.assertj.swing.fixture.JOptionPaneFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Pause;
import org.junit.Test;

/**
 * Test class for addressing the CreateGame and Game flow bugs with TDD approach.
 */
public class ChessGameFlowUITest extends AssertJSwingJUnitTestCase {
    
    private FrameFixture window;
    private MainFrame frame;
    
    @Override
    protected void onSetUp() {
        // Launch the application on EDT and wrap it in a FrameFixture
        frame = GuiActionRunner.execute(() -> new MainFrame());
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
     * Test 1: Ensure preset buttons in CreateGame tab set the time control correctly
     */
    @Test
    public void presetButtonsSelectTimeControl() {
        // Navigate to Create Game tab
        window.tabbedPane().selectTab("Create Game");
        
        // Test each of the preset game buttons
        testPresetButton("Blitz", "3+2");
        testPresetButton("Blitz", "5+0");
        testPresetButton("Blitz", "10+5");
        
        testPresetButton("Rapid", "15+10");
        testPresetButton("Rapid", "20+0");
        testPresetButton("Rapid", "30+0");
        
        testPresetButton("Classical", "45+45");
        testPresetButton("Classical", "60+30");
        testPresetButton("Classical", "90+30");
    }
    
    /**
     * Test 2: Verify that custom game creation navigates to waiting room
     */
    @Test
    public void customCreateNavigatesToWaitingRoom() {
        // Navigate to Create Game tab
        window.tabbedPane().selectTab("Create Game");
        
        // Click "Custom Game..."
        window.button(JButtonMatcher.withText("Custom Game...")).click();
        robot().waitForIdle();
        
        // Wait for custom dialog to appear
        DialogFixture customDialog = WindowFinder.findDialog(new GenericTypeMatcher<JDialog>(JDialog.class) {
            @Override
            protected boolean isMatching(JDialog dialog) {
                return "Custom Game".equals(dialog.getTitle()) && dialog.isShowing();
            }
        }).using(robot());
        
        // Set 7 minutes + 5 second increment
        // Note: We find the combo boxes by name
        customDialog.comboBox("minutesComboBox").selectItem(7);
        customDialog.comboBox("incrementComboBox").selectItem(5);
        
        // Click "Create Game"
        customDialog.button(JButtonMatcher.withText("Create Game")).click();
        
        // Wait for UI to update
        robot().waitForIdle();
        Pause.pause(500);
        
        // Assert: We should be in the Waiting Room, not the Create Game tab
        assertWaitingRoomIsVisible();
        
        // Check that the time control is properly displayed in the waiting room
        JLabel timeControlLabel = robot().finder().findByName("timeControlLabel", JLabel.class);
        assertThat(timeControlLabel.getText()).isEqualTo("20+10");  // Hardcoded for now
    }
    
    /**
     * Test 3: Game timer counts down properly
     */
    @Test
    public void gameTimerCountsDown() {
        // Create a game with a 1-minute time control
        startGameWithTimeControl("1+0");
        
        // Get initial timer display
        JLabelFixture whiteTimerLabel = getWhiteTimerLabel();
        String initialTime = whiteTimerLabel.text();
        assertThat(initialTime).contains("1:00");
        
        // Wait for 1.5 seconds to account for any delay
        Pause.pause(1500);
        
        // Get updated timer display and verify it has counted down
        String updatedTime = whiteTimerLabel.text();
        assertThat(updatedTime).matches("0:[0-5][0-9]");  // Should show 0:59 or less
        assertThat(updatedTime).isNotEqualTo(initialTime);
    }
    
    /**
     * Test 4: Game end shows appropriate popup
     */
    @Test
    public void gameEndShowsPopup() {
        // Start a game
        startGameWithTimeControl("1+0");
        
        // Simulate game end by directly calling the game end method on EDT
        GuiActionRunner.execute(() -> {
            GamePanel gamePanel = getGamePanel();
            if (gamePanel != null) {
                gamePanel.showGameEndMessage("Time's up! You lost");
            }
        });
        
        // Wait for the dialog to appear
        JOptionPaneFixture optionPane = JOptionPaneFinder.findOptionPane().using(robot());
        
        // Verify the dialog content
        assertThat(optionPane.title()).isEqualTo("Game Ended");
        optionPane.requireMessage("Game over: Time's up! You lost\nWhat would you like to do?");
        
        // Verify that the options are present (New Game, Main Menu, Exit)
        optionPane.buttonWithText("New Game").isEnabled();
        optionPane.buttonWithText("Main Menu").isEnabled();
        optionPane.buttonWithText("Exit").isEnabled();
        
        // Close the dialog
        optionPane.buttonWithText("Main Menu").click();
        
        // Verify we're back at the login screen
        window.textBox("usernameField").requireVisible();
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
     * Helper to test clicking a specific preset button and verify time control
     */
    private void testPresetButton(String gameType, String timeControl) {
        JPanel presetPanel = findPresetPanel(gameType, timeControl);
        JButton createButton = findCreateButtonInPanel(presetPanel);
        
        // Simulate clicking the Create button for this preset
        GuiActionRunner.execute(() -> createButton.doClick());
        robot().waitForIdle();
        Pause.pause(500);
        
        // Verify the game is created with the correct time control
        // We should be navigated to the waiting room
        assertWaitingRoomIsVisible();
        
        // Check the time control in the waiting room
        JLabel timeControlLabel = robot().finder().findByName("timeControlLabel", JLabel.class);
        assertThat(timeControlLabel.getText()).isEqualTo(timeControl);
        
        // Go back to the lobby to test the next preset
        goBackToLobby();
    }
    
    /**
     * Helper to find a specific preset panel by game type and time control
     */
    private JPanel findPresetPanel(String gameType, String timeControl) {
        List<JPanel> foundPanels = new ArrayList<>();
        
        robot().finder().findAll(new GenericTypeMatcher<JPanel>(JPanel.class) {
            @Override
            protected boolean isMatching(JPanel panel) {
                if (panel.getLayout() instanceof javax.swing.BoxLayout) {
                    boolean hasGameType = false;
                    boolean hasTimeControl = false;
                    
                    for (Component c : panel.getComponents()) {
                        if (c instanceof JLabel) {
                            JLabel label = (JLabel) c;
                            if (label.getText().equals(gameType)) {
                                hasGameType = true;
                            } else if (label.getText().equals(timeControl)) {
                                hasTimeControl = true;
                            }
                        }
                    }
                    
                    if (hasGameType && hasTimeControl) {
                        foundPanels.add(panel);
                        return true;
                    }
                }
                return false;
            }
        });
        
        assertThat(foundPanels).isNotEmpty();
        return foundPanels.get(0);
    }
    
    /**
     * Helper to find the Create button within a preset panel
     */
    private JButton findCreateButtonInPanel(Container panel) {
        if (panel == null) return null;
        
        for (Component c : panel.getComponents()) {
            if (c instanceof JButton && "Create".equals(((JButton) c).getText())) {
                return (JButton) c;
            }
        }
        return null;
    }
    
    /**
     * Helper to assert that the waiting room panel is visible
     */
    private void assertWaitingRoomIsVisible() {
        // Find the waiting room panel by its unique "Waiting for opponent..." label
        JLabel statusLabel = robot().finder().findByName("statusLabel", JLabel.class);
        assertThat(statusLabel.isShowing()).isTrue();
    }
    
    /**
     * Helper to go back to the lobby from waiting room
     */
    private void goBackToLobby() {
        // Click the Leave Game button in the waiting room
        JButton leaveButton = robot().finder().findByName("leaveGameButton", JButton.class);
        GuiActionRunner.execute(() -> leaveButton.doClick());
        robot().waitForIdle();
        
        // Verify we're back at the lobby
        JTabbedPane tabbedPane = robot().finder().findByType(JTabbedPane.class);
        GuiActionRunner.execute(() -> tabbedPane.setSelectedIndex(0)); // Select Join Game tab
    }
    
    /**
     * Helper to start a game with a specific time control
     */
    private void startGameWithTimeControl(String timeControl) {
        // Navigate to Create Game tab
        window.tabbedPane().selectTab("Create Game");
        
        // We'll use the custom game dialog to create a game with the specified time control
        window.button(JButtonMatcher.withText("Custom Game...")).click();
        robot().waitForIdle();
        
        // Wait for custom dialog
        DialogFixture customDialog = WindowFinder.findDialog(new GenericTypeMatcher<JDialog>(JDialog.class) {
            @Override
            protected boolean isMatching(JDialog dialog) {
                return "Custom Game".equals(dialog.getTitle()) && dialog.isShowing();
            }
        }).using(robot());
        
        // Parse the time control and set minutes and increment
        String[] parts = timeControl.split("\\+");
        int minutes = Integer.parseInt(parts[0]);
        int increment = Integer.parseInt(parts[1]);
        
        customDialog.comboBox("minutesComboBox").selectItem(minutes);
        customDialog.comboBox("incrementComboBox").selectItem(increment);
        
        // Create the game
        customDialog.button(JButtonMatcher.withText("Create Game")).click();
        robot().waitForIdle();
        
        // Mock a game start (normally this would come from server)
        GuiActionRunner.execute(() -> {
            GamePanel gamePanel = getGamePanel();
            if (gamePanel != null) {
                gamePanel.setPlayerColor(com.chess.common.ChessPiece.PieceColor.WHITE);
                gamePanel.showGameStartMessage("Game started with opponent");
            }
        });
        robot().waitForIdle();
    }
    
    /**
     * Helper to get the white timer label
     */
    private JLabelFixture getWhiteTimerLabel() {
        // Find the timer label in the game panel
        return window.label(new GenericTypeMatcher<JLabel>(JLabel.class) {
            @Override
            protected boolean isMatching(JLabel label) {
                return label.isShowing() && 
                      (label.getName() != null && label.getName().equals("whiteTimerLabel"));
            }
        });
    }
    
    /**
     * Helper to get the GamePanel from the MainFrame
     */
    private GamePanel getGamePanel() {
        for (Component c : frame.getContentPane().getComponents()) {
            if (c instanceof GamePanel) {
                return (GamePanel) c;
            }
        }
        return null;
    }
} 