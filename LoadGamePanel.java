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
 * Extends panel to create a screen for loading the game.
 * <br><br>
 * This panel creates a screen where the user can load a game.
 * 
 * <b>Example Use:</b>
 * <pre>
 * {@code
 *   Screen frame = new Screen("");
 *   EventDispatcher dispatcher = new EventDispatcher();
 *   LoadGamePanel loadGamePanel = new LoadGamePanel(dispatcher);
 *   frame.setContentPane(loadGamePanel);
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
public class LoadGamePanel extends Panel {
    /** A JPanel which lists the users pets */
    private JPanel petListPanel;
    /** A JPanel which contains other elements */
    private JPanel containerPanel;
    /** Default text for some buttons */
    private String buttonText = "Select";
    /** Whether or not the user is in this panel to select a pet to revive, it is set to false by default */
    private boolean revive = false;
    /** The dimensions of an interior panel */
    private final Dimension PET_PANEL_DIM = new Dimension(600, 150);

    /**
     * Constructs a new LoadGamePanel to handle game loading.
     * 
     * @param eventDispatcher The event dispatcher for handling button events
     */
    public LoadGamePanel(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
        init();
    }

    /**
     * Constructs a new LoadGamePanel to handle game loading.
     * 
     * @param eventDispatcher The event dispatcher for handling button events
     * @param revive Whether or not the user is in this panel to select a pet to revive
     */
    public LoadGamePanel(EventDispatcher eventDispatcher, boolean revive) {
        super(eventDispatcher);
        this.revive = revive;
        init();
    }

    /**
     * Sets the text of the select buttons to the specified text.
     * 
     * @param text
     */
    public void overrideButtonText(String text) {
        this.buttonText = text;
        init();
    }

    /**
     * Displays a selection dialogue to the user.
     */
    public void displaySelectionDialogue() {
        JOptionPane j = new JOptionPane("Select a save to override!", JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = j.createDialog("Select Save Slot");
        dialog.setVisible(true);
    }

    /**
     * Initializes the LoadGamePanel.
     */
    public void init() {
        setLayout(new BorderLayout());
        petListPanel = new JPanel();
        petListPanel.setLayout(new BoxLayout(petListPanel, BoxLayout.Y_AXIS));
        petListPanel.setBackground(new Color(255, 255, 255, 200));
        petListPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        // Loads the saves to display to the user
        SaveManager saveManager = new SaveManager();
        Tuple<Pet, Tuple<Inventory, Integer>> save1 = saveManager.loadGame(1);
        Tuple<Pet, Tuple<Inventory, Integer>> save2 = saveManager.loadGame(2);
        Tuple<Pet, Tuple<Inventory, Integer>> save3 = saveManager.loadGame(3);

        Pet pet1 = save1.x;
        Inventory inventory1 = save1.y.x;
        Pet pet2 = save2.x;
        Inventory inventory2 = save2.y.x;
        Pet pet3 = save3.x;
        Inventory inventory3 = save3.y.x;

        // Adds each save as an option on to the petListPanel
        addPetOption(pet1, inventory1, 1);
        addPetOption(pet2, inventory2, 2);
        addPetOption(pet3, inventory3, 3);
        containerPanel = new JPanel(new GridBagLayout());
        containerPanel.setOpaque(false);
        containerPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        // Add the petListPanel to the containerPanel
        containerPanel.add(petListPanel);
        // Add the containerPanel to the main panel
        add(containerPanel, BorderLayout.CENTER);
        // Create a back button panel on the left side
        JPanel backButtonPanel = new JPanel();
        backButtonPanel.setLayout(new BorderLayout());
        backButtonPanel.setOpaque(false);
        backButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        Button backButton = new Button("Back", GameEvent.MENU, eventDispatcher);
        // Add the back button panel to the left side
        add(backButton, BorderLayout.WEST);
    }

    /**
     * Helper method which adds a pet option to the petListPanel.
     * <br><br>
     * There are three entries on the petListPanel, each represents one
     * of the three save slots which the user may select.
     * 
     * @param pet The pet of the save slot to be displayed
     * @param inventory The inventory of the save slot to be displayed 
     * @param saveSlot The save slot number to be displayed
     */
    private void addPetOption(Pet pet, Inventory inventory, int saveSlot) {
        // Each save slot has a different event to load the game
        GameEvent loadEvent = null;
        if (saveSlot == 1) {
            loadEvent = GameEvent.LOAD1;
        } else if (saveSlot == 2) {
            loadEvent = GameEvent.LOAD2;
        } else if (saveSlot == 3) {
            loadEvent = GameEvent.LOAD3;
        }
        // If the pet is null create a new save option
        if (pet == null) {
            JPanel emptySave = new JPanel();
            emptySave.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            emptySave.setBackground(Color.WHITE);
            emptySave.setPreferredSize(PET_PANEL_DIM);
            Button emptySaveButton = new Button("New", loadEvent, eventDispatcher);
            emptySaveButton.setPreferredSize(new Dimension(100, 20));
            emptySave.setLayout(new GridBagLayout()); // Center the button
            emptySave.add(emptySaveButton);
            petListPanel.add(emptySave);
            // If the user is reviving a pet, disable the button (the user cannot create a
            // new save when reviving a pet)
            if (revive)
                emptySaveButton.setEnabled(false);
            return;
        }
        JPanel petPanel = new JPanel();
        petPanel.setLayout(new BorderLayout());
        petPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        petPanel.setBackground(Color.WHITE);
        petPanel.setPreferredSize(PET_PANEL_DIM);

        Button selectButton = null;
        JLabel imageLabel = new JLabel();
        Animation animation = null;
        // Create an animation for the pet based on its type
        if (pet.getType().toLowerCase().equals("chopper")) {
            animation = new Animation("resources/sprites/chopper", 6, 150, imageLabel);
        } else if (pet.getType().toLowerCase().equals("dugong")) {
            animation = new Animation("resources/sprites/dugong", 3, 250, imageLabel);
        } else if (pet.getType().toLowerCase().equals("laboon")) {
            animation = new Animation("resources/sprites/laboon", 3, 250, imageLabel);
        }
        // Create the select button for the pet
        selectButton = new Button(buttonText, loadEvent, eventDispatcher);

        if (animation != null)
            animation.start();
        imageLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 35)); // Add padding

        // Create some panels which will holds stats or information about each pet/save
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel nameLabel = new JLabel(pet.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel statsHeaderLabel = new JLabel("Stats");
        statsHeaderLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statsHeaderLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0)); // Add spacing around the header
        String stats = "Health: " + pet.getHealth() + "<br>" +
                "Hunger: " + pet.getHunger() + "<br>" +
                "Happiness: " + pet.getHappiness() + "<br>" +
                "Sleep: " + pet.getSleep() + "<br>" +
                "State: " + pet.getState().toString().toLowerCase() + "<br>";
        JLabel statsLabel = new JLabel("<html>" + stats + "</html>");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 10, 10, 20)); // Default padding
        textPanel.add(nameLabel);
        textPanel.add(statsHeaderLabel);
        textPanel.add(statsLabel);
        if (selectButton != null)
            buttonPanel.add(selectButton);

        // If the user is reviving a pet, and the pet is not dead, then disable the button
        // (obviously, a pet that is alive cannot be revived)
        if (revive && !pet.getState().equals(PetState.DEAD))
            selectButton.setEnabled(false);

        petPanel.add(imageLabel, BorderLayout.WEST);
        petPanel.add(buttonPanel, BorderLayout.EAST);
        petPanel.add(textPanel, BorderLayout.CENTER);

        petListPanel.add(petPanel);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the background image
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null); // Scale to fit the panel
        }
    }
}
