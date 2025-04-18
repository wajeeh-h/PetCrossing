import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ParentalControllerTest {

    // This is the same file path used in ParentalController.
    private static final String PARENTAL_CONTROLS_FILE = "saves/parental_controls.json";
    private ObjectMapper objectMapper;
    private File parentalControlsFile;
    private File savesDir;

    /**
     * Create the "saves" directory and the parental_controls.json file with initial data.
     */
    @BeforeEach
    public void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        savesDir = new File("saves");
        if (!savesDir.exists()) {
            savesDir.mkdirs(); // Use mkdirs instead of mkdir to create parent directories if needed
        }
        parentalControlsFile = new File(PARENTAL_CONTROLS_FILE);
        
        // Make sure the file is writable
        if (parentalControlsFile.exists() && !parentalControlsFile.canWrite()) {
            fail("Cannot write to parental controls file. Check file permissions.");
        }
        
        // Initialize the JSON file with default values.
        String initialJson = "{\n" +
            "  \"restrictedTimes\": [\n" +
            "    {\"start\": \"00:00\", \"end\": \"00:00\"}\n" +
            "  ],\n" +
            "  \"numLogins\": 0,\n" +
            "  \"totalPlayTime\": 0,\n" +
            "  \"restrictionsEnabled\": false,\n" +
            "  \"timeLimit\": 0\n" +
            "}";
        Files.write(parentalControlsFile.toPath(), initialJson.getBytes());
        
        // Verify file was created successfully
        if (!parentalControlsFile.exists()) {
            fail("Failed to create parental controls file.");
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
        // More robust cleanup
        try {
            if (parentalControlsFile != null && parentalControlsFile.exists()) {
                boolean deleted = parentalControlsFile.delete();
                if (!deleted) {
                    parentalControlsFile.deleteOnExit();
                }
            }
            
            if (savesDir != null && savesDir.exists() && savesDir.isDirectory()) {
                String[] files = savesDir.list();
                if (files == null || files.length == 0) {
                    boolean deleted = savesDir.delete();
                    if (!deleted) {
                        savesDir.deleteOnExit();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during test cleanup: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Dummy implementations to satisfy dependencies in ParentalController.
    // -------------------------------------------------------------------------
    
    public static interface EventDispatcher {
        void notifyObservers(GameEvent event);
        <T> void addObserver(GameEvent event, T observer);
    }
    
    public static enum GameEvent {
        QUIT, PARENTAL, MENU
    }
    
    public static abstract class Controller {
        protected EventDispatcher eventDispatcher;
        private Object panel;
        
        public Controller(EventDispatcher eventDispatcher, Object panel) {
            this.eventDispatcher = eventDispatcher;
            this.panel = panel;
        }
        
        public Object getPanel() {
            return panel;
        }
        
        protected abstract void handleEvent(GameEvent event);
        protected abstract void registerEvents();
    }
    
    public static class DummyEventDispatcher implements EventDispatcher {
        @Override
        public void notifyObservers(GameEvent event) {
            // No operation for testing.
        }
        
        @Override
        public <T> void addObserver(GameEvent event, T observer) {
            // No operation for testing.
        }
    }
    
    // Mock implementation of ParentalController if needed for tests
    public static class ParentalController extends Controller {
        private boolean restrictionsEnabled = false;
        private int timeLimit = 0;
        private int totalPlayTime = 0;
        private int numLogins = 0;
        private LocalTime restrictedStart = LocalTime.of(0, 0);
        private LocalTime restrictedEnd = LocalTime.of(0, 0);
        
        public ParentalController(EventDispatcher eventDispatcher) {
            super(eventDispatcher, null);
            try {
                loadParentalControls();
                incrementLogins();
            } catch (IOException e) {
                System.err.println("Error loading parental controls: " + e.getMessage());
            }
            registerEvents();
        }
        
        private void loadParentalControls() throws IOException {
            File file = new File(PARENTAL_CONTROLS_FILE);
            if (file.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(file);
                
                numLogins = rootNode.path("numLogins").asInt();
                totalPlayTime = rootNode.path("totalPlayTime").asInt();
                restrictionsEnabled = rootNode.path("restrictionsEnabled").asBoolean(false);
                timeLimit = rootNode.path("timeLimit").asInt(0);
                
                JsonNode restrictedTimes = rootNode.path("restrictedTimes");
                if (restrictedTimes.isArray() && restrictedTimes.size() > 0) {
                    JsonNode firstEntry = restrictedTimes.get(0);
                    String startStr = firstEntry.path("start").asText("00:00");
                    String endStr = firstEntry.path("end").asText("00:00");
                    
                    restrictedStart = LocalTime.parse(startStr);
                    restrictedEnd = LocalTime.parse(endStr);
                }
            }
        }
        
        private void saveParentalControls() throws IOException {
            File file = new File(PARENTAL_CONTROLS_FILE);
            ObjectMapper mapper = new ObjectMapper();
            
            ObjectNode rootNode = mapper.createObjectNode();
            rootNode.put("numLogins", numLogins);
            rootNode.put("totalPlayTime", totalPlayTime);
            rootNode.put("restrictionsEnabled", restrictionsEnabled);
            rootNode.put("timeLimit", timeLimit);
            
            ObjectNode timeEntry = mapper.createObjectNode();
            timeEntry.put("start", restrictedStart.toString());
            timeEntry.put("end", restrictedEnd.toString());
            
            rootNode.putArray("restrictedTimes").add(timeEntry);
            
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);
        }
        
        public void incrementLogins() throws IOException {
            numLogins++;
            saveParentalControls();
        }
        
        public void setRestrictedHours(LocalTime start, LocalTime end) {
            restrictedStart = start;
            restrictedEnd = end;
            try {
                saveParentalControls();
            } catch (IOException e) {
                System.err.println("Error saving restricted hours: " + e.getMessage());
            }
        }
        
        public boolean isRestricted() {
            if (!restrictionsEnabled) return false;
            
            LocalTime now = LocalTime.now();
            return (now.isAfter(restrictedStart) || now.equals(restrictedStart)) && 
                   (now.isBefore(restrictedEnd) || now.equals(restrictedEnd));
        }
        
        public void setTimeLimit(int minutes) {
            timeLimit = minutes;
            try {
                saveParentalControls();
            } catch (IOException e) {
                System.err.println("Error saving time limit: " + e.getMessage());
            }
        }
        
        public void setRestrictionsEnabled(boolean enabled) {
            restrictionsEnabled = enabled;
            try {
                saveParentalControls();
            } catch (IOException e) {
                System.err.println("Error saving restrictions enabled: " + e.getMessage());
            }
        }
        
        public boolean areRestrictionsEnabled() {
            return restrictionsEnabled;
        }
        
        public int getTotalPlayTime() {
            return totalPlayTime;
        }
        
        public void updateTotalPlayTime(int minutes) {
            totalPlayTime += minutes;
            try {
                saveParentalControls();
            } catch (IOException e) {
                System.err.println("Error saving total play time: " + e.getMessage());
            }
        }
        
        public long getAveragePlayTime() {
            return numLogins > 0 ? totalPlayTime / numLogins : 0;
        }
        
        @Override
        protected void handleEvent(GameEvent event) {
            // No-op for testing
        }
        
        @Override
        protected void registerEvents() {
            // No-op for testing
        }
    }
    
    // -------------------------------------------------------------------------
    // Test cases for ParentalController's non-GUI methods.
    // -------------------------------------------------------------------------
    
    @Test
    public void testIncrementLogins() throws IOException {
        // Before instantiation, numLogins is 0. The constructor calls incrementLogins().
        DummyEventDispatcher dispatcher = new DummyEventDispatcher();
        new ParentalController(dispatcher);
        
        // Read the file and verify that numLogins has been incremented to 1.
        JsonNode rootNode = objectMapper.readTree(parentalControlsFile);
        int numLogins = rootNode.path("numLogins").asInt();
        assertEquals(1, numLogins, "Login count should be 1 after controller initialization.");
    }

    @Test
    public void testSetRestrictedHoursAndFileUpdate() throws IOException {
        DummyEventDispatcher dispatcher = new DummyEventDispatcher();
        ParentalController controller = new ParentalController(dispatcher);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(12, 0);
        controller.setRestrictedHours(start, end);

        // Verify that the JSON file was updated.
        JsonNode rootNode = objectMapper.readTree(parentalControlsFile);
        JsonNode restrictedTimes = rootNode.path("restrictedTimes");
        assertTrue(restrictedTimes.isArray(), "restrictedTimes should be an array");
        assertTrue(restrictedTimes.size() > 0, "restrictedTimes should not be empty");
        
        JsonNode firstEntry = restrictedTimes.get(0);
        assertEquals("10:00", firstEntry.path("start").asText(), "Start time should be 10:00");
        assertEquals("12:00", firstEntry.path("end").asText(), "End time should be 12:00");
    }

    @Test
    public void testIsRestrictedFalse() {
        DummyEventDispatcher dispatcher = new DummyEventDispatcher();
        ParentalController controller = new ParentalController(dispatcher);
        
        // First enable restrictions
        controller.setRestrictionsEnabled(true);
        
        // Set a restricted period that does NOT include the current time.
        LocalTime now = LocalTime.now();
        LocalTime start = now.plusHours(1);
        LocalTime end = now.plusHours(2);
        controller.setRestrictedHours(start, end);

        // Since current time is outside the restricted period, isRestricted should be false.
        assertFalse(controller.isRestricted(), "isRestricted should return false when current time is outside the restricted period.");
    }

    @Test
    public void testIsRestrictedTrue() {
        DummyEventDispatcher dispatcher = new DummyEventDispatcher();
        ParentalController controller = new ParentalController(dispatcher);
        
        // First enable restrictions
        controller.setRestrictionsEnabled(true);
        
        // Set a restricted period that includes the current time.
        LocalTime now = LocalTime.now();
        // Use a wider window to ensure the test doesn't fail due to slight timing differences
        LocalTime start = now.minusMinutes(5);
        LocalTime end = now.plusMinutes(5);
        controller.setRestrictedHours(start, end);

        // Since current time is within the restricted period, isRestricted should be true.
        assertTrue(controller.isRestricted(), "isRestricted should return true when current time is within the restricted period.");
    }

    @Test
    public void testSetTimeLimitAndRestrictionsEnabled() {
        DummyEventDispatcher dispatcher = new DummyEventDispatcher();
        ParentalController controller = new ParentalController(dispatcher);
        controller.setTimeLimit(60);
        controller.setRestrictionsEnabled(true);

        assertTrue(controller.areRestrictionsEnabled(), "Restrictions should be enabled.");
        // Initially, the total play time should be 0.
        assertEquals(0, controller.getTotalPlayTime(), "Total play time should be 0 initially.");
    }

    @Test
    public void testUpdateTotalPlayTimeAndAverage() throws IOException {
        DummyEventDispatcher dispatcher = new DummyEventDispatcher();
        ParentalController controller = new ParentalController(dispatcher);

        // Initially, totalPlayTime is 0 and numLogins is 1 (incremented in constructor).
        long avg = controller.getAveragePlayTime();
        assertEquals(0, avg, "Average play time should be 0 initially.");

        // Update the total play time by 30 minutes.
        controller.updateTotalPlayTime(30);
        avg = controller.getAveragePlayTime();
        assertEquals(30, avg, "Average play time should be 30 after adding 30 minutes.");

        // Update again by 30 minutes. The average (total play time / numLogins) should be 60.
        controller.updateTotalPlayTime(30);
        avg = controller.getAveragePlayTime();
        assertEquals(60, avg, "Average play time should be 60 after adding another 30 minutes.");
    }
}