import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.TimeUnit;
import java.time.LocalTime;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * ParentalController class for managing playtime restrictions and parental controls.
 * <br><br>
 * This class is responsible for enforcing time limits and restricted hours for gameplay. It tracks the user's session time,
 * updates JSON configuration files, and integrates with the game's event system to notify observers when restrictions apply.
 * <br><br>
 * <b>Example Use:</b>
 * <pre>
 * {@code
 *   ParentalController pc = new ParentalController(eventDispatcher);
 *   pc.setTimeLimit(60); // Limit to 60 minutes
 *   if (pc.isRestricted()) {
 *       // Handle restriction logic
 *   }
 * }
 * </pre>
 * 
 * @see EventDispatcher
 * @see GameEvent
 * @see ParentalPanel
 */
public class ParentalController extends Controller {

    /** The system time when the session started */
    private long startTime;

    /** The total play time accumulated in the current session, in minutes */
    private long totalPlayTime;

    /** The time limit (in minutes) set for the session */
    private int timeLimit;

    /** Thread used to periodically check for restriction violations */
    private Thread timerThread;

    /** Flag indicating whether the timer is currently running */
    private AtomicBoolean isRunning;

    /** Flag to enable or disable restrictions */
    private boolean restrictionsEnabled;

    /** Start time for restricted hours (e.g. 21:00) */
    private LocalTime restrictedStartTime;

    /** End time for restricted hours (e.g. 07:00) */
    private LocalTime restrictedEndTime;

    /** The file path to the parental controls JSON configuration file */
    private static final String PARENTAL_CONTROLS_FILE = "saves/parental_controls.json";

    /**
     * Constructs a ParentalController and starts the internal timer.
     *
     * @param eventDispatcher The event dispatcher used to notify observers of restriction events.
     */
    public ParentalController(EventDispatcher eventDispatcher) {
        super(eventDispatcher, null);
        this.timeLimit = -1;
        this.isRunning = new AtomicBoolean(true);
        this.startTime = System.currentTimeMillis();
        this.restrictionsEnabled = false;

        loadRestrictedTimes();
        incrementLogins();
        startTimer();
    }

    /**
     * Loads restricted time intervals from the parental controls JSON file.
     */
    private void loadRestrictedTimes() {
        try {
            File file = new File(PARENTAL_CONTROLS_FILE);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(file);

            JsonNode restrictedTimesNode = rootNode.path("restrictedTimes").get(0);
            if (!restrictedTimesNode.isMissingNode()) {
                String startTime = restrictedTimesNode.path("start").asText();
                String endTime = restrictedTimesNode.path("end").asText();

                if (!startTime.equals("00:00") || !endTime.equals("00:00")) {
                    this.restrictedStartTime = LocalTime.parse(startTime);
                    this.restrictedEndTime = LocalTime.parse(endTime);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the restricted hours and updates the parental controls file.
     *
     * @param startTime The beginning of the restricted period.
     * @param endTime   The end of the restricted period.
     */
    public void setRestrictedHours(LocalTime startTime, LocalTime endTime) {
        this.restrictedStartTime = startTime;
        this.restrictedEndTime = endTime;

        try {
            File file = new File(PARENTAL_CONTROLS_FILE);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(file);

            ((ObjectNode) rootNode).putArray("restrictedTimes")
                    .addObject()
                    .put("start", startTime.toString())
                    .put("end", endTime.toString());

            objectMapper.writeValue(file, rootNode);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks whether the current time falls within restricted hours.
     *
     * @return true if access is restricted; false otherwise.
     */
    public boolean isRestricted() {
        if (restrictedStartTime == null || restrictedEndTime == null) {
            return false;
        }

        LocalTime now = LocalTime.now();

        if (restrictedStartTime.isBefore(restrictedEndTime)) {
            return now.isAfter(restrictedStartTime) && now.isBefore(restrictedEndTime);
        } else {
            return now.isAfter(restrictedStartTime) || now.isBefore(restrictedEndTime);
        }
    }

    /**
     * Starts a background timer thread that enforces parental restrictions.
     */
    private void startTimer() {
        if (timerThread != null && timerThread.isAlive()) {
            return;
        }

        timerThread = new Thread(() -> {
            while (isRunning.get()) {
                enforceRestrictions();
                try {
                    Thread.sleep(60000); // Check every minute
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }

    /**
     * Enforces time restrictions or limits. Prompts user if gameplay is restricted.
     */
    public void enforceRestrictions() {
        if (isRestricted()) {
            SwingUtilities.invokeLater(() -> {
                int result = JOptionPane.showConfirmDialog(
                        null,
                        "Gameplay is restricted during this time.",
                        "Access Restricted",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    eventDispatcher.notifyObservers(GameEvent.QUIT);
                }
            });
        } else {
            updatePlayTime();
            if (restrictionsEnabled && timeLimit > 0 && totalPlayTime >= timeLimit) {
                SwingUtilities.invokeLater(() -> {
                    int result = JOptionPane.showConfirmDialog(
                            null,
                            "Time limit reached! The game will now close.",
                            "Time Limit Reached",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (result == JOptionPane.OK_OPTION) {
                        eventDispatcher.notifyObservers(GameEvent.QUIT);
                        isRunning.set(false);
                    }
                });
            }
        }
    }

    /**
     * Updates total play time and the timer label in the UI.
     */
    private void updatePlayTime() {
        long currentTime = System.currentTimeMillis();
        long elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(currentTime - startTime);

        if (elapsedMinutes > totalPlayTime) {
            totalPlayTime++;
            SwingUtilities.invokeLater(() -> {
                if (this.getPanel() instanceof ParentalPanel) {
                    ((ParentalPanel) this.getPanel()).updateTimerLabel(totalPlayTime);
                }
            });
        }
    }

    /**
     * Returns the total play time in minutes.
     *
     * @return Total play time.
     */
    public long getTotalPlayTime() {
        return totalPlayTime;
    }

    /**
     * Sets the maximum time allowed for play in minutes.
     *
     * @param minutes Maximum play time.
     */
    public void setTimeLimit(int minutes) {
        this.timeLimit = minutes;
    }

    /**
     * Enables or disables restriction enforcement.
     *
     * @param enabled true to enable restrictions; false to disable.
     */
    public void setRestrictionsEnabled(boolean enabled) {
        this.restrictionsEnabled = enabled;
    }

    /**
     * Returns whether restrictions are currently enabled.
     *
     * @return true if enabled; false otherwise.
     */
    public boolean areRestrictionsEnabled() {
        return restrictionsEnabled;
    }

    /**
     * Handles events received from the event dispatcher.
     *
     * @param event The game event.
     */
    @Override
    protected void handleEvent(GameEvent event) {
        switch (event) {
            default:
                break;
        }
    }

    /**
     * Registers this controller to observe relevant game events.
     */
    @Override
    protected void registerEvents() {
        eventDispatcher.addObserver(GameEvent.QUIT, this);
        eventDispatcher.addObserver(GameEvent.PARENTAL, this);
        eventDispatcher.addObserver(GameEvent.MENU, this);
    }

    /**
     * Attaches a listener to a button that sets restricted hours based on user input.
     *
     * @param setRestrictedHoursButton The button to attach to.
     * @param startTimeField           Field containing the start time.
     * @param endTimeField             Field containing the end time.
     */
    public void addSetRestrictedHoursButtonListener(javax.swing.JButton setRestrictedHoursButton,
            javax.swing.JTextField startTimeField, javax.swing.JTextField endTimeField) {
        setRestrictedHoursButton.addActionListener(e -> {
            try {
                if (this == null) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Parental Controller is not initialized.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                LocalTime startTime = LocalTime.parse(startTimeField.getText());
                LocalTime endTime = LocalTime.parse(endTimeField.getText());

                this.setRestrictedHours(startTime, endTime);

                JOptionPane.showMessageDialog(
                        null,
                        "Restricted hours set: " + startTime + " to " + endTime,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Invalid time format. Please use HH:mm (24-hour clock).",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Increments the login counter in the parental controls JSON file.
     */
    private void incrementLogins() {
        try {
            File file = new File(PARENTAL_CONTROLS_FILE);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(file);

            int numLogins = rootNode.path("numLogins").asInt();
            ((ObjectNode) rootNode).put("numLogins", numLogins + 1);

            objectMapper.writeValue(file, rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the total playtime in the parental controls file based on the current session.
     *
     * @param sessionPlayTime The current session's playtime in minutes.
     */
    public void updateTotalPlayTime(long sessionPlayTime) {
        try {
            File file = new File(PARENTAL_CONTROLS_FILE);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(file);

            long totalPlayTimeFromFile = rootNode.path("totalPlayTime").asLong();
            long updatedTotalPlayTime = totalPlayTimeFromFile + sessionPlayTime;
            ((ObjectNode) rootNode).put("totalPlayTime", updatedTotalPlayTime);

            objectMapper.writeValue(file, rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculates the average playtime per session using data from the parental controls file.
     *
     * @return The average playtime, or 0 if no logins are recorded.
     */
    public long getAveragePlayTime() {
        try {
            File file = new File(PARENTAL_CONTROLS_FILE);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(file);

            long totalPlayTime = rootNode.path("totalPlayTime").asLong();
            int numLogins = rootNode.path("numLogins").asInt();

            if (numLogins > 0) {
                return totalPlayTime / numLogins;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}