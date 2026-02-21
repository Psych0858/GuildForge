package ru.guildforge.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.guildforge.GuildForge;
import ru.guildforge.data.PlayerData;
import ru.guildforge.guilds.GuildType;

public class GuildAdminCommand implements CommandExecutor {
    private final GuildForge plugin;

    public GuildAdminCommand(GuildForge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sender.sendMessage("§cИспользование: /guildadmin <reload|info|setguild|addcontribution|setleader>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.getConfigManager().reloadConfigs();
                sender.sendMessage("§aКонфиги перезагружены!");
                break;

            case "info":
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /guildadmin info <игрок>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cИгрок не найден!");
                    return true;
                }
                PlayerData data = plugin.getGuildManager().getPlayerData(target.getUniqueId());
                if (data == null) {
                    sender.sendMessage("§cДанные игрока не загружены!");
                    return true;
                }
                sender.sendMessage("§6Информация об игроке " + target.getName());
                sender.sendMessage("§7Гильдия: " + (data.hasGuild() ? data.getGuild().getDisplayName() : "§7Нет"));
                sender.sendMessage("§7Ранг: " + (data.getRank() != null ? data.getRank().getDisplayName() : "§7Нет"));
                sender.sendMessage("§7Contribution: " + data.getContribution());
                sender.sendMessage("§7Глава гильдии: " + (data.isGuildLeader() ? "§aДа" : "§7Нет"));
                if (data.getGuildLeader() != null) {
                    Player leader = Bukkit.getPlayer(data.getGuildLeader());
                    sender.sendMessage("§7Глава (UUID): " + (leader != null ? leader.getName() : data.getGuildLeader().toString()));
                }
                break;

            case "setguild":
                if (args.length < 3) {
                    sender.sendMessage("§cИспользование: /guildadmin setguild <игрок> <гильдия>");
                    return true;
                }
                Player setTarget = Bukkit.getPlayer(args[1]);
                if (setTarget == null) {
                    sender.sendMessage("§cИгрок не найден!");
                    return true;
                }
                GuildType guild = GuildType.fromString(args[2]);
                if (guild == null) {
                    sender.sendMessage("§cГильдия не найдена! Доступные: smith, alchemist, cartographer, farmer, miner");
                    return true;
                }
                if (plugin.getGuildManager().joinGuild(setTarget, guild)) {
                    sender.sendMessage("§aИгроку " + setTarget.getName() + " установлена гильдия " + guild.getDisplayName());
                } else {
                    sender.sendMessage("§cНе удалось установить гильдию!");
                }
                break;

            case "addcontribution":
                if (args.length < 3) {
                    sender.sendMessage("§cИспользование: /guildadmin addcontribution <игрок> <количество>");
                    return true;
                }
                Player contTarget = Bukkit.getPlayer(args[1]);
                if (contTarget == null) {
                    sender.sendMessage("§cИгрок не найден!");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    plugin.getGuildManager().addContribution(contTarget, amount);
                    sender.sendMessage("§aДобавлено " + amount + " Contribution игроку " + contTarget.getName());
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cНеверное число!");
                }
                break;

            // 👑 НОВАЯ КОМАНДА
            case "setleader":
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /guildadmin setleader <игрок>");
                    return true;
                }
                Player targetLeader = Bukkit.getPlayer(args[1]);
                if (targetLeader == null) {
                    sender.sendMessage("§cИгрок не найден!");
                    return true;
                }

                Player admin = null;
                if (sender instanceof Player) {
                    admin = (Player) sender;
                }

                boolean success = plugin.getGuildManager().setGuildLeader(admin, targetLeader);
                if (!success && admin == null) {
                    sender.sendMessage("§cНе удалось назначить главу!");
                }
                break;

            default:
                sender.sendMessage("§cНеизвестная команда!");
                break;
        }

        return true;
    }
}