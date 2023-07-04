package com.lthoerner.betteranvils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class AnvilResult {
    public final ItemStack resultItem;
    public final int cost;

    public AnvilResult(ItemStack resultItem, int cost) {
        this.resultItem = resultItem;
        this.cost = cost;
    }
}

class AnvilAction {
    public final ItemStack leftItem;
    public final ItemStack rightItem;
    public final String renameText;
    public final ArrayList<AnvilActionOption> options;

    public AnvilAction(ItemStack leftItem, ItemStack rightItem, String renameText) {
        this.leftItem = leftItem;
        this.rightItem = rightItem;
        this.renameText = renameText;
        this.options = EnchantmentUtils.getAnvilActionOptions(leftItem, rightItem, renameText);
    }

    public AnvilResult getResult() {
        if (options.isEmpty()) {
            return null;
        }

        ItemStack resultItem = null;

        if (options.contains(AnvilActionOption.COMBINE_ENCHANT) || options.contains(AnvilActionOption.BOOK_ENCHANT)) {
            resultItem = cloneLeftItemIfResultNull(leftItem, null);

            Map<Enchantment, Integer> combinedEnchants = EnchantmentUtils.combineEnchants(leftItem, rightItem);
            EnchantmentUtils.applyEnchantments(resultItem, combinedEnchants, true);
        }

        if (options.contains(AnvilActionOption.COMBINE_REPAIR)) {
            resultItem = cloneLeftItemIfResultNull(leftItem, resultItem);

            int combinedDurability = EnchantmentUtils.combineDamage(leftItem, rightItem);
            Damageable resultMeta = (Damageable) resultItem.getItemMeta();
            assert resultMeta != null;
            resultMeta.setDamage(combinedDurability);
            resultItem.setItemMeta(resultMeta);
        }

        if (options.contains(AnvilActionOption.RENAME)) {
            resultItem = cloneLeftItemIfResultNull(leftItem, resultItem);

            ItemMeta resultMeta = resultItem.getItemMeta();
            assert resultMeta != null;
            resultMeta.setDisplayName(renameText);
            resultItem.setItemMeta(resultMeta);
        }

        return new AnvilResult(resultItem, 20);
    }

    // This function is used to ensure that the AnvilActionOptions can be applied both independently and
    // in conjunction by forcing the result to be the left item unless it is already instantiated
    static @NotNull ItemStack cloneLeftItemIfResultNull(@NotNull ItemStack leftItem, ItemStack resultItem) {
        if (resultItem == null) {
            return leftItem.clone();
        }

        return resultItem;
    }
}

enum AnvilActionOption {
    RENAME,
    COMBINE_ENCHANT,
    BOOK_ENCHANT,
    COMBINE_REPAIR,
    MATERIAL_REPAIR,
}

enum DamageableMaterial {
    LEATHER,
    WOOD,
    STONE,
    IRON,
    GOLD,
    DIAMOND,
    NETHERITE,
    SCUTE,
    PHANTOM_MEMBRANE,
}

public class EnchantmentUtils {
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
    static void applyEnchantments(ItemStack item, Map<Enchantment, Integer> enchantments, boolean replaceEnchants) {
        // If the replaceEnchants flag is set, reset the enchantments on the item before adding the new ones
        if (replaceEnchants) {
            stripEnchantments(item);
        }

        item.addUnsafeEnchantments(enchantments);

        // If the item is an enchanted book, this converts the standard enchantments to stored enchantments
        storeEnchantsInBook(item);
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

    // Gets the result of combine repairing two items in an anvil, represented by the amount of damage rather than
    // the amount of durability due to the way that Damageable works
    // Note: If one or both items are enchanted, this should be used in conjunction with combineEnchants
    static int combineDamage(ItemStack leftItem, ItemStack rightItem) {
        Damageable leftMeta = (Damageable) leftItem.getItemMeta();
        Damageable rightMeta = (Damageable) rightItem.getItemMeta();
        // Both items must be damageable to get to this point, because otherwise the AnvilAction
        // should not have been classified as a COMBINE_REPAIR
        assert leftMeta != null;
        assert rightMeta != null;

        int maxDurability = leftItem.getType().getMaxDurability();

        int leftRemainingDurability = maxDurability - leftMeta.getDamage();
        int rightRemainingDurability = maxDurability - rightMeta.getDamage();

        int combinedDurability = leftRemainingDurability + rightRemainingDurability;
        // If the combined durability is higher than the max durability for the item, the resulting damage is 0
        int combinedDamage = maxDurability - combinedDurability;
        if (combinedDamage < 0) {
            combinedDamage = 0;
        }

        return combinedDamage;
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

//    // Calculates the cost of combining two items in an anvil
//    public static int calculateAnvilCost(ItemStack resultItem) {
//        Map<Enchantment, Integer> enchantments = getAllEnchantments(resultItem);
//
//        // The cost of the enchantment is the sum of the levels of all enchantments multiplied by 2
//        int cost = 0;
//        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
//            cost += entry.getValue();
//        }
//
//        return cost * 2;
//    }

    // Determines if an item can be enchanted, indicated by the fact that it is either a book, tool, weapon, or armor
    static boolean isEnchantable(ItemStack item) {
        if (item == null) {
            return false;
        }

        // TODO: Exclude non-enchantable tools
        Material type = item.getType();
        return isDamageable(item) || type == Material.ENCHANTED_BOOK || type == Material.BOOK;
    }

    // Determines if the given item is a book or enchanted book
    static boolean isBook(ItemStack item) {
        if (item == null) {
            return false;
        }

        Material type = item.getType();
        return type == Material.ENCHANTED_BOOK || type == Material.BOOK;
    }

    // Determines if the given item is a tool, weapon, or armor, indicated by the fact that it is damageable
    static boolean isDamageable(ItemStack item) {
        return item != null && item.getItemMeta() instanceof Damageable;
    }

    // Determines if the given item both is damageable and is not at full durability (is damaged)
    static boolean isDamaged(ItemStack item) {
        if (!isDamageable(item)) {
            return false;
        }

        Damageable meta = (Damageable) item.getItemMeta();
        // The item has to have a Damageable meta if it is damageable, so this should never be null
        assert meta != null;
        return meta.hasDamage();
    }

    // Determines if the material on the right side of the anvil matches the tool, weapon, or armor on the left side
    static boolean materialRepairsItem(ItemStack leftItem, ItemStack rightItem) {
        if (leftItem == null || rightItem == null) {
            return false;
        }

        if (!isDamageable(leftItem) || repairMaterialType(rightItem) == null) {
            return false;
        }

        DamageableMaterial leftMaterial = getRepairMaterial(leftItem);
        DamageableMaterial rightMaterial = repairMaterialType(rightItem);
        return leftMaterial == rightMaterial;
    }

    // Determines the material used to repair the given item, or null if the item cannot be repaired with a material
    static DamageableMaterial getRepairMaterial(ItemStack item) {
        // Only damageable items can be repaired
        if (!isDamageable(item)) {
            return null;
        }

        Material damageableItemType = item.getType();
        // Special cases for tools that are not made of the standard materials, or do not have a material prefix
        if (damageableItemType == Material.SHIELD) {
            return DamageableMaterial.WOOD;
        } else if (damageableItemType == Material.TURTLE_HELMET) {
            return DamageableMaterial.SCUTE;
        } else if (damageableItemType == Material.ELYTRA) {
            return DamageableMaterial.PHANTOM_MEMBRANE;
        }

        // General cases for most tools, weapons, and armor
        String damageableItemName = damageableItemType.name();
        if (damageableItemName.startsWith("LEATHER_")) {
            return DamageableMaterial.LEATHER;
        } else if (damageableItemName.startsWith("WOODEN_")) {
            return DamageableMaterial.WOOD;
        } else if (damageableItemName.startsWith("STONE_")) {
            return DamageableMaterial.STONE;
        } else if (damageableItemName.startsWith("IRON_") || damageableItemName.startsWith("CHAINMAIL_")) {
            return DamageableMaterial.IRON;
        } else if (damageableItemName.startsWith("GOLDEN_")) {
            return DamageableMaterial.GOLD;
        } else if (damageableItemName.startsWith("DIAMOND_")) {
            return DamageableMaterial.DIAMOND;
        } else if (damageableItemName.startsWith("NETHERITE_")) {
            return DamageableMaterial.NETHERITE;
        } else {
            return null;
        }
    }

    // If the given item is a material used to repair tools, weapons, or armor, converts it from a Material to a DamageableMaterial
    static DamageableMaterial repairMaterialType(ItemStack item) {
        if (item == null) {
            return null;
        }

        Material itemType = item.getType();

        if (itemType.name().endsWith("_PLANKS")) {
            return DamageableMaterial.WOOD;
        }

        switch (itemType) {
            case LEATHER:
                return DamageableMaterial.LEATHER;
            case STONE:
                return DamageableMaterial.STONE;
            case IRON_INGOT:
                return DamageableMaterial.IRON;
            case GOLD_INGOT:
                return DamageableMaterial.GOLD;
            case DIAMOND:
                return DamageableMaterial.DIAMOND;
            case NETHERITE_INGOT:
                return DamageableMaterial.NETHERITE;
            case SCUTE:
                return DamageableMaterial.SCUTE;
            case PHANTOM_MEMBRANE:
                return DamageableMaterial.PHANTOM_MEMBRANE;
            default:
                return null;
        }
    }

    // Determines the type of action that the anvil is performing based on the input items
    static ArrayList<AnvilActionOption> getAnvilActionOptions(ItemStack leftItem, ItemStack rightItem, String renameText) {
        // If the left slot is empty, the anvil does nothing
        if (leftItem == null) {
            return new ArrayList<>();
        }

        ArrayList<AnvilActionOption> options = new ArrayList<>();

        ItemMeta leftMeta = leftItem.getItemMeta();
        assert leftMeta != null;
        String leftName = leftMeta.getDisplayName();

        // If there is a new name specified for the item, the anvil is renaming the item
        if (renameText != null && !renameText.equals(leftName)) {
            options.add(AnvilActionOption.RENAME);
        }

        // If there is no item in the right slot, the anvil only renames the item and does nothing else
        if (rightItem == null) {
            return options;
        }

        // If both items are the same, they are being combined
        if (leftItem.getType() == rightItem.getType()) {
            // If at least the right item is enchanted, the items are being "combine enchanted"
            Map<Enchantment, Integer> rightEnchantments = getAllEnchantments(rightItem);
            if (!rightEnchantments.isEmpty() && isEnchantable(leftItem)) {
                options.add(AnvilActionOption.COMBINE_ENCHANT);
            }

            // If the items are damageable and are not at full durability, they are being "combine repaired"
            if (isDamageable(leftItem) && isDamaged(leftItem)) {
                options.add(AnvilActionOption.COMBINE_REPAIR);
            }
        }

        // If the right item is an enchanted book, it is being "book enchanted," unless the left item is also an enchanted book
        // TODO: Does this exception need to exist? They basically do the same thing
        if (rightItem.getType() == Material.ENCHANTED_BOOK && leftItem.getType() != Material.ENCHANTED_BOOK) {
            options.add(AnvilActionOption.BOOK_ENCHANT);
        }

        // If the left item is repairable, and the right item is a material used to repair it, it is being "material repaired"
        if (materialRepairsItem(leftItem, rightItem)) {
            options.add(AnvilActionOption.MATERIAL_REPAIR);
        }

        return options;
    }
}
