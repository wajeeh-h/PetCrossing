import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Timer;

/**
 * Animation class to handle the loading and displaying of animated sprites.
 * <br><br>
 * This class is responsible for loading frames from a specified directory,
 * scaling them to a specified size, and displaying them in a JLabel. It uses
 * a Timer to change frames periodically.
 * <br><br>
 * 
 * <b>Example Use:</b>
 * <pre>
 * {@code
 *    JLabel label = new JLabel();
 *    Animation animation = new Animation("path/to/frames/", 10, 100, label);
 *    animation.start(120);
 * }
 * </pre>
 */
public class Animation {
    Logger LOGGER = Logger.getLogger(Animation.class.getName());
    
    /** The path to the directory containing the frames */
    private String root;
    /** The number of frames in the animation */
    private int numFrames;
    /** The JLabel to display the animation */
    private JLabel imageLabel;
    /** The delay between frames in milliseconds */
    private int frameDelay;
    /** The size to scale the frames to */
    private int size = -1;
    /** The timer to control the frame updates */
    private Timer timer;

    /**
     * Constructor for the Animation class.
     * <br><br>
     * Creates an instance of the Animation class with the specified image path,
     * number of frames, frame delay, and JLabel to display the animation.
     * 
     * @param root The path to the directory containing the frames
     * @param numFrames The number of frames in the animation
     * @param frameDelay The delay between frames in milliseconds
     * @param imageLabel The JLabel to display the animation
     */
    public Animation(String root, int numFrames, int frameDelay, JLabel imageLabel) {
        this.root = root;
        this.numFrames = numFrames;
        this.frameDelay = frameDelay;
        this.imageLabel = imageLabel;
    }

    /**
     * Loads the frames from the specified directory.
     * <br><br>
     * Loads a number of frames from the specified directory, based on the number of frames
     * specified in the constructor. The frames are loaded into an ArrayList of Strings,
     * where each string is the path to some image (.png) file.
     * 
     * @return An ArrayList of Strings containing the paths to the frames
     */
    private ArrayList<String> loadFrames() {
        ArrayList<String> frames = new ArrayList<>();
        // Paths are given by "pet1.png", "pet2.png", etc.
        // The root path is the directory where the images are stored (e.g., "resources/pet")
        for (int i = 1; i <= numFrames; i++) {
            String framePath = root + i + ".png";
            frames.add(framePath);
        }
        return frames;
    }

    /**
     * Starts the animation with the default size of 120 pixels.
     */
    public void start() {
        startAnimation(imageLabel, loadFrames(), frameDelay, 120);
        this.size = 120;
    }

    /**
     * Starts the animation with a specified size. 
     * 
     * @param size The size of the animation in pixels
     */
    public void start(int size) {
        startAnimation(imageLabel, loadFrames(), frameDelay, size);
        this.size = size;
    }

    /**
     * Starts the animation.
     * <br><br>
     * Initializes a timer that periodically updates a JLabel with the next frame
     * in the animation, scaled to a certain size, every n seconnds.
     * 
     * @param imageLabel The JLabel to display the animation
     * @param frames The ArrayList of frame paths
     * @param interval The delay between frames in milliseconds (n)
     * @param size The size to scale the frames to
     */
    protected void startAnimation(JLabel imageLabel, ArrayList<String> frames, int interval, int size) {
        timer = new Timer(interval, new ActionListener() {
            int frameIndex = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Scale the image and display it
                    BufferedImage frameImage = ImageIO.read(new File(frames.get(frameIndex)));
                    Image scaledImage = frameImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaledImage));
                    // Get the next frame, go back to the first frame if at the end
                    frameIndex = (frameIndex + 1) % frames.size();
                } catch (IOException ex) {
                    imageLabel.setIcon(null); // Set icon to null if image loading fails
                    LOGGER.log(Level.WARNING, "Failed to load image: " + frames.get(frameIndex), ex);
                }
            }
        });
        // Start the timer
        timer.start();
    }

    /**
     * Restarts the timer
     */
    public void restart() {
        if (timer == null)
            return;
        if (!timer.isRunning())
            timer.start();
    }

    /**
     * Changes the animation to a static image, until restarted.
     * <br><br>
     * This method stops the timer and sets the JLabel to display a static image.
     * It is used when we want to change the sprite of the pet to reflect a 
     * certain state.
     * 
     * @param image The image to display.
     */
    public void updateAnimation(BufferedImage image) {
        timer.stop();
        Image scaledImage = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaledImage));
    }
}