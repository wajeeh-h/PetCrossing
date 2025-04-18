import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * JUnit test class for non-GUI methods of LoadGamePanel
 */
public class LoadGamePanelTest {
    
    private LoadGamePanel loadGamePanel;
    private TestEventDispatcher eventDispatcher;
    
    @BeforeEach
    void setUp() {
        // Use a mock event dispatcher to avoid actual event handling
        eventDispatcher = new TestEventDispatcher();
        
        // Create panel instance but don't call init() yet to avoid GUI initialization
        loadGamePanel = new NoInitLoadGamePanel(eventDispatcher);
    }
    
    @Test
    @DisplayName("Test constructor sets event dispatcher")
    void testConstructorSetsEventDispatcher() throws Exception {
        // Verify event dispatcher was set in constructor
        EventDispatcher assignedDispatcher = getPrivateField(loadGamePanel, "eventDispatcher");
        assertSame(eventDispatcher, assignedDispatcher);
    }
    
    @Test
    @DisplayName("Test overrideButtonText sets new button text")
    void testOverrideButtonText() {
        // Default button text should be "Select"
        assertEquals("Select", getPrivateField(loadGamePanel, "buttonText"));
        
        // Override the text
        loadGamePanel.overrideButtonText("Override");
        
        // Verify text was changed
        assertEquals("Override", getPrivateField(loadGamePanel, "buttonText"));
    }
    
    @Test
    @DisplayName("Test PET_PANEL_DIM constant has correct values")
    void testPetPanelDimConstant() {
        // Get dimension constant
        java.awt.Dimension dim = getPrivateField(loadGamePanel, "PET_PANEL_DIM");
        
        // Verify dimensions are as expected
        assertEquals(600, dim.width);
        assertEquals(150, dim.height);
    }
    
    /**
     * Test subclass that doesn't call init() in constructor
     * This allows us to test the non-GUI methods without initializing the UI
     */
    private static class NoInitLoadGamePanel extends LoadGamePanel {
        public NoInitLoadGamePanel(EventDispatcher eventDispatcher) {
            super(eventDispatcher);
        }
        
        // Override init to do nothing (avoid GUI initialization)
        @Override
        public void init() {
            // Do nothing
        }
        
        // Override to avoid actual dialog creation
        @Override
        public void displaySelectionDialogue() {
            // Do nothing
        }
    }
    
    /**
     * Test event dispatcher that doesn't do any actual dispatching
     */
    private static class TestEventDispatcher extends EventDispatcher {
        // No special implementation needed for this test
    }
    
    /**
     * Helper method to get a private field using reflection
     */
    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object obj, String fieldName) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (Exception e) {
            fail("Failed to access field " + fieldName + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Helper method to find a field in class hierarchy
     */
    private Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), fieldName);
            }
            throw new RuntimeException("Field not found: " + fieldName, e);
        }
    }
}