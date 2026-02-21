package ru.guildforge.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.guildforge.GuildForge;
import ru.guildforge.data.PlayerData;
import ru.guildforge.gui.GUIHolder;
import ru.guildforge.gui.GuildMenuGUI;
import ru.guildforge.gui.GuildManagementGUI;
import ru.guildforge.guilds.GuildType;
import ru.guildforge.recipes.ExclusiveItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIListener implements Listener {
    private final GuildForge plugin;

    // Храним выбранного игрока для каждого лидера
    private final Map<UUID, UUID> selectedTargets = new HashMap<>();

    public GUIListener(GuildForge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof GUIHolder holder)) return;

        event.setCancelled(true);

        switch (holder.getType()) {
            case "main_menu":
                handleMainMenuClick(player, event.getSlot(), event.getCurrentItem());
                break;
            case "guild_management":
                handleManagementMenuClick(player, event.getSlot(), event.getCurrentItem());
                break;
            case "confirm_action":
                handleConfirmMenuClick(player, event.getSlot(), event.getCurrentItem());
                break;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof GUIHolder) {
            event.setCancelled(true);
        }
    }

    private void handleMainMenuClick(Player player, int slot, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        if (item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§cОшибка загрузки данных!");
            return;
        }

        if (!data.hasGuild()) {
            handleNoGuildClick(player, slot, item);
        } else {
            handleHasGuildClick(player, data, slot, item);
        }
    }

    private void handleNoGuildClick(Player player, int slot, ItemStack item) {
        GuildType selectedGuild = null;

        switch (slot) {
            case 19 -> selectedGuild = GuildType.SMITH;
            case 20 -> selectedGuild = GuildType.ALCHEMIST;
            case 21 -> selectedGuild = GuildType.CARTOGRAPHER;
            case 22 -> selectedGuild = GuildType.FARMER;
            case 23 -> selectedGuild = GuildType.MINER;
            default -> {
                player.sendMessage("§cНеизвестная гильдия!");
                return;
            }
        }

        if (selectedGuild != null) {
            GuildType finalSelectedGuild = selectedGuild;

            plugin.getTaskScheduler().runAtEntity(player, (p) -> {
                boolean success = plugin.getGuildManager().joinGuild(player, finalSelectedGuild);

                if (success) {
                    player.sendMessage("§aВы вступили в гильдию " +
                            finalSelectedGuild.getColorCode() + finalSelectedGuild.getDisplayName());

                    plugin.getTaskScheduler().runAtEntity(player, (p2) -> {
                        GuildMenuGUI.open(player);
                    });
                } else {
                    player.sendMessage("§cНе удалось вступить в гильдию!");
                }
            });
        }
    }

    private void handleHasGuildClick(Player player, PlayerData data, int slot, ItemStack item) {
        // Кнопка выхода
        if (slot == 49 && item.getType() == Material.REDSTONE_BLOCK) {
            handleInstantLeaveGuild(player, data);
            return;
        }

        // Эксклюзивный предмет
        if (slot == 31 && item.getType() == Material.NETHER_STAR) {
            handleExclusiveItem(player, data);
            return;
        }

        // Кнопка управления для главы
        if (data.isGuildLeader() && slot == 25 && item.getType() == Material.PLAYER_HEAD) {
            openManagementGUI(player);
            return;
        }

        // Информация о гильдии
        if (slot == 22) {
            showGuildInfo(player, data);
        }
    }

    private void handleManagementMenuClick(Player player, int slot, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        if (item.getType() == Material.GOLD_BLOCK) return;

        // Кнопка назад
        if (slot == 49 && item.getType() == Material.ARROW) {
            plugin.getTaskScheduler().runAtEntity(player, (p) -> {
                GuildMenuGUI.open(player);
            });
            return;
        }

        // Кнопка повышения
        if (slot == 18 && item.getType() == Material.EMERALD) {
            UUID targetUUID = selectedTargets.get(player.getUniqueId());
            if (targetUUID == null) {
                player.sendMessage("§cСначала выберите игрока!");
                return;
            }

            Player target = plugin.getServer().getPlayer(targetUUID);
            if (target == null || !target.isOnline()) {
                player.sendMessage("§cИгрок оффлайн!");
                selectedTargets.remove(player.getUniqueId());
                return;
            }

            // Открываем меню подтверждения
            GuildManagementGUI.openConfirmation(player, target, true);
            return;
        }

        // Кнопка понижения
        if (slot == 26 && item.getType() == Material.REDSTONE_BLOCK) {
            UUID targetUUID = selectedTargets.get(player.getUniqueId());
            if (targetUUID == null) {
                player.sendMessage("§cСначала выберите игрока!");
                return;
            }

            Player target = plugin.getServer().getPlayer(targetUUID);
            if (target == null || !target.isOnline()) {
                player.sendMessage("§cИгрок оффлайн!");
                selectedTargets.remove(player.getUniqueId());
                return;
            }

            // Открываем меню подтверждения
            GuildManagementGUI.openConfirmation(player, target, false);
            return;
        }

        // Клик по игроку - сохраняем выбранного
        if (item.getType() == Material.PLAYER_HEAD) {
            if (!(item.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta meta)) return;

            Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
            if (target == null) return;

            PlayerData targetData = plugin.getGuildManager().getPlayerData(target.getUniqueId());
            if (targetData == null || targetData.isGuildLeader() || target.equals(player)) return;

            // Сохраняем выбранного игрока
            selectedTargets.put(player.getUniqueId(), target.getUniqueId());
            player.sendMessage("§aВыбран игрок: §e" + target.getName());
            player.sendMessage("§7Теперь нажмите кнопку §aПовысить §7или §cПонизить");
        }
    }

    private void handleConfirmMenuClick(Player player, int slot, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        // Кнопка подтверждения
        if ((slot == 11 && (item.getType() == Material.EMERALD_BLOCK || item.getType() == Material.REDSTONE_BLOCK))) {
            UUID targetUUID = selectedTargets.get(player.getUniqueId());
            if (targetUUID == null) {
                player.sendMessage("§cОшибка: игрок не выбран!");
                player.closeInventory();
                return;
            }

            Player target = plugin.getServer().getPlayer(targetUUID);
            if (target == null || !target.isOnline()) {
                player.sendMessage("§cИгрок оффлайн!");
                selectedTargets.remove(player.getUniqueId());
                player.closeInventory();
                return;
            }

            boolean isPromote = item.getType() == Material.EMERALD_BLOCK;

            plugin.getTaskScheduler().runAtEntity(player, (p) -> {
                boolean success;
                if (isPromote) {
                    success = plugin.getGuildManager().promotePlayer((Player) p, target);
                } else {
                    success = plugin.getGuildManager().demotePlayer((Player) p, target);
                }

                if (success) {
                    selectedTargets.remove(p.getUniqueId());
                    // ИСПРАВЛЕНО: runLaterAtEntity -> runAtEntityLater
                    plugin.getTaskScheduler().runAtEntityLater(p, (p2) -> {
                        GuildManagementGUI.open((Player) p2);
                    }, 5L);
                }
            });
            return;
        }

        // Кнопка отмены
        if (slot == 15 && item.getType() == Material.BARRIER) {
            plugin.getTaskScheduler().runAtEntity(player, (p) -> {
                GuildManagementGUI.open((Player) p);
            });
            return;
        }
    }

    private void handleInstantLeaveGuild(Player player, PlayerData data) {
        String oldGuildName = data.getGuild().getColorCode() + data.getGuild().getDisplayName();
        int oldContribution = data.getContribution();

        plugin.getTaskScheduler().runAtEntity(player, (p) -> {
            boolean success = plugin.getGuildManager().leaveGuild(player);

            if (success) {
                player.sendMessage("§cВы покинули гильдию " + oldGuildName);
                player.sendMessage("§7Весь прогресс (Contribution: §e" + oldContribution + "§7) сброшен");

                player.closeInventory();
                plugin.getTaskScheduler().runAtEntity(player, (p2) -> {
                    GuildMenuGUI.open(player);
                });
            } else {
                player.sendMessage("§cНе удалось покинуть гильдию!");
            }
        });
    }

    private void handleExclusiveItem(Player player, PlayerData data) {
        if (!data.isMaster()) {
            player.sendMessage("§cЭтот предмет доступен только Мастерам!");
            return;
        }

        String cooldownKey = data.getGuild().name() + "_exclusive";
        if (data.isOnCooldown(cooldownKey)) {
            String timeLeft = data.getCooldownRemainingFormatted(cooldownKey);
            player.sendMessage("§cПредмет ещё не доступен! Осталось: " + timeLeft);
            return;
        }

        int requiredContribution = switch (data.getGuild()) {
            case SMITH -> 5000;
            case ALCHEMIST -> 10000;
            case CARTOGRAPHER -> 10000;
            case FARMER -> 8000;
            case MINER -> 20000;
        };

        if (data.getContribution() < requiredContribution) {
            player.sendMessage("§cВам нужно " + requiredContribution + " Contribution!");
            return;
        }

        int oldContribution = data.getContribution();
        data.setContribution(oldContribution - requiredContribution);
        data.incrementExclusiveItemsClaimed();

        plugin.getDatabaseManager().savePlayerData(data);
        plugin.getAPIImpl().fireExclusiveItemClaim(player, data.getGuild());

        plugin.getTaskScheduler().runAtEntity(player, (p) -> {
            ItemStack exclusiveItem = switch (data.getGuild()) {
                case SMITH -> ExclusiveItems.createMendingBook();
                case ALCHEMIST -> ExclusiveItems.createEternalPotion();
                case CARTOGRAPHER -> ExclusiveItems.createReturnMap();
                case FARMER -> ExclusiveItems.createGoldenApple();
                case MINER -> ExclusiveItems.createHeartOfMountain();
            };

            player.getInventory().addItem(exclusiveItem);

            int cooldownDays = switch (data.getGuild()) {
                case SMITH -> 1;
                case ALCHEMIST -> 3;
                case CARTOGRAPHER -> 2;
                case FARMER -> 3;
                case MINER -> 7;
            };

            data.setCooldownDays(cooldownKey, cooldownDays);
            plugin.getDatabaseManager().savePlayerData(data);

            player.sendMessage("§aВы получили эксклюзивный предмет!");
            player.sendMessage("§7Списано: §e" + requiredContribution + " §7Contribution");
            player.sendMessage("§7Осталось: §e" + data.getContribution() + " §7Contribution");

            GuildMenuGUI.open(player);
        });
    }

    private void openManagementGUI(Player player) {
        plugin.getTaskScheduler().runAtEntity(player, (p) -> {
            GuildManagementGUI.open(player);
        });
    }

    private void showGuildInfo(Player player, PlayerData data) {
        player.sendMessage("§6=== Информация о гильдии ===");
        player.sendMessage("§7Гильдия: " + data.getGuild().getColorCode() + data.getGuild().getDisplayName());
        player.sendMessage("§7Ранг: " + data.getRank().getDisplayName());
        if (data.isGuildLeader()) {
            player.sendMessage("§6§lГЛАВА ГИЛЬДИИ");
        }
        player.sendMessage("§7Contribution: §e" + data.getContribution());

        if (data.getRank().getMaxContribution() != Integer.MAX_VALUE) {
            int needed = data.getContributionToNextRank();
            double progress = data.getRankProgress();

            player.sendMessage("§7До следующего ранга: §e" + needed + " Contribution");

            StringBuilder progressBar = new StringBuilder("§7[");
            int bars = (int) (progress / 10);
            for (int i = 0; i < 10; i++) {
                if (i < bars) {
                    progressBar.append("§a■");
                } else {
                    progressBar.append("§7□");
                }
            }
            progressBar.append("§7] §f").append(String.format("%.1f", progress)).append("%");
            player.sendMessage(progressBar.toString());

            if (needed <= 0) {
                player.sendMessage("§e§l⚡ У вас достаточно очков для повышения! Обратитесь к главе гильдии.");
            }
        } else {
            player.sendMessage("§6§lМАКСИМАЛЬНЫЙ РАНГ!");
        }
    }
}