package com.chess.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class CreateGamePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private JRadioButton blitzOption;
    private JRadioButton rapidOption;
    private JRadioButton classicalOption;
    private JRadioButton customOption;
    private JComboBox<String> timeMinutesCombo;
    private JComboBox<String> incrementCombo;
    private JButton createButton;
    private JButton cancelButton;
    
    private CreateGameListener createGameListener;
    
    public CreateGamePanel() {
        setLayout(new BorderLayout());
        initializeUI();
    }
    
    private void initializeUI() {
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(40, 40, 40));
        headerPanel.setPreferredSize(new Dimension(500, 60));
        
        JLabel titleLabel = new JLabel("Create New Game", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 15, 5);
        
        JLabel infoLabel = new JLabel("<html>Select the time control to create a new chess game. " +
                "The selected time will apply to both players.</html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        contentPanel.add(infoLabel, gbc);
        
        // Time options panel
        JPanel timeOptionsPanel = new JPanel(new GridBagLayout());
        timeOptionsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), 
                "Time Control", 
                TitledBorder.LEFT, 
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)));
        
        ButtonGroup timeGroup = new ButtonGroup();
        
        // Blitz option
        GridBagConstraints optionGbc = new GridBagConstraints();
        optionGbc.anchor = GridBagConstraints.WEST;
        optionGbc.insets = new Insets(5, 5, 5, 5);
        optionGbc.gridx = 0;
        optionGbc.gridy = 0;
        optionGbc.gridwidth = 3;
        
        blitzOption = new JRadioButton("Blitz (5+0)", true);
        blitzOption.setFont(new Font("Arial", Font.PLAIN, 14));
        timeGroup.add(blitzOption);
        timeOptionsPanel.add(blitzOption, optionGbc);
        
        // Rapid option
        optionGbc.gridy = 1;
        rapidOption = new JRadioButton("Rapid (10+0)", false);
        rapidOption.setFont(new Font("Arial", Font.PLAIN, 14));
        timeGroup.add(rapidOption);
        timeOptionsPanel.add(rapidOption, optionGbc);
        
        // Classical option
        optionGbc.gridy = 2;
        classicalOption = new JRadioButton("Classical (30+0)", false);
        classicalOption.setFont(new Font("Arial", Font.PLAIN, 14));
        timeGroup.add(classicalOption);
        timeOptionsPanel.add(classicalOption, optionGbc);
        
        // Custom option
        optionGbc.gridy = 3;
        optionGbc.gridwidth = 1;
        customOption = new JRadioButton("Custom:", false);
        customOption.setFont(new Font("Arial", Font.PLAIN, 14));
        timeGroup.add(customOption);
        timeOptionsPanel.add(customOption, optionGbc);
        
        // Custom time selection
        optionGbc.gridx = 1;
        String[] minutes = {"1", "2", "3", "5", "10", "15", "20", "30", "45", "60", "90", "120"};
        timeMinutesCombo = new JComboBox<>(minutes);
        timeMinutesCombo.setSelectedItem("10");
        timeMinutesCombo.setEnabled(false);
        timeMinutesCombo.setPreferredSize(new Dimension(60, 25));
        timeOptionsPanel.add(timeMinutesCombo, optionGbc);
        
        optionGbc.gridx = 2;
        JLabel timeLabel = new JLabel(" minutes + ");
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        timeOptionsPanel.add(timeLabel, optionGbc);
        
        optionGbc.gridx = 3;
        String[] increments = {"0", "1", "2", "3", "5", "10", "15", "20", "30"};
        incrementCombo = new JComboBox<>(increments);
        incrementCombo.setEnabled(false);
        incrementCombo.setPreferredSize(new Dimension(60, 25));
        timeOptionsPanel.add(incrementCombo, optionGbc);
        
        optionGbc.gridx = 4;
        JLabel incrementLabel = new JLabel(" seconds increment");
        incrementLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        timeOptionsPanel.add(incrementLabel, optionGbc);
        
        // Enable combo boxes when custom option is selected
        customOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isCustom = customOption.isSelected();
                timeMinutesCombo.setEnabled(isCustom);
                incrementCombo.setEnabled(isCustom);
            }
        });
        
        gbc.insets = new Insets(5, 5, 20, 5);
        contentPanel.add(timeOptionsPanel, gbc);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
        
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(120, 35));
        cancelButton.addActionListener(e -> {
            if (createGameListener != null) {
                createGameListener.onCancel();
            }
        });
        buttonPanel.add(cancelButton);
        
        createButton = new JButton("Create Game");
        createButton.setFont(new Font("Arial", Font.BOLD, 14));
        createButton.setBackground(new Color(70, 130, 180));
        createButton.setForeground(Color.WHITE);
        createButton.setPreferredSize(new Dimension(120, 35));
        createButton.setFocusPainted(false);
        createButton.addActionListener(e -> {
            if (createGameListener != null) {
                createGameListener.onCreateGame(getSelectedTimeControl());
            }
        });
        buttonPanel.add(createButton);
        
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPanel.add(buttonPanel, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    public String getSelectedTimeControl() {
        if (blitzOption.isSelected()) {
            return "5+0";
        } else if (rapidOption.isSelected()) {
            return "10+0";
        } else if (classicalOption.isSelected()) {
            return "30+0";
        } else {
            return timeMinutesCombo.getSelectedItem() + "+" + incrementCombo.getSelectedItem();
        }
    }
    
    public void setCreateGameListener(CreateGameListener listener) {
        this.createGameListener = listener;
    }
    
    public interface CreateGameListener {
        void onCreateGame(String timeControl);
        void onCancel();
    }
} 