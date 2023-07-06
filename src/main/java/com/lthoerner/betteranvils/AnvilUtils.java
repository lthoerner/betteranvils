package com.lthoerner.betteranvils;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

import static com.lthoerner.betteranvils.EnchantUtils.*;

enum AnvilOperation {
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

class AnvilAction {
    final Plugin plugin;

    final ItemStack leftItem;
    final ItemStack rightItem;
    final String renameText;
    final @NotNull ArrayList<AnvilOperation> operations;

    final int MAX_COST;
    final double ENCHANT_COST_MULTIPLIER;
    final int RENAME_COST;
    final int DURABILITY_PER_LEVEL;
    final int MIN_REPAIR_COST;

    AnvilAction(ItemStack leftItem, ItemStack rightItem, String renameText) {
        this.plugin = BetterAnvils.getInstance();

        this.leftItem = leftItem;
        this.rightItem = rightItem;
        this.renameText = renameText;
        // An AnvilAction consists of one or more AnvilOperations
        this.operations = AnvilUtils.getAnvilOperations(leftItem, rightItem, renameText);

        Configuration config = plugin.getConfig();

        this.MAX_COST = config.getInt("max-cost");
        this.ENCHANT_COST_MULTIPLIER = config.getDouble("enchant-cost-multiplier");
        this.RENAME_COST = config.getInt("rename-cost");
        this.DURABILITY_PER_LEVEL = config.getInt("durability-per-level");
        this.MIN_REPAIR_COST = config.getInt("min-repair-cost");
    }

    AnvilResult getResult() {
        if (operations.isEmpty()) {
            return null;
        }

        boolean combineEnchant = operations.contains(AnvilOperation.COMBINE_ENCHANT);
        boolean bookEnchant = operations.contains(AnvilOperation.BOOK_ENCHANT);
        boolean combineRepair = operations.contains(AnvilOperation.COMBINE_REPAIR);
        boolean materialRepair = operations.contains(AnvilOperation.MATERIAL_REPAIR);
        boolean rename = operations.contains(AnvilOperation.RENAME);

        ItemStack resultItem = null;
        int cost = 0;

        // An item cannot be combine enchanted and book enchanted simultaneously
        if (combineEnchant ^ bookEnchant) {
            resultItem = cloneLeftItemIfResultNull(leftItem, null);

            Map<Enchantment, Integer> combinedEnchants = combineEnchants(leftItem, rightItem);
            try {
                resultItem = applyEnchants(resultItem, combinedEnchants);
            } catch (IllegalArgumentException e) {
                // If enchantment has failed, the entire operation must be canceled to prevent the result item
                // from being losing its enchantments in the process of being renamed or repaired
                plugin.getLogger().warning(e.getMessage());
                return null;
            }

            cost += ENCHANT_COST_MULTIPLIER * totalLevels(resultItem);
        }

        // An item cannot be combine repaired and material repaired simultaneously
        if (combineRepair ^ materialRepair) {
            resultItem = cloneLeftItemIfResultNull(leftItem, resultItem);

            Damageable metaBeforeRepair = (Damageable) resultItem.getItemMeta();
            assert metaBeforeRepair != null;
            int damageBeforeRepair = metaBeforeRepair.getDamage();

            int damageAfterRepair;
            if (combineRepair) {
                damageAfterRepair = AnvilUtils.combineDamage(leftItem, rightItem);
            } else {
                try {
                    damageAfterRepair = AnvilUtils.materialRepairDamage(leftItem, rightItem.getAmount());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().severe(e.getMessage());
                    plugin.getLogger().severe("This is a bug in BetterAnvils. Please report it at " +
                            "https://github.com/lthoerner/betteranvils.");
                    return null;
                }
            }

            Damageable resultMeta = (Damageable) resultItem.getItemMeta();
            assert resultMeta != null;
            resultMeta.setDamage(damageAfterRepair);
            resultItem.setItemMeta(resultMeta);

            // If the items are being combine repaired, they may not necessarily be combine enchanted, but
            // their enchantments must be combined nonetheless
            if (combineRepair) {
                Map<Enchantment, Integer> combinedEnchants = combineEnchants(leftItem, rightItem);
                // This should never fail, because if two items are being combine repaired without having already
                // gone through the validation process in the combineEnchant/bookEnchant section, one of the items is
                // not enchanted and validation is not necessary
                try {
                    resultItem = applyEnchants(resultItem, combinedEnchants);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().severe(e.getMessage());
                    plugin.getLogger().severe("This is a bug in BetterAnvils. Please report it at " +
                            "https://github.com/lthoerner/betteranvils.");
                    return null;
                }
            }

            int healedDamage = damageBeforeRepair - damageAfterRepair;

            cost += Math.max(MIN_REPAIR_COST, healedDamage / DURABILITY_PER_LEVEL);
        }

        if (rename) {
            // If there has been some other operation performed on the item, and the result item is not any different
            // from the left item prior to the rename operation, then no action should be performed
            // This generally happens if an enchantment has reached its max level and combine enchanting
            // will not increase the level of the enchantment
            if (resultItem != null && resultItem.equals(leftItem)) {
                return null;
            }

            resultItem = cloneLeftItemIfResultNull(leftItem, resultItem);

            ItemMeta resultMeta = resultItem.getItemMeta();
            assert resultMeta != null;
            resultMeta.setDisplayName(renameText);
            resultItem.setItemMeta(resultMeta);

            cost += RENAME_COST;
        }

        assert resultItem != null;
        // If the result item is the same as the left item, the anvil does not take any action,
        // in order to prevent the player from accidentally spending experience or materials on a useless action
        if (resultItem.equals(leftItem)) {
            return null;
        }

        return new AnvilResult(resultItem, Math.min(MAX_COST, cost));
    }

    // This function is used to ensure that the AnvilOperations can be applied both independently and
    // in conjunction by forcing the result to be the left item unless it is already instantiated
    static @NotNull ItemStack cloneLeftItemIfResultNull(@NotNull ItemStack leftItem, ItemStack resultItem) {
        if (resultItem == null) {
            return leftItem.clone();
        }

        return resultItem;
    }
}

class AnvilUtils {
    static final double MATERIAL_REPAIR_COST_MULTIPLIER =
            BetterAnvils.getInstance().getConfig().getDouble("material-repair-cost-multiplier");

    // Determines the type of operations that the anvil is performing based on the input items
    static @NotNull ArrayList<AnvilOperation> getAnvilOperations(ItemStack leftItem, ItemStack rightItem, String renameText) {
        // If the left slot is empty, the anvil does nothing
        if (leftItem == null) {
            return new ArrayList<>();
        }

        ArrayList<AnvilOperation> operations = new ArrayList<>();

        ItemMeta leftMeta = leftItem.getItemMeta();
        assert leftMeta != null;
        String leftName = leftMeta.getDisplayName();

        // If there is a new name specified for the item, the anvil is renaming the item
        if (renameText != null && !renameText.equals(leftName)) {
            operations.add(AnvilOperation.RENAME);
        }

        // If there is no item in the right slot, the anvil only renames the item and does nothing else
        if (rightItem == null) {
            return operations;
        }

        // If both items are the same, they are being combined
        if (leftItem.getType() == rightItem.getType()) {
            // If at least one item is enchanted, the items are being "combine enchanted"
            Map<Enchantment, Integer> leftEnchantments = getAllEnchants(leftItem);
            Map<Enchantment, Integer> rightEnchantments = getAllEnchants(rightItem);
            boolean bothItemsEnchanted = !leftEnchantments.isEmpty() && !rightEnchantments.isEmpty();
            boolean wastefulEnchant = isWastefulCombine(leftEnchantments, rightEnchantments);
            boolean leftItemEnchantable = isEnchantable(leftItem);
            if (!wastefulEnchant && bothItemsEnchanted && leftItemEnchantable) {
                operations.add(AnvilOperation.COMBINE_ENCHANT);
            }

            // If the items are damageable and are not at full durability, they are being "combine repaired"
            if (isDamageable(leftItem) && isDamaged(leftItem)) {
                operations.add(AnvilOperation.COMBINE_REPAIR);
            }
        }

        // If the right item is an enchanted book, it is being "book enchanted," unless the left item is also an enchanted book
        // This exception needs to exist because combine enchanting and book enchanting are mutually exclusive,
        // mostly to avoid duplicate level costs
        if (rightItem.getType() == Material.ENCHANTED_BOOK && leftItem.getType() != Material.ENCHANTED_BOOK) {
            operations.add(AnvilOperation.BOOK_ENCHANT);
        }

        // If the left item is repairable, and the right item is a material used to repair it, it is being "material repaired"
        if (materialRepairsItem(leftItem, rightItem)) {
            operations.add(AnvilOperation.MATERIAL_REPAIR);
        }

        // If no operations other than RENAME are specified, and there is an item in the right-hand slot, the rename
        // operation would consume the right item unnecessarily, hence this condition
        // All these conditions would automatically be true if operations only has one element at this point
        if (operations.size() == 1) {
            operations.remove(AnvilOperation.RENAME);
        }

        return operations;
    }

    // Gets the result of combine repairing two items in an anvil, represented by the amount of damage rather than
    // the amount of durability due to the way that Damageable works
    // Note: If one or both items are enchanted, this should be used in conjunction with combineEnchants
    static int combineDamage(@NotNull ItemStack leftItem, @NotNull ItemStack rightItem) {
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

    // Gets the damage value of an item being material repaired, given the amount of materials provided
    static int materialRepairDamage(@NotNull ItemStack item, int materialCount) throws IllegalArgumentException {
        Damageable itemMeta = (Damageable) item.getItemMeta();
        assert itemMeta != null;

        int maxDurability = item.getType().getMaxDurability();
        int remainingDurability = maxDurability - itemMeta.getDamage();

        int fullRepairMaterialCost = repairMaterialCost(item);

        int durabilityPerMaterial = maxDurability / fullRepairMaterialCost;
        int materialAddedDurability = materialCount * durabilityPerMaterial;
        int combinedDurability = remainingDurability + materialAddedDurability;

        int combinedDamage = maxDurability - combinedDurability;
        if (combinedDamage < 0) {
            combinedDamage = 0;
        }

        return combinedDamage;
    }

    // Determines if the given item is a tool, weapon, or armor, indicated by the fact that it is damageable
    static boolean isDamageable(@NotNull ItemStack item) {
        return item.getItemMeta() instanceof Damageable;
    }

    // Determines if the given item both is damageable and is not at full durability (is damaged)
    static boolean isDamaged(@NotNull ItemStack item) {
        if (!isDamageable(item)) {
            return false;
        }

        Damageable meta = (Damageable) item.getItemMeta();
        // The item has to have a Damageable meta if it is damageable, so this should never be null
        assert meta != null;
        return meta.hasDamage();
    }

    // Determines if the material on the right side of the anvil matches the tool, weapon, or armor on the left side
    static boolean materialRepairsItem(@NotNull ItemStack leftItem, @NotNull ItemStack rightItem) {
        if (!isDamageable(leftItem) || repairMaterialType(rightItem) == null) {
            return false;
        }

        DamageableMaterial leftMaterial = getRepairMaterial(leftItem);
        DamageableMaterial rightMaterial = repairMaterialType(rightItem);
        return leftMaterial == rightMaterial;
    }

    // Determines the material used to repair the given item, or null if the item cannot be repaired with a material
    static DamageableMaterial getRepairMaterial(@NotNull ItemStack item) {
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
    static DamageableMaterial repairMaterialType(@NotNull ItemStack item) {
        Material itemType = item.getType();

        if (itemType.name().endsWith("_PLANKS")) {
            return DamageableMaterial.WOOD;
        }

        switch (itemType) {
            case LEATHER:
                return DamageableMaterial.LEATHER;
            case COBBLESTONE:
            case COBBLED_DEEPSLATE:
            case BLACKSTONE:
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

    // Gets the amount of repair material required to repair a given item from 0 to full durability
    static int repairMaterialCost(@NotNull ItemStack item) throws IllegalArgumentException {
        Material damageableItemType = item.getType();
        String damageableItemName = damageableItemType.name();

        double materialCount;

        if (damageableItemType == Material.SHIELD) {
            materialCount = 6;
        } else if (damageableItemType == Material.ELYTRA) {
            materialCount = 7;
        } else if (damageableItemName.endsWith("_HELMET") || damageableItemName.endsWith("_CAP")) {
            materialCount = 5;
        } else if (damageableItemName.endsWith("_CHESTPLATE") || damageableItemName.endsWith("_TUNIC")) {
            materialCount = 8;
        } else if (damageableItemName.endsWith("_LEGGINGS") || damageableItemName.endsWith("_PANTS")) {
            materialCount = 7;
        } else if (damageableItemName.endsWith("_BOOTS")) {
            materialCount = 4;
        } else if (damageableItemName.endsWith("_SWORD")) {
            materialCount = 2;
        } else if (damageableItemName.endsWith("_PICKAXE")) {
            materialCount = 3;
        } else if (damageableItemName.endsWith("_AXE")) {
            materialCount = 3;
        } else if (damageableItemName.endsWith("_SHOVEL")) {
            materialCount = 1;
        } else if (damageableItemName.endsWith("_HOE")) {
            materialCount = 2;
        } else {
            throw new IllegalArgumentException(
                    "Item " + damageableItemType.name() + " has no corresponding repair material");
        }

        return (int) (MATERIAL_REPAIR_COST_MULTIPLIER * materialCount);
    }
}

class AnvilResult {
    final @NotNull ItemStack resultItem;
    final int cost;

    AnvilResult(@NotNull ItemStack resultItem, int cost) {
        this.resultItem = resultItem;
        this.cost = cost;
    }
}
