package ru.guildforge.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import ru.guildforge.GuildForge;
import ru.guildforge.data.PlayerData;
import ru.guildforge.guilds.GuildType;

public class BrewingListener implements Listener {

    private final GuildForge plugin;

    // Сколько очков давать за зелье (в зависимости от сложности)
    private static final int BASE_BREW_POINTS = 2;
    private static final int ADVANCED_BREW_POINTS = 5;
    private static final int FUEL_POINTS = 1;

    public BrewingListener(GuildForge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBrew(BrewEvent event) {
        BrewerInventory inventory = event.getContents();
        ItemStack ingredient = inventory.getIngredient();

        // Проверяем, есть ли игрок рядом с варочной стойкой
        if (!(inventory.getHolder() instanceof org.bukkit.block.BrewingStand)) return;

        org.bukkit.block.BrewingStand stand = (org.bukkit.block.BrewingStand) inventory.getHolder();

        // Ищем игрока в радиусе 5 блоков
        for (Player player : stand.getWorld().getNearbyPlayers(stand.getLocation(), 5)) {
            PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
            if (data != null && data.getGuild() == GuildType.ALCHEMIST) {
                // Определяем сложность зелья
                int points = getBrewPoints(ingredient);

                // Даём очки сразу (без задержки)
                plugin.getGuildManager().addContribution(player, points);
                player.sendActionBar("§d✦ +" + points + " очков алхимии");
                break; // Нашли первого алхимика, выходим
            }
        }
    }

    @EventHandler
    public void onFuel(BrewingStandFuelEvent event) {
        // Проверяем, есть ли игрок рядом при заправке топливом
        if (!(event.getBlock().getState() instanceof org.bukkit.block.BrewingStand)) return;

        org.bukkit.block.BrewingStand stand = (org.bukkit.block.BrewingStand) event.getBlock().getState();

        for (Player player : stand.getWorld().getNearbyPlayers(stand.getLocation(), 5)) {
            PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
            if (data != null && data.getGuild() == GuildType.ALCHEMIST) {
                plugin.getGuildManager().addContribution(player, FUEL_POINTS);
                player.sendActionBar("§d✦ +" + FUEL_POINTS + " за топливо");
                break;
            }
        }
    }

    /**
     * Определяет количество очков за ингредиент
     */
    private int getBrewPoints(ItemStack ingredient) {
        if (ingredient == null) return 0;

        Material type = ingredient.getType();

        // Базовые ингредиенты
        if (type == Material.NETHER_WART) {
            return BASE_BREW_POINTS;
        }

        // Вторичные ингредиенты (обычные)
        if (type == Material.REDSTONE ||
                type == Material.GLOWSTONE_DUST ||
                type == Material.FERMENTED_SPIDER_EYE ||
                type == Material.GUNPOWDER ||
                type == Material.DRAGON_BREATH) {
            return BASE_BREW_POINTS;
        }

        // Продвинутые ингредиенты (редкие)
        if (type == Material.GOLDEN_CARROT ||
                type == Material.MAGMA_CREAM ||
                type == Material.SUGAR ||
                type == Material.RABBIT_FOOT ||
                type == Material.SPIDER_EYE ||
                type == Material.PUFFERFISH ||
                type == Material.GHAST_TEAR ||
                type == Material.BLAZE_POWDER) {
            return ADVANCED_BREW_POINTS;
        }

        // Очень редкие
        if (type == Material.TURTLE_HELMET ||
                type == Material.PHANTOM_MEMBRANE) {
            return ADVANCED_BREW_POINTS + 2;
        }

        return BASE_BREW_POINTS;
    }
}