package ru.guildforge.utils;

import org.bukkit.entity.Player;
import ru.guildforge.GuildForge;
import ru.guildforge.data.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownManager {
    private final GuildForge plugin;
    private final Map<UUID, Map<String, Long>> cooldowns;

    public CooldownManager(GuildForge plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
    }

    /**
     * Проверить, есть ли у игрока кулдаун
     */
    public boolean hasCooldown(Player player, String key) {
        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
        return data != null && data.isOnCooldown(key);
    }

    /**
     * Получить оставшееся время кулдауна в миллисекундах
     */
    public long getCooldownRemaining(Player player, String key) {
        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
        return data != null ? data.getCooldownRemaining(key) : 0;
    }

    /**
     * Получить оставшееся время в читаемом формате
     */
    public String getCooldownRemainingFormatted(Player player, String key) {
        long remaining = getCooldownRemaining(player, key);

        if (remaining <= 0) {
            return "§aДоступно";
        }

        long seconds = remaining / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("§c%dч %dмин", hours, minutes);
        } else if (minutes > 0) {
            return String.format("§c%dмин %dсек", minutes, secs);
        } else {
            return String.format("§c%dсек", secs);
        }
    }

    /**
     * Установить кулдаун
     */
    public void setCooldown(Player player, String key, long duration, TimeUnit unit) {
        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
        if (data != null) {
            long millis = unit.toMillis(duration);
            data.setCooldown(key, millis);
            plugin.getDatabaseManager().savePlayerData(data);
        }
    }

    /**
     * Установить кулдаун в часах
     */
    public void setCooldownHours(Player player, String key, int hours) {
        setCooldown(player, key, hours, TimeUnit.HOURS);
    }

    /**
     * Установить кулдаун в днях
     */
    public void setCooldownDays(Player player, String key, int days) {
        setCooldown(player, key, days, TimeUnit.DAYS);
    }

    /**
     * Снять кулдаун
     */
    public void removeCooldown(Player player, String key) {
        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
        if (data != null) {
            data.getCooldowns().remove(key);
            plugin.getDatabaseManager().savePlayerData(data);
        }
    }

    /**
     * Очистить все кулдауны игрока
     */
    public void clearAllCooldowns(Player player) {
        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
        if (data != null) {
            data.getCooldowns().clear();
            plugin.getDatabaseManager().savePlayerData(data);
        }
    }
}