
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * CatchGamePanel class implements a simple apple-catching minigame.
 * <br><br>
 * This panel represents a playable minigame where the player controls a basket to catch falling apples.
 * The game features animations, sound effects, and changing visuals based on score.
 * The game runs on a fixed timer, and the player's objective is to catch as many apples as possible before time runs out.
 * <br><br>
 * <b>Example Use:</b>
 * <pre>{@code
 *   EventDispatcher dispatcher = new EventDispatcher();
 *   CatchGamePanel panel = new CatchGamePanel(dispatcher);
 *   someFrame.add(panel);
 * }</pre>
 *
 * @see Apple
 * @see Animation
 * @see Panel
 * @see EventDispatcher
 * @see GameEvent
 */
public class CatchGamePanel extends Panel implements ActionListener, KeyListener {

    /** Timer for game loop (approx. 60 FPS) */
    private Timer timer;

    /** Timer for spawning apples periodically */
    private Timer appleSpawnTimer;

    /** Timer for tracking the total duration of the game */
    private Timer gameTimer;

    /** List of falling apples */
    private ArrayList<Apple> apples;

    /** Random number generator for apple spawn positions */
    private Random rand = new Random();

    // --- Player properties ---

    /** X-coordinate of the player (basket) */
    private int playerX;

    /** Y-coordinate of the player (basket), set dynamically */
    private int playerY;

    /** Width of the basket image */
    private final int playerWidth = 100;

    /** Height of the basket image */
    private final int playerHeight = 100;

    /** Movement speed of the basket */
    private final int playerSpeed = 20;

    /** Current score of the player */
    private int score = 0;

    /** Total game duration in seconds */
    private final int GAME_DURATION = 60;

    /** Time elapsed since game start */
    private int timeElapsed = 0;

    /** Flag indicating whether the game is over */
    private boolean isGameOver = false;

    /** Button for returning to the main game after minigame ends */
    private JButton returnButton;

    /** Flag to determine if it's the first frame (for centering basket) */
    private boolean isFirstFrame = true;

    // --- Images ---

    /** Basket image (default) */
    private BufferedImage basketImage;

    /** Basket image for score > 5 */
    private BufferedImage basketImage2;

    /** Basket image for score > 10 */
    private BufferedImage basketImage3;

    /** Basket image for score > 15 */
    private BufferedImage basketImage15;

    /** Basket image for score > 20 */
    private BufferedImage basketImage20;

    /** Basket image for score > 25 */
    private BufferedImage basketImage25;

    /** Apple image */
    private BufferedImage appleImage;

    /** Ground background image */
    private BufferedImage groundImage;

    /** Label to display Bepo animation on game over */
    private JLabel bepoLabel;

    /**
     * Constructs a CatchGamePanel with the given event dispatcher.
     * Initializes the game state, assets, timers, listeners, and UI components.
     *
     * @param eventDispatcher the event dispatcher for communicating with other parts of the game
     */
    public CatchGamePanel(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
        setFocusable(true);
        addKeyListener(this);
        setLayout(null);
        setBackground(Color.BLACK);

        apples = new ArrayList<>();

        try {
            basketImage = ImageIO.read(new File("resources/sprites/basket.png"));
            basketImage2 = ImageIO.read(new File("resources/sprites/basket5.png"));
            basketImage3 = ImageIO.read(new File("resources/sprites/basket10.png"));
            basketImage15 = ImageIO.read(new File("resources/sprites/basket15.png"));
            basketImage20 = ImageIO.read(new File("resources/sprites/basket20.png"));
            basketImage25 = ImageIO.read(new File("resources/sprites/basket25.png"));
            appleImage = ImageIO.read(new File("resources/sprites/apple.png"));
            groundImage = ImageIO.read(new File("resources/sprites/ground.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        bepoLabel = new JLabel();
        bepoLabel.setBounds((getWidth() / 2) + 10, (getHeight() / 2) - 50, 120, 120);
        add(bepoLabel);

        timer = new Timer(16, this);
        timer.start();

        appleSpawnTimer = new Timer(1000, e -> {
            int x = rand.nextInt(Math.max(1, getWidth() - 20));
            apples.add(new Apple(x, appleImage));
        });
        appleSpawnTimer.start();

        gameTimer = new Timer(1000, e -> {
            timeElapsed++;
            if (timeElapsed >= GAME_DURATION) {
                endGame();
            }
        });
        gameTimer.start();
    }

    /**
     * Ends the game, stopping timers, triggering animations and sound, and displaying return button.
     */
    private void endGame() {
        isGameOver = true;
        timer.stop();
        appleSpawnTimer.stop();
        gameTimer.stop();

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int gameOverTextWidth = 200;
        int gameOverTextHeight = 50;
        int bepoX = (panelWidth / 2) + gameOverTextWidth / 2 + 40;
        int bepoY = (panelHeight / 2) - gameOverTextHeight / 2;
        bepoLabel.setBounds(bepoX, bepoY, 150, 170);

        Animation a = new Animation("resources/sprites/bepo", 4, 150, bepoLabel);
        a.start();

        repaint();

        try {
            File soundFile = new File("resources/sounds/gameOver.wav");
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
            Clip gameOverClip = AudioSystem.getClip();
            gameOverClip.open(audioInput);
            gameOverClip.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        returnButton = new Button("Return to Game", GameEvent.INGAME, eventDispatcher);
        returnButton.setFocusPainted(false);
        returnButton.setBounds((getWidth() - 200) / 2, getHeight() / 2 + 80, 200, 40);
        add(returnButton);
        returnButton.setVisible(true);
        SwingUtilities.invokeLater(this::requestFocusInWindow);
        eventDispatcher.notifyObservers(GameEvent.STOPSOUND);
        eventDispatcher.notifyObservers(GameEvent.LEAVEMINIGAME);
    }

    /**
     * Plays the apple pickup sound when an apple is caught.
     */
    private void playApplePickupSound() {
        try {
            File soundFile = new File("resources/sounds/applePickup.wav");
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Renders all game elements including background, player, apples, and UI overlays.
     *
     * @param g the Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        playerY = panelHeight - playerHeight - 30;

        if (groundImage != null) {
            int groundHeight = 50;
            g.drawImage(groundImage, 0, panelHeight - groundHeight, panelWidth, groundHeight, null);
        }

        if (isFirstFrame) {
            playerX = (panelWidth - playerWidth) / 2;
            isFirstFrame = false;
        }

        if (basketImage != null) {
            g.drawImage(basketImage, playerX, playerY, playerWidth, playerHeight, null);
        } else {
            g.setColor(Color.GREEN);
            g.fillRect(playerX, playerY, playerWidth, playerHeight);
        }

        for (Apple apple : apples) {
            apple.draw(g);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 20, 30);

        int timeLeft = GAME_DURATION - timeElapsed;
        String timeText = "Time: " + timeLeft + "s";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(timeText);
        g.drawString(timeText, panelWidth - textWidth - 20, 30);

        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Game Over!", panelWidth / 2 - 100, panelHeight / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("Final Score: " + score, panelWidth / 2 - 80, panelHeight / 2 + 40);
        }
    }

    /**
     * Main game loop method called on each timer tick.
     *
     * @param e the ActionEvent from the timer
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (isGameOver) return;

        Iterator<Apple> it = apples.iterator();
        while (it.hasNext()) {
            Apple apple = it.next();
            apple.update();

            if (apple.getY() + apple.getSize() >= playerY &&
                apple.getY() + apple.getSize() <= playerY + playerHeight &&
                apple.getX() + apple.getSize() >= playerX &&
                apple.getX() <= playerX + playerWidth) {

                score++;
                it.remove();
                playApplePickupSound();
                continue;
            }

            if (apple.getY() > getHeight()) {
                it.remove();
            }
        }

        if (score > 25) {
            basketImage = basketImage25;
        } else if (score > 20) {
            basketImage = basketImage20;
        } else if (score > 15) {
            basketImage = basketImage15;
        } else if (score > 10) {
            basketImage = basketImage3;
        } else if (score > 5) {
            basketImage = basketImage2;
        }

        repaint();
    }

    /**
     * Handles keyboard input for moving the basket left or right.
     *
     * @param e the KeyEvent
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT && playerX > 0) {
            playerX -= playerSpeed;
        } else if (key == KeyEvent.VK_RIGHT && playerX + playerWidth < getWidth()) {
            playerX += playerSpeed;
        }
        repaint();
    }

    /** Unused but required override for KeyListener */
    @Override
    public void keyReleased(KeyEvent e) {}

    /** Unused but required override for KeyListener */
    @Override
    public void keyTyped(KeyEvent e) {}
}