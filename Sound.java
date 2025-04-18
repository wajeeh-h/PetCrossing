import java.io.IOException;
import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Sound class to handle audio playback in the game.
 * <br><br>
 * This class provides methods to play and stop sound effects. It uses the Java Sound API
 * to load and play audio files. The sound is played in a loop until stopped.
 * <br><br>
 * <b>Example Use:</b>
 * <pre>
 * {@code
 *   Sound sound = new Sound("path/to/sound.wav");
 *   sound.play();
 *   sound.stop();
 * }
 * </pre>
 */
public class Sound {
    /** The audio clip used for playback */
    private Clip clip;
    /** The audio input stream used to read the audio file */
    private AudioInputStream audioInput;

    /**
     * Constructor for the Sound class.
     * 
     * @param path The path of the audio file to be played (in WAV format).
     * @throws UnsupportedAudioFileException if the audio file is not supported (e.g. mp3)
     * @throws IOException if an I/O error occurs while reading the file (e.g. file doesn't exist)
     * @throws LineUnavailableException if an audio output error occurs
     * @throws InterruptedException if the sound thread is interrupted while playing
     * 
     * @see UnsupportedAudioFileException
     * @see IOException
     * @see LineUnavailableException
     * @see InterruptedException
     * @see AudioInputStream
     * @see Clip
     */
    public Sound(String path)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
        audioInput = AudioSystem.getAudioInputStream(new File(path));
        clip = AudioSystem.getClip();
    }

    /**
     * Plays the sound effect in a loop.
     * 
     * @throws IOException 
     * @throws LineUnavailableException if an audio output error occurs
     * @throws InterruptedException if the thread is interrupted while playing
     */
    public void play() throws IOException, LineUnavailableException, InterruptedException {
        clip.open(audioInput);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
    }

    /**
     * Stops the sound effect, if it is currently playing.
     */
    public void stop() {
        clip.stop();
    }

    /**
     * Checks if the sound is currently playing.
     * 
     * @return true if the sound is playing, false otherwise
     */
    public boolean isPlaying() {
        return clip.isActive();
    }
}
