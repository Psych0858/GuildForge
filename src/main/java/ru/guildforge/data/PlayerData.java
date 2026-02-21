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
    private Rank rank; // Теперь ранг хранится отдельно и НЕ обновляется автоматически
    private final Map<String, Long> cooldowns;
    private long lastLogin;
    private long totalPlayTime;
    private int exclusiveItemsClaimed;
    private final Map<String, Integer> guildStats;

    private UUID guildLeader;
    private boolean isLeader;

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
        this.guildLeader = null;
        this.isLeader = false;
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
        this.guildLeader = null;
        this.isLeader = false;

        // УСТАНАВЛИВАЕМ НАЧАЛЬНЫЙ РАНГ, НО НЕ БУДЕМ ЕГО АВТОМАТИЧЕСКИ ОБНОВЛЯТЬ
        if (guild != null) {
            this.rank = Rank.getRank(guild, contribution);
        }
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public GuildType getGuild() {
        return guild;
    }

    public void setGuild(GuildType guild) {
        this.guild = guild;
        if (guild != null) {
            // При смене гильдии устанавливаем начальный ранг
            this.rank = Rank.getRank(guild, contribution);
            guildStats.clear();
            this.guildLeader = null;
            this.isLeader = false;
        } else {
            this.rank = null;
        }
    }

    public boolean hasGuild() {
        return guild != null;
    }

    public int getContribution() {
        return contribution;
    }

    /**
     * Добавить Contribution (БЕЗ автоматического обновления ранга)
     */
    public void addContribution(int amount) {
        if (amount > 0) {
            this.contribution += amount;
            // ❌ НЕ вызываем updateRank() - ранг меняется только через главу
        }
    }

    /**
     * Установить Contribution (только для админов)
     */
    public void setContribution(int contribution) {
        this.contribution = Math.max(0, contribution);
        // ❌ НЕ вызываем updateRank() - ранг меняется только через главу
    }

    /**
     * Снять Contribution (для покупок)
     */
    public boolean removeContribution(int amount) {
        if (this.contribution >= amount) {
            this.contribution -= amount;
            // ❌ НЕ вызываем updateRank() - ранг меняется только через главу
            return true;
        }
        return false;
    }

    /**
     * ПОЛУЧИТЬ ТЕКУЩИЙ РАНГ (хранится отдельно, не вычисляется на лету)
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * УСТАНОВИТЬ РАНГ ВРУЧНУЮ (вызывается только главой или админом)
     */
    public void setRank(Rank newRank) {
        this.rank = newRank;
    }

    /**
     * Проверить, является ли игрок мастером (высший ранг)
     */
    public boolean isMaster() {
        return rank != null && rank.isMaster();
    }

    /**
     * ❌ МЕТОД УДАЛЕН - больше нет автоматического обновления ранга
     * private void updateRank() { ... }
     */

    /**
     * Получить прогресс до следующего ранга (0-100%) - ТОЛЬКО ДЛЯ ИНФОРМАЦИИ
     */
    public double getRankProgress() {
        if (guild == null || rank == null) return 0;

        int currentContribution = contribution;
        int min = rank.getMinContribution();
        int max = rank.getMaxContribution();

        if (max == Integer.MAX_VALUE) return 100.0;

        int needed = max - min;
        int have = currentContribution - min;

        return (have * 100.0) / needed;
    }

    /**
     * Получить оставшееся Contribution до следующего ранга - ТОЛЬКО ДЛЯ ИНФОРМАЦИИ
     */
    public int getContributionToNextRank() {
        if (guild == null || rank == null) return 0;

        Rank nextRank = getNextRank();
        if (nextRank == rank) return 0;

        return nextRank.getMinContribution() - contribution;
    }

    /**
     * Получить следующий ранг (для информации)
     */
    public Rank getNextRank() {
        if (guild == null || rank == null) return rank;

        Rank[] ranks = Rank.values();
        boolean found = false;

        for (Rank r : ranks) {
            if (r.getGuildName().equals(guild.getDisplayName())) {
                if (found) return r;
                if (r == rank) found = true;
            }
        }
        return rank;
    }

    // ===== Cooldown Methods =====

    public boolean isOnCooldown(String key) {
        if (!cooldowns.containsKey(key)) return false;
        return cooldowns.get(key) > System.currentTimeMillis();
    }

    public long getCooldownRemaining(String key) {
        if (!cooldowns.containsKey(key)) return 0;
        long remaining = cooldowns.get(key) - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public long getCooldownRemainingSeconds(String key) {
        return getCooldownRemaining(key) / 1000;
    }

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

    public void setCooldown(String key, long durationMillis) {
        cooldowns.put(key, System.currentTimeMillis() + durationMillis);
    }

    public void setCooldownSeconds(String key, long seconds) {
        setCooldown(key, seconds * 1000);
    }

    public void setCooldownMinutes(String key, long minutes) {
        setCooldown(key, minutes * 60 * 1000);
    }

    public void setCooldownHours(String key, long hours) {
        setCooldown(key, hours * 60 * 60 * 1000);
    }

    public void setCooldownDays(String key, int days) {
        setCooldown(key, days * 24 * 60 * 60 * 1000L);
    }

    public void removeCooldown(String key) {
        cooldowns.remove(key);
    }

    public Map<String, Long> getCooldowns() {
        return cooldowns;
    }

    // ===== Play Time Methods =====

    public void updateLastLogin() {
        this.lastLogin = System.currentTimeMillis();
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void addPlayTime(long millis) {
        this.totalPlayTime += millis;
    }

    public long getTotalPlayTime() {
        return totalPlayTime;
    }

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

    public int getExclusiveItemsClaimed() {
        return exclusiveItemsClaimed;
    }

    public void incrementExclusiveItemsClaimed() {
        this.exclusiveItemsClaimed++;
    }

    public int getGuildStat(String stat) {
        return guildStats.getOrDefault(stat, 0);
    }

    public void incrementGuildStat(String stat) {
        guildStats.put(stat, guildStats.getOrDefault(stat, 0) + 1);
    }

    public void addGuildStat(String stat, int amount) {
        guildStats.put(stat, guildStats.getOrDefault(stat, 0) + amount);
    }

    public void setGuildStat(String stat, int value) {
        guildStats.put(stat, value);
    }

    public Map<String, Integer> getGuildStats() {
        return guildStats;
    }

    // ===== Guild Leader Methods =====

    public void setGuildLeader(UUID leaderUUID) {
        this.guildLeader = leaderUUID;
        this.isLeader = this.playerUUID.equals(leaderUUID);
    }

    public UUID getGuildLeader() {
        return guildLeader;
    }

    public boolean isGuildLeader() {
        return isLeader;
    }

    public boolean isLeaderOf(UUID playerUUID) {
        return guildLeader != null && guildLeader.equals(playerUUID);
    }

    public void resetGuildLeader() {
        this.guildLeader = null;
        this.isLeader = false;
    }

    // ===== Utility Methods =====

    public String getGuildColor() {
        return guild != null ? guild.getColorCode() : "&7";
    }

    public String getFullName(String playerName) {
        if (guild != null && rank != null) {
            String prefix = guild.getColorCode() + "[" + guild.getDisplayName() + " " + rank.getDisplayName() + "] ";
            if (isLeader) {
                return prefix + "§6§l👑 " + playerName + " §6§l👑";
            }
            return prefix + "§7" + playerName;
        }
        return "§7" + playerName;
    }

    public boolean canUseExclusiveItem(int requiredLevel, int requiredContribution) {
        return hasGuild() && isMaster() && contribution >= requiredContribution;
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "uuid=" + playerUUID +
                ", guild=" + (guild != null ? guild.getDisplayName() : "null") +
                ", rank=" + (rank != null ? rank.getDisplayName() : "null") +
                ", contribution=" + contribution +
                ", isLeader=" + isLeader +
                ", cooldowns=" + cooldowns.size() +
                '}';
    }
}