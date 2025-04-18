import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JPanel;

/**
 * Extends panel to create a menu screen.
 * <br><br>
 * This panel creates a menu screen where the user can start a new game, load a game, or quit the game.
 * 
 * <b>Example Use:</b>
 * <pre>
 * {@code
 *   Screen frame = new Screen("");
 *   EventDispatcher dispatcher = new EventDispatcher();
 *   MenuPanel menuPanel = new MenuPanel(dispatcher);
 *   frame.setContentPane(menuPanel);
 * * }
 * </pre>
 * @see Panel
 * @see EventDispatcher
 * @see GameEvent
 * @see Button
 * @see Screen
 */
public class MenuPanel extends Panel {
    /** The title image */
    BufferedImage title;
    /** The credits */
    private final String credits = "GROUP 64: Wajeeh Haider, Tim Han, Ryan Ding, Suren Sadeghtehrani, Kian Kowsari | CS 2212 @ Western University | Spring 2025";

    /**
     * Constructs a new MenuPanel to handle menu interactions.
     * 
     * @param eventDispatcher
     */
    public MenuPanel(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
        init();
    }

    /**
     * Initializes the menu panel by setting up the layout, loading images, and adding buttons.
     */
    private void init() {
        // Create constraints (contols the layout of components, like a grid)
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(Box.createVerticalStrut(255), gbc);
        // Load the title image
        try {
            title = ImageIO.read(new File("resources/sprites/title.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Create the buttons
        Button newGameButton = new Button("New Game", GameEvent.NEW_GAME, eventDispatcher);
        Button loadGameButton = new Button("Load Game", GameEvent.LOAD_GAME, eventDispatcher);
        Button quitButton = new Button("Quit", GameEvent.QUIT, eventDispatcher);
        Button tutorialButton = new Button("Tutorial", GameEvent.TUTORIAL, eventDispatcher);
        Button parentalButton = new Button("Parental", GameEvent.PARENTAL, eventDispatcher);
        // Add the buttons to the screen
        this.add(newGameButton, gbc);
        this.add(loadGameButton, gbc);
        this.add(tutorialButton, gbc);
        this.add(parentalButton, gbc);
        this.add(quitButton, gbc);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the background and title image
        g.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g.drawImage(background, 0, 0, null);
        g.drawImage(title, calaculateCenterX(title), 60, null);
        // Draw the credits
        g.setFont(new Font("Segoe UI", Font.BOLD, 15));
        Tuple<Integer, Integer> creditsCoords = Panel.centerText(credits, g.getFont());
        g.drawString(credits,  creditsCoords.x, Screen.HEIGHT - 50);
    }
}
