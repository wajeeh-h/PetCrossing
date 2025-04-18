/**
 * The pet class represents a pet in the game. It contains attributes such as name, type, health hunger, etc.
 */
public class Pet {
    /** The name of the pet */
    private String name;
    /** The type of the pet */
    private String type;
    /** The state of the pet */
    private PetState state = PetState.NORMAL; 
    /** The health of the pet */
    private double health;
    /** The hunger of the pet */
    private double hunger;
    /** The happiness of the pet */
    private double happiness;
    /** The sleep of the pet */
    private double sleep;
    /** The rates at which the pet's happiness depreciates */
    private double happinessRate = 0.05;
    /** The rates at which the pet's hunger depreciates */
    private double hungerRate = 0.05;
    /** The rates at which the pet's health depreciates */
    private double sleepRate = 0.1;

    /**
     * Constructor for the Pet class.
     * 
     * @param name The name of the pet.
     * @param type The type of the pet.
     * @param health The health of the pet.
     * @param hunger The hunger of the pet.
     * @param happiness The happiness of the pet.
     * @param sleep The sleep of the pet.
     */
    public Pet(String name, String type, double health, double hunger, double happiness, double sleep) {
        this.name = name;
        this.type = type;
        this.health = health;
        this.hunger = hunger;
        this.happiness = happiness;
        this.sleep = sleep;
        setRates();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public PetState getState() {
        return state;
    }

    public double getHealth() {
        return health;
    }

    public double getHunger() {
        return hunger;
    }

    public double getHappiness() {
        return happiness;
    }

    public double getSleep() {
        return sleep;
    }

    public double getHappinessRate() {
        return happinessRate;
    }

    public double getHungerRate() {
        return hungerRate;
    }

    public double getSleepRate() {
        return sleepRate;
    }

    public void setHealth(double health) {
        this.health = Math.max(0, Math.min(100, health));
    }
    
    public void setHunger(double hunger) {
        this.hunger = Math.max(0, Math.min(100, hunger));
    }
    
    public void setHappiness(double happiness) {
        this.happiness = Math.max(0, Math.min(100, happiness));  
    }
    
    public void setSleep(double sleep) {
        this.sleep = Math.max(0, Math.min(100, sleep)); 
    }

    public void setState(PetState state) {
        this.state = state;
    }

    /**
     * Sets the rates at which the pet's happiness, hunger, and sleep depreciate based on its type.
     * <br><br>
     * The rates are set to default values if the type is not recognized. Each
     * recognized type has its own rates (e.g. chopper get sad faster than other pets).
     */
    private void setRates() {
        switch (type.toLowerCase()) {
            case "chopper":
                happinessRate = 0.1;
                hungerRate = 0.05;
                sleepRate = 0.1;
                break;
            case "dugong":
                happinessRate = 0.05;
                hungerRate = 0.05;
                sleepRate = 0.15;
                break;
            case "laboon":
                happinessRate = 0.05;
                hungerRate = 0.1;
                sleepRate = 0.1;
                break;
            default:
                happinessRate = 0.05;
                hungerRate = 0.05;
                sleepRate = 0.1;
                break;
        }
    }

    /**
     * Updates the state of the pet based on its attributes.
     * <br><br>
     * For example, if the pet's health is 0, it will be set to the DEAD state.
     */
    public void updateState() {
        if (state == PetState.SLEEPING && sleep < 100 && health > 0) {
            return;   
        }
        
        if (health <= 0) {
            state = PetState.DEAD;
        } else if (sleep <= 0.5) {
            state = PetState.SLEEPING;
        } else if (hunger <= 0) {
            state = PetState.HUNGRY;
        } else if (happiness <= 0) {
            state = PetState.ANGRY;
        } else {
            state = PetState.NORMAL;
        }
    }
}
