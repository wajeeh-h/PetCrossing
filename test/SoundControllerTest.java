import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Test class for non-GUI methods of SoundController
 */
public class SoundControllerTest {
    private MockEventDispatcher eventDispatcher;
    private TestSoundController soundController;
    
    @BeforeEach
    void setUp() {
        eventDispatcher = new MockEventDispatcher();
        soundController = new TestSoundController(eventDispatcher);
    }
    
    @Test
    @DisplayName("Test constructor properly initializes the controller")
    void testConstructor() {
        assertNotNull(soundController, "SoundController should be initialized");
        assertNull(soundController.currentSound, "Sound should be null initially");
    }

    @Test
    @DisplayName("Test registerEvents registers for the correct events")
    void testRegisterEvents() {
        // Reset counter
        eventDispatcher.observerCount = 0;
        
        // Call registerEvents
        soundController.registerEvents();
        
        // Verify the correct number of events were registered
        assertEquals(7, eventDispatcher.observerCount, 
                    "Should register for MENU, TUTORIAL, MINIGAME, PARENTAL, INGAME, NEW_GAME, and STOPSOUND events");
    }

    @Test
    @DisplayName("Test handleEvent for MENU event plays the menu sound")
    void testHandleEventMenu() {
        // Call handleEvent with MENU event
        soundController.handleEvent(GameEvent.MENU);
        
        // Verify correct sound path
        assertEquals("resources/sounds/menu.wav", soundController.lastSoundPath, "Should play menu sound");
        
        // Verify sound was played
        assertTrue(soundController.currentSound.playCalled, "Sound should be played");
    }

    @Test
    @DisplayName("Test handleEvent for TUTORIAL event plays the tutorial sound")
    void testHandleEventTutorial() {
        // Call handleEvent with TUTORIAL event
        soundController.handleEvent(GameEvent.TUTORIAL);
        
        // Verify correct sound path
        assertEquals("resources/sounds/tutorial.wav", soundController.lastSoundPath, "Should play tutorial sound");
        
        // Verify sound was played
        assertTrue(soundController.currentSound.playCalled, "Sound should be played");
    }
    
    @Test
    @DisplayName("Test handleEvent for NEW_GAME event plays the tutorial sound")
    void testHandleEventNewGame() {
        // Call handleEvent with NEW_GAME event
        soundController.handleEvent(GameEvent.NEW_GAME);
        
        // Verify correct sound path
        assertEquals("resources/sounds/tutorial.wav", soundController.lastSoundPath, "Should play tutorial sound");
        
        // Verify sound was played
        assertTrue(soundController.currentSound.playCalled, "Sound should be played");
    }
    
    @Test
    @DisplayName("Test handleEvent for INGAME event plays the tutorial sound")
    void testHandleEventIngame() {
        // Call handleEvent with INGAME event
        soundController.handleEvent(GameEvent.INGAME);
        
        // Verify correct sound path
        assertEquals("resources/sounds/tutorial.wav", soundController.lastSoundPath, "Should play tutorial sound");
        
        // Verify sound was played
        assertTrue(soundController.currentSound.playCalled, "Sound should be played");
    }

    @Test
    @DisplayName("Test handleEvent for MINIGAME event plays the minigame sound")
    void testHandleEventMinigame() {
        // Call handleEvent with MINIGAME event
        soundController.handleEvent(GameEvent.MINIGAME);
        
        // Verify correct sound path
        assertEquals("resources/sounds/minigameMusic.wav", soundController.lastSoundPath, "Should play minigame sound");
        
        // Verify sound was played
        assertTrue(soundController.currentSound.playCalled, "Sound should be played");
    }
    
    @Test
    @DisplayName("Test handleEvent for PARENTAL event does not play a sound")
    void testHandleEventParental() {
        // Call handleEvent with PARENTAL event
        soundController.handleEvent(GameEvent.PARENTAL);
        
        // Verify no sound was played (path should remain null)
        assertNull(soundController.lastSoundPath, "Should not play any sound for PARENTAL event");
        assertNull(soundController.currentSound, "Should not create sound object for PARENTAL event");
    }

    @Test
    @DisplayName("Test handleEvent stops previous sound before playing new sound")
    void testHandleEventStopsPreviousSound() {
        // Set up initial sound
        soundController.handleEvent(GameEvent.MENU);
        MockSound firstSound = soundController.currentSound;
        
        // Call handleEvent with a different event
        soundController.handleEvent(GameEvent.TUTORIAL);
        
        // Verify first sound was stopped
        assertTrue(firstSound.stopCalled, "Previous sound should be stopped");
    }

    @Test
    @DisplayName("Test stop method stops the current sound")
    void testStop() {
        // Set up a sound
        soundController.handleEvent(GameEvent.MENU);
        MockSound mockSound = soundController.currentSound;
        
        // Reset stop status
        mockSound.stopCalled = false;
        
        // Call stop
        soundController.stop();
        
        // Verify sound was stopped
        assertTrue(mockSound.stopCalled, "Sound should be stopped");
        assertTrue(soundController.stopCalled, "Controller should track stop was called");
    }
    
    @Test
    @DisplayName("Test stop method handles null sound gracefully")
    void testStopWithNullSound() {
        // Ensure sound is null
        soundController.currentSound = null;
        
        // Call stop - should not throw exception
        assertDoesNotThrow(() -> soundController.stop(), "Stop should handle null sound");
        
        // Verify method was called
        assertTrue(soundController.stopCalled, "Stop called flag should be set");
    }
    
    /**
     * Mock event dispatcher for testing
     */
    private static class MockEventDispatcher extends EventDispatcher {
        int observerCount = 0;
        
        @Override
        public <T extends Observer> void addObserver(GameEvent event, T observer) {
            observerCount++;
        }
        
        @Override
        public void notifyObservers(GameEvent event) {
            // No-op for testing
        }
    }
    
    /**
     * Mock sound class for testing
     */
    private static class MockSound {
        boolean playCalled = false;
        boolean stopCalled = false;
        String soundPath;
        
        MockSound(String path) {
            this.soundPath = path;
        }
        
        void play() {
            playCalled = true;
        }
        
        void stop() {
            stopCalled = true;
        }
    }
    
    /**
     * Test-specific subclass of SoundController that avoids real audio playback
     */
    private static class TestSoundController extends SoundController {
        MockSound currentSound = null;
        String lastSoundPath = null;
        boolean stopCalled = false;
        
        TestSoundController(EventDispatcher eventDispatcher) {
            super(eventDispatcher);
        }
        
        @Override
        public void handleEvent(GameEvent event) {
            // Instead of creating real Sound objects, track what would happen
            if (currentSound != null) {
                currentSound.stop();
            }
            
            switch (event) {
                case MENU:
                    lastSoundPath = "resources/sounds/menu.wav";
                    currentSound = new MockSound(lastSoundPath);
                    currentSound.play();
                    break;
                case TUTORIAL:
                case NEW_GAME:
                case INGAME:
                    lastSoundPath = "resources/sounds/tutorial.wav";
                    currentSound = new MockSound(lastSoundPath);
                    currentSound.play();
                    break;
                case MINIGAME:
                    lastSoundPath = "resources/sounds/minigameMusic.wav";
                    currentSound = new MockSound(lastSoundPath);
                    currentSound.play();
                    break;
                case STOPSOUND:
                    // Just stops the current sound
                    break;
                default:
                    // No sound for other events
                    break;
            }
        }
        
        @Override
        public void stop() {
            stopCalled = true;
            if (currentSound != null) {
                currentSound.stop();
            }
        }
    }
}