package ru.guildforge.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.guildforge.GuildForge;
import ru.guildforge.gui.GuildMenuGUI;

public class GuildCommand implements CommandExecutor {
    private final GuildForge plugin;

    public GuildCommand(GuildForge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }

        Player player = (Player) sender;

        // Открываем GUI меню
        GuildMenuGUI.open(player);

        return true;
    }
}