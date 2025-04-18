import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;

/**
 * Button class that extends JButton to create a custom button with a specific
 * color and font. It also handles game events through the EventDispatcher class.
 * <br><br>
 * 
 * <b>Example Use:</b>
 * <pre>
 * {@code
 *    EventDispatcher eventDispatcher = new EventDispatcher();
 *    Button button = new Button("", GameEvent.MY_EVENT, eventDispatcher);
 * }
 * </pre>
 * 
 * @see EventDispatcher
 * @see GameEvent
 * @see JButton
 */
public class Button extends JButton {
    /** The event which clicking this button triggers */
    private GameEvent action;
    /** The event dispatcher which will handle the event */
    public EventDispatcher eventDispatcher;
    /** The color of the button */
    public static final Color BUTTON_COLOR = new Color(129, 156, 222);
    /** The font of the button */
    public static Font GAME_FONT = null;
    static {
        try {
            // Load the custom font from the resources folder
            GAME_FONT = Font.createFont(Font.TRUETYPE_FONT, new File("resources/ARCADECLASSIC.TTF"));
        } catch (FontFormatException | IOException e) {
            // If the font file is not found or cannot be loaded, fall back to arial
            // (available on all systems)
            GAME_FONT = new Font("Arial", Font.PLAIN, 20);
        }
    }

    /**
     * Constructor for the Button class.
     * <br><br>
     * Creates a button with the specified name and action. The button's color,
     * font, and action listener are set up as the default. The button will notify 
     * the event dispatcher than an action has occurred when clicked.
     * 
     * @param name The name of the button.
     * @param action The action to be performed when the button is clicked.
     * @param eventDispatcher The event dispatcher to handle the action.
     */
    public Button(String name, GameEvent action, EventDispatcher eventDispatcher) {
        super(name);
        this.setName(name);
        this.action = action;
        this.eventDispatcher = eventDispatcher;
        this.addActionListener(e -> onClick());
        this.setBackground(BUTTON_COLOR);
        this.setForeground(Color.WHITE);
        this.setFocusPainted(false);
        this.setFont(GAME_FONT.deriveFont(Font.TRUETYPE_FONT, 20));
    }

    /**
     * Constructor for the Button class, where the name of the component differs from its text.
     * <br><br>
     * Creates a button with the specified name and action. The button's color,
     * font, and action listener are set up as the default. The button will notify 
     * the event dispatcher than an action has occurred when clicked.
     * 
     * @param text The text to be displayed on the button.
     * @param action The action to be performed when the button is clicked.
     * @param eventDispatcher The event dispatcher to handle the action.
     * @param name The name of the button.
     */
    public Button(String text, GameEvent action, EventDispatcher eventDispatcher, String name) {
        super(text);
        this.setName(name);
        this.action = action;
        this.eventDispatcher = eventDispatcher;
        this.addActionListener(e -> onClick());
        this.setBackground(BUTTON_COLOR);
        this.setForeground(Color.WHITE);
        this.setFocusPainted(false);
        this.setFont(GAME_FONT.deriveFont(Font.TRUETYPE_FONT, 20));
    }

    /**
     * Method to handle the button click event. It notifies the event dispatcher
     * with the action associated with this button.
     */
    private void onClick() {
        eventDispatcher.notifyObservers(action);
    }
}
