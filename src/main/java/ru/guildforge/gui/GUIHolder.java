package ru.guildforge.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class GUIHolder implements InventoryHolder {
    private final Inventory inventory;
    private final String type; // "main_menu", "confirm_menu", и т.д.

    public GUIHolder(Inventory inventory, String type) {
        this.inventory = inventory;
        this.type = type;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public String getType() {
        return type;
    }
}