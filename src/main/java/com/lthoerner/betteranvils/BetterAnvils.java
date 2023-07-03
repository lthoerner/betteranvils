package com.lthoerner.betteranvils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

import static com.lthoerner.betteranvils.EnchantmentUtils.*;

public final class BetterAnvils extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("Successfully loaded TestPlugin.");

        // Register the listener
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("Successfully unloaded TestPlugin.");
    }

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent e) {
        AnvilInventory anvilInventory = e.getInventory();
        ItemStack leftItem = anvilInventory.getItem(0);
        ItemStack rightItem = anvilInventory.getItem(1);

        if (leftItem != null && rightItem != null) {
            // Get the enchantments on both items
            Map<Enchantment, Integer> leftEnchantments = getAllEnchantments(leftItem);
            Map<Enchantment, Integer> rightEnchantments = getAllEnchantments(rightItem);
            ItemStack resultItem = stripEnchantments(leftItem.clone());

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
                        resultItem.addUnsafeEnchantment(enchantment, Math.max(leftLevel, rightLevel));
                    } else {
                        resultItem.addUnsafeEnchantment(enchantment, leftLevel + 1);
                    }
                } else {
                    // If the enchantment is only on the left item, add it
                    resultItem.addUnsafeEnchantment(enchantment, leftLevel);
                }
            }

            // Add the right enchantments
            for (Map.Entry<Enchantment, Integer> entry : rightEnchantments.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();

                // If the enchantment is only on the right item, add it
                if (!leftEnchantments.containsKey(enchantment)) {
                    resultItem.addUnsafeEnchantment(enchantment, level);
                }
            }

            storeEnchantsInBook(resultItem);

            e.setResult(resultItem);
            anvilInventory.setRepairCost(calculateAnvilCost(resultItem));
        }
    }
}
