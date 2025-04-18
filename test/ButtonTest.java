import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.AbstractButton;
import javax.swing.JButton;

/**
 * JUnit test class for the Button class
 */
public class ButtonTest {

    private Button button;
    private MockEventDispatcher eventDispatcher;
    private GameEvent testEvent;
    private String buttonName;
    private String buttonText;
    
    @BeforeEach
    void setUp() {
        // Create test data
        buttonName = "testButton";
        buttonText = "Test Button";
        testEvent = GameEvent.MENU;
        eventDispatcher = new MockEventDispatcher();
        
        // Create button instance
        button = new Button(buttonText, testEvent, eventDispatcher);
    }
    
    @Test
    @DisplayName("Test constructor with name only")
    void testConstructorWithName() {
        // Create button with just name (name and text are the same)
        Button nameButton = new Button(buttonName, testEvent, eventDispatcher);
        
        // Verify properties
        assertEquals(buttonName, nameButton.getName());
        assertEquals(buttonName, nameButton.getText());
        assertEquals(testEvent, getPrivateField(nameButton, "action"));
        assertSame(eventDispatcher, nameButton.eventDispatcher);
        
        // Check visual properties
        assertEquals(Button.BUTTON_COLOR, nameButton.getBackground());
        assertEquals(Color.WHITE, nameButton.getForeground());
        assertFalse(nameButton.isFocusPainted());
        assertNotNull(nameButton.getFont());
    }
    
    @Test
    @DisplayName("Test constructor with name and text")
    void testConstructorWithNameAndText() {
        // Create button with separate name and text
        Button namedButton = new Button(buttonText, testEvent, eventDispatcher, buttonName);
        
        // Verify properties
        assertEquals(buttonName, namedButton.getName());
        assertEquals(buttonText, namedButton.getText());
        assertEquals(testEvent, getPrivateField(namedButton, "action"));
        assertSame(eventDispatcher, namedButton.eventDispatcher);
        
        // Check visual properties
        assertEquals(Button.BUTTON_COLOR, namedButton.getBackground());
        assertEquals(Color.WHITE, namedButton.getForeground());
        assertFalse(namedButton.isFocusPainted());
        assertNotNull(namedButton.getFont());
    }
    
    @Test
    @DisplayName("Test button click triggers the correct event")
    void testButtonClick() {
        // Initially no events dispatched
        assertEquals(0, eventDispatcher.getDispatchCount());
        
        // Simulate button click
        simulateButtonClick(button);
        
        // Verify dispatcher was called with correct event
        assertEquals(1, eventDispatcher.getDispatchCount());
        assertEquals(testEvent, eventDispatcher.getLastEvent());
    }
    
    @Test
    @DisplayName("Test static BUTTON_COLOR constant")
    void testButtonColorConstant() {
        // Check that the color constant is correct
        assertEquals(new Color(129, 156, 222), Button.BUTTON_COLOR);
    }
    
    @Test
    @DisplayName("Test GAME_FONT initialization")
    void testGameFontInitialization() {
        // Verify that the font was initialized (one way or another)
        assertNotNull(Button.GAME_FONT);
    }
    
    @Test
    @DisplayName("Test button extends JButton")
    void testButtonInheritance() {
        // Verify inheritance
        assertTrue(button instanceof JButton);
        assertTrue(button instanceof AbstractButton);
    }
    
    /**
     * Helper method to simulate clicking a button
     */
    private void simulateButtonClick(Button button) {
        try {
            // Get the onClick method using reflection
            Method onClickMethod = Button.class.getDeclaredMethod("onClick");
            onClickMethod.setAccessible(true);
            
            // Call the method directly
            onClickMethod.invoke(button);
        } catch (Exception e) {
            fail("Failed to simulate button click: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to access private fields
     */
    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object obj, String fieldName) {
        try {
            Field field = Button.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (Exception e) {
            fail("Failed to access field " + fieldName + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Mock EventDispatcher class for testing
     */
    private static class MockEventDispatcher extends EventDispatcher {
        private int dispatchCount = 0;
        private GameEvent lastEvent = null;
        
        @Override
        public void notifyObservers(GameEvent event) {
            dispatchCount++;
            lastEvent = event;
        }
        
        public int getDispatchCount() {
            return dispatchCount;
        }
        
        public GameEvent getLastEvent() {
            return lastEvent;
        }
    }
}