package ru.guildforge.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import ru.guildforge.GuildForge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VillagerTradeListener implements Listener {
    private final GuildForge plugin;

    public VillagerTradeListener(GuildForge plugin) {
        this.plugin = plugin;
    }

    /**
     * Блокируем появление книги починки у жителей при получении новой профессии
     */
    @EventHandler
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        MerchantRecipe recipe = event.getRecipe();

        // Проверяем результат торговли
        ItemStack result = recipe.getResult();

        if (isMendingBook(result)) {
            event.setCancelled(true);
            plugin.getLogger().info("Заблокировано появление книги починки у жителя");
        }
    }

    /**
     * Проверяем при открытии меню торговли и убираем все книги починки из списка
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager villager) {
            // Получаем все рецепты жителя
            List<MerchantRecipe> recipes = new ArrayList<>(villager.getRecipes());
            boolean removed = false;

            // Удаляем все рецепты с книгами починки
            Iterator<MerchantRecipe> iterator = recipes.iterator();
            while (iterator.hasNext()) {
                MerchantRecipe recipe = iterator.next();
                if (isMendingBook(recipe.getResult())) {
                    iterator.remove();
                    removed = true;
                }
            }

            // Обновляем рецепты жителя
            if (removed) {
                villager.setRecipes(recipes);
                plugin.getLogger().info("Удалены книги починки из торговли жителя");
            }
        }
    }

    /**
     * Проверяем при выборе торговли, блокируем если это книга починки
     */
    @EventHandler
    public void onTradeSelect(TradeSelectEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            MerchantRecipe selectedRecipe = event.getMerchant().getRecipe(event.getIndex());

            if (selectedRecipe != null && isMendingBook(selectedRecipe.getResult())) {
                event.setCancelled(true);
                player.sendMessage("§cТорговля книгами починки запрещена!");
                plugin.getLogger().info("Игрок " + player.getName() + " пытался купить книгу починки у жителя");
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