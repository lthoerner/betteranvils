package com.lthoerner.betteranvils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

import static com.lthoerner.betteranvils.AnvilUtils.isDamageable;

public class EnchantUtils {
    // TODO: Make this configurable
    public static HashMap<Enchantment, Integer> MAX_ENCHANT_LEVELS = new HashMap<>(38);
    static {
        MAX_ENCHANT_LEVELS.put(Enchantment.DAMAGE_ALL, 10);
        MAX_ENCHANT_LEVELS.put(Enchantment.DAMAGE_UNDEAD, 10);
        MAX_ENCHANT_LEVELS.put(Enchantment.DAMAGE_ARTHROPODS, 10);
        MAX_ENCHANT_LEVELS.put(Enchantment.FIRE_ASPECT, 5);
        MAX_ENCHANT_LEVELS.put(Enchantment.KNOCKBACK, 5);
        MAX_ENCHANT_LEVELS.put(Enchantment.LOOT_BONUS_MOBS, 5);
        MAX_ENCHANT_LEVELS.put(Enchantment.SWEEPING_EDGE, 5);

        MAX_ENCHANT_LEVELS.put(Enchantment.IMPALING, 10);
        MAX_ENCHANT_LEVELS.put(Enchantment.RIPTIDE, 5);
        MAX_ENCHANT_LEVELS.put(Enchantment.LOYALTY, 5);

        MAX_ENCHANT_LEVELS.put(Enchantment.ARROW_DAMAGE, 10);
        MAX_ENCHANT_LEVELS.put(Enchantment.ARROW_FIRE, 1);
        MAX_ENCHANT_LEVELS.put(Enchantment.ARROW_KNOCKBACK, 5);
        MAX_ENCHANT_LEVELS.put(Enchantment.ARROW_INFINITE, 1);

        MAX_ENCHANT_LEVELS.put(Enchantment.PIERCING, 8);
        MAX_ENCHANT_LEVELS.put(Enchantment.QUICK_CHARGE, 5);
        MAX_ENCHANT_LEVELS.put(Enchantment.MULTISHOT, 1);

        MAX_ENCHANT_LEVELS.put(Enchantment.DIG_SPEED, 10);
        MAX_ENCHANT_LEVELS.put(Enchantment.LOOT_BONUS_BLOCKS, 5);
        MAX_ENCHANT_LEVELS.put(Enchantment.SILK_TOUCH, 1);

        MAX_ENCHANT_LEVELS.put(Enchantment.LURE, 5);
        MAX_ENCHANT_LEVELS.put(Enchantment.LUCK, 5);

        MAX_ENCHANT_LEVELS.put(Enchantment.PROTECTION_ENVIRONMENTAL, 8);
        MAX_ENCHANT_LEVELS.put(Enchantment.PROTECTION_FIRE, 8);
        MAX_ENCHANT_LEVELS.put(Enchantment.PROTECTION_PROJECTILE, 8);
        MAX_ENCHANT_LEVELS.put(Enchantment.PROTECTION_EXPLOSIONS, 8);
        MAX_ENCHANT_LEVELS.put(Enchantment.PROTECTION_FALL, 8);
        MAX_ENCHANT_LEVELS.put(Enchantment.THORNS, 5);
        MAX_ENCHANT_LEVELS.put(Enchantment.SOUL_SPEED, 5);
        MAX_ENCHANT_LEVELS.put(Enchantment.SWIFT_SNEAK, 5);
        MAX_ENCHANT_LEVELS.put(Enchantment.DEPTH_STRIDER, 5);
        MAX_ENCHANT_LEVELS.put(Enchantment.OXYGEN, 5);
        MAX_ENCHANT_LEVELS.put(Enchantment.WATER_WORKER, 1);
        MAX_ENCHANT_LEVELS.put(Enchantment.FROST_WALKER, 5);

        MAX_ENCHANT_LEVELS.put(Enchantment.DURABILITY, 10);
        MAX_ENCHANT_LEVELS.put(Enchantment.MENDING, 1);

        MAX_ENCHANT_LEVELS.put(Enchantment.BINDING_CURSE, 1);
        MAX_ENCHANT_LEVELS.put(Enchantment.VANISHING_CURSE, 1);
    }

    // Combines all the enchantments of an item, both standard and stored, into a single map
    static Map<Enchantment, Integer> getAllEnchantments(ItemStack item) {
        if (item == null) {
            return null;
        }

        // Get the standard enchantments of the item
        Map<Enchantment, Integer> allEnchantments = new HashMap<>(item.getEnchantments());

        // Get the stored enchantments of the item if it is an enchanted book
        // (Only enchanted books have an EnchantmentStorageMeta)
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta enchantmentMeta = (EnchantmentStorageMeta) meta;
            // Combine the stored enchantments of the book with its standard enchantments
            // Generally books should never have both standard and stored enchantments, but this is here just in case
            allEnchantments.putAll(enchantmentMeta.getStoredEnchants());
        }

        return allEnchantments;
    }

    // Safely applies enchantments to an item, switching to stored enchantments if necessary
    // Returns the item if the enchantments were applied successfully, but null if there are
    // conflicting or otherwise illegal enchantments
    static ItemStack applyEnchantments(ItemStack item, Map<Enchantment, Integer> enchantments, boolean replaceEnchants) {
        // The item is cloned in order to check whether each enchantment is legal without modifying the original item
        ItemStack enchantedItem = item.clone();

        // If the replaceEnchants flag is set, reset the enchantments on the item before adding the new ones
        if (replaceEnchants) {
            stripEnchantments(enchantedItem);
        }

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int originalLevel = entry.getValue();

            System.out.println("Attempting to apply " + enchantment + " at level " + originalLevel);

            // If the level is higher than the maximum allowed level for the enchantment, set it to the maximum
            int level = normalizeEnchantLevel(enchantment, entry.getValue());

            System.out.println("Level normalized to " + level);

            // If the enchantment is allowed for the given item and does not conflict, add it
            // Otherwise, cancel the operation altogether by returning null
            // For some reason Enchantment.canEnchantItem() does not work for enchanted books,
            // so they must have an exception
            // TODO: Exclude incompatible enchantments from books to prevent accidentally making books useless
            if (enchantment.canEnchantItem(enchantedItem) || isBook(enchantedItem)) {
                enchantedItem.addUnsafeEnchantment(enchantment, level);
                System.out.println("Successfully applied enchantment.");
            } else {
                System.out.println("Enchantment " + enchantment + " illegal for item " + enchantedItem + ".");
                return null;
            }
        }

        System.out.println("Successfully applied all enchantments to " + enchantedItem + ".");

        // If the item is an enchanted book, this converts the standard enchantments to stored enchantments
        storeEnchantsInBook(enchantedItem);

        return enchantedItem;
    }

    // Removes all the enchantments of an item, both standard and stored
    static void stripEnchantments(ItemStack item) {
        if (item == null) {
            return;
        }

        // Strip the standard enchantments of the item
        item.getEnchantments().keySet().forEach(item::removeEnchantment);

        // Strip the stored enchantments of the item if it is an enchanted book
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta enchantmentMeta = (EnchantmentStorageMeta) meta;
            enchantmentMeta.getStoredEnchants().keySet().forEach(enchantmentMeta::removeStoredEnchant);
            item.setItemMeta(enchantmentMeta);
        }
    }

    // If the given item is an enchanted book, converts its standard enchantments into stored enchantments
    static void storeEnchantsInBook(ItemStack item) {
        // Stored enchantments are only relevant for enchanted books
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) {
            return;
        }

        // Get all the enchantments of the item and convert them to stored enchantments
        EnchantmentStorageMeta enchantmentMeta = (EnchantmentStorageMeta) item.getItemMeta();
        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            assert enchantmentMeta != null;

            // If the book already has an enchantment, take the higher level
            int standardLevel = entry.getValue();
            int resultLevel;
            if (enchantmentMeta.hasStoredEnchant(entry.getKey())) {
                int storedLevel = enchantmentMeta.getStoredEnchantLevel(entry.getKey());
                resultLevel = Math.max(standardLevel, storedLevel);
            } else {
                resultLevel = standardLevel;
            }

            enchantmentMeta.addStoredEnchant(entry.getKey(), resultLevel, true);
            enchantmentMeta.removeEnchant(entry.getKey());
        }

        item.setItemMeta(enchantmentMeta);
    }

    // Combines the enchantments of two items into a single map
    // Note: If both items are the same, and the left item is not at full durability, this should be used
    // in conjunction with getCombineRepairResultDurability
    static Map<Enchantment, Integer> combineEnchants(ItemStack leftItem, ItemStack rightItem) {
        Map<Enchantment, Integer> leftEnchantments = getAllEnchantments(leftItem);
        Map<Enchantment, Integer> rightEnchantments = getAllEnchantments(rightItem);
        Map<Enchantment, Integer> resultEnchantments = new HashMap<>();

        // Add the left enchantments
        for (Map.Entry<Enchantment, Integer> entry : leftEnchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int leftLevel = entry.getValue();

            // If the enchantment is on both items, add the higher level or increment the level
            // If the enchantment is only on the left item, add it
            if (rightEnchantments.containsKey(enchantment)) {
                // If the levels are different, add the higher level
                // If the levels are the same, increment the level
                int rightLevel = rightEnchantments.get(enchantment);
                if (leftLevel != rightLevel) {
                    resultEnchantments.put(enchantment, Math.max(leftLevel, rightLevel));
                } else {
                    resultEnchantments.put(enchantment, leftLevel + 1);
                }
            } else {
                // If the enchantment is only on the left item, add it
                resultEnchantments.put(enchantment, leftLevel);
            }
        }

        // Add the right enchantments
        for (Map.Entry<Enchantment, Integer> entry : rightEnchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            // If the enchantment is only on the right item, add it
            if (!leftEnchantments.containsKey(enchantment)) {
                resultEnchantments.put(enchantment, level);
            }
        }

        return resultEnchantments;
    }

    // Calculates the total number of enchantment levels on an item
    // Used for anvil cost calculations
    public static int totalLevels(ItemStack item) {
        Map<Enchantment, Integer> enchantments = getAllEnchantments(item);
        int totalLevels = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            totalLevels += entry.getValue();
        }

        return totalLevels;
    }

    // Determines if an item can be enchanted, indicated by the fact that it is either a book, tool, weapon, or armor
    static boolean isEnchantable(ItemStack item) {
        if (item == null) {
            return false;
        }

        // TODO: Exclude non-enchantable tools
        return isDamageable(item) || isBook(item);
    }

    // Determines if the given item is a book or enchanted book
    static boolean isBook(ItemStack item) {
        if (item == null) {
            return false;
        }

        Material type = item.getType();
        return type == Material.ENCHANTED_BOOK || type == Material.BOOK;
    }

    // Either returns the given enchantment level or the maximum level for the enchantment,
    // depending on whether the given level exceeds the maximum
    static int normalizeEnchantLevel(Enchantment enchantment, int level) {
        return Math.min(level, MAX_ENCHANT_LEVELS.get(enchantment));
    }
}
