import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Extends panel to create a screen for tutorials.
 * <br><br>
 * This panel creates a screen where the user can learn about the game.
 * It displays screenshots with explanatory text and buttons to move 
 * through tutorial steps. <br><br>
 * 
 * <b>Example Use:</b>
 * <pre>
 * {@code
 *   Screen frame = new Screen("");
 *   EventDispatcher dispatcher = new EventDispatcher();
 *   TutorialPanel tutorialPanel = new TutorialPanel(dispatcher);
 *   frame.setContentPane(tutorialPanel);
 * }
 * </pre>
 * 
 * @see Panel
 */
public class TutorialPanel extends Panel {
    /** Logger for tracking errors and debugging information. */
    private static final Logger LOGGER = Logger.getLogger(TutorialPanel.class.getName());   
    /** Text display for tutorial instructions. */
    private JLabel tutorialText;
    /** Button to return to previous tutorial step. */
    private Button backButton;
    /** Button to advance to next tutorial step. */
    private Button nextButton;
    /** Button to exit the tutorial. */
    private Button closeButton;
    /** Label to display tutorial screenshots. */
    private JLabel screenshotLabel;
    /** Array of images for each tutorial step. */
    private BufferedImage[] tutorialImages;
    /** Array of text descriptions for each tutorial step. */
    private String[] tutorialSteps;
    /** Current tutorial step being displayed. */
    private int currentStep;

    /**
     * Constructs a new TutorialPanel to guide users through game features.
     * 
     * @param eventDispatcher The event dispatcher for handling button events
     */
    public TutorialPanel(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
        if (eventDispatcher == null) {
            LOGGER.severe("Cannot initialize TutorialPanel with null dispatcher");
            throw new NullPointerException("EventDispatcher cannot be null");
        }
        init();
    }
    
    /**
     * Initializes all components of the tutorial panel.
     */
    private void init() {
        // Setup panel layout
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create screenshot display components
        screenshotLabel = new JLabel();
        screenshotLabel.setHorizontalAlignment(JLabel.CENTER);
        screenshotLabel.setVerticalAlignment(JLabel.CENTER);
        JPanel screenshotPanel = new JPanel(new BorderLayout());
        screenshotPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        screenshotPanel.setBackground(Color.WHITE);
        screenshotPanel.add(screenshotLabel, BorderLayout.CENTER);
        
        // Load resources
        setSprites();
        
        // Create tutorial content
        tutorialSteps = new String[] {
                "Welcome to Pet Crossing! This is your pet's home screen.",
                "Give your pet a name!",
                "Make a save, or override an existing one.",
                "Play with your pet to keep them happy! Catch as many apples as you can.",
                "Visit the vet with the 'VET' button. Bepo can heal your pet here.",
                "This is the main gameplay screen where all game functions are available.",
                "This is the parental control screen. Toggle it on and off.",
                "Full parental control is available here. You can set a timer for your game."
        };
        // Create UI components
        setupUIComponents(screenshotPanel);  
        // Add resize listener
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                showStep(currentStep);
            }
        });
        // Display first step
        showStep(0);
    }

    /**
     * Sets up UI components like labels and buttons.
     * 
     * @param screenshotPanel The panel containing the screenshot display
     */
    private void setupUIComponents(JPanel screenshotPanel) {
        // Setup text area
        tutorialText = new JLabel(tutorialSteps[0]);
        tutorialText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tutorialText.setHorizontalAlignment(JLabel.CENTER);
        tutorialText.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create button panel
        JPanel controlPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        // Create navigation buttons
        backButton = new Button("Prev", null, eventDispatcher);
        nextButton = new Button("Next", null, eventDispatcher);
        closeButton = new Button("Menu", GameEvent.MENU, eventDispatcher);

        // Set button dimensions
        Dimension buttonSize = new Dimension(100, 30);
        backButton.setPreferredSize(buttonSize);
        nextButton.setPreferredSize(buttonSize);
        closeButton.setPreferredSize(buttonSize);

        // Add button listeners
        nextButton.addActionListener(e -> showStep(currentStep + 1));
        backButton.addActionListener(e -> showStep(currentStep - 1));

        // Assemble components
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(closeButton);
        controlPanel.add(tutorialText, BorderLayout.CENTER);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add panels to main layout
        add(screenshotPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        
        LOGGER.fine("UI components setup complete");
    }

    /**
     * Displays the specified tutorial step.
     * 
     * @param step Index of the tutorial step to display
     */
    private void showStep(int step) {
        // Ensure step is within valid range
        step = Math.max(0, Math.min(step, tutorialSteps.length - 1));
        currentStep = step;
        
        // Update instruction text
        tutorialText.setText(tutorialSteps[currentStep]);

        // Update screenshot image
        if (tutorialImages != null && currentStep < tutorialImages.length) {
            BufferedImage img = tutorialImages[currentStep];
            
            // Calculate dimensions to maintain aspect ratio
            int panelWidth = getWidth() - 50;
            int panelHeight = getHeight() - 150;

            if (panelWidth <= 0 || panelHeight <= 0) {
                panelWidth = 800;
                panelHeight = 500;
            }

            double imgRatio = (double) img.getWidth() / img.getHeight();
            int displayWidth = panelWidth;
            int displayHeight = (int) (displayWidth / imgRatio);

            // If too tall, adjust based on height
            if (displayHeight > panelHeight) {
                displayHeight = panelHeight;
                displayWidth = (int) (displayHeight * imgRatio);
            }

            Image scaledImage = img.getScaledInstance(
                    displayWidth, displayHeight, Image.SCALE_SMOOTH);
            screenshotLabel.setIcon(new ImageIcon(scaledImage));
        }

        // Update navigation button states
        backButton.setEnabled(currentStep > 0);
        nextButton.setEnabled(currentStep < tutorialSteps.length - 1);
        
        LOGGER.fine("Showing tutorial step " + (currentStep + 1) + " of " + tutorialSteps.length);
    }

    @Override
    protected void setSprites() {
        tutorialImages = new BufferedImage[8];
        
        try {
            for (int i = 1; i <= 8; i++) {
                String imagePath = "resources/sprites/tut" + i + ".png";
                File imageFile = new File(imagePath);
                
                if (imageFile.exists()) {
                    tutorialImages[i - 1] = ImageIO.read(imageFile);
                    LOGGER.fine("Loaded tutorial image: " + imagePath);
                } else {
                    LOGGER.warning("Tutorial image not found: " + imagePath);
                    tutorialImages[i - 1] = createPlaceholderImage("Tutorial Image " + i + " (Not Found)");
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading tutorial images", e);
        }
    }
    
    /**
     * Creates a placeholder image with error text.
     * 
     * @param message The error message to display
     * @return A BufferedImage containing the error message
     */
    private BufferedImage createPlaceholderImage(String message) {
        BufferedImage placeholder = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics g = placeholder.getGraphics();
        
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 800, 600);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Segoe UI", Font.BOLD, 24));
            
            // Draw text centered on the image
            int textWidth = g.getFontMetrics().stringWidth(message);
            g.drawString(message, (800 - textWidth) / 2, 300);
        } finally {
            g.dispose();
        }
        
        return placeholder;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Does not use the main game background, only the default one
    }
}