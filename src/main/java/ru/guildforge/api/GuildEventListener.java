package ru.guildforge.api;

import org.bukkit.entity.Player;
import ru.guildforge.guilds.GuildType;
import ru.guildforge.guilds.Rank;

public interface GuildEventListener {

    default void onGuildJoin(Player player, GuildType guild) {}
    default void onGuildLeave(Player player, GuildType guild) {}
    default void onRankUp(Player player, Rank oldRank, Rank newRank) {}
    default void onRankDown(Player player, Rank oldRank, Rank newRank) {} // Новое событие
    default void onContributionGain(Player player, int amount, int newTotal) {}
    default void onContributionSpend(Player player, int amount, int newTotal) {}
    default void onExclusiveItemClaim(Player player, GuildType guild) {}
}