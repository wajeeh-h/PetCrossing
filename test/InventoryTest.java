import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumMap;

/**
 * JUnit test class for Inventory
 */
public class InventoryTest {
    
    private Inventory inventory;
    
    @BeforeEach
    void setUp() {
        inventory = new Inventory();
    }
    
    @Test
    @DisplayName("Test default constructor creates empty inventory")
    void testDefaultConstructor() {
        // Default constructor should create an empty inventory
        EnumMap<Item, Integer> items = inventory.getInventory();
        
        // Verify inventory is initialized but empty
        assertNotNull(items);
        assertTrue(items.isEmpty());
        
        // Verify initial counts of all items are 0
        assertEquals(0, inventory.getCount(Item.APPLE));
        assertEquals(0, inventory.getCount(Item.BANANA));
        assertEquals(0, inventory.getCount(Item.PURPLEGIFT));
        assertEquals(0, inventory.getCount(Item.GREENGIFT));
    }
    
    @Test
    @DisplayName("Test parameterized constructor sets correct counts")
    void testParameterizedConstructor() {
        // Create inventory with specific counts
        Inventory customInventory = new Inventory(5, 3, 2, 1);
        
        // Verify counts match constructor parameters
        assertEquals(5, customInventory.getCount(Item.APPLE));
        assertEquals(3, customInventory.getCount(Item.BANANA));
        assertEquals(2, customInventory.getCount(Item.PURPLEGIFT));
        assertEquals(1, customInventory.getCount(Item.GREENGIFT));
    }
    
    @Test
    @DisplayName("Test addItem increments count for new and existing items")
    void testAddItem() {
        // Initially all counts should be 0
        assertEquals(0, inventory.getCount(Item.APPLE));
        
        // Add an item
        inventory.addItem(Item.APPLE);
        
        // Count should now be 1
        assertEquals(1, inventory.getCount(Item.APPLE));
        
        // Add the same item again
        inventory.addItem(Item.APPLE);
        
        // Count should now be 2
        assertEquals(2, inventory.getCount(Item.APPLE));
        
        // Add a different item
        inventory.addItem(Item.BANANA);
        
        // APPLE count should still be 2, BANANA should be 1
        assertEquals(2, inventory.getCount(Item.APPLE));
        assertEquals(1, inventory.getCount(Item.BANANA));
    }
    
    @Test
    @DisplayName("Test getCount returns correct counts for items")
    void testGetCount() {
        // Add items
        inventory.addItem(Item.APPLE);
        inventory.addItem(Item.APPLE);
        inventory.addItem(Item.BANANA);
        
        // Verify counts
        assertEquals(2, inventory.getCount(Item.APPLE));
        assertEquals(1, inventory.getCount(Item.BANANA));
        assertEquals(0, inventory.getCount(Item.PURPLEGIFT));
        assertEquals(0, inventory.getCount(Item.GREENGIFT));
    }
    
    @Test
    @DisplayName("Test removeItem decrements count correctly")
    void testRemoveItem() {
        // Add items
        inventory.addItem(Item.APPLE);
        inventory.addItem(Item.APPLE);
        inventory.addItem(Item.BANANA);
        
        // Verify initial counts
        assertEquals(2, inventory.getCount(Item.APPLE));
        assertEquals(1, inventory.getCount(Item.BANANA));
        
        // Remove an APPLE
        inventory.removeItem(Item.APPLE);
        
        // APPLE count should now be 1
        assertEquals(1, inventory.getCount(Item.APPLE));
        
        // Remove another APPLE
        inventory.removeItem(Item.APPLE);
        
        // APPLE count should now be 0
        assertEquals(0, inventory.getCount(Item.APPLE));
        
        // Try to remove an APPLE when count is already 0
        inventory.removeItem(Item.APPLE);
        
        // Count should remain 0
        assertEquals(0, inventory.getCount(Item.APPLE));
        
        // Remove the BANANA
        inventory.removeItem(Item.BANANA);
        
        // BANANA count should now be 0
        assertEquals(0, inventory.getCount(Item.BANANA));
    }
    
    @Test
    @DisplayName("Test removeItem completely removes entry when count reaches 0")
    void testRemoveItemRemovesEntry() {
        // Add an item
        inventory.addItem(Item.APPLE);
        
        // Initial state should have the item
        assertTrue(inventory.getInventory().containsKey(Item.APPLE));
        
        // Remove the item
        inventory.removeItem(Item.APPLE);
        
        // The item should be completely removed from the map
        assertFalse(inventory.getInventory().containsKey(Item.APPLE));
    }
    
    @Test
    @DisplayName("Test removeItem does nothing when item doesn't exist")
    void testRemoveNonExistentItem() {
        // Initial state: empty inventory
        assertTrue(inventory.getInventory().isEmpty());
        
        // Try to remove an item that doesn't exist
        inventory.removeItem(Item.PURPLEGIFT);
        
        // Inventory should still be empty
        assertTrue(inventory.getInventory().isEmpty());
    }
    
    @Test
    @DisplayName("Test getInventory returns the actual inventory map")
    void testGetInventory() {
        // Add items to inventory
        inventory.addItem(Item.APPLE);
        inventory.addItem(Item.BANANA);
        
        // Get the inventory
        EnumMap<Item, Integer> items = inventory.getInventory();
        
        // Verify it contains the expected items
        assertEquals(2, items.size());
        assertEquals(1, items.get(Item.APPLE));
        assertEquals(1, items.get(Item.BANANA));
    }
    
    @Test
    @DisplayName("Test modification of returned inventory affects original")
    void testGetInventoryReturnsReference() {
        // Add an item
        inventory.addItem(Item.APPLE);
        
        // Get the inventory
        EnumMap<Item, Integer> items = inventory.getInventory();
        
        // Modify the returned map
        items.put(Item.BANANA, 5);
        
        // Verify change affects the original inventory
        assertEquals(5, inventory.getCount(Item.BANANA));
    }
    
    @Test
    @DisplayName("Test adding multiple different items")
    void testAddMultipleDifferentItems() {
        // Add all types of items
        for (Item item : Item.values()) {
            inventory.addItem(item);
        }
        
        // Verify all items were added
        for (Item item : Item.values()) {
            assertEquals(1, inventory.getCount(item));
        }
        
        // Verify map size matches number of enum values
        assertEquals(Item.values().length, inventory.getInventory().size());
    }
    
    @Test
    @DisplayName("Test adding many of the same item")
    void testAddManyOfSameItem() {
        // Add 100 apples
        for (int i = 0; i < 100; i++) {
            inventory.addItem(Item.APPLE);
        }
        
        // Verify count is correct
        assertEquals(100, inventory.getCount(Item.APPLE));
    }
}