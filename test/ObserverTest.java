import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for the Observer abstract class
 */
public class ObserverTest {
    
    @Test
    @DisplayName("Constructor with EventDispatcher sets the dispatcher field")
    void testConstructorWithEventDispatcher() {
        // Create an event dispatcher
        EventDispatcher dispatcher = new EventDispatcher();
        
        // Create an observer with the dispatcher
        TestObserver observer = new TestObserver(dispatcher);
        
        // Verify the dispatcher was set correctly
        assertSame(dispatcher, observer.getEventDispatcher());
    }
    
    @Test
    @DisplayName("Default constructor creates a new EventDispatcher")
    void testDefaultConstructor() {
        // Create an observer with default constructor
        TestObserver observer = new TestObserver();
        
        // Verify a dispatcher was created
        assertNotNull(observer.getEventDispatcher());
        assertTrue(observer.getEventDispatcher() instanceof EventDispatcher);
    }
    
    @Test
    @DisplayName("handleEvent method is called when implemented")
    void testHandleEventCalled() {
        // Create an observer
        TestObserver observer = new TestObserver();
        
        // Call handleEvent via test method
        GameEvent testEvent = GameEvent.MENU;
        observer.testHandleEvent(testEvent);
        
        // Verify handleEvent was called with the correct event
        assertTrue(observer.wasHandleEventCalled());
        assertEquals(testEvent, observer.getLastHandledEvent());
    }
    
    @Test
    @DisplayName("registerEvents method is called when implemented")
    void testRegisterEventsCalled() {
        // Create an observer
        TestObserver observer = new TestObserver();
        
        // Call registerEvents via test method
        observer.testRegisterEvents();
        
        // Verify registerEvents was called
        assertTrue(observer.wasRegisterEventsCalled());
    }
    
    /**
     * Concrete implementation of Observer for testing
     */
    private static class TestObserver extends Observer {
        private boolean handleEventCalled = false;
        private boolean registerEventsCalled = false;
        private GameEvent lastHandledEvent = null;
        
        public TestObserver(EventDispatcher eventDispatcher) {
            super(eventDispatcher);
        }
        
        public TestObserver() {
            super();
        }
        
        @Override
        protected void handleEvent(GameEvent event) {
            handleEventCalled = true;
            lastHandledEvent = event;
        }
        
        @Override
        protected void registerEvents() {
            registerEventsCalled = true;
        }
        
        // Methods for testing
        
        public boolean wasHandleEventCalled() {
            return handleEventCalled;
        }
        
        public boolean wasRegisterEventsCalled() {
            return registerEventsCalled;
        }
        
        public GameEvent getLastHandledEvent() {
            return lastHandledEvent;
        }
        
        public EventDispatcher getEventDispatcher() {
            return eventDispatcher;
        }
        
        // Test wrapper methods to call protected methods
        
        public void testHandleEvent(GameEvent event) {
            handleEvent(event);
        }
        
        public void testRegisterEvents() {
            registerEvents();
        }
    }
}