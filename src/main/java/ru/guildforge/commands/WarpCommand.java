package ru.guildforge.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.guildforge.GuildForge;
import ru.guildforge.data.PlayerData;
import ru.guildforge.guilds.GuildType;
import ru.guildforge.warps.WarpManager;

public class WarpCommand implements CommandExecutor {
    private final GuildForge plugin;
    private final WarpManager warpManager;

    public WarpCommand(GuildForge plugin) {
        this.plugin = plugin;
        this.warpManager = new WarpManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        // Проверка: является ли игрок картографом
        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
        if (data == null || data.getGuild() != GuildType.CARTOGRAPHER) {
            player.sendMessage("§cТолько картографы могут использовать варпы!");
            return true;
        }

        if (args.length == 0) {
            // /warp - показать список
            warpManager.listWarps(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
            case "add":
            case "set":
                // /warp create <название>
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /warp create <название>");
                    return true;
                }
                String name = args[1];
                warpManager.createWarp(player, name);
                break;

            case "delete":
            case "remove":
            case "del":
                // /warp delete <название>
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /warp delete <название>");
                    return true;
                }
                warpManager.deleteWarp(player, args[1]);
                break;

            case "list":
            case "ls":
                // /warp list
                warpManager.listWarps(player);
                break;

            case "help":
            case "?":
                player.sendMessage("§6=== Команды варпов ===");
                player.sendMessage("§e/warp §7- список варпов");
                player.sendMessage("§e/warp create <название> §7- создать варп");
                player.sendMessage("§e/warp <название> §7- телепортироваться к варпу");
                player.sendMessage("§e/warp delete <название> §7- удалить варп");
                break;

            default:
                // /warp <название> - телепортация
                warpManager.teleportToWarp(player, args[0]);
                break;
        }

        return true;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }
}