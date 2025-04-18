import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * This abstract class represents a panel in the game. It extends JPanel and
 * serves as a
 * base class for different panels in the game, such as the main menu, tutorial,
 * etc.
 * This class handles the layout and appearance of the panel, including setting
 * backgroynd images and creating buttons.
 */
public abstract class Panel extends JPanel {

    /*
     * The eventDispatcher which this class uses to notify observers when an event
     * occurs (i.e. button clicked)
     */
    protected EventDispatcher eventDispatcher;
    /** The background image used by every panel */
    protected BufferedImage background;

    /**
     * Creates a new Panel with the specified event dispatcher.
     * 
     * @param eventDispatcher
     */
    Panel(EventDispatcher eventDispatcher) {
        super();
        this.eventDispatcher = eventDispatcher;
        this.setBounds(0, 0, WIDTH, HEIGHT);
        this.setVisible(true);
        setSprites();
    }

    /**
     * Helper method which calculated the x coordinate for which an image is centered.
     * 
     * @param image The image to be centered
     * @return The x coordinate for which the image is centered.
     */
    protected int calaculateCenterX(BufferedImage image) {
        int x = (this.getWidth() - image.getWidth()) / 2;
        return x;
    }

    /**
     * Sets certain sprites for the panel.
     */
    protected void setSprites() {
        try {
            background = ImageIO.read(new File("resources/sprites/background.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        /**
     * Returns the x and y coordinates for centering text on the screen.
     * 
     * @param text The text to center
     * @param font The font to use for measurement
     * @return A Tuple containing the x and y coordinates for centering
     */
    public static Tuple<Integer, Integer> centerText(String text, Font font) {
        FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
        int x = (Screen.WIDTH - metrics.stringWidth(text)) / 2;
        int y = (Screen.HEIGHT - metrics.getHeight()) / 2 + metrics.getAscent();
        return new Tuple<>(x, y);
    }
}
