import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Screen class that represents the main window of the application.
 * <br><br>
 * This class is responsible for creating the main window, managing the
 * content pane, and handling the repainting of the screen. It extends
 * JFrame which is a container in Swing that creates a window to which
 * we can render content.
 * 
 * <b>Example Use:</b>
 * <pre>
 * {@code
 *   Screen screen = new Screen("My Application");
 *   JPanel panel = new JPanel();
 *   screen.setPanel(panel);
 *   screen.refreshPanel();
 * }
 * </pre>
 */
public class Screen extends JFrame {
    /** The width constant */
    public static final int WIDTH = 1120;
    /** The height constant */
    public static final int HEIGHT = 630;
    /** The current panel being displayed by this Screen */
    private JPanel panel;
    /** A thread which refreshes the contents of the panel, allowing for dynamic content */
    private Thread repaintThread;

    /**
     * Constructor for the Screen class.
     * <br><br>
     * Initializes the JFrame with the specified title, sets it to be non-resizable,
     * (see requirements documentation) and starts a thread to refresh the panel at a 
     * rate of 60 frames per second.
     * 
     * @param title The title of the JFrame window.
     */
    Screen(String title) {
        super();
        this.setResizable(false);
        this.setTitle(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(WIDTH, HEIGHT);
        this.setVisible(true);

        // Create the refresh thread
        repaintThread = new Thread(() -> {
            while (true) {
                try {
                    // Update the contents of the panel 60 times per second (60 FPS)
                    Thread.sleep(1000 / 60);
                    if (panel != null)
                        panel.repaint();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        repaintThread.start();
    }

    /**
     * Refresh the current panel being displayed by this Screen.
     */
    public void refreshPanel() {
        this.setContentPane(panel);
        this.revalidate();
        this.repaint();
        panel.repaint();
        SwingUtilities.invokeLater(() -> panel.requestFocusInWindow());
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
    }
}
