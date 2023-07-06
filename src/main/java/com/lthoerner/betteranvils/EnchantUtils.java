package com.lthoerner.betteranvils;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.lthoerner.betteranvils.AnvilUtils.isDamageable;

public class EnchantUtils {
    public static HashMap<Enchantment, Integer> MAX_ENCHANT_LEVELS = new HashMap<>(38);
    public static ArrayList<Enchantment[]> INCOMPATIBLE_ENCHANTMENTS = new ArrayList<>(10);
    static {
        Configuration config = JavaPlugin.getPlugin(BetterAnvils.class).getConfig();
        boolean USE_VANILLA_MAX_LEVELS = config.getBoolean("use-vanilla-max-levels");

        int SHARPNESS_LEVEL = USE_VANILLA_MAX_LEVELS ? 5 : config.getInt("max-level.sharpness");
        int SMITE_LEVEL = USE_VANILLA_MAX_LEVELS ? 5 : config.getInt("max-level.smite");
        int BANE_OF_ARTHROPODS_LEVEL = USE_VANILLA_MAX_LEVELS ? 5 : config.getInt("max-level.bane-of-arthropods");
        int FIRE_ASPECT_LEVEL = USE_VANILLA_MAX_LEVELS ? 2 : config.getInt("max-level.fire-aspect");
        int KNOCKBACK_LEVEL = USE_VANILLA_MAX_LEVELS ? 2 : config.getInt("max-level.knockback");
        int LOOTING_LEVEL = USE_VANILLA_MAX_LEVELS ? 3 : config.getInt("max-level.looting");
        int SWEEPING_EDGE_LEVEL = USE_VANILLA_MAX_LEVELS ? 3 : config.getInt("max-level.sweeping-edge");

        int IMPALING_LEVEL = USE_VANILLA_MAX_LEVELS ? 5 : config.getInt("max-level.impaling");
        int RIPTIDE_LEVEL = USE_VANILLA_MAX_LEVELS ? 3 : config.getInt("max-level.riptide");
        int LOYALTY_LEVEL = USE_VANILLA_MAX_LEVELS ? 3 : config.getInt("max-level.loyalty");

        int POWER_LEVEL = USE_VANILLA_MAX_LEVELS ? 5 : config.getInt("max-level.power");
        int FLAME_LEVEL = USE_VANILLA_MAX_LEVELS ? 1 : config.getInt("max-level.flame");
        int PUNCH_LEVEL = USE_VANILLA_MAX_LEVELS ? 2 : config.getInt("max-level.punch");
        int INFINITY_LEVEL = USE_VANILLA_MAX_LEVELS ? 1 : config.getInt("max-level.infinity");

        int PIERCING_LEVEL = USE_VANILLA_MAX_LEVELS ? 4 : config.getInt("max-level.piercing");
        int QUICK_CHARGE_LEVEL = USE_VANILLA_MAX_LEVELS ? 3 : config.getInt("max-level.quick-charge");
        int MULTISHOT_LEVEL = USE_VANILLA_MAX_LEVELS ? 1 : config.getInt("max-level.multishot");

        int EFFICIENCY_LEVEL = USE_VANILLA_MAX_LEVELS ? 5 : config.getInt("max-level.efficiency");
        int SILK_TOUCH_LEVEL = USE_VANILLA_MAX_LEVELS ? 1 : config.getInt("max-level.silk-touch");
        int FORTUNE_LEVEL = USE_VANILLA_MAX_LEVELS ? 3 : config.getInt("max-level.fortune");

        int LURE_LEVEL = USE_VANILLA_MAX_LEVELS ? 3 : config.getInt("max-level.lure");
        int LUCK_OF_THE_SEA_LEVEL = USE_VANILLA_MAX_LEVELS ? 3 : config.getInt("max-level.luck-of-the-sea");

        int PROTECTION_LEVEL = USE_VANILLA_MAX_LEVELS ? 4 : config.getInt("max-level.protection");
        int FIRE_PROTECTION_LEVEL = USE_VANILLA_MAX_LEVELS ? 4 : config.getInt("max-level.fire-protection");
        int PROJECTILE_PROTECTION_LEVEL = USE_VANILLA_MAX_LEVELS ? 4 : config.getInt("max-level.projectile-protection");
        int BLAST_PROTECTION_LEVEL = USE_VANILLA_MAX_LEVELS ? 4 : config.getInt("max-level.blast-protection");
        int FEATHER_FALLING_LEVEL = USE_VANILLA_MAX_LEVELS ? 4 : config.getInt("max-level.feather-falling");
        int THORNS_LEVEL = USE_VANILLA_MAX_LEVELS ? 3 : config.getInt("max-level.thorns");
        int SOUL_SPEED_LEVEL = USE_VANILLA_MAX_LEVELS ? 3 : config.getInt("max-level.soul-speed");
        int SWIFT_SNEAK_LEVEL = USE_VANILLA_MAX_LEVELS ? 3 : config.getInt("max-level.swift-sneak");
        int DEPTH_STRIDER_LEVEL = USE_VANILLA_MAX_LEVELS ? 3 : config.getInt("max-level.depth-strider");
        int RESPIRATION_LEVEL = USE_VANILLA_MAX_LEVELS ? 3 : config.getInt("max-level.respiration");
        int AQUA_AFFINITY_LEVEL = USE_VANILLA_MAX_LEVELS ? 1 : config.getInt("max-level.aqua-affinity");
        int FROST_WALKER_LEVEL = USE_VANILLA_MAX_LEVELS ? 2 : config.getInt("max-level.frost-walker");

        int UNBREAKING_LEVEL = USE_VANILLA_MAX_LEVELS ? 3 : config.getInt("max-level.unbreaking");
        int MENDING_LEVEL = USE_VANILLA_MAX_LEVELS ? 1 : config.getInt("max-level.mending");

        int CURSE_OF_BINDING_LEVEL = USE_VANILLA_MAX_LEVELS ? 1 : config.getInt("max-level.curse-of-binding");
        int CURSE_OF_VANISHING_LEVEL = USE_VANILLA_MAX_LEVELS ? 1 : config.getInt("max-level.curse-of-vanishing");

        MAX_ENCHANT_LEVELS.put(Enchantment.DAMAGE_ALL, SHARPNESS_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.DAMAGE_UNDEAD, SMITE_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.DAMAGE_ARTHROPODS, BANE_OF_ARTHROPODS_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.FIRE_ASPECT, FIRE_ASPECT_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.KNOCKBACK, KNOCKBACK_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.LOOT_BONUS_MOBS, LOOTING_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.SWEEPING_EDGE, SWEEPING_EDGE_LEVEL);

        MAX_ENCHANT_LEVELS.put(Enchantment.IMPALING, IMPALING_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.RIPTIDE, RIPTIDE_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.LOYALTY, LOYALTY_LEVEL);

        MAX_ENCHANT_LEVELS.put(Enchantment.ARROW_DAMAGE, POWER_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.ARROW_FIRE, FLAME_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.ARROW_KNOCKBACK, PUNCH_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.ARROW_INFINITE, INFINITY_LEVEL);

        MAX_ENCHANT_LEVELS.put(Enchantment.PIERCING, PIERCING_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.QUICK_CHARGE, QUICK_CHARGE_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.MULTISHOT, MULTISHOT_LEVEL);

        MAX_ENCHANT_LEVELS.put(Enchantment.DIG_SPEED, EFFICIENCY_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.LOOT_BONUS_BLOCKS, FORTUNE_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.SILK_TOUCH, SILK_TOUCH_LEVEL);

        MAX_ENCHANT_LEVELS.put(Enchantment.LURE, LURE_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.LUCK, LUCK_OF_THE_SEA_LEVEL);

        MAX_ENCHANT_LEVELS.put(Enchantment.PROTECTION_ENVIRONMENTAL, PROTECTION_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.PROTECTION_FIRE, FIRE_PROTECTION_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.PROTECTION_PROJECTILE, PROJECTILE_PROTECTION_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.PROTECTION_EXPLOSIONS, BLAST_PROTECTION_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.PROTECTION_FALL, FEATHER_FALLING_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.THORNS, THORNS_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.SOUL_SPEED, SOUL_SPEED_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.SWIFT_SNEAK, SWIFT_SNEAK_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.DEPTH_STRIDER, DEPTH_STRIDER_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.OXYGEN, RESPIRATION_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.WATER_WORKER, AQUA_AFFINITY_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.FROST_WALKER, FROST_WALKER_LEVEL);

        MAX_ENCHANT_LEVELS.put(Enchantment.DURABILITY, UNBREAKING_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.MENDING, MENDING_LEVEL);

        MAX_ENCHANT_LEVELS.put(Enchantment.BINDING_CURSE, CURSE_OF_BINDING_LEVEL);
        MAX_ENCHANT_LEVELS.put(Enchantment.VANISHING_CURSE, CURSE_OF_VANISHING_LEVEL);

        INCOMPATIBLE_ENCHANTMENTS.add(new Enchantment[] {
                Enchantment.DAMAGE_ALL,
                Enchantment.DAMAGE_UNDEAD,
                Enchantment.DAMAGE_ARTHROPODS,
        });

        INCOMPATIBLE_ENCHANTMENTS.add(new Enchantment[] {
                Enchantment.MENDING,
                Enchantment.ARROW_INFINITE,
        });

        INCOMPATIBLE_ENCHANTMENTS.add(new Enchantment[] {
                Enchantment.MULTISHOT,
                Enchantment.PIERCING,
        });

        INCOMPATIBLE_ENCHANTMENTS.add(new Enchantment[] {
                Enchantment.LOOT_BONUS_BLOCKS,
                Enchantment.SILK_TOUCH,
        });

        INCOMPATIBLE_ENCHANTMENTS.add(new Enchantment[] {
                Enchantment.PROTECTION_ENVIRONMENTAL,
                Enchantment.PROTECTION_FIRE,
                Enchantment.PROTECTION_PROJECTILE,
                Enchantment.PROTECTION_EXPLOSIONS,
        });

        INCOMPATIBLE_ENCHANTMENTS.add(new Enchantment[] {
                Enchantment.DEPTH_STRIDER,
                Enchantment.FROST_WALKER,
        });

        INCOMPATIBLE_ENCHANTMENTS.add(new Enchantment[] {
                Enchantment.CHANNELING,
                Enchantment.RIPTIDE,
        });

        INCOMPATIBLE_ENCHANTMENTS.add(new Enchantment[] {
                Enchantment.LOYALTY,
                Enchantment.RIPTIDE,
        });
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
    static ItemStack applyEnchantments(@NotNull ItemStack item, @NotNull Map<Enchantment, Integer> enchantments) {
        // The item is cloned in order to check whether each enchantment is legal without modifying the original item
        ItemStack enchantedItem = item.clone();

        // Reset the enchantments on the item before adding the new ones
        stripEnchantments(enchantedItem);

        // If there are incompatible enchantments in the list, the operation is cancelled
        if (hasIncompatibleEnchants(enchantments)) {
            return null;
        }

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            // If the level is higher than the maximum allowed level for the enchantment, set it to the maximum
            int level = normalizeEnchantLevel(enchantment, entry.getValue());

            // If the enchantment is allowed for the given item and does not conflict, add it
            // Otherwise, cancel the operation altogether by returning null
            // For some reason Enchantment.canEnchantItem() does not work for enchanted books,
            // so they must have an exception
            if (enchantment.canEnchantItem(enchantedItem) || isBook(enchantedItem)) {
                enchantedItem.addUnsafeEnchantment(enchantment, level);
            } else {
                return null;
            }
        }

        // If the item is an enchanted book, this converts the standard enchantments to stored enchantments
        storeEnchantsInBook(enchantedItem);

        return enchantedItem;
    }

    // Removes all the enchantments of an item, both standard and stored
    static void stripEnchantments(@NotNull ItemStack item) {
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
    static void storeEnchantsInBook(@NotNull ItemStack item) {
        // Stored enchantments are only relevant for enchanted books
        if (item.getType() != Material.ENCHANTED_BOOK) {
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
    static @NotNull Map<Enchantment, Integer> combineEnchants(@NotNull ItemStack leftItem, @NotNull ItemStack rightItem) {
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
    public static int totalLevels(@NotNull ItemStack item) {
        Map<Enchantment, Integer> enchantments = getAllEnchantments(item);
        int totalLevels = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            totalLevels += entry.getValue();
        }

        return totalLevels;
    }

    // Determines if an item can be enchanted, indicated by the fact that it is either a book, tool, weapon, or armor
    static boolean isEnchantable(@NotNull ItemStack item) {
        // TODO: Exclude non-enchantable tools (are there any?)
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

    // Determines whether a set of enchantments contains incompatible enchantments
    static boolean hasIncompatibleEnchants(Map<Enchantment, Integer> enchantments) {
        for (Enchantment[] incompatibleEnchantments : INCOMPATIBLE_ENCHANTMENTS) {
            int count = 0;
            for (Enchantment enchantment : incompatibleEnchantments) {
                if (enchantments.containsKey(enchantment)) {
                    count++;
                    if (count > 1) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
