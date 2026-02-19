package ru.guildforge.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import ru.guildforge.GuildForge;

public class FishingListener implements Listener {
    private final GuildForge plugin;

    public FishingListener(GuildForge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            if (event.getCaught() instanceof org.bukkit.entity.Item caught) {
                ItemStack item = caught.getItemStack();

                if (isMendingBook(item)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("§cВы поймали книгу починки, но она исчезла! (заблокировано)");
                    plugin.getLogger().info("Заблокирована книга починки из рыбалки для " + event.getPlayer().getName());
                }
            }
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