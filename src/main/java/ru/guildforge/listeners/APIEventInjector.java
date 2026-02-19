package ru.guildforge.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.guildforge.GuildForge;
import ru.guildforge.data.PlayerData;
import ru.guildforge.guilds.GuildType;
import ru.guildforge.guilds.Rank;

/**
 * Этот класс собирает все вызовы API событий в одном месте
 * и вызывает их из нужных мест плагина
 */
public class APIEventInjector {

    private final GuildForge plugin;

    public APIEventInjector(GuildForge plugin) {
        this.plugin = plugin;
    }

    // Вызывается из GuildManager.joinGuild
    public void onGuildJoin(Player player, GuildType guild) {
        plugin.getAPIImpl().fireGuildJoin(player, guild);
    }

    // Вызывается из GuildManager.leaveGuild
    public void onGuildLeave(Player player, GuildType guild) {
        plugin.getAPIImpl().fireGuildLeave(player, guild);
    }

    // Вызывается при повышении ранга
    public void onRankUp(Player player, Rank oldRank, Rank newRank) {
        plugin.getAPIImpl().fireRankUp(player, oldRank, newRank);
    }

    // Вызывается при получении очков
    public void onContributionGain(Player player, int amount, int newTotal) {
        plugin.getAPIImpl().fireContributionGain(player, amount, newTotal);
    }

    // Вызывается при трате очков
    public void onContributionSpend(Player player, int amount, int newTotal) {
        plugin.getAPIImpl().fireContributionSpend(player, amount, newTotal);
    }

    // Вызывается при получении эксклюзивного предмета
    public void onExclusiveItemClaim(Player player, GuildType guild) {
        plugin.getAPIImpl().fireExclusiveItemClaim(player, guild);
    }
}