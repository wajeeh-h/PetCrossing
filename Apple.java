import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * This class represents an apple object in the game that falls from the top of
 * the screen.
 */
public class Apple {
    /** The default size for an apple */
    private static final int DEFAULT_SIZE = 30;
    /** The default fall speed for an apple */
    private static final int DEFAULT_SPEED = 5;
    /** The default color for an apple if no sprite is provided */
    private static final Color FALLBACK_COLOR = Color.RED;
    /** The x coordinate of the apple */
    private int x;
    /** The y coordinate of the apple */
    private int y;
    /** The sprite of the apple */
    private final BufferedImage sprite; 
    /** The bounding box of the apple */
    private Rectangle bounds;

    /**
     * Creates a new apple at the specified horizontal position, at the top of the screen.
     *
     * @param x The horizontal position of the apple
     * @param sprite The image to use for rendering the apple, can be null
     */
    public Apple(int x, BufferedImage sprite) {
        this.x = x;
        this.y = 0;
        this.sprite = sprite;
        bounds = new Rectangle(x, y, DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /**
     * Updates the apple's position by moving it downward based on its speed.
     */
    public void update() {
        y += DEFAULT_SPEED;
        bounds = new Rectangle(x, y, DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /**
     * Renders the apple to the screen.
     *
     * @param g Graphics context for rendering
     */
    public void draw(Graphics g) {
        // Use sprite if available, otherwise draw a red oval
        if (sprite != null) {
            g.drawImage(sprite, x, y, DEFAULT_SIZE, DEFAULT_SIZE, null);
        } else {
            // If the sprite is null, draw the apple as a red circle
            g.setColor(FALLBACK_COLOR);
            g.fillOval(x, y, DEFAULT_SIZE, DEFAULT_SIZE);
            g.dispose();
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return DEFAULT_SIZE;
    }

    public int getSpeed() {
        return DEFAULT_SPEED;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}