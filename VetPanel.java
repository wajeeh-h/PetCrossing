import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JLabel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extends panel to create a screen for the vet.
 * <br><br>
 * This panel creates a screen where the user can heal their pet. It displays
 * the doctor, the pet, some objects. The panel uses the animation class to
 * display sprites. <br><br>
 
 * <b>Example Use:</b>
 * <pre>
 * {@code
 *   Screen frame = new Screen("");
 *   EventDispatcher dispatcher = new EventDispatcher();
 *   VetPanel vetPanel = new VetPanel(dispatcher, userPet);
 *   frame.setContentPane(vetPanel);
 * }
 * </pre>
 * 
 * @see Panel
 * @see Pet
 * @see Animation
 */
public class VetPanel extends Panel {
    /** Logger for tracking errors and debugging information. */
    private static final Logger LOGGER = Logger.getLogger(VetPanel.class.getName());
    /** Label to display the doctor. */
    private JLabel bepoLabel;
    /** Animation controller for the doctor. */
    private Animation bepoAnimation;
    /** Animation controller for  pet. */
    private Animation petAnimation;
    /** Label to display the  pet. */
    private JLabel petLabel;

    /**
     * Constructs a new VetPanel to handle pet healing interactions.
     * <br><br>
     * Initializes the vet environment with the specified pet and
     * sets up all UI components, animations, and events needed.
     * 
     * @param eventDispatcher The event dispatcher for handling button events
     * @param pet The pet to be displayed and potentially healed
     * @throws NullPointerException if eventDispatcher or pet is null
     */
    public VetPanel(EventDispatcher eventDispatcher, Pet pet) {
        super(eventDispatcher);
        if (pet == null) {
            LOGGER.severe("Cannot initialize VetPanel with null pet");
            eventDispatcher.notifyObservers(GameEvent.FATALERROR);
            throw new NullPointerException("Pet cannot be null");
        }
        init(pet);
    }

    /**
     * Initializes all components of the veterinary panel.
     * <br><br>
     * Sets up the layout, loads the background image, initializes animations
     * for Bepo and the pet, and adds UI controls for healing and navigation.
     * 
     * @param pet The pet to be displayed in the veterinary clinic
     */
    private void init(Pet pet) {
        setLayout(null);
        setSprites();
        setupAnimations(pet);
        setupUIComponents();
    }

    @Override
    protected void setSprites() {
        try {
            background = ImageIO.read(new File("resources/sprites/vetBackground.png"));
            LOGGER.fine("Successfully loaded vet background image");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load vet background image", e);
        }
    }

    /**
     * Sets up the animation for the veterinarian character (Bepo).
     * <br><br>
     * Creates a label for Bepo and the pet, initializes their animations,
     * and positions them on the screen.
     *  
     * @param pet The pet to be displayed
     */
    private void setupAnimations(Pet pet) {
        // Create a JLabel to display Bepo's animation
        bepoLabel = new JLabel();
        int bepoWidth = 400; // Width of Bepo
        int bepoHeight = 600; // Height of Bepo
        int bepoX = getWidth() + 300; // Position on the right side of screen
        int bepoY = getHeight() + 120; // Position from the top of screen
        bepoLabel.setBounds(bepoX, bepoY, bepoWidth, bepoHeight);
        add(bepoLabel);
        // Initialize the Animation object for Bepo
        bepoAnimation = new Animation("resources/sprites/bepo", 4, 200, bepoLabel);
        bepoAnimation.start(300);
        LOGGER.fine("Bepo animation setup complete");

        // Create a JLabel to display the pet's animation
        petLabel = new JLabel();
        int petWidth = 150;
        int petHeight = 150;
        int petX = (getWidth() - petWidth) / 2 + 800;
        int petY = (getHeight() - petHeight) / 2 + 500;
        petLabel.setBounds(petX, petY, petWidth, petHeight);
        add(petLabel);
        // Choose animation based on pet type
        String petType = pet.getType().toLowerCase();
        // Chopper has a different number of frames versus dugong and laboon
        if ("chopper".equals(petType)) {
            petAnimation = new Animation("resources/sprites/chopper", 6, 200, petLabel);
        } else {
            petAnimation = new Animation("resources/sprites/" + petType, 3, 200, petLabel);
        }
        petAnimation.start();
        LOGGER.fine("Pet animation setup complete for pet type: " + petType);
    }

    /**
     * Sets up UI components like labels and buttons.
     * <br><br>
     * Adds a welcome label, a back button for returning to the game,
     * and a heal button for treating the pet.
     */
    private void setupUIComponents() {
        // Welcome label
        JLabel vetLabel = new JLabel("Welcome to the Vet!");
        vetLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        vetLabel.setForeground(Color.WHITE);
        vetLabel.setBounds(400, 50, 300, 150);
        add(vetLabel);

        // Back button
        Button backButton = new Button("Back", GameEvent.INGAME, eventDispatcher);
        backButton.setBounds(1000, 10, 100, 30);
        backButton.setFocusPainted(false);
        add(backButton);

        // Heal button
        Button healButton = new Button("Heal", GameEvent.HEAL, eventDispatcher);
        healButton.setBounds(500, 200, 100, 30);
        healButton.setFocusPainted(false);
        add(healButton);

        LOGGER.fine("UI components setup complete");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the background image
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        } else {
            LOGGER.warning("Background image is null during painting");
        }
    }
}