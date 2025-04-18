import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * GameplayPanel class that extends Panel to create a screen for the game
 * <br><br>
 * This panel creates a screen where the user can interact with their pet, feed it,
 * play with it, etc. It displays the pet's stats, action buttons, and other options.
 * 
 * <b>Example Use:</b>
 * <pre>
 * {@code
 *   Screen frame = new Screen("");
 *   EventDispatcher dispatcher = new EventDispatcher();
 *   GameplayPanel gameplayPanel = new GameplayPanel(dispatcher);
 *   frame.setContentPane(gameplayPanel);
 * }
 * </pre>
 * 
 * @see Panel
 * @see EventDispatcher
 * @see GameEvent
 * @see Button
 * @see Screen
 */
public class GameplayPanel extends Panel implements MouseListener, KeyListener {

    private static final Logger LOGGER = Logger.getLogger(GameplayPanel.class.getName());
    /** The width of the the pet */
    private static final int PET_WIDTH = 150;
    /** The height of the pet */
    private static final int PET_HEIGHT = 150;
    /** The width of an action button */
    private static final int ACTION_BUTTON_WIDTH = 130;
    /** The height of an action button */
    private static final int ACTION_BUTTON_HEIGHT = 50;
    /** The height of a status bar */
    private static final int STATUS_BAR_HEIGHT = 150;
    /** The vertical spacing between components */
    private static final int VERTICAL_SPACING = 15;
    /** The background colour of status bars */
    private static final Color STATUS_BAR_BACKGROUND = new Color(132, 185, 239);
    /** The background colour of buttoms */
    private static final Color BUTTON_BACKGROUND = new Color(210, 180, 140);
    /** The background colour of progress bars */
    private static final Color PROGRESS_BAR_BACKGROUND = new Color(230, 230, 230);
    /** The colour of the health bar */
    private static final Color HEALTH_COLOR = new Color(76, 175, 80);
    /** The colour of the hunger bar */
    private static final Color HUNGER_COLOR = new Color(244, 67, 150);
    /** The colour of the happiness bar */
    private static final Color HAPPINESS_COLOR = new Color(255, 193, 7);
    /** The colour of the sleep bar */
    private static final Color SLEEP_COLOR = new Color(33, 150, 243);
    /** The colour to change a bar to when it reaches a dangerous level (below 25%) */
    private static final Color WARNING_COLOR = Color.RED;
    /** The colour of text */
    private static final Color TEXT_COLOR = Color.WHITE;
    /** The label which displays a pets sprite */
    private JLabel petLabel;
    /** The animation for the current pet, cycles through a sequence of images */
    private Animation petAnimation;
    /** A panel which contains all status bars */
    private JPanel statusBarPanel;
    /** A panel which contains all action buttons */
    private JPanel actionButtonsPanel;
    /** A map which contains references to all status bars */
    private final Map<String, JProgressBar> statusBars = new HashMap<>();
    /** A map which contains references to all buttons */
    private final Map<String, Button> buttons = new HashMap<>();
    /** A map which contains references to all images */
    private final Map<String, BufferedImage> images = new HashMap<>();
    /** A map which contains references to all scaled images */
    private final Map<String, Image> scaledImages = new HashMap<>();
    /** A map which contains the hover states for certain buttons (hovered or not) */
    private final Map<String, Boolean> hoverStates = new HashMap<>();
    /** A map which contains the dimensions for certain components */
    private final Map<String, Integer> dimensions = new HashMap<>();
    /** A map which contains images for the pet, for each alternative state (angry, dead, etc.) */
    private final Map<PetState, BufferedImage> petStateImages = new EnumMap<>(PetState.class);
    /** The number of apples in the inventory, to be displayed */
    private String appleCount = "0";
    /** The number of bananas in the inventory, to be displayed */
    private String bananaCount = "0";
    /** The number of purple gifts in the inventory, to be displayed */
    private String purpleGiftCount = "0";
    /** The number of green gifts in the inventory, to be displayed */
    private String greenGiftCount = "0";
    /** The score of the game, to be displayed */
    private String score = "0";
    /** The current pet's status (hungry, angry, etc.) */
    private String petStatus = "normal";
    /** Whether or not the user is allowed to visit the vet */
    public boolean allowVet = true;
    /** Whether or not the user is allowed to walk/exercise */
    public boolean allowWalk = true;
    /** Whether or not the user is allowed to play with their pet */
    public boolean allowPlay = true;

    /**
     * Creates a new GameplayPanel.
     * 
     * @param eventDispatcher The event dispatcher for handling game events
     */
    public GameplayPanel(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
        initializePanel();
        loadResources();
        this.addKeyListener(this);
    }

    /**
     * Initializes the panel's basic properties.
     */
    private void initializePanel() {
        setLayout(null);
        setDoubleBuffered(true);
        initDimensions();
        initHoverStates();
    }

    /**
     * Loads all required resources like images and scales them.
     */
    private void loadResources() {
        try {
            loadImages();
            scaleImages();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load game resources", e);
            // Show a visual error to the user
            JLabel errorLabel = new JLabel("Failed to load game resources: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            add(errorLabel);
        }
    }

    /**
     * Initializes the standard dimensions for UI elements.
     */
    private void initDimensions() {
        dimensions.put("groundHeight", 50);
        dimensions.put("signWidth", 100);
        dimensions.put("signHeight", 100);
        dimensions.put("bedWidth", 150);
        dimensions.put("bedHeight", 100);
        dimensions.put("backWidth", 100);
        dimensions.put("backHeight", 100);
        dimensions.put("cloudWidth", 250);
        dimensions.put("cloudHeight", 150);
        dimensions.put("chainsWidth", 300);
        dimensions.put("chainsHeight", 400);
        dimensions.put("foodWidth", 75);
        dimensions.put("foodHeight", 75);
        dimensions.put("giftWidth", 75);
        dimensions.put("giftHeight", 75);
    }

    /**
     * Initializes hover states for interactive elements.
     */
    private void initHoverStates() {
        String[] elements = { "sign", "bed", "back", "food", "food2", "gift", "gift2" };
        for (String element : elements) {
            hoverStates.put(element, false);
        }
    }

    /**
     * Loads all images required for the game.
     * 
     * @throws IOException If there's an error loading any image
     */
    private void loadImages() throws IOException {
        // Load background
        background = loadImageSafely("background", "resources/sprites/background.png");

        // Load environmental elements
        loadImageSafely("ground", "resources/sprites/ground.png");
        loadImageSafely("cloud", "resources/sprites/cloud.png");
        loadImageSafely("chains", "resources/sprites/chains.png");

        // Load interactive elements with hover states
        loadElementWithHoverState("sign", "resources/sprites/sign.png", "resources/sprites/signGlow.png");
        loadElementWithHoverState("bed", "resources/sprites/bed.png", "resources/sprites/bedGlow.png");
        loadElementWithHoverState("back", "resources/sprites/backSign.png", "resources/sprites/backSignGlow.png");

        // Load consumables with hover states
        loadElementWithHoverState("food", "resources/sprites/apple.png", "resources/sprites/appleGlow.png");
        loadElementWithHoverState("food2", "resources/sprites/banana.png", "resources/sprites/bananaGlow.png");
        loadElementWithHoverState("gift", "resources/sprites/gift.png", "resources/sprites/giftGlow.png");
        loadElementWithHoverState("gift2", "resources/sprites/gift2.png", "resources/sprites/gift2Glow.png");
    }

    /**
     * Loads an element and its hover state image.
     * 
     * @param name Base name of the element
     * @param normalPath Path to the normal image
     * @param hoverPath Path to the hover image
     * @throws IOException If there's an error loading the images
     */
    private void loadElementWithHoverState(String name, String normalPath, String hoverPath) throws IOException {
        loadImageSafely(name, normalPath);
        loadImageSafely(name + "Hover", hoverPath);
    }

    /**
     * Safely loads an image, with error logging.
     * 
     * @param key The key to store the image under
     * @param path The path to the image
     * @return The loaded BufferedImage
     * @throws IOException If the image cannot be loaded
     */
    private BufferedImage loadImageSafely(String key, String path) throws IOException {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            images.put(key, image);
            return image;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load image: " + path, e);
            throw e;
        }
    }

    /**
     * Scales all loaded images to appropriate dimensions.
     */
    private void scaleImages() {
        // For each key 
        for (Map.Entry<String, BufferedImage> entry : images.entrySet()) {
            String key = entry.getKey();
            // Skip the background as it's handled separately
            if (key.equals("background"))
                continue;
            // Determine dimensions based on image type
            int width, height;
            // Scale the dimensions for the value (image) associated with each key
            if (key.contains("sign") || key.contains("bed") || key.contains("back") ||
                    key.contains("cloud") || key.contains("chains")) {
                // Environmental elements
                width = dimensions.getOrDefault(key.replace("Hover", "") + "Width", 100);
                height = dimensions.getOrDefault(key.replace("Hover", "") + "Height", 100);
            } else {
                // Items (food, gifts)
                width = dimensions.get("foodWidth");
                height = dimensions.get("foodHeight");
            }
            scaleImage(key, width, height);
        }
    }

    /**
     * Scales a specific image to the given dimensions.
     * 
     * @param key The key of the image to scale
     * @param width The target width
     * @param height The target height
     */
    private void scaleImage(String key, int width, int height) {
        BufferedImage original = images.get(key);
        if (original != null) {
            Image scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            scaledImages.put(key, scaled);
        } else {
            LOGGER.warning("Attempted to scale missing image: " + key);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderEnvironment(g);
        renderInteractiveElements(g);
        renderInventory(g);
    }

    /**
     * Renders the base environment elements, liike the ground and background.
     * 
     * @param g The Graphics context
     */
    private void renderEnvironment(Graphics g) {
        // Draw background
        g.drawImage(background, 0, 0, null);

        // Draw ground
        Image ground = images.get("ground");
        if (ground != null) {
            g.drawImage(ground, 0, getHeight() - dimensions.get("groundHeight"),
                    getWidth(), dimensions.get("groundHeight"), null);
        }

        // Draw cloud and chains
        int cloudX = (getWidth() - dimensions.get("cloudWidth")) / 2 + 450;
        int cloudY = (getHeight() - dimensions.get("cloudHeight")) / 2 - 100;

        Image cloud = scaledImages.get("cloud");
        if (cloud != null) {
            g.drawImage(cloud, cloudX, cloudY, null);
        }

        Image chains = scaledImages.get("chains");
        if (chains != null) {
            int chainsX = cloudX - 20;
            int chainsY = cloudY + dimensions.get("cloudHeight") - 110;
            g.drawImage(chains, chainsX, chainsY, null);
        }
    }

    /**
     * Renders interactive elements like sign, bed, etc.
     * <br><br>
     * Interactive elements change on hover
     * 
     * @param g The Graphics context
     */
    private void renderInteractiveElements(Graphics g) {
        // Draw sign
        drawInteractiveElement(g, "sign", 150,
                getHeight() - dimensions.get("groundHeight") - dimensions.get("signHeight") + 20);
        // Draw bed
        drawInteractiveElement(g, "bed", getWidth() - dimensions.get("bedWidth") - 250,
                getHeight() - dimensions.get("groundHeight") - dimensions.get("bedHeight"));
        // Draw back button
        drawInteractiveElement(g, "back", 20,
                getHeight() - dimensions.get("groundHeight") - dimensions.get("backHeight") + 20);
    }

    /**
     * Renders inventory items.
     * 
     * @param g The Graphics context
     */
    private void renderInventory(Graphics g) {
        // Draw items
        drawInteractiveElement(g, "food", 150, 200);
        drawInteractiveElement(g, "food2", 20, 200);
        drawInteractiveElement(g, "gift", 20, 300);
        drawInteractiveElement(g, "gift2", 150, 300);
        // Draw inventory counts
        drawText(g);
    }

    /**
     * Draws an interactive element with hover effect if applicable.
     * 
     * @param g The Graphics context
     * @param name The name of the element
     * @param x The x-coordinate to draw at
     * @param y The y-coordinate to draw at
     */
    private void drawInteractiveElement(Graphics g, String name, int x, int y) {
        boolean isHovered = hoverStates.getOrDefault(name, false);
        // If an interactive image is hovered draw its hovered version, otherwise draw the normal variant
        Image image = isHovered ? scaledImages.get(name + "Hover") : scaledImages.get(name);
        if (image != null) {
            g.drawImage(image, x, y, null);
        } else {
            LOGGER.warning("Missing image for element: " + name);
        }
    }

    /**
     * Draws inventory counts for consumable items.
     * 
     * @param g The Graphics context
     */
    private void drawText(Graphics g) {
        g.setFont(new Font("Segoe UI", Font.BOLD, 30));
        g.setColor(TEXT_COLOR);
        // Draw counts beside the corresponding items
        g.drawString(appleCount, 225, 255); // Apple count
        g.drawString(bananaCount, 100, 255); // Banana count
        g.drawString(purpleGiftCount, 100, 350); // Purple gift count
        g.drawString(greenGiftCount, 225, 350); // Green gift count
        // Draw score and status in a magenta colour
        g.setColor(HUNGER_COLOR);
        Tuple<Integer, Integer> scoreCoords = Panel.centerText(score, g.getFont());
        g.drawString("Score: " + score, scoreCoords.x - 75, scoreCoords.y + 100);
        Tuple<Integer, Integer> statusCoords = Panel.centerText(petStatus, g.getFont());
        g.drawString("Status: " + petStatus, statusCoords.x - 75, statusCoords.y + 150);
    }

    /**
     * Updates the score displayed on the screen.
     * 
     * @param score The new score to display
     */
    public void updateScore(int score) {
        // Update the score variable and repaint
        this.score = String.valueOf(score);
        repaint();
    }

    /**
     * Updates the inventory display.
     * 
     * @param inventory The current inventory state
     */
    public void updateInventory(Inventory inventory) {
        appleCount = String.valueOf(inventory.getCount(Item.APPLE));
        bananaCount = String.valueOf(inventory.getCount(Item.BANANA));
        purpleGiftCount = String.valueOf(inventory.getCount(Item.PURPLEGIFT));
        greenGiftCount = String.valueOf(inventory.getCount(Item.GREENGIFT));
        repaint();
    }

    /**
     * Initializes the gameplay screen with a specific pet.
     * 
     * @param pet The pet to display and interact with
     */
    public void init(Pet pet) {
        try {
            setupPet(pet);
            setupStatusBar(pet);
            setupActionButtons();
            setupInteractiveElements();
            updateStatusBars(pet);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize gameplay", e);
        }
    }

    /**
     * Sets up the pet display and animations.
     * 
     * @param pet The pet to display
     * @throws IOException If pet images cannot be loaded
     */
    private void setupPet(Pet pet) throws IOException {
        // Create and position pet label
        petLabel = new JLabel();
        petLabel.setBounds(
                (getWidth() - PET_WIDTH) / 2,
                (getHeight() - PET_HEIGHT) / 2,
                PET_WIDTH,
                PET_HEIGHT);
        add(petLabel);

        // Initialize animation based on pet type
        String petType = pet.getType().toLowerCase();
        String animationPath;
        int frameCount;

        if ("chopper".equals(petType)) {
            animationPath = "resources/sprites/chopper";
            frameCount = 6;
        } else {
            animationPath = "resources/sprites/" + petType;
            frameCount = 3;
        }
        petAnimation = new Animation(animationPath, frameCount, 200, petLabel);
        petAnimation.start();
        // Load state-specific images for this pet
        loadPetStateImages(petType);
    }

    /**
     * Loads images for all pet states.
     * 
     * @param petType The type of pet
     * @throws IOException If images cannot be loaded
     */
    private void loadPetStateImages(String petType) throws IOException {
        String basePath = "resources/sprites/" + petType.toLowerCase();
        petStateImages.put(PetState.HUNGRY, loadImageSafely("hungryPet", basePath + "Hungry.png"));
        petStateImages.put(PetState.ANGRY, loadImageSafely("angryPet", basePath + "Angry.png"));
        petStateImages.put(PetState.SLEEPING, loadImageSafely("sleepingPet", basePath + "Sleep.png"));
        petStateImages.put(PetState.DEAD, loadImageSafely("deadPet", basePath + "Dead.png"));
    }

    /**
     * Sets up the status bar displaying pet stats.
     * 
     * @param pet The pet whose stats to display
     */
    private void setupStatusBar(Pet pet) {
        statusBarPanel = createStatusBarPanel(pet);
        setLayout(new BorderLayout());
        add(statusBarPanel, BorderLayout.NORTH);
    }

    /**
     * Sets up buttons for pet actions.
     */
    private void setupActionButtons() {
        actionButtonsPanel = createActionButtonsPanel();
        add(actionButtonsPanel, BorderLayout.EAST);
    }

    /**
     * Sets up the buttons for interactive elements in the environment.
     */
    private void setupInteractiveElements() {
        // Environmental interactive elements
        createInteractiveButton("sign", 150,
                getHeight() - dimensions.get("groundHeight") - dimensions.get("signHeight") + 20,
                dimensions.get("signWidth"), dimensions.get("signHeight"),
                GameEvent.MINIGAME);

        createInteractiveButton("bed", getWidth() - dimensions.get("bedWidth") - 250,
                getHeight() - dimensions.get("groundHeight") - dimensions.get("bedHeight"),
                dimensions.get("bedWidth"), dimensions.get("bedHeight"),
                GameEvent.SLEEP);

        createInteractiveButton("back", 20,
                getHeight() - dimensions.get("groundHeight") - dimensions.get("backHeight"),
                dimensions.get("backWidth"), dimensions.get("backHeight"),
                GameEvent.MENU);

        // Consumable interactive elements
        createInteractiveButton("food", 150, 200, dimensions.get("foodWidth"), dimensions.get("foodHeight"),
                GameEvent.FEED1);
        createInteractiveButton("food2", 20, 200, dimensions.get("foodWidth"), dimensions.get("foodHeight"),
                GameEvent.FEED2);
        createInteractiveButton("gift", 20, 300, dimensions.get("giftWidth"), dimensions.get("giftHeight"),
                GameEvent.GIFT1);
        createInteractiveButton("gift2", 150, 300, dimensions.get("giftWidth"), dimensions.get("giftHeight"),
                GameEvent.GIFT2);
    }

    /**
     * Creates an interactive button with invisible hitbox.
     * <br><br>
     * This invisible hitbox will handle click and hover events.
     * 
     * @param name The name of the button
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param width The width of the button
     * @param height The height of the button
     * @param event The event to trigger when clicked
     */
    private void createInteractiveButton(String name, int x, int y, int width, int height, GameEvent event) {
        Button button = new Button("", event, eventDispatcher, name);
        button.setBounds(x, y, width, height);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.addMouseListener(this);
        button.setName(name);

        buttons.put(name, button);
        add(button);
    }

    /**
     * Creates a status bar panel showing pet stats.
     * 
     * @param pet The pet whose stats to display
     * @return The created panel
     */
    private JPanel createStatusBarPanel(Pet pet) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(STATUS_BAR_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 0),
                new EmptyBorder(10, 15, 10, 15)));
        panel.setPreferredSize(new Dimension(getWidth(), STATUS_BAR_HEIGHT));

        // Create info panel structure
        JPanel leftPanel = createLeftPanel(pet);
        JPanel centerPanel = createCenterPanel(pet);
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        // Assemble the status bar
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Creates the left panel of the status bar showing pet name and type.
     * 
     * @param pet The pet whose info to display
     * @return The created panel
     */
    private JPanel createLeftPanel(Pet pet) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 0, 10));

        // Pet name
        JLabel nameLabel = new JLabel(pet.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(TEXT_COLOR);
        panel.add(nameLabel);

        panel.add(Box.createVerticalStrut(5));

        // Pet type
        JLabel typeLabel = new JLabel(pet.getType());
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        typeLabel.setForeground(TEXT_COLOR);
        panel.add(typeLabel);

        return panel;
    }

    /**
     * Creates the center panel of the status bar showing pet stats as progress
     * bars.
     * 
     * @param pet The pet whose stats to display
     * @return The created panel
     */
    private JPanel createCenterPanel(Pet pet) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 10, 0, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridy = 0;

        // Add status bars with appropriate colors
        statusBars.put("health", addLabeledProgressBar(panel, gbc, "Health", pet.getHealth(), HEALTH_COLOR));
        statusBars.put("hunger", addLabeledProgressBar(panel, gbc, "Hunger", pet.getHunger(), HUNGER_COLOR));
        statusBars.put("happiness",
                addLabeledProgressBar(panel, gbc, "Happiness", pet.getHappiness(), HAPPINESS_COLOR));
        statusBars.put("sleep", addLabeledProgressBar(panel, gbc, "Sleep", pet.getSleep(), SLEEP_COLOR));

        return panel;
    }

    /**
     * Creates a labeled progress bar for a pet stat.
     * 
     * @param panel The panel to add the bar to
     * @param gbc The layout constraints
     * @param labelText The text label for the bar
     * @param value The initial value of the bar
     * @param barColor The colour of the bar
     * @return The created progress bar
     */
    private JProgressBar addLabeledProgressBar(JPanel panel, GridBagConstraints gbc, String labelText, double value,
            Color barColor) {
        // Create label
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_COLOR);
        // Create progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) value);
        progressBar.setStringPainted(false);
        progressBar.setForeground(barColor);
        progressBar.setBackground(PROGRESS_BAR_BACKGROUND);
        progressBar.setPreferredSize(new Dimension(200, 50));
        // Add label
        gbc.gridx = 1;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(label, gbc);
        // Add progress bar
        gbc.gridx = 2;
        gbc.weightx = 3;
        gbc.weighty = 2.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(progressBar, gbc);
        // Increment row for next item
        gbc.gridy++;
        return progressBar;
    }

    /**
     * Creates the action buttons panel.
     * 
     * @return The created panel
     */
    private JPanel createActionButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(80, 0, 0, 20));
        panel.setPreferredSize(new Dimension(160, getHeight()));
        // Create action buttons
        createActionButton(panel, "vet", "vet", GameEvent.VET);
        createActionButton(panel, "walk", "walk", GameEvent.WALK);
        createActionButton(panel, "play", "play", GameEvent.PLAY);
        return panel;
    }

    /**
     * Creates an action button and adds it to the panel.
     * 
     * @param panel The panel to add the button to
     * @param name  The name/id of the button
     * @param label The visible text on the button
     * @param event The event to trigger when clicked
     */
    private void createActionButton(JPanel panel, String name, String label, GameEvent event) {
        Button button = new Button(label, event, eventDispatcher);
        button.setName(name);
        // Configure button appearance
        Dimension buttonSize = new Dimension(ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT);
        button.setPreferredSize(buttonSize);
        button.setMaximumSize(buttonSize);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setBackground(BUTTON_BACKGROUND);
        button.setOpaque(true);
        buttons.put(name, button);
        // Add spacing between buttons
        if (panel.getComponentCount() > 0) {
            panel.add(Box.createVerticalStrut(VERTICAL_SPACING));
        } else {
            panel.add(Box.createVerticalStrut(50));
        }
        panel.add(button);
    }

    /**
     * Updates all status bars and the current status displayed to reflect the pets
     * state.
     * 
     * @param pet The pet whose stats to display
     */
    public void updateStatusBars(Pet pet) {
        if (statusBars.isEmpty())
            return;
        // Update all progress bar values
        statusBars.get("health").setValue((int) pet.getHealth());
        statusBars.get("hunger").setValue((int) pet.getHunger());
        statusBars.get("happiness").setValue((int) pet.getHappiness());
        statusBars.get("sleep").setValue((int) pet.getSleep());
        // If a progress bar is below 25%, change its colour to a warning colour
        if (pet.getHealth() < 25) {
            statusBars.get("health").setForeground(WARNING_COLOR);
        }
        if (pet.getHunger() < 25) {
            statusBars.get("hunger").setForeground(WARNING_COLOR);
        }
        if (pet.getHappiness() < 25) {
            statusBars.get("happiness").setForeground(WARNING_COLOR);
        }
        if (pet.getSleep() < 25) {
            statusBars.get("sleep").setForeground(WARNING_COLOR);
        }
        // Update the pet status string then repaint
        petStatus = pet.getState().toString().toLowerCase();
        repaint();
    }

    /**
     * Updates the pet sprite based on its current state.
     * 
     * @param state The current state of the pet
     */
    public void updatePetSprite(PetState state) {
        if (petAnimation == null)
            return;

        LOGGER.info("Updating pet sprite for state: " + state);

        switch (state) {
            case NORMAL:
                petAnimation.restart();
                break;
            case ANGRY:
            case SLEEPING:
            case DEAD:
            case HUNGRY:
                BufferedImage stateImage = petStateImages.get(state);
                if (stateImage != null) {
                    petAnimation.updateAnimation(stateImage);
                } else {
                    LOGGER.warning("Missing state image for: " + state);
                }
                break;
            default:
                LOGGER.warning("Unknown pet state: " + state);
                break;
        }
        // Update button states based on pet state
        updateButtonsForPetState(state);
    }

    /**
     * Updates button availability based on pet state.
     * 
     * @param state The current state of the pet
     */
    private void updateButtonsForPetState(PetState state) {
        switch (state) {
            // For example, a dead pet cannot do anything, so disable all buttons
            case DEAD:
                setButtons(false, false, false, false,false,false, false);//
                break;
            case SLEEPING:
                setButtons(false, false, false, false,false,false, false);//
                break;
            case ANGRY:
                setButtons(true, true, false, false, allowPlay, false, false);
                break;
            case HUNGRY:
            default:
                setButtons(true, true, true, false, allowPlay, allowWalk, allowVet);//
                break;
        }
    }

    /**
     * Updates button cooldowns and availability.
     * 
     * @param pet The current pet
     */
    public void updateButtonCooldowns(Pet pet) {
        if (pet.getHealth() <= 0 || pet.getState() == PetState.DEAD) {
            setButtons(false, false, false, false, false, false, false);
        } else if (pet.getSleep() == 0 || pet.getState() == PetState.SLEEPING) {
            setButtons(false, false, false, false, false, false, false);
        } else if (pet.getState() == PetState.ANGRY || pet.getHappiness() == 0) {
            setButtons(true, true, false, false, true, false, false);
        } else {
            setButtons(true, true, true, true, allowPlay, allowWalk, allowVet);
        }
    }

    /**
     * Sets the enabled state of all buttons.
     * 
     * @param gift  Whether the gift button is enabled
     * @param gift2 Whether the gift2 button is enabled
     * @param food  Whether the food button is enabled
     * @param food2 Whether the food2 button is enabled
     * @param play  Whether the play button is enabled
     * @param walk  Whether the walk button is enabled
     * @param vet   Whether the vet button is enabled
     */
    private void setButtons(boolean gift, boolean gift2, boolean food, boolean food2, boolean play, boolean walk,
            boolean vet) {
        setButtonEnabled("gift", gift);
        setButtonEnabled("gift2", gift2);
        setButtonEnabled("food", food);
        setButtonEnabled("food2", food2);
        setButtonEnabled("play", play);
        setButtonEnabled("walk", walk);
        setButtonEnabled("vet", vet);
    }

    /**
     * Resets the game panel.
     */
    public void reset() {
        if (petAnimation != null) {
            petAnimation = null;
        }
    }

    @Override
    public void setSprites() {
        // No action needed - handled by loadImages() and scaleImages()
    }

    @Override
    public void doLayout() {
        super.doLayout();
        repositionElements();
    }

    /**
     * Repositions UI elements when the window is resized.
     */
    private void repositionElements() {
        // Reposition interactive elements
        repositionInteractiveButton("sign", 150,
                getHeight() - dimensions.get("groundHeight") - dimensions.get("signHeight") + 20);

        repositionInteractiveButton("bed", getWidth() - dimensions.get("bedWidth") - 250,
                getHeight() - dimensions.get("groundHeight") - dimensions.get("bedHeight"));

        repositionInteractiveButton("back", 20,
                getHeight() - dimensions.get("groundHeight") - dimensions.get("backHeight") + 20);

        // Reposition pet
        if (petLabel != null) {
            int petX = (getWidth() - PET_WIDTH) / 2;
            int petY = (getHeight() - PET_HEIGHT) / 2;
            petLabel.setBounds(petX, petY, PET_WIDTH, PET_HEIGHT);
        }
    }

    /**
     * Repositions an interactive button.
     * 
     * @param name The name of the button
     * @param x The new x-coordinate
     * @param y The new y-coordinate
     */
    private void repositionInteractiveButton(String name, int x, int y) {
        Button button = buttons.get(name);
        if (button != null) {
            int width = dimensions.get(name + "Width");
            int height = dimensions.get(name + "Height");
            button.setBounds(x, y, width, height);
        }
    }

    // MouseListener methods
    @Override
    public void mouseClicked(MouseEvent e) {
        // No action needed
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // No action needed
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // No action needed
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (e.getSource() instanceof JButton) {
            String name = ((JButton) e.getSource()).getName();
            if (name != null && hoverStates.containsKey(name)) {
                hoverStates.put(name, true);
                repaint();
            }
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (e.getSource() instanceof JButton) {
            String name = ((JButton) e.getSource()).getName();
            if (name != null && hoverStates.containsKey(name)) {
                hoverStates.put(name, false);
                repaint();
            }
        }
    }

    /**
     * Sets the enabled state of a specific button.
     * 
     * @param buttonName The name of the button
     * @param enabled Whether the button should be enabled
     */
    public void setButtonEnabled(String buttonName, boolean enabled) {
        Button button = buttons.get(buttonName);
        if (button != null) {
            button.setEnabled(enabled);
        }
    }

    /**
     * Updates the text displayed on a button.
     * 
     * @param buttonName The name of the button
     * @param text The new text to display
     */
    public void setButtonText(String buttonName, String text) {
        Button button = buttons.get(buttonName);
        if (button != null) {
            button.setText(text);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        // Keybindings for certain actions
        if (e.getKeyChar() == 'v' && buttons.get("vet").isEnabled() && allowVet) {
            eventDispatcher.notifyObservers(GameEvent.VET);
        } 
        if (e.getKeyChar() == 'w' && buttons.get("walk").isEnabled() && allowWalk) {
            eventDispatcher.notifyObservers(GameEvent.WALK);
        }
        if (e.getKeyChar() == 'p' && buttons.get("play").isEnabled() && allowPlay) {
            eventDispatcher.notifyObservers(GameEvent.PLAY);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}