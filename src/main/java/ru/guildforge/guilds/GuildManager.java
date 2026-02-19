package ru.guildforge.guilds;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import ru.guildforge.GuildForge;
import ru.guildforge.data.PlayerData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuildManager {
    private final GuildForge plugin;
    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();

    public GuildManager(GuildForge plugin) {
        this.plugin = plugin;
    }

    /**
     * Получить данные игрока из кэша
     */
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    /**
     * Добавить игрока в кэш
     */
    public void addToCache(UUID uuid, PlayerData data) {
        playerDataCache.put(uuid, data);
    }

    /**
     * Удалить игрока из кэша
     */
    public void removeFromCache(UUID uuid) {
        playerDataCache.remove(uuid);
    }

    /**
     * Сохранить все данные из кэша в БД
     */
    public void saveAllData() {
        int saved = 0;
        for (PlayerData data : playerDataCache.values()) {
            plugin.getDatabaseManager().savePlayerData(data);
            saved++;
        }
        plugin.getLogger().info("§a[GuildManager] Сохранено " + saved + " профилей игроков");
    }

    /**
     * Вступление в гильдию
     */
    public boolean joinGuild(Player player, GuildType guild) {
        UUID uuid = player.getUniqueId();
        PlayerData data = getPlayerData(uuid);

        if (data == null) {
            player.sendMessage("§cОшибка загрузки данных!");
            return false;
        }

        if (data.hasGuild()) {
            player.sendMessage("§cВы уже состоите в гильдии!");
            return false;
        }

        // Устанавливаем гильдию (ранг определится автоматически через getRank)
        data.setGuild(guild);
        data.setContribution(0);

        // Сохраняем в БД
        plugin.getDatabaseManager().savePlayerData(data);

        // Сообщение игроку
        player.sendMessage("§aВы вступили в гильдию " + guild.getColorCode() + guild.getDisplayName());
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, SoundCategory.PLAYERS, 1.0f, 1.0f);

        // ВЫЗОВ API СОБЫТИЯ
        plugin.getAPIImpl().fireGuildJoin(player, guild);

        plugin.getLogger().info("Игрок " + player.getName() + " вступил в гильдию " + guild.name());

        return true;
    }

    /**
     * Выход из гильдии
     */
    public boolean leaveGuild(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = getPlayerData(uuid);

        if (data == null || !data.hasGuild()) {
            player.sendMessage("§cВы не состоите в гильдии!");
            return false;
        }

        GuildType oldGuild = data.getGuild();
        int oldContribution = data.getContribution();

        // Сбрасываем данные
        data.setGuild(null);
        data.setContribution(0);

        // Сохраняем в БД
        plugin.getDatabaseManager().savePlayerData(data);

        // Сообщение игроку
        player.sendMessage("§cВы покинули гильдию " + oldGuild.getColorCode() + oldGuild.getDisplayName());
        player.sendMessage("§7Весь прогресс (Contribution: §e" + oldContribution + "§7) сброшен");
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, SoundCategory.PLAYERS, 1.0f, 1.0f);

        // ВЫЗОВ API СОБЫТИЯ
        plugin.getAPIImpl().fireGuildLeave(player, oldGuild);

        plugin.getLogger().info("Игрок " + player.getName() + " покинул гильдию " + oldGuild.name());

        return true;
    }

    /**
     * Добавить очки вклада игроку
     */
    public void addContribution(Player player, int amount) {
        PlayerData data = getPlayerData(player.getUniqueId());

        if (data == null || !data.hasGuild()) {
            return;
        }

        int oldTotal = data.getContribution();
        int newTotal = oldTotal + amount;

        // Защита от переполнения
        if (newTotal < 0) {
            newTotal = Integer.MAX_VALUE;
        }

        // Получаем старый и новый ранги (не сохраняются в данных)
        Rank oldRank = Rank.getRank(data.getGuild(), oldTotal);
        data.setContribution(newTotal);
        Rank newRank = Rank.getRank(data.getGuild(), newTotal);

        // Проверяем повышение ранга
        if (!oldRank.equals(newRank)) {
            handleRankUp(player, data, oldRank, newRank);
        }

        // Сохраняем в БД
        plugin.getDatabaseManager().savePlayerData(data);

        // Показываем полученные очки
        player.sendActionBar("§a+" + amount + " Contribution");

        // ВЫЗОВ API СОБЫТИЯ
        plugin.getAPIImpl().fireContributionGain(player, amount, newTotal);
    }

    /**
     * Списать очки вклада у игрока
     * @return true если успешно списано
     */
    public boolean takeContribution(Player player, int amount) {
        PlayerData data = getPlayerData(player.getUniqueId());

        if (data == null || !data.hasGuild()) {
            return false;
        }

        int oldTotal = data.getContribution();
        if (oldTotal < amount) {
            return false;
        }

        int newTotal = oldTotal - amount;
        data.setContribution(newTotal);

        // Сохраняем в БД
        plugin.getDatabaseManager().savePlayerData(data);

        // Показываем списанные очки
        player.sendActionBar("§c-" + amount + " Contribution");

        // ВЫЗОВ API СОБЫТИЯ
        plugin.getAPIImpl().fireContributionSpend(player, amount, newTotal);

        return true;
    }

    /**
     * Установить количество очков вклада
     */
    public void setContribution(Player player, int amount) {
        PlayerData data = getPlayerData(player.getUniqueId());

        if (data == null || !data.hasGuild()) {
            return;
        }

        int oldTotal = data.getContribution();
        int newTotal = Math.max(0, amount);

        Rank oldRank = Rank.getRank(data.getGuild(), oldTotal);
        data.setContribution(newTotal);
        Rank newRank = Rank.getRank(data.getGuild(), newTotal);

        // Проверяем повышение/понижение ранга
        if (!oldRank.equals(newRank)) {
            if (newRank.ordinal() > oldRank.ordinal()) {
                handleRankUp(player, data, oldRank, newRank);
            }
        }

        plugin.getDatabaseManager().savePlayerData(data);

        int diff = newTotal - oldTotal;
        if (diff > 0) {
            plugin.getAPIImpl().fireContributionGain(player, diff, newTotal);
        } else if (diff < 0) {
            plugin.getAPIImpl().fireContributionSpend(player, -diff, newTotal);
        }
    }

    /**
     * Обработка повышения ранга
     */
    private void handleRankUp(Player player, PlayerData data, Rank oldRank, Rank newRank) {
        // Сообщение о повышении
        player.sendMessage(" ");
        player.sendMessage("§6§l⚡ ПОВЫШЕНИЕ РАНГА! ⚡");
        player.sendMessage("§7Ваш новый ранг: " + data.getGuild().getColorCode() + newRank.getDisplayName());
        player.sendMessage(" ");

        // Звук
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0f, 1.0f);

        // Сообщение о доступности эксклюзива
        if (data.isMaster()) {
            player.sendMessage("§6§lТЕПЕРЬ ВАМ ДОСТУПЕН ЭКСКЛЮЗИВНЫЙ ПРЕДМЕТ!");
            player.sendMessage("§7Зайдите в меню гильдии (/guild) чтобы получить его!");
        }

        // ВЫЗОВ API СОБЫТИЯ
        plugin.getAPIImpl().fireRankUp(player, oldRank, newRank);

        plugin.getLogger().info("Игрок " + player.getName() + " повысил ранг: " +
                oldRank.name() + " -> " + newRank.name());
    }

    /**
     * Получить префикс игрока для PlaceholderAPI
     */
    public String getPlayerPrefix(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());

        if (data == null || !data.hasGuild()) {
            return "";
        }

        GuildType guild = data.getGuild();
        Rank rank = Rank.getRank(guild, data.getContribution());

        return guild.getColorCode() + "[" + guild.getDisplayName() + " " + rank.getDisplayName() + "]§r";
    }

    /**
     * Получить префикс игрока по UUID
     */
    public String getPlayerPrefix(UUID uuid) {
        PlayerData data = getPlayerData(uuid);

        if (data == null || !data.hasGuild()) {
            return "";
        }

        GuildType guild = data.getGuild();
        Rank rank = Rank.getRank(guild, data.getContribution());

        return guild.getColorCode() + "[" + guild.getDisplayName() + " " + rank.getDisplayName() + "]§r";
    }

    /**
     * Проверить, состоит ли игрок в гильдии
     */
    public boolean hasGuild(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        return data != null && data.hasGuild();
    }

    /**
     * Проверить, состоит ли игрок в гильдии (по UUID)
     */
    public boolean hasGuild(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        return data != null && data.hasGuild();
    }

    /**
     * Получить гильдию игрока
     */
    public GuildType getPlayerGuild(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        return data != null ? data.getGuild() : null;
    }

    /**
     * Получить гильдию игрока (по UUID)
     */
    public GuildType getPlayerGuild(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        return data != null ? data.getGuild() : null;
    }

    /**
     * Получить ранг игрока (вычисляется на лету)
     */
    public Rank getPlayerRank(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        if (data == null || !data.hasGuild()) return null;
        return Rank.getRank(data.getGuild(), data.getContribution());
    }

    /**
     * Получить ранг игрока (по UUID)
     */
    public Rank getPlayerRank(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        if (data == null || !data.hasGuild()) return null;
        return Rank.getRank(data.getGuild(), data.getContribution());
    }

    /**
     * Получить очки вклада игрока
     */
    public int getPlayerContribution(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        return data != null ? data.getContribution() : 0;
    }

    /**
     * Получить очки вклада игрока (по UUID)
     */
    public int getPlayerContribution(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        return data != null ? data.getContribution() : 0;
    }

    /**
     * Является ли игрок Мастером
     */
    public boolean isMaster(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        if (data == null || !data.hasGuild()) return false;
        Rank rank = Rank.getRank(data.getGuild(), data.getContribution());
        return rank.isMaster();
    }

    /**
     * Является ли игрок Мастером (по UUID)
     */
    public boolean isMaster(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        if (data == null || !data.hasGuild()) return false;
        Rank rank = Rank.getRank(data.getGuild(), data.getContribution());
        return rank.isMaster();
    }

    /**
     * Получить список всех игроков в гильдии (онлайн)
     */
    public Map<UUID, PlayerData> getGuildMembers(GuildType guild) {
        Map<UUID, PlayerData> members = new ConcurrentHashMap<>();

        for (Map.Entry<UUID, PlayerData> entry : playerDataCache.entrySet()) {
            if (entry.getValue() != null && entry.getValue().getGuild() == guild) {
                members.put(entry.getKey(), entry.getValue());
            }
        }

        return members;
    }

    /**
     * Получить количество игроков в гильдии (онлайн)
     */
    public int getGuildMemberCount(GuildType guild) {
        int count = 0;

        for (PlayerData data : playerDataCache.values()) {
            if (data != null && data.getGuild() == guild) {
                count++;
            }
        }

        return count;
    }

    /**
     * Получить общее количество очков гильдии (онлайн)
     */
    public int getGuildTotalContribution(GuildType guild) {
        int total = 0;

        for (PlayerData data : playerDataCache.values()) {
            if (data != null && data.getGuild() == guild) {
                total += data.getContribution();
            }
        }

        return total;
    }

    /**
     * Загрузить данные игрока из БД
     */
    public void loadPlayerData(UUID uuid) {
        plugin.getDatabaseManager().loadPlayerData(uuid).thenAccept(data -> {
            if (data != null) {
                playerDataCache.put(uuid, data);
            }
        });
    }

        /**
         * Выгрузить данные игрока из кэша (при выходе)
         */
    public void unloadPlayerData(UUID uuid) {
        PlayerData data = playerDataCache.remove(uuid);
        if (data != null) {
            plugin.getDatabaseManager().savePlayerData(data);
        }
    }
}