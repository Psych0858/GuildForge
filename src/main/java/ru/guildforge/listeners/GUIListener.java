package ru.guildforge.listeners;

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
import ru.guildforge.guilds.GuildType;
import ru.guildforge.recipes.ExclusiveItems;

public class GUIListener implements Listener {
    private final GuildForge plugin;

    public GUIListener(GuildForge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Проверяем, что кликнул игрок
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Проверяем, что кликнули по предмету
        if (event.getCurrentItem() == null) return;

        // Проверяем, что это наше GUI
        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof GUIHolder holder)) return;

        // Отменяем все клики в нашем GUI
        event.setCancelled(true);

        // Обрабатываем клик в зависимости от типа меню
        if ("main_menu".equals(holder.getType())) {
            handleMainMenuClick(player, event.getSlot(), event.getCurrentItem());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Запрещаем перетаскивание предметов в нашем GUI
        if (event.getInventory().getHolder() instanceof GUIHolder) {
            event.setCancelled(true);
        }
    }

    /**
     * Обработка кликов в главном меню
     */
    private void handleMainMenuClick(Player player, int slot, ItemStack item) {
        // Игнорируем пустые предметы и рамку
        if (item == null || item.getType() == Material.AIR) return;
        if (item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        // Получаем данные игрока
        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§cОшибка загрузки данных!");
            return;
        }

        // Логируем клик для отладки
        plugin.getLogger().info("Player " + player.getName() + " clicked slot " + slot + " with item " + item.getType());

        // Разная обработка для игроков с гильдией и без
        if (!data.hasGuild()) {
            handleNoGuildClick(player, slot, item);
        } else {
            handleHasGuildClick(player, data, slot, item);
        }
    }

    /**
     * Обработка кликов для игрока без гильдии
     */
    private void handleNoGuildClick(Player player, int slot, ItemStack item) {
        GuildType selectedGuild = null;

        // Определяем какая гильдия была нажата по слоту
        switch (slot) {
            case 19 -> selectedGuild = GuildType.SMITH;      // Кузнецы (ANVIL)
            case 20 -> selectedGuild = GuildType.ALCHEMIST;  // Алхимики (BREWING_STAND)
            case 21 -> selectedGuild = GuildType.CARTOGRAPHER; // Картографы (MAP)
            case 22 -> selectedGuild = GuildType.FARMER;     // Фермеры (WHEAT)
            case 23 -> selectedGuild = GuildType.MINER;      // Шахтеры (DIAMOND_PICKAXE)
            default -> {
                player.sendMessage("§cНеизвестная гильдия!");
                return;
            }
        }

        if (selectedGuild != null) {
            GuildType finalSelectedGuild = selectedGuild;

            // Выполняем в регионе игрока (Folia-safe)
            plugin.getTaskScheduler().runAtEntity(player, (p) -> {
                boolean success = plugin.getGuildManager().joinGuild(player, finalSelectedGuild);

                if (success) {
                    // Успешное вступление
                    player.sendMessage("§aВы вступили в гильдию " +
                            finalSelectedGuild.getColorCode() + finalSelectedGuild.getDisplayName());

                    plugin.getLogger().info("Player " + player.getName() +
                            " successfully joined " + finalSelectedGuild.name());

                    // Обновляем GUI (тоже в регионе игрока)
                    plugin.getTaskScheduler().runAtEntity(player, (p2) -> {
                        GuildMenuGUI.open(player);
                    });
                } else {
                    player.sendMessage("§cНе удалось вступить в гильдию!");
                    plugin.getLogger().warning("Failed to join guild for " + player.getName());
                }
            });
        }
    }

    /**
     * Обработка кликов для игрока с гильдией
     */
    private void handleHasGuildClick(Player player, PlayerData data, int slot, ItemStack item) {
        // КНОПКА ВЫХОДА ИЗ ГИЛЬДИИ (МГНОВЕННО, БЕЗ ПОДТВЕРЖДЕНИЯ)
        if (slot == 49 && item.getType() == Material.REDSTONE_BLOCK) {
            handleInstantLeaveGuild(player, data);
            return;
        }

        // Кнопка эксклюзивного предмета (звезда в слоте 31)
        if (slot == 31 && item.getType() == Material.NETHER_STAR) {
            handleExclusiveItem(player, data);
            return;
        }

        // Информационный предмет в центре - показываем информацию
        if (slot == 22) {
            showGuildInfo(player, data);
        }
    }

    /**
     * МГНОВЕННЫЙ ВЫХОД ИЗ ГИЛЬДИИ БЕЗ ПОДТВЕРЖДЕНИЯ
     */
    private void handleInstantLeaveGuild(Player player, PlayerData data) {
        String oldGuildName = data.getGuild().getColorCode() + data.getGuild().getDisplayName();
        int oldContribution = data.getContribution();

        // Выполняем выход в регионе игрока
        plugin.getTaskScheduler().runAtEntity(player, (p) -> {
            boolean success = plugin.getGuildManager().leaveGuild(player);

            if (success) {
                player.sendMessage("§cВы покинули гильдию " + oldGuildName);
                player.sendMessage("§7Весь прогресс (Contribution: §e" + oldContribution + "§7) сброшен");

                plugin.getLogger().info("Player " + player.getName() + " left their guild");

                // Закрываем текущее меню и открываем меню выбора гильдий
                player.closeInventory();
                plugin.getTaskScheduler().runAtEntity(player, (p2) -> {
                    GuildMenuGUI.open(player);
                });
            } else {
                player.sendMessage("§cНе удалось покинуть гильдию!");
            }
        });
    }

    /**
     * Обработка получения эксклюзивного предмета
     */
    private void handleExclusiveItem(Player player, PlayerData data) {
        // Проверка на мастер-ранг
        if (!data.isMaster()) {
            player.sendMessage("§cЭтот предмет доступен только Мастерам!");
            return;
        }

        // Проверка кулдауна
        String cooldownKey = data.getGuild().name() + "_exclusive";
        if (data.isOnCooldown(cooldownKey)) {
            String timeLeft = data.getCooldownRemainingFormatted(cooldownKey);
            player.sendMessage("§cПредмет ещё не доступен! Осталось: " + timeLeft);
            return;
        }

        // Проверка требований (Contribution)
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

        // ✅ СПИСЫВАЕМ ОЧКИ
        int oldContribution = data.getContribution();
        data.setContribution(oldContribution - requiredContribution);

        plugin.getDatabaseManager().savePlayerData(data);
        plugin.getAPIImpl().fireExclusiveItemClaim(player, data.getGuild());

        // Выдаем предмет
        plugin.getTaskScheduler().runAtEntity(player, (p) -> {
            ItemStack exclusiveItem = switch (data.getGuild()) {
                case SMITH -> ExclusiveItems.createMendingBook();
                case ALCHEMIST -> ExclusiveItems.createEternalPotion();
                case CARTOGRAPHER -> ExclusiveItems.createReturnMap();
                case FARMER -> ExclusiveItems.createGoldenApple();
                case MINER -> ExclusiveItems.createHeartOfMountain();
            };

            // Добавляем предмет в инвентарь
            player.getInventory().addItem(exclusiveItem);


            // Устанавливаем кулдаун (в днях)
            int cooldownDays = switch (data.getGuild()) {
                case SMITH -> 1;
                case ALCHEMIST -> 3;
                case CARTOGRAPHER -> 2;
                case FARMER -> 3;
                case MINER -> 7;
            };

            data.setCooldownDays(cooldownKey, cooldownDays);

            // Сохраняем данные асинхронно
            plugin.getDatabaseManager().savePlayerData(data);

            player.sendMessage("§aВы получили эксклюзивный предмет!");
            player.sendMessage("§7Списано: §e" + requiredContribution + " §7Contribution");
            player.sendMessage("§7Осталось: §e" + data.getContribution() + " §7Contribution");

            // Обновляем GUI
            GuildMenuGUI.open(player);
        });
    }

    /**
     * Показывает информацию о гильдии
     */
    private void showGuildInfo(Player player, PlayerData data) {
        player.sendMessage("§6=== Информация о гильдии ===");
        player.sendMessage("§7Гильдия: " + data.getGuild().getColorCode() + data.getGuild().getDisplayName());
        player.sendMessage("§7Ранг: " + data.getRank().getDisplayName());
        player.sendMessage("§7Contribution: §e" + data.getContribution());

        // Прогресс до следующего ранга
        if (data.getRank().getMaxContribution() != Integer.MAX_VALUE) {
            int needed = data.getContributionToNextRank();
            double progress = data.getRankProgress();

            player.sendMessage("§7До следующего ранга: §e" + needed + " Contribution");

            // Простая полоска прогресса
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
        } else {
            player.sendMessage("§6§lМАКСИМАЛЬНЫЙ РАНГ!");
        }
    }
}