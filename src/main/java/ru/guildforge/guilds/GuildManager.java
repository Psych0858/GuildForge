package ru.guildforge.guilds;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import ru.guildforge.GuildForge;
import ru.guildforge.data.PlayerData;

import java.util.ArrayList;
import java.util.List;
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

        data.setGuild(guild);
        data.setContribution(0);
        data.resetGuildLeader();

        plugin.getDatabaseManager().savePlayerData(data);

        player.sendMessage("§aВы вступили в гильдию " + guild.getColorCode() + guild.getDisplayName());
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, SoundCategory.PLAYERS, 1.0f, 1.0f);

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

        if (data.isGuildLeader()) {
            for (Map.Entry<UUID, PlayerData> entry : playerDataCache.entrySet()) {
                PlayerData memberData = entry.getValue();
                if (memberData != null && memberData.hasGuild() &&
                        memberData.getGuild() == oldGuild && !entry.getKey().equals(uuid)) {
                    memberData.resetGuildLeader();
                    plugin.getDatabaseManager().savePlayerData(memberData);

                    Player member = plugin.getServer().getPlayer(entry.getKey());
                    if (member != null && member.isOnline()) {
                        member.sendMessage("§cГлава гильдии покинул гильдию! Статус главы сброшен.");
                    }
                }
            }
        }

        data.setGuild(null);
        data.setContribution(0);
        data.resetGuildLeader();

        plugin.getDatabaseManager().savePlayerData(data);

        player.sendMessage("§cВы покинули гильдию " + oldGuild.getColorCode() + oldGuild.getDisplayName());
        player.sendMessage("§7Весь прогресс (Contribution: §e" + oldContribution + "§7) сброшен");
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, SoundCategory.PLAYERS, 1.0f, 1.0f);

        plugin.getAPIImpl().fireGuildLeave(player, oldGuild);
        plugin.getLogger().info("Игрок " + player.getName() + " покинул гильдию " + oldGuild.name());

        return true;
    }

    /**
     * Добавить очки вклада игроку (БЕЗ автоматического повышения)
     */
    public void addContribution(Player player, int amount) {
        PlayerData data = getPlayerData(player.getUniqueId());

        if (data == null || !data.hasGuild()) {
            return;
        }

        int oldTotal = data.getContribution();
        int newTotal = oldTotal + amount;

        if (newTotal < 0) {
            newTotal = Integer.MAX_VALUE;
        }

        data.setContribution(newTotal);

        plugin.getDatabaseManager().savePlayerData(data);
        player.sendActionBar("§a+" + amount + " Contribution");
        plugin.getAPIImpl().fireContributionGain(player, amount, newTotal);

        Rank currentRank = Rank.getRank(data.getGuild(), data.getContribution());
        Rank nextRank = getNextRank(data.getGuild(), currentRank);
        if (nextRank != currentRank && data.getContribution() >= nextRank.getMinContribution()) {
            player.sendMessage("§e§l⚡ У вас достаточно очков для повышения! Обратитесь к главе гильдии.");
        }
    }

    /**
     * Списать очки вклада у игрока
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

        plugin.getDatabaseManager().savePlayerData(data);
        player.sendActionBar("§c-" + amount + " Contribution");
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

        data.setContribution(newTotal);
        plugin.getDatabaseManager().savePlayerData(data);

        int diff = newTotal - oldTotal;
        if (diff > 0) {
            plugin.getAPIImpl().fireContributionGain(player, diff, newTotal);
        } else if (diff < 0) {
            plugin.getAPIImpl().fireContributionSpend(player, -diff, newTotal);
        }
    }

    // ===== МЕТОДЫ ДЛЯ УПРАВЛЕНИЯ РАНГАМИ =====

    /**
     * Назначить главу гильдии (только для админов/OP)
     */
    public boolean setGuildLeader(Player admin, Player targetPlayer) {
        if (admin != null && !admin.isOp() && !admin.hasPermission("guildforge.admin.setleader")) {
            admin.sendMessage("§cТолько администраторы могут назначать глав гильдий!");
            return false;
        }

        PlayerData targetData = getPlayerData(targetPlayer.getUniqueId());

        if (targetData == null || !targetData.hasGuild()) {
            if (admin != null) admin.sendMessage("§cИгрок не состоит в гильдии!");
            return false;
        }

        GuildType guild = targetData.getGuild();

        // Снимаем статус главы с текущего главы этой гильдии
        for (Map.Entry<UUID, PlayerData> entry : playerDataCache.entrySet()) {
            PlayerData data = entry.getValue();
            if (data != null && data.hasGuild() && data.getGuild() == guild && data.isGuildLeader()) {
                data.resetGuildLeader();
                plugin.getDatabaseManager().savePlayerData(data);

                Player oldLeader = plugin.getServer().getPlayer(entry.getKey());
                if (oldLeader != null && oldLeader.isOnline()) {
                    oldLeader.sendMessage("§cВы больше не являетесь главой гильдии " + guild.getColorCode() + guild.getDisplayName());
                }
                break;
            }
        }

        targetData.setGuildLeader(targetPlayer.getUniqueId());
        plugin.getDatabaseManager().savePlayerData(targetData);

        if (admin != null) {
            admin.sendMessage("§aИгрок §e" + targetPlayer.getName() + " §aназначен главой гильдии " +
                    guild.getColorCode() + guild.getDisplayName());
        }

        targetPlayer.sendMessage("§6§lПОЗДРАВЛЯЕМ!");
        targetPlayer.sendMessage("§aВы назначены главой гильдии " + guild.getColorCode() + guild.getDisplayName());
        targetPlayer.sendMessage("§7Теперь вы можете повышать и понижать игроков в ранге через меню гильдии!");

        targetPlayer.playSound(targetPlayer.getLocation(),
                Sound.UI_TOAST_CHALLENGE_COMPLETE,
                SoundCategory.PLAYERS, 1.0f, 1.0f);

        plugin.getLogger().info((admin != null ? admin.getName() : "Console") + " назначил " +
                targetPlayer.getName() + " главой гильдии " + guild.name());

        return true;
    }

    /**
     * Проверить, может ли глава управлять игроком
     */
    public boolean canManagePlayer(Player leader, Player target) {
        PlayerData leaderData = getPlayerData(leader.getUniqueId());
        PlayerData targetData = getPlayerData(target.getUniqueId());

        if (leaderData == null || targetData == null) return false;
        if (!leaderData.hasGuild() || !targetData.hasGuild()) return false;
        if (leaderData.getGuild() != targetData.getGuild()) return false;
        if (!leaderData.isGuildLeader()) return false;
        if (targetData.isGuildLeader()) return false;

        return true;
    }

    /**
     * Повысить игрока в ранге
     */
    public boolean promotePlayer(Player leader, Player target) {
        if (!canManagePlayer(leader, target)) {
            leader.sendMessage("§cВы не можете повысить этого игрока!");
            return false;
        }

        PlayerData targetData = getPlayerData(target.getUniqueId());
        Rank currentRank = targetData.getRank();
        Rank nextRank = getNextRank(targetData.getGuild(), currentRank);

        if (nextRank == null || nextRank == currentRank) {
            leader.sendMessage("§cИгрок уже имеет максимальный ранг!");
            return false;
        }

        if (targetData.getContribution() < nextRank.getMinContribution()) {
            int needed = nextRank.getMinContribution() - targetData.getContribution();
            leader.sendMessage("§cНедостаточно очков! Нужно ещё §e" + needed + " §cContribution");
            return false;
        }

        // УСТАНАВЛИВАЕМ НОВЫЙ РАНГ ВРУЧНУЮ
        targetData.setRank(nextRank);
        plugin.getDatabaseManager().savePlayerData(targetData);

        leader.sendMessage("§aВы повысили игрока §e" + target.getName() +
                " §aдо ранга " + targetData.getGuild().getColorCode() + nextRank.getDisplayName());

        target.sendMessage(" ");
        target.sendMessage("§6§l⚡ ПОВЫШЕНИЕ РАНГА! ⚡");
        target.sendMessage("§7Глава гильдии §e" + leader.getName() + " §7повысил вас до ранга:");
        target.sendMessage("§7Новый ранг: " + targetData.getGuild().getColorCode() + nextRank.getDisplayName());
        target.sendMessage(" ");

        target.playSound(target.getLocation(),
                Sound.UI_TOAST_CHALLENGE_COMPLETE,
                SoundCategory.PLAYERS, 1.0f, 1.0f);

        plugin.getAPIImpl().fireRankUp(target, currentRank, nextRank);

        return true;
    }

    /**
     * Понизить игрока в ранге
     */
    public boolean demotePlayer(Player leader, Player target) {
        if (!canManagePlayer(leader, target)) {
            leader.sendMessage("§cВы не можете понизить этого игрока!");
            return false;
        }

        PlayerData targetData = getPlayerData(target.getUniqueId());
        Rank currentRank = targetData.getRank();
        Rank previousRank = getPreviousRank(targetData.getGuild(), currentRank);

        if (previousRank == null || previousRank == currentRank) {
            leader.sendMessage("§cИгрок уже имеет минимальный ранг!");
            return false;
        }

        int newContribution = previousRank.getMinContribution();
        targetData.setContribution(newContribution);
        // УСТАНАВЛИВАЕМ НОВЫЙ РАНГ ВРУЧНУЮ
        targetData.setRank(previousRank);

        plugin.getDatabaseManager().savePlayerData(targetData);

        leader.sendMessage("§cВы понизили игрока §e" + target.getName() +
                " §cдо ранга " + targetData.getGuild().getColorCode() + previousRank.getDisplayName());

        target.sendMessage(" ");
        target.sendMessage("§c§l⚠ ПОНИЖЕНИЕ РАНГА ⚠");
        target.sendMessage("§7Глава гильдии §e" + leader.getName() + " §7понизил вас до ранга:");
        target.sendMessage("§7Новый ранг: " + targetData.getGuild().getColorCode() + previousRank.getDisplayName());
        target.sendMessage("§7Contribution уменьшен до §e" + newContribution);
        target.sendMessage(" ");

        target.playSound(target.getLocation(),
                Sound.BLOCK_ANVIL_LAND,
                SoundCategory.PLAYERS, 1.0f, 1.0f);

        plugin.getAPIImpl().fireRankDown(target, currentRank, previousRank);

        return true;
    }

    /**
     * Получить следующий ранг
     */
    public Rank getNextRank(GuildType guild, Rank currentRank) {
        Rank[] ranks = Rank.values();
        boolean found = false;

        for (Rank rank : ranks) {
            if (rank.getGuildName().equals(guild.getDisplayName())) {
                if (found) return rank;
                if (rank == currentRank) found = true;
            }
        }

        return currentRank;
    }

    /**
     * Получить предыдущий ранг
     */
    public Rank getPreviousRank(GuildType guild, Rank currentRank) {
        Rank[] ranks = Rank.values();
        Rank previous = null;

        for (Rank rank : ranks) {
            if (rank.getGuildName().equals(guild.getDisplayName())) {
                if (rank == currentRank) {
                    return previous != null ? previous : currentRank;
                }
                previous = rank;
            }
        }

        return currentRank;
    }

    /**
     * Получить список всех членов гильдии для GUI
     */
    public List<Player> getGuildMembersForGUI(GuildType guild) {
        List<Player> members = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData data = getPlayerData(player.getUniqueId());
            if (data != null && data.getGuild() == guild) {
                members.add(player);
            }
        }

        members.sort((p1, p2) -> {
            PlayerData d1 = getPlayerData(p1.getUniqueId());
            PlayerData d2 = getPlayerData(p2.getUniqueId());

            if (d1.isGuildLeader()) return -1;
            if (d2.isGuildLeader()) return 1;

            return Integer.compare(d2.getContribution(), d1.getContribution());
        });

        return members;
    }

    /**
     * Получить список игроков, готовых к повышению
     */
    public List<Player> getPromotableMembers(Player leader) {
        List<Player> promotable = new ArrayList<>();
        PlayerData leaderData = getPlayerData(leader.getUniqueId());

        if (leaderData == null || !leaderData.isGuildLeader()) return promotable;

        GuildType guild = leaderData.getGuild();
        List<Player> members = getGuildMembersForGUI(guild);

        for (Player member : members) {
            PlayerData memberData = getPlayerData(member.getUniqueId());
            if (memberData == null || memberData.isGuildLeader()) continue;

            Rank nextRank = getNextRank(guild, memberData.getRank());
            if (nextRank != memberData.getRank() &&
                    memberData.getContribution() >= nextRank.getMinContribution()) {
                promotable.add(member);
            }
        }

        return promotable;
    }

    /**
     * Получить список игроков, которых можно понизить
     */
    public List<Player> getDemotableMembers(Player leader) {
        List<Player> demotable = new ArrayList<>();
        PlayerData leaderData = getPlayerData(leader.getUniqueId());

        if (leaderData == null || !leaderData.isGuildLeader()) return demotable;

        GuildType guild = leaderData.getGuild();
        List<Player> members = getGuildMembersForGUI(guild);

        for (Player member : members) {
            PlayerData memberData = getPlayerData(member.getUniqueId());
            if (memberData == null || memberData.isGuildLeader()) continue;

            Rank previousRank = getPreviousRank(guild, memberData.getRank());
            if (previousRank != memberData.getRank()) {
                demotable.add(member);
            }
        }

        return demotable;
    }

    // ===== СУЩЕСТВУЮЩИЕ МЕТОДЫ =====

    public String getPlayerPrefix(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        if (data == null || !data.hasGuild()) return "";

        GuildType guild = data.getGuild();
        Rank rank = Rank.getRank(guild, data.getContribution());

        if (data.isGuildLeader()) {
            return guild.getColorCode() + "[" + guild.getDisplayName() + " " + rank.getDisplayName() + " §6§l👑]§r";
        }

        return guild.getColorCode() + "[" + guild.getDisplayName() + " " + rank.getDisplayName() + "]§r";
    }

    public String getPlayerPrefix(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        if (data == null || !data.hasGuild()) return "";

        GuildType guild = data.getGuild();
        Rank rank = Rank.getRank(guild, data.getContribution());

        if (data.isGuildLeader()) {
            return guild.getColorCode() + "[" + guild.getDisplayName() + " " + rank.getDisplayName() + " §6§l👑]§r";
        }

        return guild.getColorCode() + "[" + guild.getDisplayName() + " " + rank.getDisplayName() + "]§r";
    }

    public boolean hasGuild(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        return data != null && data.hasGuild();
    }

    public boolean hasGuild(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        return data != null && data.hasGuild();
    }

    public GuildType getPlayerGuild(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        return data != null ? data.getGuild() : null;
    }

    public GuildType getPlayerGuild(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        return data != null ? data.getGuild() : null;
    }

    public Rank getPlayerRank(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        if (data == null || !data.hasGuild()) return null;
        return Rank.getRank(data.getGuild(), data.getContribution());
    }

    public Rank getPlayerRank(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        if (data == null || !data.hasGuild()) return null;
        return Rank.getRank(data.getGuild(), data.getContribution());
    }

    public int getPlayerContribution(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        return data != null ? data.getContribution() : 0;
    }

    public int getPlayerContribution(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        return data != null ? data.getContribution() : 0;
    }

    public boolean isMaster(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        if (data == null || !data.hasGuild()) return false;
        Rank rank = Rank.getRank(data.getGuild(), data.getContribution());
        return rank.isMaster();
    }

    public boolean isMaster(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        if (data == null || !data.hasGuild()) return false;
        Rank rank = Rank.getRank(data.getGuild(), data.getContribution());
        return rank.isMaster();
    }

    public Map<UUID, PlayerData> getGuildMembers(GuildType guild) {
        Map<UUID, PlayerData> members = new ConcurrentHashMap<>();
        for (Map.Entry<UUID, PlayerData> entry : playerDataCache.entrySet()) {
            if (entry.getValue() != null && entry.getValue().getGuild() == guild) {
                members.put(entry.getKey(), entry.getValue());
            }
        }
        return members;
    }

    public int getGuildMemberCount(GuildType guild) {
        int count = 0;
        for (PlayerData data : playerDataCache.values()) {
            if (data != null && data.getGuild() == guild) {
                count++;
            }
        }
        return count;
    }

    public int getGuildTotalContribution(GuildType guild) {
        int total = 0;
        for (PlayerData data : playerDataCache.values()) {
            if (data != null && data.getGuild() == guild) {
                total += data.getContribution();
            }
        }
        return total;
    }

    public void loadPlayerData(UUID uuid) {
        plugin.getDatabaseManager().loadPlayerData(uuid).thenAccept(data -> {
            if (data != null) {
                playerDataCache.put(uuid, data);
            }
        });
    }

    public void unloadPlayerData(UUID uuid) {
        PlayerData data = playerDataCache.remove(uuid);
        if (data != null) {
            plugin.getDatabaseManager().savePlayerData(data);
        }
    }
}