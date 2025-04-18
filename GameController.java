import javax.swing.Timer;
import java.util.HashMap;
import java.util.Map;

/**
 * GameController is responsible for managing the game state and represents one game instance.
 * <br><br>
 * This class extends the Controller class and handles events related to the pet's state,
 * inventory management, and game actions. It also manages the cooldowns for various actions.   
 * For example, if the user enters the vet, the vet button will be disabled for 15 seconds.
 *
 * @see Controller
 * @see Pet
 * @see Inventory
 * @see Timer
 * @see PetState 
 */
public class GameController extends Controller {
    /** The pet associated with this game instance */
    private Pet pet;
    /** The inventory associated with this game instance */
    private Inventory inventory;
    /** The save slot for this game instance */
    private int saveSlot;
    /** The timer for updating the pet's stats */
    private Timer statTimer;
    /** The previous state of the pet */
    private PetState previousState;
    /** Track if the sleep penalty was applied (minus health when sleep hits 0) */
    private boolean sleepPenaltyApplied = false; // Track if sleep penalty was already applied
    /** Tracks if the anger penalty was applied (cannot do certain actions until 50% happiness, if angry) */
    private boolean angerPenalty = false;
    /** Tracks the score */
    private int score = 0;
    /** The cooldown times for each action */
    private Map<String, Long> cooldowns = new HashMap<>();
    /** The cooldown time for the vet */
    private static final int VET_COOLDOWN = 15000; 
    /** The cooldown time for the walk (exercise) */
    private static final int WALK_COOLDOWN = 10000; 
    /** The cooldown time for the play action */
    private static final int PLAY_COOLDOWN = 8000; 

    /**
     * Constructor for the GameController class.
     * <br><br>
     * Creates an instance of the GameController class with the specified event dispatcher,
     * panel, inventory, save slot, pet, and score.
     * 
     * @param eventDispatcher The event dispatcher to handle events
     * @param panel The gameplay panel associated with this controller
     * @param inventory The inventory associated with this game instance
     * @param saveSlot The save slot for this game instance
     * @param pet The pet associated with this game instance
     * @param score The score for this game instance
     */
    public GameController(EventDispatcher eventDispatcher, GameplayPanel panel, Inventory inventory, int saveSlot,
            Pet pet, int score) {
        super(eventDispatcher, panel);
        this.inventory = inventory;
        this.saveSlot = saveSlot;
        this.pet = pet;
        this.score = score;
        // Cooldowns start at 0
        cooldowns.put("vet", 0L);
        cooldowns.put("walk", 0L);
        cooldowns.put("play", 0L);
        init();
    }

    /**
     * Private helper method which takes care of some initialization tasks.
     * <br><br>
     * This method creates the timer which periodically updates the pet's stats,
     * the inventory, the score, and tracks the cooldowns for actions. 
     */
    private void init() {
        ((GameplayPanel) this.getPanel()).updateInventory(inventory);
        previousState = pet.getState();
        // Create the timer
        statTimer = new Timer(1000 / 60, e -> {
            // Update the pet's stats 60 times per second
            pet.setHunger(pet.getHunger() - pet.getHungerRate());
            pet.setHappiness(pet.getHappiness() - pet.getHappinessRate());

            // If the pet is sleeping, restore is sleep value, if the pet is not sleeping, decay its sleep value
            if (pet.getState() != PetState.SLEEPING) {
                pet.setSleep(pet.getSleep() - pet.getSleepRate()); // Slightly adjusted sleep decay rate
            } else {
                pet.setSleep(pet.getSleep() + pet.getSleepRate() / 2);
            }

            // If the pet's sleep reaches 0, apply a penalty and subtract the pets health by some amount
            if (pet.getSleep() <= 0.5 && !sleepPenaltyApplied) {
                pet.setHealth(pet.getHealth() - 20); // 
                sleepPenaltyApplied = true;
            } else if (pet.getSleep() > 0) {
                // Reset the penalty once sleep is retored
                sleepPenaltyApplied = false;
            }

            // If the pets hunger is 0, subtract health and score
            if (pet.getHunger() <= 0) {
                pet.setHealth(pet.getHealth() - 0.05);
                score = Math.max(0, score - 1); 
            }
            updateButtonCooldowns();

            ((GameplayPanel) this.getPanel()).updateButtonCooldowns(pet);
            // If the anger penalty is active, keep the pet angry as long as its happiness is below 50%
            if (angerPenalty && pet.getHappiness() <= 50) {
                pet.setState(PetState.ANGRY);
            }
            // If the pet is angry then apply the anger penalty
            else if (pet.getState() == PetState.ANGRY && !angerPenalty) {
                angerPenalty = true; 
            } 
            // Otherwise update the pet's state
            else {
                pet.updateState();
            }
            // Update other indicators
            ((GameplayPanel) this.getPanel()).updateStatusBars(pet);
            ((GameplayPanel) this.getPanel()).updateScore(score);
            // Change the pet's sprite if its state has changed
            if (pet.getState() != previousState) {
                ((GameplayPanel) this.getPanel()).updatePetSprite(pet.getState());
                previousState = pet.getState();
            }
        });
    }

    public int getScore() {
        return score;
    }

    /**
     * Updates the cooldowns for the actions.
     * <br><br>
     * This method checks if the cooldowns for the actions (vet, walk, play) have expired.
     * If the cooldown has expired, it allows the action to be performed again. 
    */
    private void updateButtonCooldowns() {
        long currentTime = System.currentTimeMillis();
        GameplayPanel panel = (GameplayPanel) this.getPanel();

        String[] actions = { "vet", "walk", "play" };
        for (String action : actions) {
            long cooldownEnd = cooldowns.get(action);
            boolean isReady = currentTime >= cooldownEnd;
            // If an action is still on cooldown, skip it
            if (!isReady)
                continue;
            // Otherwise, allow it to be performed again
            if (action.equals("vet")) {
                panel.allowVet = true;
            } else if (action.equals("walk")) {
                panel.allowWalk = true;
            } else if (action.equals("play")) {
                panel.allowPlay = true;
            }
            ((GameplayPanel) this.getPanel()).updateButtonCooldowns(pet);
        }
    }

    /**
     * Checks if the action is on cooldown.
     * 
     * @param action The action to check cooldown for (vet, walk, play)
     * @return true if the action is on cooldown, false otherwise
     */
    private boolean isOnCooldown(String action) {
        return System.currentTimeMillis() < cooldowns.get(action);
    }

    /**
     * Starts the cooldown for the specified action.
     * 
     * @param action The action to start cooldown for (vet, walk, play)
     */
    private void startCooldown(String action) {
        int cooldownTime = 0;
        switch (action) {
            // Do not allow an action to occur if it is on cooldown
            case "vet":
                cooldownTime = VET_COOLDOWN;
                ((GameplayPanel) this.getPanel()).allowVet = false;
                break;
            case "walk":
                cooldownTime = WALK_COOLDOWN;
                ((GameplayPanel) this.getPanel()).allowWalk = false;
                break;
            case "play":
                cooldownTime = PLAY_COOLDOWN;
                ((GameplayPanel) this.getPanel()).allowPlay = false;
                break;
        }

        cooldowns.put(action, System.currentTimeMillis() + cooldownTime);
        ((GameplayPanel) this.getPanel()).updateButtonCooldowns(pet);
    }

    /**
     * Starts the stat timer.
     */
    public void start() {
        statTimer.start();
    }

    /**
     * Pauses the stat timer.
     * <br><br>
     * When we leave the game (e.g. go to the minigame or main menu) we want to pause
     * the stat decay timers.
     */
    public void pause() {
        statTimer.stop();
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void setSaveSlot(int saveSlot) {
        this.saveSlot = saveSlot;
    }

    public int getSaveSlot() {
        return saveSlot;
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    protected void handleEvent(GameEvent event) {
        switch (event) {
            case FEED1:
                // Do not allow the user to feed the pet an apple if they do not have any
                if (inventory.getCount(Item.APPLE) == 0) {
                    return;
                }
                // If the user uses an apple, give the pet 10 hunger
                pet.setHunger(pet.getHunger() + 10);
                inventory.removeItem(Item.APPLE);
                // Remove one apple from the inventory
                ((GameplayPanel) this.getPanel()).updateInventory(inventory);
                score += 10;
                break;
            case FEED2:
                if (inventory.getCount(Item.BANANA) == 0) {
                    return;
                }
                // If the user uses a banana, give the pet 20 hunger
                pet.setHunger(pet.getHunger() + 20);
                inventory.removeItem(Item.BANANA);
                ((GameplayPanel) this.getPanel()).updateInventory(inventory);
                score += 20;
                break;
            case GIFT1:
                if (inventory.getCount(Item.PURPLEGIFT) == 0) {
                    return;
                }
                // If the user uses a purple gift, give the pet 5 happiness
                pet.setHappiness(pet.getHappiness() + 5);
                inventory.removeItem(Item.PURPLEGIFT);
                ((GameplayPanel) this.getPanel()).updateInventory(inventory);
                score += 5;
                break;
            case GIFT2:
                if (inventory.getCount(Item.GREENGIFT) == 0) {
                    return;
                }
                // If the user uses a green gift, give the pet 15 happiness
                pet.setHappiness(pet.getHappiness() + 15);
                inventory.removeItem(Item.GREENGIFT);
                ((GameplayPanel) this.getPanel()).updateInventory(inventory);
                score += 15;
                break;
            case PLAY:
                // If the user is allowed to play, then allow the user to play
                if (isOnCooldown("play"))
                    break;
                // Playing increases happiness by 10
                pet.setHappiness(pet.getHappiness() + 10);
                startCooldown("play");
                score += 10;
                break;
            case WALK:
                // If the user is allowed to walk, then allow the user to walk
                if (isOnCooldown("walk"))
                    break;
                // Walking increases happiness by 5
                pet.setHappiness(pet.getHappiness() + 5);
                startCooldown("walk");
                score += 5;
                break;
            case VET:
                // If vet is allowed, then allow the user to go to the vet
                if (isOnCooldown("vet") || score < 5)
                    break;
                score -= 5; // Visiting the vet costs 5 points
                startCooldown("vet");
                break;
            case SLEEP:
                pet.setState(PetState.SLEEPING);
                break;
            case HEAL:
                // Healing increments health by 10
                pet.setHealth(pet.getHealth() + 10);
                score += 10;
                break;
            case MINIGAME:
            case MENU:
                pause();
                break;
            case INGAME:
                start();
                break;
            case LEAVEMINIGAME:
                // Upon leaving the minigame, give the user one of each item
                inventory.addItem(Item.APPLE);
                inventory.addItem(Item.BANANA);
                inventory.addItem(Item.PURPLEGIFT);
                inventory.addItem(Item.GREENGIFT);
                ((GameplayPanel) this.getPanel()).updateInventory(inventory);
                break;
            default:
                break;
        }
    }

    @Override
    protected void registerEvents() {
        eventDispatcher.addObserver(GameEvent.SAVE_GAME, this);
        eventDispatcher.addObserver(GameEvent.LOAD_GAME, this);
        eventDispatcher.addObserver(GameEvent.NEW_GAME, this);
        eventDispatcher.addObserver(GameEvent.EXIT_GAME, this);
        eventDispatcher.addObserver(GameEvent.MENU, this);
        eventDispatcher.addObserver(GameEvent.QUIT, this);
        eventDispatcher.addObserver(GameEvent.TUTORIAL, this);
        eventDispatcher.addObserver(GameEvent.INGAME, this);
        eventDispatcher.addObserver(GameEvent.MINIGAME, this);
        eventDispatcher.addObserver(GameEvent.PARENTAL, this);
        eventDispatcher.addObserver(GameEvent.VET, this);
        eventDispatcher.addObserver(GameEvent.FEED1, this);
        eventDispatcher.addObserver(GameEvent.WALK, this);
        eventDispatcher.addObserver(GameEvent.GIFT1, this);
        eventDispatcher.addObserver(GameEvent.GIFT2, this);
        eventDispatcher.addObserver(GameEvent.FEED2, this);
        eventDispatcher.addObserver(GameEvent.PLAY, this);
        eventDispatcher.addObserver(GameEvent.HEAL, this);
        eventDispatcher.addObserver(GameEvent.SLEEP, this);
        eventDispatcher.addObserver(GameEvent.LEAVEMINIGAME, this);
    }
}