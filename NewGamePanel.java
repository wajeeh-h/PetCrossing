import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Extends panel to create a screen for starting a new game.
 * <br><br>
 * This panel creates a screen where the user can select a pet to start a new game.
 * 
 * <b>Example Use:</b>
 * <pre>
 * {@code
 *  Screen frame = new Screen("");
 *  EventDispatcher dispatcher = new EventDispatcher();
 *  NewGamePanel newGamePanel = new NewGamePanel(dispatcher);
 *  frame.setContentPane(newGamePanel);
 * }
 * </pre>
 * 
 * @see Panel
 * @see EventDispatcher
 * @see GameEvent
 * @see Button
 * @see Screen
 * @see Animation
 */
public class NewGamePanel extends Panel {
    /** A JPanel which lists the users pets */
    private JPanel petListPanel;
    /** A JPanel which contains other elements */
    private JPanel containerPanel;
    /** The default dimension for some components */
    private final Dimension PET_PANEL_DIM = new Dimension(600, 150);

    /**
     * Constructs a new NewGamePanel to handle new game interactions.
     * 
     * @param eventDispatcher The event dispatcher for handling button events
     */
    public NewGamePanel(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
        init();
    }

    /**
     * Initializes the new game panel by setting up the layout, loading images, and adding buttons.
     */
    private void init() {
        setLayout(new BorderLayout());
        // Adds a panel which displays pet options
        petListPanel = new JPanel();
        petListPanel.setLayout(new BoxLayout(petListPanel, BoxLayout.Y_AXIS));
        petListPanel.setBackground(new Color(255, 255, 255, 200));
        petListPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        // Creates each pet option, fit with a name, stats, and description
        addPetOption("Chopper", "Chopper is very chill, but gets sad quickly.",
                "• HEALTH: 100<br>• SLEEP: 100<br>• HUNGER: 1000<br>• HAPPINESS: 100.");
        addPetOption("Dugong", "Dugong loves treats, but gets sleepy quickly.",
                "• HEALTH: 100<br>• SLEEP: 100<br>• HUNGER: 100<br>• HAPPINESS: 100.");
        addPetOption("Laboon", "Laboon loves hunger and the One Piece.",
                "• HEALTH: 100<br>• SLEEP: 100<br>• HUNGER: 100<br>• HAPPINESS: 100.");
        // Creats a panel which contains other components (like buttons)
        containerPanel = new JPanel(new GridBagLayout());
        containerPanel.setOpaque(false);
        containerPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 10, 50));
        containerPanel.add(petListPanel);
        Button backButton = new Button("Back", GameEvent.MENU, eventDispatcher);
        // Add the back button panel to the left side
        add(backButton, BorderLayout.WEST);
        // Add the pet list panel to the center of the container panel
        add(containerPanel, BorderLayout.CENTER);
    }

    /**
     * Sets the name of the pet.
     * <br><br>
     * Prompts the user to enter a name for their pet using a dialog box.
     * @return The name entered by the user, or an empty string if the input is invalid.
     */
    public String setName() {
        JOptionPane input = new JOptionPane("Enter your pet's name:", JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        // Force the input dialog to wait for input
        input.setWantsInput(true);
        input.setInitialValue("");
        JDialog dialog = input.createDialog("Pet Name");
        dialog.setVisible(true);
        Object selectedValue = input.getValue();
        // Check if the user clicked OK
        if (selectedValue != null && selectedValue.equals(JOptionPane.OK_OPTION)) {
            // Get the input value from the dialog
            Object inputValue = input.getInputValue();
            if (inputValue != null) {
                String petName = inputValue.toString().trim();
                if (!petName.isEmpty()) {
                    return petName;
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a valid name.", "Invalid Name", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        dialog.dispose();   
        return "";
    }

    /**
     * Helper method to add a pet option to the panel.
     * 
     * @param petName The name of the pet
     * @param petDescription The description of the pet
     * @param spritePath The path to the sprite for the pet
     * @param stats The base stats of the pet
     */
    private void addPetOption(String petName, String petDescription, String stats) {
        JPanel petPanel = new JPanel();
        // Creates the panel which holds each pet option
        petPanel.setLayout(new BorderLayout());
        petPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        petPanel.setBackground(Color.WHITE);
        petPanel.setPreferredSize(PET_PANEL_DIM);

        Button selectButton = null;
        JLabel imageLabel = new JLabel();
        Animation animation = null;
        // Uses the pet's type/name to determine which animation to use
        if (petName.equals("Chopper")) {
            animation = new Animation("resources/sprites/chopper", 6, 150, imageLabel);
            selectButton = new Button("Select", GameEvent.CHOPPER, eventDispatcher);
        } else if (petName.equals("Dugong")) {
            animation = new Animation("resources/sprites/dugong", 3, 250, imageLabel);
            selectButton = new Button("Select", GameEvent.DUGONG, eventDispatcher);
        } else if (petName.equals("Laboon")) {
            animation = new Animation("resources/sprites/laboon", 3, 250, imageLabel);
            selectButton = new Button("Select", GameEvent.LABOON, eventDispatcher);
        }
        // Start the animation
        if (animation != null)
            animation.start();

        imageLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 35)); // Add padding
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        // Creats labels and descriptions for each pet option
        JLabel nameLabel = new JLabel(petName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel descriptionLabel = new JLabel(petDescription);
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        JLabel statsHeaderLabel = new JLabel("Base Stats");
        statsHeaderLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statsHeaderLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0)); // Add spacing around the header
        JLabel statsLabel = new JLabel("<html>" + stats + "</html>");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 10, 10, 20)); // Default padding
        // Adds each component to a text panel
        textPanel.add(nameLabel);
        textPanel.add(descriptionLabel);
        textPanel.add(statsHeaderLabel);
        textPanel.add(statsLabel);
        if (selectButton != null) {
            buttonPanel.add(selectButton);
        }
        // Adds each larger components to the pet panel
        petPanel.add(imageLabel, BorderLayout.WEST);
        petPanel.add(buttonPanel, BorderLayout.EAST);
        petPanel.add(textPanel, BorderLayout.CENTER);
        // There are three pet panels, each of which are added to the pet list panel
        petListPanel.add(petPanel);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }
    }
}

