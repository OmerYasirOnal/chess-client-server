package com.chess.client.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.Border;

/**
 * Utility class for consistent UI styling across the application
 */
public class UIUtils {
    
    // Color constants for the application
    public static final Color PRIMARY_COLOR = new Color(70, 130, 180);  // SteelBlue
    public static final Color DANGER_COLOR = new Color(200, 50, 50);    // Red
    public static final Color SUCCESS_COLOR = new Color(0, 180, 0);     // Green
    public static final Color NEUTRAL_COLOR = new Color(240, 240, 240); // Light gray
    public static final Color WAITING_COLOR = new Color(255, 165, 0);   // Orange
    
    // Standard button border
    private static final Border STANDARD_BUTTON_BORDER = BorderFactory.createEmptyBorder(8, 15, 8, 15);
    
    /**
     * Apply primary button style (blue with white text)
     * @param button The button to style
     */
    public static void setPrimaryButtonStyle(JButton button) {
        setButtonStyle(button, PRIMARY_COLOR, Color.WHITE, new Font("Arial", Font.BOLD, 14));
    }
    
    /**
     * Apply danger button style (red with white text)
     * @param button The button to style
     */
    public static void setDangerButtonStyle(JButton button) {
        setButtonStyle(button, DANGER_COLOR, Color.WHITE, new Font("Arial", Font.BOLD, 14));
    }
    
    /**
     * Apply success button style (green with white text)
     * @param button The button to style
     */
    public static void setSuccessButtonStyle(JButton button) {
        setButtonStyle(button, SUCCESS_COLOR, Color.WHITE, new Font("Arial", Font.BOLD, 14));
    }
    
    /**
     * Apply neutral button style (light gray with BLACK text for better visibility)
     * @param button The button to style
     */
    public static void setNeutralButtonStyle(JButton button) {
        setButtonStyle(button, NEUTRAL_COLOR, Color.BLACK, new Font("Arial", Font.PLAIN, 14));
    }
    
    /**
     * Generic method to apply a specific style to a button
     * @param button The button to style
     * @param background Background color
     * @param foreground Foreground (text) color
     * @param font Font to use for the button text
     */
    public static void setButtonStyle(JButton button, Color background, Color foreground, Font font) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(font);
        button.setFocusPainted(false);
        button.setBorder(STANDARD_BUTTON_BORDER);
        button.setOpaque(true);
    }
    
    /**
     * Set a specific size for a button
     * @param button The button to size
     * @param width Width in pixels
     * @param height Height in pixels
     */
    public static void setButtonSize(JButton button, int width, int height) {
        button.setPreferredSize(new Dimension(width, height));
    }
    
    /**
     * Bekleyen durum için etiket stili uygula
     * @param label Stil uygulanacak etiket
     */
    public static void setWaitingStyle(JLabel label) {
        label.setForeground(WAITING_COLOR);
        label.setFont(new Font("Arial", Font.BOLD, 16));
    }
} 