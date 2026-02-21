package ru.guildforge.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.guildforge.GuildForge;
import ru.guildforge.data.PlayerData;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    private final GuildForge plugin;

    public PlaceholderAPIHook(GuildForge plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "guildforge";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Ragnarok";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
        if (data == null) return "";

        switch (params.toLowerCase()) {
            case "guild":
                return data.hasGuild() ? data.getGuild().getDisplayName() : "Нет гильдии";
            case "guild_color":
                return data.hasGuild() ? data.getGuild().getColorCode() : "&7";
            case "rank":
                return data.hasGuild() && data.getRank() != null ?
                        data.getRank().getDisplayName() : "Нет ранга";
            case "rank_prefix":
                if (!data.hasGuild() || data.getRank() == null) return "";
                if (data.isGuildLeader()) {
                    return data.getGuild().getColorCode() + "[" + data.getGuild().getDisplayName() + " " +
                            data.getRank().getDisplayName() + " §6§l👑]";
                }
                return data.getGuild().getColorCode() + "[" + data.getGuild().getDisplayName() + " " +
                        data.getRank().getDisplayName() + "]";
            case "contribution":
                return String.valueOf(data.getContribution());
            case "has_guild":
                return data.hasGuild() ? "да" : "нет";
            case "is_master":
                return data.isMaster() ? "да" : "нет";
            case "is_leader":
                return data.isGuildLeader() ? "да" : "нет";
            case "leader_name":
                if (data.getGuildLeader() == null) return "";
                Player leader = plugin.getServer().getPlayer(data.getGuildLeader());
                return leader != null ? leader.getName() : "оффлайн";
            default:
                return null;
        }
    }
}