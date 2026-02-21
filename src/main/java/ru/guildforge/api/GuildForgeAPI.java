package ru.guildforge.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.guildforge.guilds.GuildType;
import ru.guildforge.guilds.Rank;

import java.util.List;
import java.util.UUID;

public interface GuildForgeAPI {

    // Информация об игроке
    boolean hasGuild(Player player);
    boolean hasGuild(UUID uuid);
    GuildType getPlayerGuild(Player player);
    GuildType getPlayerGuild(UUID uuid);
    Rank getPlayerRank(Player player);
    Rank getPlayerRank(UUID uuid);
    int getPlayerContribution(Player player);
    int getPlayerContribution(UUID uuid);
    String getPlayerPrefix(Player player);
    String getPlayerPrefix(UUID uuid);

    // Управление очками
    void addContribution(Player player, int amount);
    void addContribution(UUID uuid, int amount);
    boolean takeContribution(Player player, int amount);
    boolean takeContribution(UUID uuid, int amount);
    void setContribution(Player player, int amount);
    void setContribution(UUID uuid, int amount);

    // Проверки
    boolean isMaster(Player player);
    boolean isMaster(UUID uuid);

    // Методы для глав
    boolean isGuildLeader(Player player);
    boolean isGuildLeader(UUID uuid);
    Player getGuildLeader(Player player);
    Player getGuildLeader(UUID uuid);
    boolean setGuildLeader(Player admin, Player target);

    // Управление рангами
    boolean canManagePlayer(Player leader, Player target);
    boolean promotePlayer(Player leader, Player target);
    boolean demotePlayer(Player leader, Player target);
    Rank getNextRank(Player player);
    Rank getPreviousRank(Player player);
    List<Player> getPromotableMembers(Player leader);
    List<Player> getDemotableMembers(Player leader);

    // Информация о гильдии
    List<Player> getGuildMembers(GuildType guild);
    List<UUID> getGuildMemberUUIDs(GuildType guild);
    int getGuildMemberCount(GuildType guild);
    int getGuildTotalContribution(GuildType guild);

    // Эксклюзивные предметы
    ItemStack createExclusiveItem(GuildType guild);
    boolean isExclusiveItem(ItemStack item);
    GuildType getGuildFromItem(ItemStack item);
    boolean canUseExclusiveItem(Player player, ItemStack item);

    // События
    void registerGuildListener(GuildEventListener listener);
    void unregisterGuildListener(GuildEventListener listener);
}