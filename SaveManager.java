import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles saving and loading game data to and from JSON files.
 * <br><br>
 * This class provides methods to save the current state of the game, including
 * the pet's attributes and inventory, to a JSON file. It also allows loading the game
 * state from a JSON file, restoring the pet's attributes and inventory.
 * <br><br>
 * 
 * <b>Example Use:</b>
 * <pre>
 * {@code
 *    SaveManager saveManager = new SaveManager();
 *    Pet pet = new Pet("Chopper", "Dog", 100, 100, 100, 100);
 *    Inventory inventory = new Inventory(5, 10, 2, 3);
 *    saveManager.saveGame(pet, inventory, 1, 1000);
 * }
 * </pre>
 * 
 * @see Pet
 * @see Inventory
 * @see PetState
 * @see Item
 * @see Tuple
 */
public class SaveManager {

    /**
     * Default constructor for SaveManager.
     * <br><br>
     * Initializes a new instance of the SaveManager class. 
     * Does not have attributes or methods to initialize.
     */
    public SaveManager() {}

    /**
     * Loads the game state from a specified save slot.
     * <br><br>
     * This method reads the JSON file corresponding to the specified save slot,
     * and restores the pet's attributes and inventory. If the file does not exist
     * or is invalid, it returns tuples null values.
     * 
     * @param slot The save slot number (1, 2, or 3) to load from.
     * @return A Tuple containing the Pet object and a Tuple of Inventory and score.
     */
    public Tuple<Pet, Tuple<Inventory, Integer>> loadGame(int slot) {
        // Variables for Pet and Inventory objects and the game score
        Pet pet = null;
        int apples = 0;
        int bananas = 0;
        int purpleGifts = 0;
        int greenGifts = 0;
        int score = 0;

        Tuple<Pet, Tuple<Inventory, Integer>> saveData;
        try {
            File file = new File("saves\\save" + slot + ".json");
            ObjectMapper objectMapper = new ObjectMapper();

            // Read each attribute from the JSON file
            JsonNode rootNode = objectMapper.readTree(file);

            apples = rootNode.path("apples").asInt();
            bananas = rootNode.path("bananas").asInt();
            purpleGifts = rootNode.path("purplegifts").asInt();
            greenGifts = rootNode.path("greengifts").asInt();

            String name = rootNode.path("name").asText();
            String type = rootNode.path("type").asText();

            int health = rootNode.path("health").asInt();
            int hunger = rootNode.path("hunger").asInt();
            int happiness = rootNode.path("happiness").asInt();
            int sleep = rootNode.path("sleep").asInt();
            String state = rootNode.path("state").asText();
            score = rootNode.path("score").asInt();

            PetState petState = PetState.valueOf(state.toUpperCase());
            // If the state is not found, set it to NORMAL
            if (petState == null) {
                petState = PetState.NORMAL;
            }

            pet = new Pet(name, type, health, hunger, happiness, sleep);
            pet.setState(PetState.valueOf(state.toUpperCase()));
            saveData = new Tuple<>(pet, new Tuple<>(new Inventory(apples, bananas, purpleGifts, greenGifts), Integer.valueOf(score)));  
        } catch (Exception e) {
            // If an error occurs, return null values
            saveData = new Tuple<>(null, new Tuple<>(null, null));
        }
        return saveData;
    }

    /**
     * Saves the current game state to a specified save slot.
     * <br><br>
     * This method writes the pet's attributes, an inventory and a score to a JSON file
     * corresponding to the specified save slot. If the file cannot be created or written to,
     * it returns false.
     * 
     * @param pet The Pet object.
     * @param inventory The Inventory object.
     * @param saveSlot The save slot number (1, 2, or 3) to save to.
     * @param score The current score of the game.
     * @return true if the game was saved successfully, false otherwise.
     */
    public boolean saveGame(Pet pet, Inventory inventory, int saveSlot, int score) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File("saves\\save" + saveSlot + ".json");

            JsonNode petNode = objectMapper.createObjectNode()
                    .put("apples", inventory.getCount(Item.APPLE))
                    .put("bananas", inventory.getCount(Item.BANANA))
                    .put("purplegifts", inventory.getCount(Item.PURPLEGIFT))
                    .put("greengifts", inventory.getCount(Item.GREENGIFT))
                    .put("name", pet.getName())
                    .put("type", pet.getType())
                    .put("state", pet.getState().toString().toLowerCase())
                    .put("health", pet.getHealth())
                    .put("hunger", pet.getHunger())
                    .put("happiness", pet.getHappiness())
                    .put("sleep", pet.getSleep())
                    .put("score", score);

            objectMapper.writeValue(file, petNode);
        } catch (Exception e) {
            return false; // could not save the game
        }
        return true;
    }
}
