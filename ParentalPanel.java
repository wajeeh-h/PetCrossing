import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalTime;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Extends panel to create a screen for parental controls.
 * <br><br>
 * This panel creates a screen where the user can set parental controls for their pet.
 * It allows the user to set a time limit for playtime and restrict playtime to certain hours.
 * It allow shows average play time per session, current session play time, and allows
 * parents to revive a dead pet.
 * 
 * <b>Example Use:</b>
 * <pre>
 * {@code
 *  Screen frame = new Screen("");
 *  EventDispatcher dispatcher = new EventDispatcher();
 *  ParentalPanel parentalPanel = new ParentalPanel(dispatcher, parentalController);
 *  frame.setContentPane(parentalPanel);
 * }
 * </pre>
 * @see Panel
 * @see EventDispatcher
 * @see GameEvent
 * @see Button
 * @see Screen
 */
public class ParentalPanel extends Panel {
    Logger LOGGER = Logger.getLogger(ParentalPanel.class.getName());
    
    /** Button to toggle the parental controls */
    private Button parentalToggleButton;
    /** The parental controls password (default is 1234) */
    private static final String PIN = "1234"; 
    /** The label which contains avergae play time */
    private JLabel averagePlaytimeLabel;
    /** The panel which contains the playtime controls */
    private JPanel playtimePanel;
    /** The label which contains playtime info */
    private JLabel playtimeLabel;
    /** The field where the parent enters options */
    private JTextField playtimeField;
    /** The button to set the playtime options */
    private Button setPlaytimeButton;
    /** Label which contains current playtime */
    private JLabel timerLabel;
    /** The timer which updates playtime every minute or so*/
    private Timer updateTimer;
    /** The field where the parent enters the starting playtime restriction */
    private JTextField startTimeField;
    /** The field where the parent enters the ending playtime restriction */
    private JTextField endTimeField;
    /** Whether or not playtime restrictions are enabled */
    private boolean restrictionsEnabled = false;
    /** Current session playtime */
    private long playTime = 0; 
    /** The controller associated with this panel */
    private ParentalController parentalController;

    /**
     * Constructs a new ParentalPanel to handle parental control interactions.
     * <br><br>
     * Initializes the parental control panel with the specified event dispatcher and controller.
     * 
     * @param eventDispatcher The event dispatcher for handling button events
     * @param parentalController The parental controller for managing parental controls
     */
    public ParentalPanel(EventDispatcher eventDispatcher, ParentalController parentalController) {
        super(eventDispatcher);
        if (parentalController == null) {
            // Log an error and notify observers of a fatal error
            LOGGER.severe("Cannot initialize ParentalPanel with null parentalController");
            eventDispatcher.notifyObservers(GameEvent.FATALERROR);
        }
        this.parentalController = parentalController;
        // Initialize timer before components, with a 1-second delay
        updateTimer = new Timer(1000, e -> {
            if (timerLabel != null) {
                timerLabel.setText("Session Time: " + playTime + " minutes");
            }
            LOGGER.info("Session Time: " + playTime + " minutes");
        });
        initializeComponents();
        // Start the timer if restrictions are enabled
        if (restrictionsEnabled) {
            updateTimer.start();
            timerLabel.setVisible(true);
        }
    }

    /**
     * Initializes the components of the parental control panel.
     */
    private void initializeComponents() {
        // Set the panel layout and background color
        setLayout(new BorderLayout());
        setBackground(new Color(135, 206, 250)); // Sky blue background
        // Center panel to hold the controls box
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        // Add vertical space to move content down
        centerPanel.add(Box.createVerticalStrut(60));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        // Create the box container
        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
        boxPanel.setBackground(new Color(255, 255, 255, 200)); // Semi-transparent white
        boxPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(20, 30, 20, 30))); // Top padding is 20px
        // Set static size for the boxPanel
        Dimension staticSize = new Dimension(600, 400); // Width: 600px, Height: 400px
        boxPanel.setPreferredSize(staticSize);
        boxPanel.setMinimumSize(staticSize);
        boxPanel.setMaximumSize(staticSize);
        // Create a panel for the toggle label and button with BorderLayout
        JPanel togglePanel = new JPanel(new BorderLayout(10, 0)); // 10px horizontal gap
        togglePanel.setOpaque(false);
        // Left side - label
        JLabel toggleLabel = new JLabel("Show Parental Controls");
        toggleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        toggleLabel.setForeground(new Color(50, 50, 50));
        togglePanel.add(toggleLabel, BorderLayout.WEST);
        // Right side - button in its own panel to maintain size
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.add(Box.createVerticalGlue()); // Add vertical spacing to center the button
        parentalToggleButton = new Button("Off", GameEvent.PARENTAL, eventDispatcher);
        // Set initial text based on controller state and force update
        parentalToggleButton.setText("Off");
        parentalToggleButton.invalidate();
        parentalToggleButton.repaint();
        buttonPanel.add(parentalToggleButton);
        buttonPanel.add(Box.createVerticalGlue()); // Add vertical spacing to center the button
        togglePanel.add(buttonPanel, BorderLayout.EAST);
        // Add ActionListener to handle the toggle functionality
        parentalToggleButton.addActionListener(e -> {
            boolean isSelected = !parentalToggleButton.getText().equals("On");
            if (isSelected) {
                String inputPin = JOptionPane.showInputDialog(
                        ParentalPanel.this,
                        "Enter PIN to enable parental controls:",
                        "PIN Required",
                        JOptionPane.PLAIN_MESSAGE);
                if (inputPin != null && inputPin.equals(PIN)) {
                    restrictionsEnabled = true;
                    parentalToggleButton.setText("On");
                    parentalToggleButton.repaint(); // Update display after setting text
                    playtimePanel.setVisible(true);
                    timerLabel.setVisible(true); // Make the timer label visible
                    averagePlaytimeLabel.setVisible(true); // Make the average playtime label visible
                    updateTimer.start();
                } else {
                    JOptionPane.showMessageDialog(
                            ParentalPanel.this,
                            "Incorrect PIN. Parental controls remain disabled.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    parentalToggleButton.setText("Off");
                    parentalToggleButton.invalidate();
                    parentalToggleButton.repaint();
                }
            } else {
                restrictionsEnabled = false;
                parentalToggleButton.setText("Off");
                parentalToggleButton.invalidate();
                parentalToggleButton.getParent().validate();
                parentalToggleButton.repaint();
                playtimePanel.setVisible(false);
                timerLabel.setVisible(false); // Hide the timer label
                averagePlaytimeLabel.setVisible(false); // Hide the average playtime label
                updateTimer.stop();
            }
        });

        // Add the toggle panel to the box panel first (at the top)
        boxPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add some initial padding from the top border
        boxPanel.add(togglePanel);
        boxPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Add spacing after the toggle 
        // Timer label
        timerLabel = new JLabel("Session Time: 0 minutes", SwingConstants.CENTER); // Center-align text
        timerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        timerLabel.setForeground(new Color(0, 0, 128));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center-align within the boxPanel
        timerLabel.setVisible(false); // Initially hidden
        boxPanel.add(timerLabel);
        boxPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Add spacing below
        // Add the "Average playtime" label below the "Session Time" label
        long averagePlaytime = parentalController.getAveragePlayTime();
        averagePlaytimeLabel = new JLabel("Average playtime: " + averagePlaytime + " minutes", SwingConstants.CENTER);
        averagePlaytimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        averagePlaytimeLabel.setForeground(new Color(0, 0, 128)); // Same color as the "Session Time" label
        averagePlaytimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center-align within the boxPanel
        averagePlaytimeLabel.setVisible(false); // Initially hidden
        boxPanel.add(averagePlaytimeLabel);
        boxPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Add spacing below
        // Initialize playtimeLabel, playtimeField, and setPlaytimeButton
        playtimeLabel = new JLabel("Set Playtime (minutes):");
        playtimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        playtimeField = new JTextField(5); // Text field with a width of 5 columns
        playtimeField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        setPlaytimeButton = new Button("Set", null, eventDispatcher); // Use custom Button class
        // Add components to playtimePanel
        playtimePanel = new JPanel(new GridBagLayout()); // Use GridBagLayout for precise control
        playtimePanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Add padding between components
        // Add playtimeLabel to the left
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST; // Align to the left
        gbc.weightx = 0; // No extra horizontal space for the label
        playtimePanel.add(playtimeLabel, gbc);
        // Add playtimeField next to the label
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST; // Align to the left
        gbc.weightx = 1; // Allow the text field to take extra horizontal space
        gbc.fill = GridBagConstraints.HORIZONTAL; // Make the text field stretch horizontally
        playtimePanel.add(playtimeField, gbc);
        // Add setPlaytimeButton to the far right
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST; // Align to the right
        gbc.weightx = 0; // No extra horizontal space for the button
        gbc.fill = GridBagConstraints.NONE; // Prevent the button from stretching
        playtimePanel.add(setPlaytimeButton, gbc);
        // Add fields for setting restricted hours
        JLabel startTimeLabel = new JLabel("Restricted Start Time (HH:mm):");
        startTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        startTimeField = new JTextField(5); // Text field for start time
        startTimeField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        JLabel endTimeLabel = new JLabel("Restricted End Time (HH:mm):");
        endTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        endTimeField = new JTextField(5); // Text field for end time
        endTimeField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        Button setRestrictedHoursButton = new Button("Restrict", null, eventDispatcher);
        setRestrictedHoursButton.addActionListener(e -> {
            try {
                if (parentalController == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Parental Controller is not initialized.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                LocalTime startTime = LocalTime.parse(startTimeField.getText());
                LocalTime endTime = LocalTime.parse(endTimeField.getText());
                parentalController.setRestrictedHours(startTime, endTime);

                JOptionPane.showMessageDialog(
                        this,
                        "Restricted hours set: " + startTime + " to " + endTime,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid time format. Please use HH:mm.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        Button reviveButton = new Button("Revive", GameEvent.REVIVE, eventDispatcher);
        // Add components to the playtimePanel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST; // Align to the left
        playtimePanel.add(startTimeLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST; // Align to the left
        playtimePanel.add(startTimeField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST; // Align to the left
        playtimePanel.add(endTimeLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST; // Align to the left
        playtimePanel.add(endTimeField, gbc);
        // Add the "Restrict" button to the far right
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridheight = 2; // Span two rows to align with both fields
        gbc.anchor = GridBagConstraints.EAST; // Align to the right
        gbc.fill = GridBagConstraints.NONE; // Prevent stretching
        playtimePanel.add(setRestrictedHoursButton, gbc);
        gbc.gridheight = 1; // Reset grid height for the revive button
        gbc.gridx = 2;
        gbc.gridy = 3; // Place it below the other buttons
        gbc.anchor = GridBagConstraints.EAST; // Align to the right
        gbc.fill = GridBagConstraints.NONE; // Prevent stretching
        gbc.weighty = 1; // Add some vertical space below the button
        playtimePanel.add(reviveButton, gbc);
        // Ensure playtimePanel visibility is tied to restrictionsEnabled
        playtimePanel.setVisible(restrictionsEnabled);
        boxPanel.add(playtimePanel);
        // Wrap boxPanel in a centering panel
        JPanel centeringPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centeringPanel.setOpaque(false);
        centeringPanel.add(boxPanel);
        // Add the centeringPanel to the center panel
        centerPanel.add(centeringPanel);
        add(centerPanel, BorderLayout.CENTER);
        // Add ActionListener to the "Set" button to set the time limit
        setPlaytimeButton.addActionListener(e -> {
            try {
                if (parentalController == null) {
                    System.err.println("Error: parentalController is null!");
                    JOptionPane.showMessageDialog(
                            this,
                            "Parental Controller is not initialized.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int timeLimit = Integer.parseInt(playtimeField.getText());
                if (timeLimit > 0) {
                    parentalController.setTimeLimit(timeLimit);
                    JOptionPane.showMessageDialog(
                            this,
                            "Time limit set to " + timeLimit + " minutes.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Please enter a valid positive number.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid input. Please enter a number.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        // Add back button
        Button backButton = new Button("Back", GameEvent.MENU, eventDispatcher);
        // Add the back button to the left side
        add(backButton, BorderLayout.WEST);
    }

    /**
     * Updates the timer label with the current playtime.
     * 
     * @param playTime The current playtime in minutes
     */
    public void updateTimerLabel(long playTime) {
        this.playTime = playTime;
        if (timerLabel != null) {
            timerLabel.setText("Session Time: " + playTime + " minutes");
        }
    }

    public void setParentalController(ParentalController parentalController) {
        this.parentalController = parentalController;
    }

    /**
     * Sets the visibility of the panel and starts/stops the timer accordingly.
     * 
     * @param visible True to make the panel visible, false to hide it
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (updateTimer != null && timerLabel != null) {
            if (visible && restrictionsEnabled) {
                updateTimer.start();
                timerLabel.setVisible(true);
            } else {
                updateTimer.stop();
                timerLabel.setVisible(false);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the background image
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }
    }
}

