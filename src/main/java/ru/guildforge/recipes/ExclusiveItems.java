package ru.guildforge.recipes;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import ru.guildforge.GuildForge;
import ru.guildforge.guilds.GuildType;

import java.util.ArrayList;
import java.util.List;

public class ExclusiveItems {
    private final GuildForge plugin;

    // Ключи для NamespacedKey (для NBT данных)
    public static final NamespacedKey EXCLUSIVE_ITEM_KEY;
    public static final NamespacedKey GUILD_TYPE_KEY;
    public static final NamespacedKey COOLDOWN_KEY;

    static {
        EXCLUSIVE_ITEM_KEY = new NamespacedKey("guildforge", "exclusive_item");
        GUILD_TYPE_KEY = new NamespacedKey("guildforge", "guild_type");
        COOLDOWN_KEY = new NamespacedKey("guildforge", "cooldown_hours");
    }

    public ExclusiveItems(GuildForge plugin) {
        this.plugin = plugin;
    }

    /**
     * Создать книгу починки (для Кузнецов)
     */
    public static ItemStack createMendingBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        meta.displayName(Component.text("§7§lКнига Починки"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Эксклюзивный предмет Кузнецов"));
        lore.add(Component.text("§8Позволяет наложить чары"));
        lore.add(Component.text("§8«Починка» на любой предмет"));
        lore.add(Component.text(""));
        lore.add(Component.text("§7▪ Кулдаун: §f1 день"));
        lore.add(Component.text("§7▪ Требуется: §f50 уровень, 5000 Contribution"));
        lore.add(Component.text(""));
        lore.add(Component.text("§eНажмите ПКМ, чтобы использовать"));

        meta.lore(lore);

        // Добавляем NBT метки через PersistentDataContainer
        // Временно без PDC для простоты

        book.setItemMeta(meta);
        book.addUnsafeEnchantment(Enchantment.MENDING, 1);

        return book;
    }

    /**
     * Создать вечный флакон (для Алхимиков)
     */
    public static ItemStack createEternalPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        meta.displayName(Component.text("§d§lВечный Флакон"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Эксклюзивный предмет Алхимиков"));
        lore.add(Component.text("§dМногоразовое зелье"));
        lore.add(Component.text("§dНе исчезает после использования"));
        lore.add(Component.text(""));
        lore.add(Component.text("§7▪ Кулдаун: §f3 дня"));
        lore.add(Component.text("§7▪ Требуется: §f100 уровень, 10000 Contribution"));
        lore.add(Component.text(""));
        lore.add(Component.text("§eНажмите ПКМ, чтобы выпить"));

        meta.lore(lore);

        // Добавляем эффект
        meta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 1), true);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 1), true);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.RESISTANCE, 1200, 0), true);

        potion.setItemMeta(meta);

        return potion;
    }

    /**
     * Создать карту возвращения (для Картографов)
     */
    public static ItemStack createReturnMap() {
        ItemStack map = new ItemStack(Material.FILLED_MAP);
        ItemMeta meta = map.getItemMeta();

        meta.displayName(Component.text("§b§lКарта Возвращения"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Эксклюзивный предмет Картографов"));
        lore.add(Component.text("§bОдноразовый варп домой"));
        lore.add(Component.text(""));
        lore.add(Component.text("§7▪ Кулдаун: §f2 дня"));
        lore.add(Component.text("§7▪ Требуется: §f50 уровень, 10000 Contribution"));
        lore.add(Component.text(""));
        lore.add(Component.text("§eНажмите ПКМ, чтобы телепортироваться"));

        meta.lore(lore);
        map.setItemMeta(meta);

        return map;
    }

    /**
     * Создать золотое яблоко изобилия (для Фермеров)
     */
    public static ItemStack createGoldenApple() {
        ItemStack apple = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        ItemMeta meta = apple.getItemMeta();

        meta.displayName(Component.text("§a§lЗолотое Яблоко Изобилия"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Эксклюзивный предмет Фермеров"));
        lore.add(Component.text("§aУлучшенное золотое яблоко"));
        lore.add(Component.text(""));
        lore.add(Component.text("§7▪ Кулдаун: §f3 дня"));
        lore.add(Component.text("§7▪ Требуется: §f30 уровень, 8000 Contribution"));
        lore.add(Component.text(""));
        lore.add(Component.text("§eНажмите ПКМ, чтобы съесть"));

        meta.lore(lore);
        apple.setItemMeta(meta);

        return apple;
    }

    /**
     * Создать сердце горы (для Шахтеров)
     */
    public static ItemStack createHeartOfMountain() {
        ItemStack heart = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = heart.getItemMeta();

        meta.displayName(Component.text("§8§lСердце Горы"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Эксклюзивный предмет Шахтёров"));
        lore.add(Component.text("§8Генерирует руду вокруг игрока"));
        lore.add(Component.text(""));
        lore.add(Component.text("§7▪ Кулдаун: §f7 дней"));
        lore.add(Component.text("§7▪ Требуется: §f100 уровень, 20000 Contribution"));
        lore.add(Component.text(""));
        lore.add(Component.text("§eНажмите ПКМ, чтобы активировать"));

        meta.lore(lore);
        heart.setItemMeta(meta);

        return heart;
    }

    /**
     * Проверить, является ли предмет эксклюзивным
     */
    public static boolean isExclusiveItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return false;

        String name = meta.displayName().toString();
        return name.contains("Книга Починки") ||
                name.contains("Вечный Флакон") ||
                name.contains("Карта Возвращения") ||
                name.contains("Золотое Яблоко Изобилия") ||
                name.contains("Сердце Горы");
    }

    /**
     * Получить тип гильдии по предмету
     */
    public static GuildType getGuildFromItem(ItemStack item) {
        if (!isExclusiveItem(item)) return null;

        String name = item.getItemMeta().displayName().toString();

        if (name.contains("Книга Починки")) return GuildType.SMITH;
        if (name.contains("Вечный Флакон")) return GuildType.ALCHEMIST;
        if (name.contains("Карта Возвращения")) return GuildType.CARTOGRAPHER;
        if (name.contains("Золотое Яблоко Изобилия")) return GuildType.FARMER;
        if (name.contains("Сердце Горы")) return GuildType.MINER;

        return null;
    }

    /**
     * Получить кулдаун предмета в часах
     */
    public static int getItemCooldownHours(ItemStack item) {
        if (!isExclusiveItem(item)) return 0;

        String name = item.getItemMeta().displayName().toString();

        if (name.contains("Книга Починки")) return 24; // 1 день
        if (name.contains("Вечный Флакон")) return 72; // 3 дня
        if (name.contains("Карта Возвращения")) return 48; // 2 дня
        if (name.contains("Золотое Яблоко Изобилия")) return 72; // 3 дня
        if (name.contains("Сердце Горы")) return 168; // 7 дней

        return 0;
    }
}