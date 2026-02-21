package ru.guildforge.guilds;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;

public enum GuildType {
    SMITH("Кузнецы", ChatColor.DARK_GRAY, "&7"),
    ALCHEMIST("Алхимики", ChatColor.LIGHT_PURPLE, "&d"),
    CARTOGRAPHER("Картографы", ChatColor.AQUA, "&b"),
    FARMER("Фермеры", ChatColor.GREEN, "&a"),
    MINER("Шахтеры", ChatColor.DARK_GRAY, "&8");

    private final String displayName;
    private final ChatColor color;
    private final String colorCode;

    GuildType(String displayName, ChatColor color, String colorCode) {
        this.displayName = displayName;
        this.color = color;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getColorCode() {
        return colorCode;
    }

    public static GuildType fromString(String name) {
        for (GuildType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}