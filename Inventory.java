import java.util.EnumMap;

/**
 * Inventory class that manages the items in the game.
 * The class uses an EnumMap to store the count of each item type.
 */
public class Inventory {
    /** The map of items, where the key is the item type and the value is the count of that item */
    private EnumMap<Item, Integer> items;

    /**
     * Default constructor for the Inventory class. Initializes the items map.
     */
    public Inventory() {
        items = new EnumMap<>(Item.class);
    }

    /**
     * Constructor for the Inventory class. 
     * <br><br>
     * Initializes an items map with the specified counts for each item.
     * 
     * @param countApples The initial count of apples.
     * @param countBanana The initial count of bananas.
     * @param countPurple The initial count of purple gifts.
     * @param countGreen  The initial count of green gifts.
     */
    public Inventory(int countApples, int countBanana, int countPurple, int countGreen) {
        items = new EnumMap<>(Item.class);
        items.put(Item.APPLE, countApples);
        items.put(Item.BANANA, countBanana);
        items.put(Item.PURPLEGIFT, countPurple);
        items.put(Item.GREENGIFT, countGreen);
    }

    /**
     * Adds one count of the specified item to the inventory. 
     * <br><br>
     * If an item is not already in the inventory, it is added with a count of 1. 
     * If it is already in the inventory, its count is incremented by 1.
     * 
     * @param item The item to add to the inventory.
     */
    public void addItem(Item item) {
        items.put(item, items.getOrDefault(item, 0) + 1);
    }

    /**
     * Returns the count of a specified item in the inventory, if there is any.
     * 
     * @param item The item to get the count for.
     * @return The count of the specified item in the inventory.
     */
    public int getCount(Item item) {
        if (items.containsKey(item)) {
            return items.get(item);
        } else {
            return 0;
        }
    }

    /**
     * Removes one count of the specified item from the inventory. 
     * <br><br>
     * If the item is in the inventory and its count is greater than 1, its count 
     * is decremented by 1. If the count is 1, the item is removed from the inventory.
     * 
     * @param item The item to remove from the inventory.
     */
    public void removeItem(Item item) {
        if (items.containsKey(item)) {
            int count = items.get(item);
            if (count > 1) {
                items.put(item, count - 1);
            } else {
                items.remove(item);
            }
        }
    }

    public EnumMap<Item, Integer> getInventory() {
        return items;
    }
}