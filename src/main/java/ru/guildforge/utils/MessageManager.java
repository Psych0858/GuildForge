package ru.guildforge.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.guildforge.GuildForge;

public class MessageManager {
    private final GuildForge plugin;
    private FileConfiguration messages;

    public MessageManager(GuildForge plugin) {
        this.plugin = plugin;
        this.messages = plugin.getConfigManager().getMessages();
    }

    public void sendMessage(CommandSender sender, String key) {
        String message = messages.getString(key, "§cСообщение не найдено: " + key);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public void sendMessage(CommandSender sender, String key, String... replacements) {
        String message = messages.getString(key, "§cСообщение не найдено: " + key);

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public String getMessage(String key) {
        String message = messages.getString(key, "§cСообщение не найдено: " + key);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String key, String... replacements) {
        String message = messages.getString(key, "§cСообщение не найдено: " + key);

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}