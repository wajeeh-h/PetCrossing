import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;

/**
 * JUnit test class for EventDispatcher
 */
public class EventDispatcherTest {
    
    private EventDispatcher dispatcher;
    
    @BeforeEach
    void setUp() {
        dispatcher = new EventDispatcher();
    }
    
    @Test
    @DisplayName("Test constructor initializes observers map")
    void testConstructor() {
        // Get the observers map using reflection
        HashMap<GameEvent, List<Observer>> observers = getPrivateField(dispatcher, "observers");
        
        // Verify map is initialized
        assertNotNull(observers);
        assertTrue(observers.isEmpty());
    }
    
    @Test
    @DisplayName("Test addObserver with new event")
    void testAddObserverNewEvent() {
        // Create a test observer
        TestObserver observer = new TestObserver();
        
        // Add the observer for a test event
        dispatcher.addObserver(GameEvent.MENU, observer);
        
        // Get the observers map using reflection
        HashMap<GameEvent, List<Observer>> observers = getPrivateField(dispatcher, "observers");
        
        // Verify the observer was added
        assertTrue(observers.containsKey(GameEvent.MENU));
        assertEquals(1, observers.get(GameEvent.MENU).size());
        assertSame(observer, observers.get(GameEvent.MENU).get(0));
    }
    
    @Test
    @DisplayName("Test addObserver with existing event")
    void testAddObserverExistingEvent() {
        // Create test observers
        TestObserver observer1 = new TestObserver();
        TestObserver observer2 = new TestObserver();
        
        // Add the first observer
        dispatcher.addObserver(GameEvent.MENU, observer1);
        
        // Add the second observer for the same event
        dispatcher.addObserver(GameEvent.MENU, observer2);
        
        // Get the observers map using reflection
        HashMap<GameEvent, List<Observer>> observers = getPrivateField(dispatcher, "observers");
        
        // Verify both observers were added
        assertTrue(observers.containsKey(GameEvent.MENU));
        assertEquals(2, observers.get(GameEvent.MENU).size());
        assertSame(observer1, observers.get(GameEvent.MENU).get(0));
        assertSame(observer2, observers.get(GameEvent.MENU).get(1));
    }
    
    @Test
    @DisplayName("Test addObserver with multiple events")
    void testAddObserverMultipleEvents() {
        // Create a test observer
        TestObserver observer = new TestObserver();
        
        // Add the observer for multiple events
        dispatcher.addObserver(GameEvent.MENU, observer);
        dispatcher.addObserver(GameEvent.QUIT, observer);
        
        // Get the observers map using reflection
        HashMap<GameEvent, List<Observer>> observers = getPrivateField(dispatcher, "observers");
        
        // Verify the observer was added to both events
        assertTrue(observers.containsKey(GameEvent.MENU));
        assertTrue(observers.containsKey(GameEvent.QUIT));
        assertEquals(1, observers.get(GameEvent.MENU).size());
        assertEquals(1, observers.get(GameEvent.QUIT).size());
        assertSame(observer, observers.get(GameEvent.MENU).get(0));
        assertSame(observer, observers.get(GameEvent.QUIT).get(0));
    }
    
    @Test
    @DisplayName("Test removeObserver with existing observer")
    void testRemoveObserverExisting() {
        // Create test observers
        TestObserver observer1 = new TestObserver();
        TestObserver observer2 = new TestObserver();
        
        // Add both observers
        dispatcher.addObserver(GameEvent.MENU, observer1);
        dispatcher.addObserver(GameEvent.MENU, observer2);
        
        // Remove the first observer
        dispatcher.removeObserver(GameEvent.MENU, observer1);
        
        // Get the observers map using reflection
        HashMap<GameEvent, List<Observer>> observers = getPrivateField(dispatcher, "observers");
        
        // Verify the first observer was removed but the second remains
        assertTrue(observers.containsKey(GameEvent.MENU));
        assertEquals(1, observers.get(GameEvent.MENU).size());
        assertSame(observer2, observers.get(GameEvent.MENU).get(0));
    }
    
    @Test
    @DisplayName("Test removeObserver with non-existent event")
    void testRemoveObserverNonExistentEvent() {
        // Create a test observer
        TestObserver observer = new TestObserver();
        
        // Try to remove from an event that doesn't exist
        dispatcher.removeObserver(GameEvent.QUIT, observer);
        
        // Get the observers map using reflection
        HashMap<GameEvent, List<Observer>> observers = getPrivateField(dispatcher, "observers");
        
        // Verify the map is still empty
        assertTrue(observers.isEmpty());
    }
    
    @Test
    @DisplayName("Test removeObserver with non-existent observer")
    void testRemoveObserverNonExistentObserver() {
        // Create test observers
        TestObserver observer1 = new TestObserver();
        TestObserver observer2 = new TestObserver();
        
        // Add only the first observer
        dispatcher.addObserver(GameEvent.MENU, observer1);
        
        // Try to remove the second observer that was never added
        dispatcher.removeObserver(GameEvent.MENU, observer2);
        
        // Get the observers map using reflection
        HashMap<GameEvent, List<Observer>> observers = getPrivateField(dispatcher, "observers");
        
        // Verify the first observer is still there
        assertTrue(observers.containsKey(GameEvent.MENU));
        assertEquals(1, observers.get(GameEvent.MENU).size());
        assertSame(observer1, observers.get(GameEvent.MENU).get(0));
    }
    
    @Test
    @DisplayName("Test notifyObservers with existing event")
    void testNotifyObserversExistingEvent() {
        // Create test observers
        TestObserver observer1 = new TestObserver();
        TestObserver observer2 = new TestObserver();
        
        // Add both observers for the same event
        dispatcher.addObserver(GameEvent.MENU, observer1);
        dispatcher.addObserver(GameEvent.MENU, observer2);
        
        // Verify observers haven't been notified yet
        assertFalse(observer1.wasNotified());
        assertFalse(observer2.wasNotified());
        
        // Notify observers for this event
        dispatcher.notifyObservers(GameEvent.MENU);
        
        // Verify both observers were notified
        assertTrue(observer1.wasNotified());
        assertTrue(observer2.wasNotified());
        
        // Verify they were notified with the correct event
        assertEquals(GameEvent.MENU, observer1.getLastEvent());
        assertEquals(GameEvent.MENU, observer2.getLastEvent());
    }
    
    @Test
    @DisplayName("Test notifyObservers with multiple events")
    void testNotifyObserversMultipleEvents() {
        // Create test observers
        TestObserver observer1 = new TestObserver();
        TestObserver observer2 = new TestObserver();
        
        // Add observers for different events
        dispatcher.addObserver(GameEvent.MENU, observer1);
        dispatcher.addObserver(GameEvent.QUIT, observer2);
        
        // Notify observers for MENU event
        dispatcher.notifyObservers(GameEvent.MENU);
        
        // Verify only observer1 was notified
        assertTrue(observer1.wasNotified());
        assertFalse(observer2.wasNotified());
        assertEquals(GameEvent.MENU, observer1.getLastEvent());
        
        // Reset notification status
        observer1.reset();
        observer2.reset();
        
        // Notify observers for QUIT event
        dispatcher.notifyObservers(GameEvent.QUIT);
        
        // Verify only observer2 was notified
        assertFalse(observer1.wasNotified());
        assertTrue(observer2.wasNotified());
        assertEquals(GameEvent.QUIT, observer2.getLastEvent());
    }
    
    @Test
    @DisplayName("Test notifyObservers with non-existent event")
    void testNotifyObserversNonExistentEvent() {
        // Create a test observer
        TestObserver observer = new TestObserver();
        
        // Add observer for MENU event
        dispatcher.addObserver(GameEvent.MENU, observer);
        
        // Notify observers for a different event
        dispatcher.notifyObservers(GameEvent.QUIT);
        
        // Verify observer was not notified
        assertFalse(observer.wasNotified());
    }
    
    /**
     * Helper method to get a private field using reflection
     */
    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException("Error accessing field: " + fieldName, e);
        }
    }
    
    /**
     * Test implementation of Observer for verifying notification behavior
     */
    private static class TestObserver extends Observer {
        private boolean notified = false;
        private GameEvent lastEvent = null;
        
        @Override
        protected void handleEvent(GameEvent event) {
            notified = true;
            lastEvent = event;
        }
        
        @Override
        protected void registerEvents() {
            // Not needed for testing
        }
        
        public boolean wasNotified() {
            return notified;
        }
        
        public GameEvent getLastEvent() {
            return lastEvent;
        }
        
        public void reset() {
            notified = false;
            lastEvent = null;
        }
    }
}