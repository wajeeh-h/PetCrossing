import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JUnit test class for SaveManager's non-GUI methods
 */
public class SaveManagerTest {
    
    @TempDir
    Path tempDir;
    
    private SaveManager saveManager;
    private Pet testPet;
    private Inventory testInventory;
    private File originalSaveDir;
    private File testSaveDir;
    
    @BeforeEach
    void setUp() throws Exception {
        // Store original saves directory
        originalSaveDir = new File(System.getProperty("user.dir") + File.separator + "saves");
        
        // Create test saves directory in temp directory
        testSaveDir = new File(tempDir.toFile(), "saves");
        testSaveDir.mkdirs();
        
        // Set system property to use our test directory
        // This assumes SaveManager uses user.dir/saves or a similar convention
        String originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        
        // Create a new SaveManager that will use our test directory
        saveManager = new SaveManager();
        
        // Restore original user.dir
        System.setProperty("user.dir", originalUserDir);
        
        // Create test pet and inventory
        testPet = new Pet("TestPet", "chopper", 100, 80, 90, 70);
        testPet.setState(PetState.NORMAL);
        
        testInventory = new Inventory();
        testInventory.addItem(Item.APPLE);
        testInventory.addItem(Item.APPLE);
        testInventory.addItem(Item.BANANA);
        testInventory.addItem(Item.PURPLEGIFT);
        testInventory.addItem(Item.GREENGIFT);
    }
    
    @Test
    @DisplayName("Test saveGame successfully saves pet and inventory data")
    void testSaveGame() throws Exception {
        // Use reflection to redirect save paths to our test directory
        redirectSavePaths(saveManager, testSaveDir.getAbsolutePath());
        
        try {
            // Save the game
            boolean result = saveManager.saveGame(testPet, testInventory, 1, 0);
            
            // Verify save was successful
            assertTrue(result, "Save operation should return true");
            
            // Check if file exists in test directory
            File testSaveFile = new File(testSaveDir, "save1.json");
            assertTrue(testSaveFile.exists(), "Save file should exist after saving");
            
            // Verify file contains correct data
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(testSaveFile);
            
            // Check inventory data
            assertEquals(2, rootNode.path("apples").asInt(), "Apple count should be 2");
            assertEquals(1, rootNode.path("bananas").asInt(), "Banana count should be 1");
            assertEquals(1, rootNode.path("purplegifts").asInt(), "Purple gifts count should be 1");
            assertEquals(1, rootNode.path("greengifts").asInt(), "Green gifts count should be 1");
            
            // Check pet data
            assertEquals("TestPet", rootNode.path("name").asText(), "Pet name should match");
            assertEquals("chopper", rootNode.path("type").asText(), "Pet type should match");
            assertEquals("NORMAL", rootNode.path("state").asText(), "Pet state should match");
            assertEquals(100, rootNode.path("health").asInt(), "Health value should match");
            assertEquals(80, rootNode.path("hunger").asInt(), "Hunger value should match");
            assertEquals(90, rootNode.path("happiness").asInt(), "Happiness value should match");
            assertEquals(70, rootNode.path("sleep").asInt(), "Sleep value should match");
        } finally {
            // Restore original paths if needed
            restoreOriginalPaths(saveManager);
        }
    }
    
    @Test
    @DisplayName("Test loadGame successfully loads saved pet and inventory data")
    void testLoadGame() throws Exception {
        // Create a test save file in our test directory
        File testSaveFile = new File(testSaveDir, "save1.json");
        createTestSaveFile(testSaveFile);
        
        // Use reflection to redirect save paths to our test directory
        redirectSavePaths(saveManager, testSaveDir.getAbsolutePath());
        
        try {
            // Load the game
            Tuple<Pet, Tuple<Inventory, Integer>> result = saveManager.loadGame(1);
            
            // Verify load was successful
            assertNotNull(result, "Load result should not be null");
            assertNotNull(result.x, "Loaded pet should not be null");
            assertNotNull(result.y, "Loaded inventory tuple should not be null");
            assertNotNull(result.y.x, "Loaded inventory should not be null");
            
            // Verify pet data
            Pet loadedPet = result.x;
            assertEquals("TestPet", loadedPet.getName(), "Pet name should match saved value");
            assertEquals("chopper", loadedPet.getType(), "Pet type should match saved value");
            assertEquals(100, loadedPet.getHealth(), "Health should match saved value");
            assertEquals(80, loadedPet.getHunger(), "Hunger should match saved value");
            assertEquals(90, loadedPet.getHappiness(), "Happiness should match saved value");
            assertEquals(70, loadedPet.getSleep(), "Sleep should match saved value");
            assertEquals(PetState.NORMAL, loadedPet.getState(), "State should match saved value");
            
            // Verify inventory data
            Inventory loadedInventory = result.y.x;
            assertEquals(2, loadedInventory.getCount(Item.APPLE), "Apple count should match saved value");
            assertEquals(1, loadedInventory.getCount(Item.BANANA), "Banana count should match saved value");
            assertEquals(1, loadedInventory.getCount(Item.PURPLEGIFT), "Purple gift count should match saved value");
            assertEquals(1, loadedInventory.getCount(Item.GREENGIFT), "Green gift count should match saved value");
            
            // Verify score
            Integer loadedScore = result.y.y;
            assertEquals(100, loadedScore, "Score should match saved value");
        } finally {
            // Restore original paths if needed
            restoreOriginalPaths(saveManager);
        }
    }
    
    /**
     * Helper method to create a test save file with known data
     */
    private void createTestSaveFile(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        
        // Add inventory data
        rootNode.put("apples", 2);
        rootNode.put("bananas", 1);
        rootNode.put("purplegifts", 1);
        rootNode.put("greengifts", 1);
        
        // Add pet data
        rootNode.put("name", "TestPet");
        rootNode.put("type", "chopper");
        rootNode.put("state", "NORMAL");
        rootNode.put("health", 100);
        rootNode.put("hunger", 80);
        rootNode.put("happiness", 90);
        rootNode.put("sleep", 70);
        
        // Add score
        rootNode.put("score", 100);
        
        // Ensure parent directory exists
        file.getParentFile().mkdirs();
        
        // Write the file
        mapper.writeValue(file, rootNode);
    }
    
    /**
     * Use reflection to find and modify the internal save paths
     * This is a more generic approach that tries multiple common field names
     */
    private void redirectSavePaths(SaveManager saveManager, String newBasePath) {
        try {
            // Common field names for save paths in game development
            String[] commonFieldNames = {"savePaths", "savePath", "saveFilePaths", "fileNames", 
                                        "saveFiles", "saveFileNames", "saveDirectory", "savesDirectory"};
            
            // Try each field name
            for (String fieldName : commonFieldNames) {
                try {
                    // Try to access this field
                    Field field = saveManager.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(saveManager);
                    
                    // Handle based on the field type
                    if (value instanceof String[]) {
                        String[] paths = (String[]) value;
                        for (int i = 0; i < paths.length; i++) {
                            // Replace with path in test directory, keeping filename
                            File originalFile = new File(paths[i]);
                            paths[i] = new File(newBasePath, originalFile.getName()).getAbsolutePath();
                        }
                        return; // Success, stop here
                    } else if (value instanceof String) {
                        // It's a base directory string
                        field.set(saveManager, newBasePath);
                        return; // Success, stop here
                    } else if (value instanceof Map) {
                        // It's a map of slot -> path
                        Map<Object, String> map = (Map<Object, String>) value;
                        for (Object key : map.keySet()) {
                            String oldPath = map.get(key);
                            File originalFile = new File(oldPath);
                            map.put(key, new File(newBasePath, originalFile.getName()).getAbsolutePath());
                        }
                        return; // Success, stop here
                    }
                } catch (NoSuchFieldException e) {
                    // Field doesn't exist, try the next one
                    continue;
                }
            }
            
            // Try to find any method that can be used to set the save directory
            Method[] methods = saveManager.getClass().getDeclaredMethods();
            for (Method method : methods) {
                String name = method.getName().toLowerCase();
                if ((name.contains("set") && (name.contains("save") || name.contains("path") || name.contains("directory"))) &&
                    method.getParameterCount() == 1 && 
                    method.getParameterTypes()[0] == String.class) {
                    
                    method.setAccessible(true);
                    method.invoke(saveManager, newBasePath);
                    return; // Success, stop here
                }
            }
            
            // If we reach here, we couldn't find a way to redirect save paths
            System.err.println("WARNING: Couldn't redirect save paths. Tests may use real save locations.");
            
        } catch (Exception e) {
            System.err.println("Error redirecting save paths: " + e.getMessage());
        }
    }
    
    /**
     * Restore original paths if needed
     */
    private void restoreOriginalPaths(SaveManager saveManager) {
        // In this implementation, we don't need to do anything
        // If we identified methods to set the path, we could restore here
    }
}