package ru.guildforge.data;

import ru.guildforge.guilds.GuildType;
import ru.guildforge.guilds.Rank;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private final UUID playerUUID;
    private GuildType guild;
    private int contribution;
    private Rank rank;
    private final Map<String, Long> cooldowns;
    private long lastLogin;
    private long totalPlayTime;
    private int exclusiveItemsClaimed;
    private final Map<String, Integer> guildStats;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.guild = null;
        this.contribution = 0;
        this.rank = null;
        this.cooldowns = new HashMap<>();
        this.lastLogin = System.currentTimeMillis();
        this.totalPlayTime = 0;
        this.exclusiveItemsClaimed = 0;
        this.guildStats = new HashMap<>();
    }

    public PlayerData(UUID playerUUID, GuildType guild, int contribution, Map<String, Long> cooldowns) {
        this.playerUUID = playerUUID;
        this.guild = guild;
        this.contribution = contribution;
        this.cooldowns = cooldowns != null ? cooldowns : new HashMap<>();
        this.lastLogin = System.currentTimeMillis();
        this.totalPlayTime = 0;
        this.exclusiveItemsClaimed = 0;
        this.guildStats = new HashMap<>();

        if (guild != null) {
            this.rank = Rank.getRank(guild, contribution);
        }
    }

    /**
     * Получить UUID игрока
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Получить тип гильдии игрока
     */
    public GuildType getGuild() {
        return guild;
    }

    /**
     * Установить гильдию игрока
     */
    public void setGuild(GuildType guild) {
        this.guild = guild;
        if (guild != null) {
            this.rank = Rank.getRank(guild, contribution);
            // Сброс статистики при смене гильдии
            guildStats.clear();
        } else {
            this.rank = null;
        }
    }

    /**
     * Проверить, есть ли у игрока гильдия
     */
    public boolean hasGuild() {
        return guild != null;
    }

    /**
     * Получить количество Contribution
     */
    public int getContribution() {
        return contribution;
    }

    /**
     * Добавить Contribution
     */
    public void addContribution(int amount) {
        if (amount > 0) {
            this.contribution += amount;
            updateRank();
        }
    }

    /**
     * Установить Contribution (только для админов)
     */
    public void setContribution(int contribution) {
        this.contribution = Math.max(0, contribution);
        updateRank();
    }

    /**
     * Снять Contribution (для покупок)
     */
    public boolean removeContribution(int amount) {
        if (this.contribution >= amount) {
            this.contribution -= amount;
            updateRank();
            return true;
        }
        return false;
    }

    /**
     * Получить текущий ранг
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Проверить, является ли игрок мастером (высший ранг)
     */
    public boolean isMaster() {
        return rank != null && rank.isMaster();
    }

    /**
     * Обновить ранг на основе Contribution
     */
    private void updateRank() {
        if (guild != null) {
            Rank newRank = Rank.getRank(guild, contribution);
            if (newRank != rank) {
                this.rank = newRank;
            }
        }
    }

    /**
     * Получить прогресс до следующего ранга (0-100%)
     */
    public double getRankProgress() {
        if (guild == null || rank == null) return 0;

        int currentContribution = contribution;
        int min = rank.getMinContribution();
        int max = rank.getMaxContribution();

        if (max == Integer.MAX_VALUE) return 100.0; // Максимальный ранг

        int needed = max - min;
        int have = currentContribution - min;

        return (have * 100.0) / needed;
    }

    /**
     * Получить оставшееся Contribution до следующего ранга
     */
    public int getContributionToNextRank() {
        if (guild == null || rank == null) return 0;

        int max = rank.getMaxContribution();
        if (max == Integer.MAX_VALUE) return 0;

        return max - contribution;
    }

    // ===== Cooldown Methods =====

    /**
     * Проверить, есть ли кулдаун по ключу
     */
    public boolean isOnCooldown(String key) {
        if (!cooldowns.containsKey(key)) return false;
        return cooldowns.get(key) > System.currentTimeMillis();
    }

    /**
     * Получить оставшееся время кулдауна в миллисекундах
     */
    public long getCooldownRemaining(String key) {
        if (!cooldowns.containsKey(key)) return 0;
        long remaining = cooldowns.get(key) - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * Получить оставшееся время кулдауна в секундах
     */
    public long getCooldownRemainingSeconds(String key) {
        return getCooldownRemaining(key) / 1000;
    }

    /**
     * Получить оставшееся время кулдауна в читаемом формате
     */
    public String getCooldownRemainingFormatted(String key) {
        long remaining = getCooldownRemainingSeconds(key);

        if (remaining <= 0) {
            return "§aДоступно";
        }

        long hours = remaining / 3600;
        long minutes = (remaining % 3600) / 60;
        long seconds = remaining % 60;

        if (hours > 0) {
            return String.format("§c%dч %dмин", hours, minutes);
        } else if (minutes > 0) {
            return String.format("§c%dмин %dсек", minutes, seconds);
        } else {
            return String.format("§c%dсек", seconds);
        }
    }

    /**
     * Установить кулдаун (в миллисекундах)
     */
    public void setCooldown(String key, long durationMillis) {
        cooldowns.put(key, System.currentTimeMillis() + durationMillis);
    }

    /**
     * Установить кулдаун (в секундах)
     */
    public void setCooldownSeconds(String key, long seconds) {
        setCooldown(key, seconds * 1000);
    }

    /**
     * Установить кулдаун (в минутах)
     */
    public void setCooldownMinutes(String key, long minutes) {
        setCooldown(key, minutes * 60 * 1000);
    }

    /**
     * Установить кулдаун (в часах)
     */
    public void setCooldownHours(String key, long hours) {
        setCooldown(key, hours * 60 * 60 * 1000);
    }

    /**
     * Установить кулдаун (в днях)
     */
    public void setCooldownDays(String key, int days) {
        setCooldown(key, days * 24 * 60 * 60 * 1000L);
    }

    /**
     * Снять кулдаун
     */
    public void removeCooldown(String key) {
        cooldowns.remove(key);
    }

    /**
     * Получить все кулдауны
     */
    public Map<String, Long> getCooldowns() {
        return cooldowns;
    }

    // ===== Play Time Methods =====

    /**
     * Обновить время последнего входа
     */
    public void updateLastLogin() {
        this.lastLogin = System.currentTimeMillis();
    }

    /**
     * Получить время последнего входа
     */
    public long getLastLogin() {
        return lastLogin;
    }

    /**
     * Добавить время игры
     */
    public void addPlayTime(long millis) {
        this.totalPlayTime += millis;
    }

    /**
     * Получить общее время игры в миллисекундах
     */
    public long getTotalPlayTime() {
        return totalPlayTime;
    }

    /**
     * Получить общее время игры в читаемом формате
     */
    public String getTotalPlayTimeFormatted() {
        long seconds = totalPlayTime / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return hours + "ч " + minutes + "мин";
        } else {
            return minutes + "мин";
        }
    }

    // ===== Stats Methods =====

    /**
     * Получить количество полученных эксклюзивных предметов
     */
    public int getExclusiveItemsClaimed() {
        return exclusiveItemsClaimed;
    }

    /**
     * Увеличить счетчик полученных эксклюзивных предметов
     */
    public void incrementExclusiveItemsClaimed() {
        this.exclusiveItemsClaimed++;
    }

    /**
     * Получить статистику по гильдии
     */
    public int getGuildStat(String stat) {
        return guildStats.getOrDefault(stat, 0);
    }

    /**
     * Увеличить статистику по гильдии
     */
    public void incrementGuildStat(String stat) {
        guildStats.put(stat, guildStats.getOrDefault(stat, 0) + 1);
    }

    /**
     * Увеличить статистику по гильдии на определенное количество
     */
    public void addGuildStat(String stat, int amount) {
        guildStats.put(stat, guildStats.getOrDefault(stat, 0) + amount);
    }

    /**
     * Установить статистику по гильдии
     */
    public void setGuildStat(String stat, int value) {
        guildStats.put(stat, value);
    }

    /**
     * Получить все статистики гильдии
     */
    public Map<String, Integer> getGuildStats() {
        return guildStats;
    }

    // ===== Utility Methods =====

    /**
     * Получить цвет гильдии
     */
    public String getGuildColor() {
        return guild != null ? guild.getColorCode() : "&7";
    }

    /**
     * Получить полное имя с префиксом
     */
    public String getFullName(String playerName) {
        if (guild != null && rank != null) {
            return guild.getColorCode() + "[" + guild.getDisplayName() + " " + rank.getDisplayName() + "] " +
                    "&7" + playerName;
        }
        return "&7" + playerName;
    }

    /**
     * Проверить, может ли игрок использовать эксклюзивный предмет
     */
    public boolean canUseExclusiveItem(int requiredLevel, int requiredContribution) {
        // Временно убираем проверку уровня, так как уровни не хранятся в PlayerData
        return hasGuild() && isMaster() && contribution >= requiredContribution;
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "uuid=" + playerUUID +
                ", guild=" + (guild != null ? guild.getDisplayName() : "null") +
                ", rank=" + (rank != null ? rank.getDisplayName() : "null") +
                ", contribution=" + contribution +
                ", cooldowns=" + cooldowns.size() +
                '}';
    }
}