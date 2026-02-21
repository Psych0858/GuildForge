package ru.guildforge.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.guildforge.GuildForge;
import ru.guildforge.data.PlayerData;
import ru.guildforge.guilds.GuildType;
import ru.guildforge.guilds.Rank;
import ru.guildforge.recipes.ExclusiveItems;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class GuildForgeAPIImpl implements GuildForgeAPI {

    private final GuildForge plugin;
    private final List<GuildEventListener> listeners = new CopyOnWriteArrayList<>();

    public GuildForgeAPIImpl(GuildForge plugin) {
        this.plugin = plugin;
    }

    // ===== БАЗОВАЯ ИНФОРМАЦИЯ =====

    @Override
    public boolean hasGuild(Player player) {
        return hasGuild(player.getUniqueId());
    }

    @Override
    public boolean hasGuild(UUID uuid) {
        PlayerData data = plugin.getGuildManager().getPlayerData(uuid);
        return data != null && data.hasGuild();
    }

    @Override
    public GuildType getPlayerGuild(Player player) {
        return getPlayerGuild(player.getUniqueId());
    }

    @Override
    public GuildType getPlayerGuild(UUID uuid) {
        PlayerData data = plugin.getGuildManager().getPlayerData(uuid);
        return data != null ? data.getGuild() : null;
    }

    @Override
    public Rank getPlayerRank(Player player) {
        return getPlayerRank(player.getUniqueId());
    }

    @Override
    public Rank getPlayerRank(UUID uuid) {
        PlayerData data = plugin.getGuildManager().getPlayerData(uuid);
        return data != null ? data.getRank() : null;
    }

    @Override
    public int getPlayerContribution(Player player) {
        return getPlayerContribution(player.getUniqueId());
    }

    @Override
    public int getPlayerContribution(UUID uuid) {
        PlayerData data = plugin.getGuildManager().getPlayerData(uuid);
        return data != null ? data.getContribution() : 0;
    }

    @Override
    public String getPlayerPrefix(Player player) {
        return getPlayerPrefix(player.getUniqueId());
    }

    @Override
    public String getPlayerPrefix(UUID uuid) {
        return plugin.getGuildManager().getPlayerPrefix(uuid);
    }

    // ===== УПРАВЛЕНИЕ ОЧКАМИ =====

    @Override
    public void addContribution(Player player, int amount) {
        addContribution(player.getUniqueId(), amount);
    }

    @Override
    public void addContribution(UUID uuid, int amount) {
        PlayerData data = plugin.getGuildManager().getPlayerData(uuid);
        if (data != null && data.hasGuild()) {
            int oldTotal = data.getContribution();
            data.setContribution(oldTotal + amount);
            plugin.getDatabaseManager().savePlayerData(data);

            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                fireContributionGain(player, amount, data.getContribution());
            }
        }
    }

    @Override
    public boolean takeContribution(Player player, int amount) {
        return takeContribution(player.getUniqueId(), amount);
    }

    @Override
    public boolean takeContribution(UUID uuid, int amount) {
        PlayerData data = plugin.getGuildManager().getPlayerData(uuid);
        if (data == null || !data.hasGuild() || data.getContribution() < amount) {
            return false;
        }

        int oldTotal = data.getContribution();
        data.setContribution(oldTotal - amount);
        plugin.getDatabaseManager().savePlayerData(data);

        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            fireContributionSpend(player, amount, data.getContribution());
        }

        return true;
    }

    @Override
    public void setContribution(Player player, int amount) {
        setContribution(player.getUniqueId(), amount);
    }

    @Override
    public void setContribution(UUID uuid, int amount) {
        PlayerData data = plugin.getGuildManager().getPlayerData(uuid);
        if (data != null && data.hasGuild()) {
            data.setContribution(amount);
            plugin.getDatabaseManager().savePlayerData(data);
        }
    }

    // ===== ПРОВЕРКИ =====

    @Override
    public boolean isMaster(Player player) {
        return isMaster(player.getUniqueId());
    }

    @Override
    public boolean isMaster(UUID uuid) {
        PlayerData data = plugin.getGuildManager().getPlayerData(uuid);
        return data != null && data.isMaster();
    }

    // ===== МЕТОДЫ ДЛЯ ГЛАВ =====

    @Override
    public boolean isGuildLeader(Player player) {
        return isGuildLeader(player.getUniqueId());
    }

    @Override
    public boolean isGuildLeader(UUID uuid) {
        PlayerData data = plugin.getGuildManager().getPlayerData(uuid);
        return data != null && data.isGuildLeader();
    }

    @Override
    public Player getGuildLeader(Player player) {
        return getGuildLeader(player.getUniqueId());
    }

    @Override
    public Player getGuildLeader(UUID uuid) {
        PlayerData data = plugin.getGuildManager().getPlayerData(uuid);
        if (data == null || !data.hasGuild()) return null;

        UUID leaderUUID = data.getGuildLeader();
        if (leaderUUID == null) return null;

        return plugin.getServer().getPlayer(leaderUUID);
    }

    @Override
    public boolean setGuildLeader(Player admin, Player target) {
        return plugin.getGuildManager().setGuildLeader(admin, target);
    }

    // ===== УПРАВЛЕНИЕ РАНГАМИ =====

    @Override
    public boolean canManagePlayer(Player leader, Player target) {
        return plugin.getGuildManager().canManagePlayer(leader, target);
    }

    @Override
    public boolean promotePlayer(Player leader, Player target) {
        return plugin.getGuildManager().promotePlayer(leader, target);
    }

    @Override
    public boolean demotePlayer(Player leader, Player target) {
        return plugin.getGuildManager().demotePlayer(leader, target);
    }

    @Override
    public Rank getNextRank(Player player) {
        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
        if (data == null || !data.hasGuild()) return null;
        return plugin.getGuildManager().getNextRank(data.getGuild(), data.getRank());
    }

    @Override
    public Rank getPreviousRank(Player player) {
        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
        if (data == null || !data.hasGuild()) return null;
        return plugin.getGuildManager().getPreviousRank(data.getGuild(), data.getRank());
    }

    @Override
    public List<Player> getPromotableMembers(Player leader) {
        return plugin.getGuildManager().getPromotableMembers(leader);
    }

    @Override
    public List<Player> getDemotableMembers(Player leader) {
        return plugin.getGuildManager().getDemotableMembers(leader);
    }

    // ===== ИНФОРМАЦИЯ О ГИЛЬДИИ =====

    @Override
    public List<Player> getGuildMembers(GuildType guild) {
        List<Player> members = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
            if (data != null && data.getGuild() == guild) {
                members.add(player);
            }
        }
        return members;
    }

    @Override
    public List<UUID> getGuildMemberUUIDs(GuildType guild) {
        List<UUID> members = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
            if (data != null && data.getGuild() == guild) {
                members.add(player.getUniqueId());
            }
        }
        return members;
    }

    @Override
    public int getGuildMemberCount(GuildType guild) {
        return getGuildMembers(guild).size();
    }

    @Override
    public int getGuildTotalContribution(GuildType guild) {
        int total = 0;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
            if (data != null && data.getGuild() == guild) {
                total += data.getContribution();
            }
        }
        return total;
    }

    // ===== ЭКСКЛЮЗИВНЫЕ ПРЕДМЕТЫ =====

    @Override
    public ItemStack createExclusiveItem(GuildType guild) {
        return switch (guild) {
            case SMITH -> ExclusiveItems.createMendingBook();
            case ALCHEMIST -> ExclusiveItems.createEternalPotion();
            case CARTOGRAPHER -> ExclusiveItems.createReturnMap();
            case FARMER -> ExclusiveItems.createGoldenApple();
            case MINER -> ExclusiveItems.createHeartOfMountain();
        };
    }

    @Override
    public boolean isExclusiveItem(ItemStack item) {
        return ExclusiveItems.isExclusiveItem(item);
    }

    @Override
    public GuildType getGuildFromItem(ItemStack item) {
        return ExclusiveItems.getGuildFromItem(item);
    }

    @Override
    public boolean canUseExclusiveItem(Player player, ItemStack item) {
        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
        if (data == null || !data.hasGuild()) return false;

        GuildType itemGuild = getGuildFromItem(item);
        return data.getGuild() == itemGuild && data.isMaster();
    }

    // ===== УПРАВЛЕНИЕ СЛУШАТЕЛЯМИ =====

    @Override
    public void registerGuildListener(GuildEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void unregisterGuildListener(GuildEventListener listener) {
        listeners.remove(listener);
    }

    // ===== ВЫЗОВ СОБЫТИЙ =====

    public void fireGuildJoin(Player player, GuildType guild) {
        for (GuildEventListener listener : listeners) {
            try {
                listener.onGuildJoin(player, guild);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка в onGuildJoin: " + e.getMessage());
            }
        }
    }

    public void fireGuildLeave(Player player, GuildType guild) {
        for (GuildEventListener listener : listeners) {
            try {
                listener.onGuildLeave(player, guild);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка в onGuildLeave: " + e.getMessage());
            }
        }
    }

    public void fireRankUp(Player player, Rank oldRank, Rank newRank) {
        for (GuildEventListener listener : listeners) {
            try {
                listener.onRankUp(player, oldRank, newRank);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка в onRankUp: " + e.getMessage());
            }
        }
    }

    public void fireRankDown(Player player, Rank oldRank, Rank newRank) {
        for (GuildEventListener listener : listeners) {
            try {
                listener.onRankDown(player, oldRank, newRank);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка в onRankDown: " + e.getMessage());
            }
        }
    }

    public void fireContributionGain(Player player, int amount, int newTotal) {
        for (GuildEventListener listener : listeners) {
            try {
                listener.onContributionGain(player, amount, newTotal);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка в onContributionGain: " + e.getMessage());
            }
        }
    }

    public void fireContributionSpend(Player player, int amount, int newTotal) {
        for (GuildEventListener listener : listeners) {
            try {
                listener.onContributionSpend(player, amount, newTotal);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка в onContributionSpend: " + e.getMessage());
            }
        }
    }

    public void fireExclusiveItemClaim(Player player, GuildType guild) {
        for (GuildEventListener listener : listeners) {
            try {
                listener.onExclusiveItemClaim(player, guild);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка в onExclusiveItemClaim: " + e.getMessage());
            }
        }
    }
}