import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * SoundController class to manage sound effects in the game.
 * <br><br>
 * This class is responsible for playing different sound effects based on the game state.
 * It uses the Sound class to handle audio playback. The sound is played in a loop until stopped.
 * <br><br>
 * <b>Example Use:</b>
 * <pre>
 * {@code
 *   SoundController soundController = new SoundController(eventDispatcher);
 *   soundController.handleEvent(GameEvent.MENU);
 *   soundController.stop();
 * }
 * 
 * @see Sound
 * @see GameEvent
 * @see Observer
 * @see EventDispatcher
 */
public class SoundController extends Observer {

    /** The sound object used for playback */
    private Sound sound;
    /** The path to the menu sound file */
    private static final String menuSoundPath = "resources/sounds/menu.wav";
    /** The path to the tutorial sound file */
    private static final String tutorialSoundPath = "resources/sounds/tutorial.wav";
    /** The path to the minigame sound file */
    private static final String minigameSoundPath = "resources/sounds/minigameMusic.wav";

    /**
     * Constructor for the SoundController class.
     * <br><br>
     * Initializes the sound controller with the specified event dispatcher.
     * @param eventDispatcher The event dispatcher to handle game events.
     */
    SoundController(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
    }

    @Override
    public void registerEvents() {
        eventDispatcher.addObserver(GameEvent.MENU, this);
        eventDispatcher.addObserver(GameEvent.TUTORIAL, this);
        eventDispatcher.addObserver(GameEvent.MINIGAME, this);
        eventDispatcher.addObserver(GameEvent.PARENTAL, this);
        eventDispatcher.addObserver(GameEvent.INGAME, this);
        eventDispatcher.addObserver(GameEvent.NEW_GAME, this);
        eventDispatcher.addObserver(GameEvent.STOPSOUND, this);
    }

    /**
     * Handles game events and plays corresponding sound effects.
     * 
     * @param event The game event to handle.
     */
    @Override
    public void handleEvent(GameEvent event) {

        if (sound != null) {
            sound.stop();
        }
        try {
            switch (event) {
                case MENU:
                    sound = new Sound(menuSoundPath);
                    sound.play();
                    break;
                case TUTORIAL:
                    sound = new Sound(tutorialSoundPath);
                    sound.play();
                    break;
                case NEW_GAME:
                case INGAME:
                    sound = new Sound(tutorialSoundPath);
                    sound.play();
                    break;
                case MINIGAME:
                    sound = new Sound(minigameSoundPath);
                    sound.play();
                    break;
                default:
                    break;
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the currently playing sound.
     */
    public void stop() {
        sound.stop();
    }
}
