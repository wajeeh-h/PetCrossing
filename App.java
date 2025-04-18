import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * The main class responsible for running Pet Crossing: New Horizons. 
 * <br><br>
 * This class is responsible for initializing the game, switching screens in response
 * to certain events, and managing the integration of different game elements.
 * 
 * <b>Example Use:</b>
 * <pre>
 * {@code
 *   App app = new App();
 *   app.start();
 * }
 * </pre>
 * 
 * @see Observer
 * @see EventDispatcher
 * @see Screen
 * @see Controller
 * @see Panel
 * @see SaveManager
 * @see GameController
 * @see SoundController
 * @see ParentalController
 * @see TutorialPanel
 * @see NewGamePanel
 * @see ParentalPanel
 * @see MenuPanel
 * @see GameplayPanel
 * @see LoadGamePanel
 * @see CatchGamePanel
 * @see VetPanel 
 * @see Inventory
 * @see Pet
 */
public class App extends Observer {
    Logger LOGGER = Logger.getLogger(App.class.getName());

    /** A list of observers to notify when an events occurs */
    private ArrayList<Observer> observers;
    /** The title of the game */
    private final String title = "Pet Crossing: New Horizons";
    /** The screen that displays the game */
    private Screen screen;
    /** The event dispatcher that notifies observers of events in the game */
    private EventDispatcher eventDispatcher;
    /** The game controller that manages the game logic */
    private GameController gameController;
    /** The sound controller that manages the sound effects and music */
    private SoundController soundController;
    /** The parental controller that manages parental controls */
    private ParentalController parentalController;
    /** The panel used for the tutorial screen */
    private TutorialPanel tutorialPanel;
    /** The panel used for the new game screen */
    private NewGamePanel newGamePanel;
    /** The panel used for the parental controls screen */
    private ParentalPanel parentalPanel;
    /** The panel used for the menu screen */
    private MenuPanel menuPanel;
    /** The save manager used for saving and loading game data */
    private SaveManager saveManager;
    /** If the user is reviving a pet */
    private boolean reviveOverride = false;
    /** If the user is overriding an existing save */
    private boolean override = false;
    /** The type of pet to override the save with */
    private String overridePet = "Chopper";
    /** The name of the pet to override the save with */
    private String overrideName = "";

    /**
     * Constructor for the App class.
     * <br><br>
     * Iniitializes the App class and sets up the observers list.
     */
    public App() {
        super();
        this.observers = new ArrayList<Observer>();
        LOGGER.fine("App initialized");
    }

    /**
     * Starts the game by initializing class variables.
     * <br><br>
     * Sets the look and feel of the UI, initializes the event dispatcher,
     * creates the game controller, sound controller, parental controller,
     * and different panels. Also, registers the observers for events.
     * and sets the initial screen to the menu panel.
     */
    public void start() {
        screen = new Screen(title);
        LOGGER.fine("Screen initialized");
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            // If the look and feel cannot be set there is a problem with the java installation
            terminate(); 
        }
        // Initialize attributes
        saveManager = new SaveManager();
        eventDispatcher = new EventDispatcher();

        gameController = null;
        soundController = new SoundController(eventDispatcher);

        parentalController = new ParentalController(eventDispatcher);
        parentalPanel = new ParentalPanel(eventDispatcher, parentalController);
        parentalController.setPanel(parentalPanel);

        tutorialPanel = new TutorialPanel(eventDispatcher);
        newGamePanel = new NewGamePanel(eventDispatcher);
        menuPanel = new MenuPanel(eventDispatcher);

        // Add observers to the list
        observers.add(soundController);
        observers.add(parentalController);
        observers.add(this);
        // Register the events for each observer
        for (Observer observer : observers) {
            observer.registerEvents();
        }
        // Notify observers that the game is loading the menu
        eventDispatcher.notifyObservers(GameEvent.MENU);
    }

    /**
     * Terminates the game.
     * <br><br>
     * Saves the game if there is a game controller, stops the sound controller,
     * and exits the program.
     */
    public void terminate() {
        if (gameController != null) {
            saveManager.saveGame(gameController.getPet(), gameController.getInventory(), gameController.getSaveSlot(), gameController.getScore());
        }
        for (Observer observer : observers) {
            if (observer instanceof SoundController) {
                ((SoundController) observer).stop();
            }
        }
        System.exit(0);
    }

    @Override
    protected void handleEvent(GameEvent event) {
        LOGGER.fine("Handling event: " + event);
        switch (event) {
            case INGAME:
                screen.setPanel(gameController.getPanel());
                break;
            case NEW_GAME:
                screen.setPanel(newGamePanel);
                break;
            case LOAD_GAME:
                screen.setPanel(new LoadGamePanel(eventDispatcher));
                break;
            case LOAD1:
                setSave(1);
                break;
            case LOAD2:
                setSave(2);
                break;
            case LOAD3:
                setSave(3);
                break;
            case CHOPPER:
                overrideSaveHelper("Chopper");
                break;
            case LABOON:
                overrideSaveHelper("Laboon");
                break;
            case DUGONG:
                overrideSaveHelper("Dugong");
                break;
            case MENU:
                screen.setPanel(menuPanel);
                if (gameController != null) {
                    saveManager.saveGame(gameController.getPet(), gameController.getInventory(),
                            gameController.getSaveSlot(), gameController.getScore());
                }
                break;
            case TUTORIAL:
                screen.setPanel(tutorialPanel);
                break;
            case MINIGAME:
                screen.setPanel(new CatchGamePanel(eventDispatcher));
                break;
            case PARENTAL:
                screen.setPanel(parentalPanel);
                break;
            case QUIT:
                terminate();
                break;
            case VET:
                screen.setPanel(new VetPanel(eventDispatcher, gameController.getPet()));
                break;
            case REVIVE:
                reviveOverride = true;
                LoadGamePanel loadGamePanel = new LoadGamePanel(eventDispatcher, true);
                loadGamePanel.overrideButtonText("Revive");
                screen.setPanel(loadGamePanel);
                break;
            case FATALERROR:
                terminate();
                break;
            default:
                break;
        }
        screen.refreshPanel();
    }

    /**
     * Helper method which sets the override flags
     * <br><br>
     * Sets the override flags which indicate that we are overriding an old save 
     * (or empty save) with a new one. 
     * 
     * @param type the type of pet to override the save with
     */
    private void overrideSaveHelper(String type) {
        override = true;
        overridePet = type;
        overrideName = newGamePanel.setName();
        LoadGamePanel loadGamePanel = new LoadGamePanel(eventDispatcher);
        loadGamePanel.overrideButtonText("Override Save");
        screen.setPanel(loadGamePanel);
    }

    /**
     * Sets the save slot for the game.
     * <br><br>
     * This method loads the game data from the specified save slot and initializes
     * the game controller with the loaded data. If the override flag is set,
     * it creates a new pet with the specified name and type, and saves the new game data
     * into the specified save slot. If the revive flag is set, it loads the dead pet's save
     * file and resets its stats to 100, then saves that data.
     * 
     * @param saveSlot
     */
    private void setSave(int saveSlot) {
        // If the user is overriding a save, create a new pet with the specified name and type
        if (override) {
            Pet pet = new Pet(overrideName, overridePet, 100, 100, 100, 100);
            Inventory inventory = new Inventory();
            // Initialize the game controller 
            gameController = new GameController(eventDispatcher, new GameplayPanel(eventDispatcher), inventory,
                    saveSlot, pet, 0);
            gameController.registerEvents();
            // Notify observers that we are now in game
            eventDispatcher.notifyObservers(GameEvent.INGAME);
            ((GameplayPanel) (gameController.getPanel())).init(pet);
            screen.setPanel(gameController.getPanel());
            override = false;
            // Save the new game data into the specified save slot
            saveManager.saveGame(pet, inventory, saveSlot, 0);
            return;
        }
        // Load the save data from a save file
        Tuple<Pet, Tuple<Inventory, Integer>> saveData = saveManager.loadGame(saveSlot);
        Pet pet = saveData.x;
        Inventory inventory = saveData.y.x;
        int score = saveData.y.y;
        // Reinitialize the game controller with the loaded data
        gameController = new GameController(eventDispatcher, new GameplayPanel(eventDispatcher), inventory, saveSlot, pet, score);
        gameController.registerEvents();
        // Notify observers that we are now in game
        eventDispatcher.notifyObservers(GameEvent.INGAME);
        if (pet.getState() == PetState.DEAD && reviveOverride) {
            pet.setHappiness(100);
            pet.setHealth(100);
            pet.setHunger(100);
            pet.setSleep(100);
            pet.setState(PetState.NORMAL);
            reviveOverride = false;
        }
        ((GameplayPanel) (gameController.getPanel())).init(pet);
        ((GameplayPanel) (gameController.getPanel())).updateScore(score);
        screen.setPanel(gameController.getPanel());
    }

    @Override
    protected void registerEvents() {
        eventDispatcher.addObserver(GameEvent.VET, this);
        eventDispatcher.addObserver(GameEvent.LOAD1, this);
        eventDispatcher.addObserver(GameEvent.LOAD2, this);
        eventDispatcher.addObserver(GameEvent.LOAD3, this);
        eventDispatcher.addObserver(GameEvent.CHOPPER, this);
        eventDispatcher.addObserver(GameEvent.LABOON, this);
        eventDispatcher.addObserver(GameEvent.DUGONG, this);
        eventDispatcher.addObserver(GameEvent.QUIT, this);
        eventDispatcher.addObserver(GameEvent.MENU, this);
        eventDispatcher.addObserver(GameEvent.TUTORIAL, this);
        eventDispatcher.addObserver(GameEvent.MINIGAME, this);
        eventDispatcher.addObserver(GameEvent.PARENTAL, this);
        eventDispatcher.addObserver(GameEvent.INGAME, this);
        eventDispatcher.addObserver(GameEvent.NEW_GAME, this);
        eventDispatcher.addObserver(GameEvent.SAVE_GAME, this);
        eventDispatcher.addObserver(GameEvent.LOAD_GAME, this);
        eventDispatcher.addObserver(GameEvent.REVIVE, this);
    }
}
