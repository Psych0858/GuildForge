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

public class GuildManagementGUI {

    private static GuildForge plugin = GuildForge.getInstance();

    public static void open(Player player) {
        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());

        if (data == null || !data.hasGuild()) {
            player.sendMessage("§cВы не состоите в гильдии!");
            return;
        }

        if (!data.isGuildLeader()) {
            player.sendMessage("§cТолько глава гильдии может управлять участниками!");
            return;
        }

        Inventory inv = Bukkit.createInventory(
                new GUIHolder(null, "guild_management"),
                54,
                Component.text("Управление гильдией").color(TextColor.color(255, 215, 0))
        );

        // Заполняем рамку
        addBorder(inv);

        // Информация о гильдии
        inv.setItem(4, createGuildInfoItem(player, data));

        // Кнопки управления
        inv.setItem(18, createControlItem("§a§lПОВЫСИТЬ", Material.EMERALD,
                "§7Нажмите на игрока, затем", "§7на эту кнопку для повышения"));
        inv.setItem(26, createControlItem("§c§lПОНИЗИТЬ", Material.REDSTONE_BLOCK,
                "§7Нажмите на игрока, затем", "§7на эту кнопку для понижения"));

        // Список членов гильдии
        addMemberItems(inv, data);

        // Информационная кнопка
        inv.setItem(48, createInfoItem());

        // Кнопка назад
        inv.setItem(49, createBackButton());

        player.openInventory(inv);
    }

    private static void addBorder(Inventory inv) {
        ItemStack border = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
        }
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, border);
        }
    }

    private static ItemStack createGuildInfoItem(Player player, PlayerData data) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§6§lИНФОРМАЦИЯ О ГИЛЬДИИ"));

        List<String> lore = new ArrayList<>();
        lore.add("§7Гильдия: " + data.getGuild().getColorCode() + data.getGuild().getDisplayName());
        lore.add("§7Глава: §e" + player.getName());
        lore.add("§7Всего участников: §e" + plugin.getGuildManager().getGuildMemberCount(data.getGuild()));
        lore.add("");
        lore.add("§e§lИНСТРУКЦИЯ:");
        lore.add("§71. Нажмите на игрока");
        lore.add("§72. Нажмите кнопку Повысить/Понизить");
        lore.add("§73. Подтвердите действие");

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private static ItemStack createControlItem(String name, Material material, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));

        List<String> lore = new ArrayList<>();
        for (String line : loreLines) {
            lore.add(line);
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private static void addMemberItems(Inventory inv, PlayerData data) {
        List<Player> members = plugin.getGuildManager().getGuildMembersForGUI(data.getGuild());

        int slot = 19; // Начинаем со слота 19 (второй ряд)
        for (Player member : members) {
            if (slot > 43) break;

            // Пропускаем слоты с кнопками управления
            if (slot == 18 || slot == 26 || slot % 9 == 0 || slot % 9 == 8) {
                slot++;
                continue;
            }

            PlayerData memberData = plugin.getGuildManager().getPlayerData(member.getUniqueId());
            if (memberData != null) {
                inv.setItem(slot, createMemberItem(member, memberData));
            }

            slot++;
        }
    }

    private static ItemStack createMemberItem(Player member, PlayerData data) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(member);

        String rankColor = data.getGuild().getColorCode();

        if (data.isGuildLeader()) {
            meta.displayName(Component.text("§6§l👑 " + member.getName() + " §6§l👑"));
        } else {
            meta.displayName(Component.text(rankColor + "§l" + member.getName()));
        }

        List<String> lore = new ArrayList<>();
        lore.add("§7Ранг: " + rankColor + data.getRank().getDisplayName());
        lore.add("§7Contribution: §e" + data.getContribution());
        lore.add("");

        if (data.isGuildLeader()) {
            lore.add("§6§lГЛАВА ГИЛЬДИИ");
        } else {
            Rank currentRank = data.getRank();
            Rank nextRank = plugin.getGuildManager().getNextRank(data.getGuild(), currentRank);
            Rank prevRank = plugin.getGuildManager().getPreviousRank(data.getGuild(), currentRank);

            // Информация о следующем ранге
            if (nextRank != currentRank) {
                int needed = nextRank.getMinContribution() - data.getContribution();
                if (needed <= 0) {
                    lore.add("§a✔ ГОТОВ К ПОВЫШЕНИЮ");
                } else {
                    lore.add("§7До повышения: §e" + needed + " Contribution");
                }
            } else {
                lore.add("§6§lМАКСИМАЛЬНЫЙ РАНГ");
            }

            // Информация о предыдущем ранге
            if (prevRank != currentRank) {
                lore.add("§7Можно понизить до: " + rankColor + prevRank.getDisplayName());
            } else {
                lore.add("§c§lМИНИМАЛЬНЫЙ РАНГ");
            }

            lore.add("");
            lore.add("§eНажмите на игрока, затем выберите действие");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private static ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§e§lИНФОРМАЦИЯ"));
        meta.setLore(List.of(
                "§7Зеленый §a■ §7- готов к повышению",
                "§7Красный §c■ §7- минимальный ранг",
                "§7Золотой §6■ §7- максимальный ранг",
                "",
                "§7Повышение: игрок должен иметь",
                "§7достаточно Contribution",
                "§7Понижение: всегда доступно, но",
                "§7Contribution уменьшится до минимума ранга"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§c§lНАЗАД"));
        meta.setLore(List.of("§7Вернуться в главное меню"));
        item.setItemMeta(meta);
        return item;
    }

    // Метод для открытия меню подтверждения
    public static void openConfirmation(Player leader, Player target, boolean isPromote) {
        String action = isPromote ? "повысить" : "понизить";
        String color = isPromote ? "§a" : "§c";

        Inventory inv = Bukkit.createInventory(
                new GUIHolder(null, "confirm_action"),
                27,
                Component.text("Подтверждение действия").color(TextColor.color(255, 215, 0))
        );

        // Информация об игроке
        ItemStack infoItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta infoMeta = (SkullMeta) infoItem.getItemMeta();
        infoMeta.setOwningPlayer(target);
        infoMeta.displayName(Component.text(color + "§l" + target.getName()));

        PlayerData targetData = plugin.getGuildManager().getPlayerData(target.getUniqueId());
        Rank targetRank = targetData.getRank();
        Rank newRank = isPromote ?
                plugin.getGuildManager().getNextRank(targetData.getGuild(), targetRank) :
                plugin.getGuildManager().getPreviousRank(targetData.getGuild(), targetRank);

        List<String> lore = new ArrayList<>();
        lore.add("§7Действие: " + color + action);
        lore.add("§7Текущий ранг: " + targetData.getGuild().getColorCode() + targetRank.getDisplayName());
        lore.add("§7Новый ранг: " + targetData.getGuild().getColorCode() + newRank.getDisplayName());
        if (isPromote) {
            int needed = newRank.getMinContribution() - targetData.getContribution();
            if (needed > 0) {
                lore.add("§cНедостаточно очков! Нужно ещё: §e" + needed);
            }
        } else {
            lore.add("§7Contribution будет уменьшен до: §e" + newRank.getMinContribution());
        }
        infoMeta.setLore(lore);
        infoItem.setItemMeta(infoMeta);
        inv.setItem(13, infoItem);

        // Кнопка подтверждения
        ItemStack confirmItem = new ItemStack(isPromote ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.displayName(Component.text(color + "§lПОДТВЕРДИТЬ"));
        confirmMeta.setLore(List.of("§7Нажмите, чтобы " + action));
        confirmItem.setItemMeta(confirmMeta);
        inv.setItem(11, confirmItem);

        // Кнопка отмены
        ItemStack cancelItem = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.displayName(Component.text("§c§lОТМЕНА"));
        cancelMeta.setLore(List.of("§7Вернуться назад"));
        cancelItem.setItemMeta(cancelMeta);
        inv.setItem(15, cancelItem);

        // Сохраняем информацию о действии в холдере
        GUIHolder holder = (GUIHolder) inv.getHolder();
        if (holder != null) {
            // Можно добавить дополнительные данные в холдер если нужно
        }

        leader.openInventory(inv);
    }
}