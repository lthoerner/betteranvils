package com.lthoerner.betteranvils;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterAnvils extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getLogger().info("Successfully loaded BetterAnvils.");

        saveDefaultConfig();

        // Register the listener
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("Successfully unloaded BetterAnvils.");
    }

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent e) {
        AnvilInventory anvilInventory = e.getInventory();
        ItemStack leftItem = anvilInventory.getItem(0);
        ItemStack rightItem = anvilInventory.getItem(1);
        String renameText = anvilInventory.getRenameText();

        AnvilAction action = new AnvilAction(leftItem, rightItem, renameText, this);
        AnvilResult result = action.getResult();

        if (result != null) {
            e.setResult(result.resultItem);
            anvilInventory.setRepairCost(result.cost);
        } else {
            e.setResult(null);
            anvilInventory.setRepairCost(0);
        }
    }
}
