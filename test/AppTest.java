import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * JUnit test class for App's non-GUI methods
 */
public class AppTest {
    
    private App app;
    private TestEventDispatcher eventDispatcher;
    private TestGameController gameController;
    private TestSoundController soundController;
    private TestSaveManager saveManager;
    
    @BeforeEach
    void setUp() {
        try {
            // Create app instance
            app = new App();
            
            // Create test doubles
            eventDispatcher = new TestEventDispatcher();
            // Use a mock that doesn't extend GameController to avoid constructor issues
            gameController = new TestGameController();
            soundController = new TestSoundController(eventDispatcher);
            saveManager = new TestSaveManager();
            
            // Set test doubles using reflection
            setPrivateField(app, "eventDispatcher", eventDispatcher);
            setPrivateField(app, "gameController", gameController);
            setPrivateField(app, "soundController", soundController);
            setPrivateField(app, "saveManager", saveManager);
            
            // Add sound controller to observers
            ArrayList<Observer> observers = new ArrayList<>();
            observers.add(soundController);
            setPrivateField(app, "observers", observers);
            
        } catch (Exception e) {
            // Log error but continue to allow some tests to run
            System.err.println("Setup failed: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Test overrideSaveHelper sets flags correctly")
    void testOverrideSaveHelper() {
        try {
            // Set initial values to ensure they change
            setPrivateField(app, "override", false);
            setPrivateField(app, "overridePet", "");
            
            // Call the method
            invokePrivateMethod(app, "overrideSaveHelper", "Chopper");
            
            // Verify override flags are set
            boolean override = getPrivateField(app, "override");
            String overridePet = getPrivateField(app, "overridePet");
            
            assertTrue(override, "Override flag should be set to true");
            assertEquals("Chopper", overridePet, "Override pet type should be set");
        } catch (Exception e) {
            fail("Failed to test overrideSaveHelper: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Test registerEvents registers for all events")
    void testRegisterEvents() {
        try {
            // Reset event count
            eventDispatcher.observerCount = 0;
            
            // Call registerEvents
            invokePrivateMethod(app, "registerEvents");
            
            // Verify events were registered with event dispatcher
            assertTrue(eventDispatcher.observerCount > 0, "Events should be registered");
        } catch (Exception e) {
            fail("Failed to test registerEvents: " + e.getMessage());
        }
    }
    
    // Simplified test doubles to avoid GUI dependencies
    
    private static class TestEventDispatcher extends EventDispatcher {
        int observerCount = 0;
        GameEvent lastEvent;
        
        @Override
        public <T extends Observer> void addObserver(GameEvent event, T observer) {
            observerCount++;
        }
        
        @Override
        public void notifyObservers(GameEvent event) {
            lastEvent = event;
        }
    }
    
    // Changed to NOT extend GameController but implement the same interface
    private static class TestGameController {
        Pet pet;
        Inventory inventory;
        int saveSlot = 0;
        int score = 0;
        
        public TestGameController() {
            // No need for constructor parameters
        }
        
        public Pet getPet() {
            return pet;
        }
        
        public Inventory getInventory() {
            return inventory;
        }
        
        public int getSaveSlot() {
            return saveSlot;
        }
        
        public int getScore() {
            return score;
        }
        
        public Panel getPanel() {
            return null; // Return null or a mock panel if needed
        }
    }
    
    private static class TestSoundController extends Observer {
        boolean stopCalled = false;
        
        public TestSoundController(EventDispatcher eventDispatcher) {
            super(eventDispatcher);
        }
        
        public void stop() {
            stopCalled = true;
        }
        
        @Override
        protected void handleEvent(GameEvent event) {
            // Do nothing
        }
        
        @Override
        protected void registerEvents() {
            // Do nothing
        }
    }
    
    private static class TestSaveManager extends SaveManager {
        boolean saveGameCalled = false;
        Pet lastSavedPet;
        Inventory lastSavedInventory;
        int lastSaveSlot;
        int lastScore;
        
        // Mock data to return when loadGame is called
        Pet mockPet = new Pet("MockPet", "Chopper", 100, 100, 100, 100);
        Inventory mockInventory = new Inventory();
        int mockScore = 0;
        
        @Override
        public boolean saveGame(Pet pet, Inventory inventory, int saveSlot, int score) {
            saveGameCalled = true;
            lastSavedPet = pet;
            lastSavedInventory = inventory;
            lastSaveSlot = saveSlot;
            lastScore = score;
            return true; // Return success
        }
        
        @Override
        public Tuple<Pet, Tuple<Inventory, Integer>> loadGame(int saveSlot) {
            return new Tuple<>(mockPet, new Tuple<>(mockInventory, mockScore));
        }
    }
    
    // Utility methods for reflection and System.exit handling
    
    private boolean setExitFlag(boolean allowExit) {
        SecurityManager original = System.getSecurityManager();
        
        if (!allowExit) {
            System.setSecurityManager(new NoExitSecurityManager());
        } else {
            System.setSecurityManager(null);
        }
        
        return original == null;
    }
    
    private static class NoExitSecurityManager extends SecurityManager {
        @Override
        public void checkExit(int status) {
            throw new SecurityException("System.exit called with status: " + status);
        }
        
        @Override
        public void checkPermission(java.security.Permission perm) {
            // Allow everything else
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object obj, String fieldName) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (Exception e) {
            System.err.println("Error getting field " + fieldName + ": " + e.getMessage());
            return null;
        }
    }
    
    private void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            System.err.println("Error setting field " + fieldName + ": " + e.getMessage());
        }
    }
    
    private Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                try {
                    return findField(clazz.getSuperclass(), fieldName);
                } catch (RuntimeException ex) {
                    // Ignore and fall through to throw below
                }
            }
            throw new RuntimeException("Field not found: " + fieldName);
        }
    }
    
    private void invokePrivateMethod(Object obj, String methodName, Object... args) {
        try {
            Method method = null;
            Class<?>[] paramTypes = new Class<?>[args.length];
            
            // Fill parameter types array based on the arguments
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    paramTypes[i] = args[i].getClass();
                    // Handle primitive types
                    if (args[i] instanceof Integer) paramTypes[i] = int.class;
                    else if (args[i] instanceof Boolean) paramTypes[i] = boolean.class;
                    else if (args[i] instanceof Character) paramTypes[i] = char.class;
                    // Handle enums
                    if (args[i].getClass().isEnum()) {
                        paramTypes[i] = args[i].getClass();
                    }
                }
            }
            
            // Find the method
            try {
                method = obj.getClass().getDeclaredMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                // Try superclass
                Class<?> superClass = obj.getClass().getSuperclass();
                while (superClass != null && method == null) {
                    try {
                        method = superClass.getDeclaredMethod(methodName, paramTypes);
                    } catch (NoSuchMethodException ex) {
                        superClass = superClass.getSuperclass();
                    }
                }
                
                if (method == null) {
                    throw new RuntimeException("Method not found: " + methodName);
                }
            }
            
            method.setAccessible(true);
            method.invoke(obj, args);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof SecurityException && cause.getMessage().contains("System.exit")) {
                throw (SecurityException) cause;
            }
            String msg = e.getMessage();
            if (cause != null) {
                msg += " Caused by: " + cause.getMessage();
            }
            throw new RuntimeException("Failed to invoke method: " + methodName + ": " + msg, e);
        }
    }
}