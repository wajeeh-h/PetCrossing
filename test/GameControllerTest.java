import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Timer;

/**
 * Test class for non-GUI methods of GameController
 */
public class GameControllerTest {
    
    private GameController gameController;
    private Pet pet;
    private Inventory inventory;
    private TestEventDispatcher eventDispatcher;
    private TestGameplayPanel gameplayPanel;
    private int saveSlot;
    private int score;
    
    @BeforeEach
    void setUp() {
        try {
            // Create test objects
            eventDispatcher = new TestEventDispatcher();
            pet = new Pet("TestPet", "chopper", 100, 100, 100, 100);
            inventory = new Inventory();
            saveSlot = 1;
            score = 0; // Default score for testing
            gameplayPanel = new TestGameplayPanel(eventDispatcher);
            
            // Add some items to inventory for testing
            inventory.addItem(Item.APPLE);
            inventory.addItem(Item.BANANA);
            inventory.addItem(Item.PURPLEGIFT);
            inventory.addItem(Item.GREENGIFT);
            
            // Create controller with the correct constructor (including score parameter)
            gameController = new GameController(eventDispatcher, gameplayPanel, inventory, saveSlot, pet, score);
            
            // Ensure the timer is properly initialized
            Timer statTimer = getPrivateField(gameController, "statTimer");
            if (statTimer == null) {
                fail("Timer is not initialized - check GameController constructor");
            }
        } catch (Exception e) {
            fail("Setup failed: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Test constructor initialization")
    void testConstructorInitialization() throws Exception {
        // Verify fields are set correctly
        assertSame(pet, gameController.getPet(), "Pet reference doesn't match");
        assertSame(inventory, gameController.getInventory(), "Inventory reference doesn't match");
        assertEquals(saveSlot, gameController.getSaveSlot(), "Save slot doesn't match");
        assertEquals(score, gameController.getScore(), "Score doesn't match");
        
        // Check cooldowns initialization
        Map<String, Long> cooldowns = getPrivateField(gameController, "cooldowns");
        assertNotNull(cooldowns, "Cooldowns map should be initialized");
        assertEquals(0L, cooldowns.getOrDefault("vet", -1L), "Vet cooldown should be initialized to 0");
        assertEquals(0L, cooldowns.getOrDefault("walk", -1L), "Walk cooldown should be initialized to 0");
        assertEquals(0L, cooldowns.getOrDefault("play", -1L), "Play cooldown should be initialized to 0");
        
        // Verify previous state is set
        PetState previousState = getPrivateField(gameController, "previousState");
        assertNotNull(previousState, "Previous state should be initialized");
        assertEquals(pet.getState(), previousState, "Previous state should match pet's initial state");
        
        // Verify sleep penalty flag is initialized to false
        boolean sleepPenaltyApplied = getPrivateField(gameController, "sleepPenaltyApplied");
        assertFalse(sleepPenaltyApplied, "Sleep penalty flag should be false initially");
    }
    
    @Test
    @DisplayName("Test start and pause methods")
    void testStartAndPause() throws Exception {
        // Get the timer
        Timer statTimer = getPrivateField(gameController, "statTimer");
        assertNotNull(statTimer, "Timer should be initialized");
        
        // Make sure timer is initially stopped
        statTimer.stop();
        assertFalse(statTimer.isRunning(), "Timer should be stopped initially");
        
        // Start the controller
        gameController.start();
        
        // Verify timer is running
        assertTrue(statTimer.isRunning(), "Timer should be running after start() call");
        
        // Pause the controller
        gameController.pause();
        
        // Verify timer is stopped
        assertFalse(statTimer.isRunning(), "Timer should be stopped after pause() call");
    }
    
    @Test
    @DisplayName("Test sleep penalty application")
    void testSleepPenalty() throws Exception {
        // Set pet sleep to low value (below 0.5)
        pet.setSleep(0.4);
        
        // Manually invoke the timer action
        boolean success = invokeTimerAction();
        assertTrue(success, "Timer action invocation failed");
        
        // Verify sleep penalty was applied
        assertEquals(80, pet.getHealth(), 0.01, "Health should be reduced by 20 points");
        
        // Verify penalty flag was set
        boolean sleepPenaltyApplied = getPrivateField(gameController, "sleepPenaltyApplied");
        assertTrue(sleepPenaltyApplied, "Sleep penalty flag should be set to true");
        
        // Reset health and invoke timer again
        pet.setHealth(100);
        invokeTimerAction();
        
        // Verify penalty isn't applied twice
        assertEquals(100, pet.getHealth(), 0.01, "Health shouldn't change on second penalty check");
        
        // Now set sleep above threshold
        pet.setSleep(1.0);
        
        // Invoke timer action
        invokeTimerAction();
        
        // Verify penalty flag was reset
        sleepPenaltyApplied = getPrivateField(gameController, "sleepPenaltyApplied");
        assertFalse(sleepPenaltyApplied, "Sleep penalty flag should be reset to false");
    }
    
    // Other tests remain the same...
    
    // Improved helper methods
    
    private boolean invokeTimerAction() throws Exception {
        try {
            // Get the timer
            Timer statTimer = getPrivateField(gameController, "statTimer");
            if (statTimer == null) {
                fail("Timer is null");
                return false;
            }
            
            // Get all action listeners (there should be at least one)
            ActionListener[] listeners = statTimer.getActionListeners();
            if (listeners.length == 0) {
                fail("Timer has no action listeners");
                return false;
            }
            
            // Get the first ActionListener
            ActionListener listener = listeners[0];
            
            // Create a dummy ActionEvent
            ActionEvent event = new ActionEvent(statTimer, ActionEvent.ACTION_PERFORMED, "test");
            
            // Try direct invocation first (works for non-anonymous classes)
            try {
                Method actionPerformed = listener.getClass().getDeclaredMethod("actionPerformed", ActionEvent.class);
                actionPerformed.setAccessible(true);
                actionPerformed.invoke(listener, event);
                return true;
            } catch (NoSuchMethodException e) {
                // If direct method access fails, try the interface method
                listener.actionPerformed(event);
                return true;
            }
        } catch (Exception e) {
            fail("Error invoking timer action: " + e.getMessage());
            return false;
        }
    }
    
    private void invokeHandleEvent(GameEvent event) {
        try {
            // Try multiple ways to find the method
            Method handleEvent = null;
            
            // First try: direct method
            try {
                handleEvent = GameController.class.getDeclaredMethod("handleEvent", GameEvent.class);
            } catch (NoSuchMethodException e) {
                // Second try: look in Observer superclass
                handleEvent = GameController.class.getSuperclass().getDeclaredMethod("handleEvent", GameEvent.class);
            }
            
            if (handleEvent == null) {
                fail("Could not find handleEvent method");
                return;
            }
            
            handleEvent.setAccessible(true);
            handleEvent.invoke(gameController, event);
        } catch (Exception e) {
            fail("Error invoking handleEvent: " + e.getMessage() + 
                 "\nCause: " + (e.getCause() != null ? e.getCause().getMessage() : "none"));
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object obj, String fieldName) throws Exception {
        try {
            Field field = findField(obj.getClass(), fieldName);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (Exception e) {
            fail("Error accessing field '" + fieldName + "': " + e.getMessage());
            return null; // never reached due to fail()
        }
    }
    
    private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        NoSuchFieldException lastException = null;
        
        // Search through class hierarchy
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                lastException = e;
                currentClass = currentClass.getSuperclass();
            }
        }
        
        // If we get here, the field wasn't found in any superclass
        throw new NoSuchFieldException("Field '" + fieldName + "' not found in " + clazz.getName() + 
                                      " or any of its superclasses");
    }
    
    /**
     * Improved mock GameplayPanel for testing
     */
    private static class TestGameplayPanel extends GameplayPanel {
        public boolean inventoryUpdated = false;
        public boolean statusBarsUpdated = false;
        public boolean petSpriteUpdated = false;
        public boolean cooldownsUpdated = false;
        public boolean allowVet = true;
        public boolean allowWalk = true;
        public boolean allowPlay = true;
        
        public TestGameplayPanel(EventDispatcher eventDispatcher) {
            super(eventDispatcher);
        }
        
        @Override
        public void updateInventory(Inventory inventory) {
            inventoryUpdated = true;
        }
        
        @Override
        public void updateStatusBars(Pet pet) {
            statusBarsUpdated = true;
        }
        
        @Override
        public void updatePetSprite(PetState state) {
            petSpriteUpdated = true;
        }
        
        @Override
        public void updateButtonCooldowns(Pet pet) {
            cooldownsUpdated = true;
        }
        
        @Override
        public void setSprites() {
            // Do nothing
        }
        
        // Override any additional GUI methods as needed
        // No-op implementations for all GUI methods
    }
    
    /**
     * Improved mock EventDispatcher for testing
     */
    private static class TestEventDispatcher extends EventDispatcher {
        @Override
        public <T extends Observer> void addObserver(GameEvent event, T observer) {
            // Do nothing
        }
        
        @Override
        public void notifyObservers(GameEvent event) {
            // Do nothing
        }
        
        // Override any additional methods as needed
    }
}