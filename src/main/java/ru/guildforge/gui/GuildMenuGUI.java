package ru.guildforge.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import ru.guildforge.GuildForge;
import ru.guildforge.data.PlayerData;
import ru.guildforge.guilds.GuildType;
import ru.guildforge.guilds.Rank;

import java.util.ArrayList;
import java.util.List;

public class GuildMenuGUI {

    public static void open(Player player) {
        GuildForge plugin = GuildForge.getInstance();
        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());

        if (data == null) {
            player.sendMessage("§cОшибка загрузки данных!");
            return;
        }

        // Создаем инвентарь с холдером
        Inventory inv = Bukkit.createInventory(
                new GUIHolder(null, "main_menu"),
                54,
                Component.text("Гильдии").color(TextColor.color(170, 170, 170))
        );

        // Заполняем рамку
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);

        // Верхняя рамка (слоты 0-8)
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
        }

        // Нижняя рамка (слоты 45-53)
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, border);
        }

        // Боковая рамка (слоты 9, 18, 27, 36 и 17, 26, 35, 44)
        for (int i = 9; i < 45; i += 9) {
            inv.setItem(i, border);
            inv.setItem(i + 8, border);
        }

        // Информация об игроке (центр верхней рамки)
        ItemStack infoItem = createPlayerInfoItem(player, data);
        inv.setItem(4, infoItem);

        if (!data.hasGuild()) {
            // Игрок без гильдии - показываем все гильдии для выбора
            addGuildSelectionItems(inv);
        } else {
            // Игрок с гильдией - показываем прогресс и эксклюзивы
            addGuildMemberItems(inv, player, data);
        }

        player.openInventory(inv);
    }

    /**
     * Создает предмет с информацией об игроке
     */
    private static ItemStack createPlayerInfoItem(Player player, PlayerData data) {
        ItemStack item;

        if (data.hasGuild()) {
            item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwningPlayer(player);
            meta.displayName(Component.text("§6§l" + player.getName()));

            List<String> lore = new ArrayList<>();
            lore.add("§7Гильдия: " + data.getGuild().getColorCode() + data.getGuild().getDisplayName());
            lore.add("§7Ранг: " + data.getGuild().getColorCode() + data.getRank().getDisplayName());
            lore.add("§7Contribution: §e" + data.getContribution());
            lore.add("");

            // Прогресс до следующего ранга
            if (data.getRank().getMaxContribution() != Integer.MAX_VALUE) {
                int needed = data.getContributionToNextRank();
                double progress = data.getRankProgress();

                lore.add("§7Прогресс до следующего ранга:");

                // Полоска прогресса
                StringBuilder progressBar = new StringBuilder("§8[");
                int bars = (int) (progress / 10);
                for (int i = 0; i < 10; i++) {
                    if (i < bars) {
                        progressBar.append("§a■");
                    } else {
                        progressBar.append("§7■");
                    }
                }
                progressBar.append("§8] §f").append(String.format("%.1f", progress)).append("%");
                lore.add(progressBar.toString());
                lore.add("§7Осталось: §e" + needed + " Contribution");
            } else {
                lore.add("§6§lМАКСИМАЛЬНЫЙ РАНГ!");
            }

            // Статистика
            lore.add("");
            lore.add("§7Эксклюзивных предметов получено: §e" + data.getExclusiveItemsClaimed());

            meta.setLore(lore);
            item.setItemMeta(meta);
        } else {
            item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("§c§lНет гильдии"));

            List<String> lore = new ArrayList<>();
            lore.add("§7Выберите гильдию ниже");
            lore.add("§7Каждая гильдия дает");
            lore.add("§7уникальные способности!");

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Добавляет предметы для выбора гильдий
     */
    private static void addGuildSelectionItems(Inventory inv) {
        // Кузнецы (слот 19)
        ItemStack smithItem = new ItemStack(Material.ANVIL);
        ItemMeta smithMeta = smithItem.getItemMeta();
        smithMeta.displayName(Component.text("§7§lКУЗНЕЦЫ"));
        smithMeta.setLore(List.of(
                "§7▸ Починка без потери чар",
                "§7▸ Перековка предметов",
                "§7▸ Зачарование: §e+5 Contribution",
                "§7▸ Крафт: §e+1 Contribution",
                "",
                "§7Эксклюзив: §fКнига Починки",
                "§7(1 раз в день, 5000 Contribution)",
                "",
                "§eНажмите, чтобы вступить!"
        ));
        smithItem.setItemMeta(smithMeta);
        inv.setItem(19, smithItem);

        // Алхимики (слот 20)
        ItemStack alchemistItem = new ItemStack(Material.BREWING_STAND);
        ItemMeta alchemistMeta = alchemistItem.getItemMeta();
        alchemistMeta.displayName(Component.text("§d§lАЛХИМИКИ"));
        alchemistMeta.setLore(List.of(
                "§7▸ Зелья действуют на 33% дольше",
                "§7▸ Тройной эффект при варке",
                "§7▸ Варка зелий: §e+3 Contribution",
                "",
                "§7Эксклюзив: §fВечный Флакон",
                "§7(1 раз в 3 дня, 10000 Contribution)",
                "",
                "§eНажмите, чтобы вступить!"
        ));
        alchemistItem.setItemMeta(alchemistMeta);
        inv.setItem(20, alchemistItem);

        // Картографы (слот 21)
        ItemStack cartographerItem = new ItemStack(Material.MAP);
        ItemMeta cartographerMeta = cartographerItem.getItemMeta();
        cartographerMeta.displayName(Component.text("§b§lКАРТОГРАФЫ"));
        cartographerMeta.setLore(List.of(
                "§7▸ До 5 точек варпов",
                "§7▸ Карты сокровищ",
                "§7▸ Исследование: §e+1 Contribution",
                "",
                "§7Эксклюзив: §fКарта Возвращения",
                "§7(1 раз в 2 дня, 10000 Contribution)",
                "",
                "§eНажмите, чтобы вступить!"
        ));
        cartographerItem.setItemMeta(cartographerMeta);
        inv.setItem(21, cartographerItem);

        // Фермеры (слот 22)
        ItemStack farmerItem = new ItemStack(Material.WHEAT);
        ItemMeta farmerMeta = farmerItem.getItemMeta();
        farmerMeta.displayName(Component.text("§a§lФЕРМЕРЫ"));
        farmerMeta.setLore(List.of(
                "§7▸ Ускоренный рост растений",
                "§7▸ Быстрое размножение животных",
                "§7▸ Сбор урожая: §e+1 Contribution",
                "",
                "§7Эксклюзив: §fЯблоко Изобилия",
                "§7(1 раз в 3 дня, 8000 Contribution)",
                "",
                "§eНажмите, чтобы вступить!"
        ));
        farmerItem.setItemMeta(farmerMeta);
        inv.setItem(22, farmerItem);

        // Шахтеры (слот 23)
        ItemStack minerItem = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta minerMeta = minerItem.getItemMeta();
        minerMeta.displayName(Component.text("§8§lШАХТЕРЫ"));
        minerMeta.setLore(List.of(
                "§7▸ +10% к шансу доп. руды",
                "§7▸ Молот 3х3",
                "§7▸ Добыча руды: §e+1-2 Contribution",
                "",
                "§7Эксклюзив: §fСердце Горы",
                "§7(1 раз в 7 дней, 20000 Contribution)",
                "",
                "§eНажмите, чтобы вступить!"
        ));
        minerItem.setItemMeta(minerMeta);
        inv.setItem(23, minerItem);
    }

    /**
     * Добавляет предметы для члена гильдии
     */
    private static void addGuildMemberItems(Inventory inv, Player player, PlayerData data) {
        // Информация о текущей гильдии (центр)
        ItemStack guildItem = createGuildInfoItem(data);
        inv.setItem(22, guildItem);

        // Статистика (слот 29)
        ItemStack statsItem = createStatsItem(data);
        inv.setItem(29, statsItem);

        // Эксклюзивный предмет (слот 31)
        ItemStack exclusiveItem = createExclusiveItem(data);
        inv.setItem(31, exclusiveItem);

        // Как получить Contribution (слот 33)
        ItemStack contributionItem = createContributionItem(data);
        inv.setItem(33, contributionItem);

        // Кнопка выхода из гильдии (слот 49)
        ItemStack leaveItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta leaveMeta = leaveItem.getItemMeta();
        leaveMeta.displayName(Component.text("§c§lПОКИНУТЬ ГИЛЬДИЮ"));
        leaveMeta.setLore(List.of(
                "§7Весь прогресс будет сброшен!",
                "§7Contribution: §e" + data.getContribution(),
                "",
                "§c§lНажмите, чтобы выйти"
        ));
        leaveItem.setItemMeta(leaveMeta);
        inv.setItem(49, leaveItem);
    }

    /**
     * Создает предмет с информацией о гильдии
     */
    private static ItemStack createGuildInfoItem(PlayerData data) {
        Material material = switch (data.getGuild()) {
            case SMITH -> Material.ANVIL;
            case ALCHEMIST -> Material.BREWING_STAND;
            case CARTOGRAPHER -> Material.MAP;
            case FARMER -> Material.WHEAT;
            case MINER -> Material.DIAMOND_PICKAXE;
        };

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(data.getGuild().getColorCode() + "§l" + data.getGuild().getDisplayName()));

        List<String> lore = new ArrayList<>();
        lore.add("§7Ваш ранг: " + data.getGuild().getColorCode() + data.getRank().getDisplayName());
        lore.add("§7Contribution: §e" + data.getContribution());
        lore.add("");

        // Добавляем механики гильдии
        lore.add("§7§lМЕХАНИКИ:");
        switch (data.getGuild()) {
            case SMITH:
                lore.add("§7▸ Починка без потери чар");
                lore.add("§7▸ Перековка предметов");
                lore.add("§7▸ Зачарование: §e+5 Contribution");
                break;
            case ALCHEMIST:
                lore.add("§7▸ Зелья действуют на 33% дольше");
                lore.add("§7▸ Тройной эффект при варке");
                lore.add("§7▸ Варка зелий: §e+3 Contribution");
                break;
            case CARTOGRAPHER:
                lore.add("§7▸ До 5 точек варпов");
                lore.add("§7▸ Карты сокровищ");
                lore.add("§7▸ Исследование: §e+1 Contribution");
                break;
            case FARMER:
                lore.add("§7▸ Ускоренный рост растений");
                lore.add("§7▸ Быстрое размножение животных");
                lore.add("§7▸ Сбор урожая: §e+1 Contribution");
                break;
            case MINER:
                lore.add("§7▸ +10% к шансу доп. руды");
                lore.add("§7▸ Молот 3х3");
                lore.add("§7▸ Добыча руды: §e+1-2 Contribution");
                break;
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Создает предмет со статистикой
     */
    private static ItemStack createStatsItem(PlayerData data) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§e§lСТАТИСТИКА"));

        List<String> lore = new ArrayList<>();
        lore.add("§7Всего Contribution: §e" + data.getContribution());
        lore.add("§7Эксклюзивных предметов: §e" + data.getExclusiveItemsClaimed());
        lore.add("§7Время в гильдии: §e" + data.getTotalPlayTimeFormatted());

        // Прогресс до следующего ранга
        lore.add("");
        lore.add("§7§lПРОГРЕСС:");
        if (data.getRank().getMaxContribution() != Integer.MAX_VALUE) {
            int needed = data.getContributionToNextRank();
            double progress = data.getRankProgress();

            lore.add("§7Текущий ранг: " + data.getGuild().getColorCode() + data.getRank().getDisplayName());
            lore.add("§7До следующего: §e" + needed + " Contribution");
            lore.add("§7Прогресс: §f" + String.format("%.1f", progress) + "%");
        } else {
            lore.add("§6§lМАКСИМАЛЬНЫЙ РАНГ!");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Создает предмет для получения эксклюзива
     */
    private static ItemStack createExclusiveItem(PlayerData data) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§6§lЭКСКЛЮЗИВНЫЙ ПРЕДМЕТ"));

        List<String> lore = new ArrayList<>();

        String itemName = switch (data.getGuild()) {
            case SMITH -> "Книга Починки";
            case ALCHEMIST -> "Вечный Флакон";
            case CARTOGRAPHER -> "Карта Возвращения";
            case FARMER -> "Золотое Яблоко Изобилия";
            case MINER -> "Сердце Горы";
        };

        int requiredContribution = switch (data.getGuild()) {
            case SMITH -> 5000;
            case ALCHEMIST -> 10000;
            case CARTOGRAPHER -> 10000;
            case FARMER -> 8000;
            case MINER -> 20000;
        };

        int cooldownDays = switch (data.getGuild()) {
            case SMITH -> 1;
            case ALCHEMIST -> 3;
            case CARTOGRAPHER -> 2;
            case FARMER -> 3;
            case MINER -> 7;
        };

        lore.add("§7Предмет: §f" + itemName);
        lore.add("§7Требуется Contribution: §e" + requiredContribution);
        lore.add("§7Кулдаун: §e" + cooldownDays + " " + getDaysWord(cooldownDays));
        lore.add("");

        if (!data.isMaster()) {
            lore.add("§c§lТРЕБУЕТСЯ РАНГ МАСТЕРА!");
            lore.add("§7Достигните §e" + (data.getGuild() == GuildType.SMITH ? "1500" :
                    data.getGuild() == GuildType.ALCHEMIST ? "1500" :
                            data.getGuild() == GuildType.CARTOGRAPHER ? "1500" :
                                    data.getGuild() == GuildType.FARMER ? "1500" : "1500") + " Contribution");
        } else {
            String cooldownKey = data.getGuild().name() + "_exclusive";
            if (data.isOnCooldown(cooldownKey)) {
                lore.add("§cКулдаун: " + data.getCooldownRemainingFormatted(cooldownKey));
            } else if (data.getContribution() < requiredContribution) {
                lore.add("§cНе хватает Contribution!");
                lore.add("§cНужно ещё: §e" + (requiredContribution - data.getContribution()));
            } else {
                lore.add("§a§lДОСТУПНО!");
                lore.add("§eНажмите, чтобы получить!");
            }
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Создает предмет с информацией о Contribution
     */
    private static ItemStack createContributionItem(PlayerData data) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§a§lКАК ПОЛУЧИТЬ CONTRIBUTION"));

        List<String> lore = new ArrayList<>();
        lore.add("§7Ваш текущий вклад: §e" + data.getContribution());
        lore.add("");
        lore.add("§7§lДЕЙСТВИЯ:");

        switch (data.getGuild()) {
            case SMITH:
                lore.add("§7▸ Зачарование предметов: §e+5-10");
                lore.add("§7▸ Крафт предметов: §e+1");
                break;
            case ALCHEMIST:
                lore.add("§7▸ Варка зелий: §e+3");
                break;
            case CARTOGRAPHER:
                lore.add("§7▸ Исследование новых чанков: §e+1");
                break;
            case FARMER:
                lore.add("§7▸ Сбор спелого урожая: §e+1");
                lore.add("§7▸ Размножение животных: §e+5 (скоро)");
                break;
            case MINER:
                lore.add("§7▸ Добыча руды: §e+1 (обычная)");
                lore.add("§7▸ Добыча глубокосланцевой руды: §e+2");
                break;
        }

        lore.add("");
        lore.add("§7§lТРЕБОВАНИЯ ДЛЯ РАНГОВ:");

        switch (data.getGuild()) {
            case SMITH:
                lore.add("§7Ученик: §e0-100");
                lore.add("§7Подмастерье: §e100-500");
                lore.add("§7Мастер-Кузнец: §e500-1500");
                lore.add("§7Старейшина: §e1500+");
                break;
            case ALCHEMIST:
                lore.add("§7Стажёр: §e0-100");
                lore.add("§7Зельевар: §e100-500");
                lore.add("§7Мастер-зелий: §e500-1500");
                lore.add("§7Верховный Алхимик: §e1500+");
                break;
            case CARTOGRAPHER:
                lore.add("§7Землемер: §e0-100");
                lore.add("§7Следопыт: §e100-500");
                lore.add("§7Мастер-картограф: §e500-1500");
                lore.add("§7Хранитель Карт: §e1500+");
                break;
            case FARMER:
                lore.add("§7Садовод: §e0-100");
                lore.add("§7Фермер: §e100-500");
                lore.add("§7Мастер-урожая: §e500-1500");
                lore.add("§7Хранитель Сада: §e1500+");
                break;
            case MINER:
                lore.add("§7Рудокоп: §e0-100");
                lore.add("§7Горняк: §e100-500");
                lore.add("§7Мастер-глубин: §e500-1500");
                lore.add("§7Хранитель Недр: §e1500+");
                break;
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Склонение слова "день/дня/дней"
     */
    private static String getDaysWord(int days) {
        if (days % 10 == 1 && days % 100 != 11) {
            return "день";
        } else if (days % 10 >= 2 && days % 10 <= 4 && (days % 100 < 10 || days % 100 >= 20)) {
            return "дня";
        } else {
            return "дней";
        }
    }
}