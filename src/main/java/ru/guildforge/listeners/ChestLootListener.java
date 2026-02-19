package ru.guildforge.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import ru.guildforge.GuildForge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChestLootListener implements Listener {
    private final GuildForge plugin;

    public ChestLootListener(GuildForge plugin) {
        this.plugin = plugin;
    }

    /**
     * Блокируем появление книги починки в луте сундуков
     */
    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        List<ItemStack> loot = new ArrayList<>(event.getLoot());
        AtomicBoolean removed = new AtomicBoolean(false);

        // Удаляем все книги починки из лута
        loot.removeIf(item -> {
            if (isMendingBook(item)) {
                removed.set(true);
                return true;
            }
            return false;
        });

        if (removed.get()) {
            event.setLoot(loot);
            plugin.getLogger().info("Удалены книги починки из лута в " + event.getWorld().getName());
        }
    }

    /**
     * Проверка, является ли предмет книгой починки
     */
    private boolean isMendingBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) return false;

        if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            return meta.hasStoredEnchant(org.bukkit.enchantments.Enchantment.MENDING);
        }

        return false;
    }
}