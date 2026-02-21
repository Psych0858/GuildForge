package ru.guildforge.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import ru.guildforge.GuildForge;
import ru.guildforge.data.PlayerData;
import ru.guildforge.guilds.GuildType;
import ru.guildforge.guilds.Rank;

import java.util.*;

public class GuildListener implements Listener {
    private final GuildForge plugin;
    private final Set<Long> exploredChunks = new HashSet<>();

    // Список ПРОСТЫХ предметов (которые НЕ ДАЮТ Contribution)
    private final Set<Material> simpleCrafts = new HashSet<>();

    public GuildListener(GuildForge plugin) {
        this.plugin = plugin;
        initSimpleCrafts();
    }

    /**
     * Инициализация списка ПРОСТЫХ крафтов (которые НЕ ДАЮТ Contribution)
     */
    private void initSimpleCrafts() {
        // ===== БЛОКИ ИЗ 9 ИНГРЕДИЕНТОВ =====
        simpleCrafts.add(Material.DIAMOND_BLOCK);
        simpleCrafts.add(Material.IRON_BLOCK);
        simpleCrafts.add(Material.GOLD_BLOCK);
        simpleCrafts.add(Material.EMERALD_BLOCK);
        simpleCrafts.add(Material.LAPIS_BLOCK);
        simpleCrafts.add(Material.REDSTONE_BLOCK);
        simpleCrafts.add(Material.COAL_BLOCK);
        simpleCrafts.add(Material.COPPER_BLOCK);
        simpleCrafts.add(Material.NETHERITE_BLOCK);
        simpleCrafts.add(Material.QUARTZ_BLOCK);
        simpleCrafts.add(Material.AMETHYST_BLOCK);
        simpleCrafts.add(Material.RAW_IRON_BLOCK);
        simpleCrafts.add(Material.RAW_GOLD_BLOCK);
        simpleCrafts.add(Material.RAW_COPPER_BLOCK);
        simpleCrafts.add(Material.SNOW_BLOCK);
        simpleCrafts.add(Material.HAY_BLOCK);
        simpleCrafts.add(Material.BONE_BLOCK);
        simpleCrafts.add(Material.SLIME_BLOCK);
        simpleCrafts.add(Material.HONEY_BLOCK);
        simpleCrafts.add(Material.DRIED_KELP_BLOCK);
        simpleCrafts.add(Material.MELON);
        simpleCrafts.add(Material.PUMPKIN);

        // ===== СЛИТКИ ИЗ БЛОКОВ =====
        simpleCrafts.add(Material.IRON_INGOT);
        simpleCrafts.add(Material.GOLD_INGOT);
        simpleCrafts.add(Material.COPPER_INGOT);
        simpleCrafts.add(Material.NETHERITE_INGOT);

        // ===== ДРАГОЦЕННОСТИ ИЗ БЛОКОВ =====
        simpleCrafts.add(Material.DIAMOND);
        simpleCrafts.add(Material.EMERALD);
        simpleCrafts.add(Material.LAPIS_LAZULI);
        simpleCrafts.add(Material.QUARTZ);
        simpleCrafts.add(Material.AMETHYST_SHARD);
        simpleCrafts.add(Material.COAL);
        simpleCrafts.add(Material.CHARCOAL);
        simpleCrafts.add(Material.REDSTONE);

        // ===== ПРОСТЫЕ РЕЦЕПТЫ =====
        simpleCrafts.add(Material.STICK);              // Палки
        simpleCrafts.add(Material.TORCH);              // Факелы
        simpleCrafts.add(Material.SOUL_TORCH);         // Факелы души
        simpleCrafts.add(Material.LADDER);              // Лестницы
        simpleCrafts.add(Material.OAK_PLANKS);          // Доски (все виды)
        simpleCrafts.add(Material.SPRUCE_PLANKS);
        simpleCrafts.add(Material.BIRCH_PLANKS);
        simpleCrafts.add(Material.JUNGLE_PLANKS);
        simpleCrafts.add(Material.ACACIA_PLANKS);
        simpleCrafts.add(Material.DARK_OAK_PLANKS);
        simpleCrafts.add(Material.MANGROVE_PLANKS);
        simpleCrafts.add(Material.CHERRY_PLANKS);
        simpleCrafts.add(Material.BAMBOO_PLANKS);
        simpleCrafts.add(Material.CRIMSON_PLANKS);
        simpleCrafts.add(Material.WARPED_PLANKS);

        // ===== БАЗОВЫЕ БЛОКИ =====
        simpleCrafts.add(Material.OAK_SLAB);            // Плиты
        simpleCrafts.add(Material.OAK_STAIRS);          // Ступеньки
        simpleCrafts.add(Material.OAK_FENCE);           // Заборы
        simpleCrafts.add(Material.OAK_FENCE_GATE);      // Калитки
        simpleCrafts.add(Material.OAK_DOOR);            // Двери
        simpleCrafts.add(Material.OAK_TRAPDOOR);        // Люки
        simpleCrafts.add(Material.OAK_PRESSURE_PLATE);  // Нажимные плиты
        simpleCrafts.add(Material.OAK_BUTTON);          // Кнопки
        simpleCrafts.add(Material.OAK_SIGN);            // Таблички
        simpleCrafts.add(Material.OAK_HANGING_SIGN);    // Подвесные таблички

        // ===== СТЕКЛО =====
        simpleCrafts.add(Material.GLASS);               // Стекло
        simpleCrafts.add(Material.GLASS_PANE);          // Стеклянные панели
        simpleCrafts.add(Material.GLASS_BOTTLE);        // Стеклянные бутылки

        // ===== КРАСИТЕЛИ =====
        simpleCrafts.add(Material.WHITE_DYE);
        simpleCrafts.add(Material.ORANGE_DYE);
        simpleCrafts.add(Material.MAGENTA_DYE);
        simpleCrafts.add(Material.LIGHT_BLUE_DYE);
        simpleCrafts.add(Material.YELLOW_DYE);
        simpleCrafts.add(Material.LIME_DYE);
        simpleCrafts.add(Material.PINK_DYE);
        simpleCrafts.add(Material.GRAY_DYE);
        simpleCrafts.add(Material.LIGHT_GRAY_DYE);
        simpleCrafts.add(Material.CYAN_DYE);
        simpleCrafts.add(Material.PURPLE_DYE);
        simpleCrafts.add(Material.BLUE_DYE);
        simpleCrafts.add(Material.BROWN_DYE);
        simpleCrafts.add(Material.GREEN_DYE);
        simpleCrafts.add(Material.RED_DYE);
        simpleCrafts.add(Material.BLACK_DYE);

        // ===== ПРОСТЫЕ МЕХАНИЗМЫ =====
        simpleCrafts.add(Material.LEVER);               // Рычаг
        simpleCrafts.add(Material.STONE_BUTTON);        // Каменная кнопка
        simpleCrafts.add(Material.STONE_PRESSURE_PLATE); // Каменная плита
        simpleCrafts.add(Material.HEAVY_WEIGHTED_PRESSURE_PLATE); // Весовая плита
        simpleCrafts.add(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);

        // ===== БУМАГА И КНИГИ =====
        simpleCrafts.add(Material.PAPER);               // Бумага
        simpleCrafts.add(Material.BOOK);                 // Книга
        simpleCrafts.add(Material.BOOKSHELF);            // Книжная полка

        // ===== КОВРЫ =====
        simpleCrafts.add(Material.WHITE_CARPET);
        simpleCrafts.add(Material.ORANGE_CARPET);
        simpleCrafts.add(Material.MAGENTA_CARPET);
        simpleCrafts.add(Material.LIGHT_BLUE_CARPET);
        simpleCrafts.add(Material.YELLOW_CARPET);
        simpleCrafts.add(Material.LIME_CARPET);
        simpleCrafts.add(Material.PINK_CARPET);
        simpleCrafts.add(Material.GRAY_CARPET);
        simpleCrafts.add(Material.LIGHT_GRAY_CARPET);
        simpleCrafts.add(Material.CYAN_CARPET);
        simpleCrafts.add(Material.PURPLE_CARPET);
        simpleCrafts.add(Material.BLUE_CARPET);
        simpleCrafts.add(Material.BROWN_CARPET);
        simpleCrafts.add(Material.GREEN_CARPET);
        simpleCrafts.add(Material.RED_CARPET);
        simpleCrafts.add(Material.BLACK_CARPET);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Загружаем данные игрока асинхронно
        plugin.getDatabaseManager().loadPlayerData(player.getUniqueId())
                .thenAccept(data -> {
                    // После загрузки, выполняем в глобальном регионе
                    plugin.getTaskScheduler().runGlobal(() -> {
                        plugin.getGuildManager().addToCache(player.getUniqueId(), data);

                        // Приветственное сообщение для членов гильдии
                        if (data.hasGuild()) {
                            player.sendMessage("§aДобро пожаловать в гильдию " +
                                    data.getGuild().getColorCode() + data.getGuild().getDisplayName());
                            player.sendMessage("§7Ваш ранг: " + data.getRank().getDisplayName());
                            player.sendMessage("§7Contribution: §e" + data.getContribution());

                            // 👇 ВЫЗОВ API - игрок загрузился (не вступление)
                             plugin.getAPIImpl().fireGuildJoin(player, data.getGuild());
                            // НЕ НУЖНО, это не вступление
                        }
                    });
                });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());

        if (data != null) {
            // Сохраняем данные асинхронно
            plugin.getDatabaseManager().savePlayerData(data)
                    .thenRun(() -> {
                        plugin.getTaskScheduler().runGlobal(() -> {
                            plugin.getGuildManager().removeFromCache(player.getUniqueId());
                        });
                    });
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        plugin.getTaskScheduler().runAtEntity(player, (taskPlayer) -> {
            PlayerData data = plugin.getGuildManager().getPlayerData(taskPlayer.getUniqueId());

            if (data == null || !data.hasGuild()) return;

            Block block = event.getBlock();
            Material blockType = block.getType();

            Rank oldRank = data.getRank();

            if (data.getGuild() == GuildType.CARTOGRAPHER && isRareBlock(blockType)) {
                if (Math.random() < 0.05) { // 5% шанс
                    player.getInventory().addItem(createTreasureMap(player.getLocation()));
                    player.sendMessage("§bВы нашли карту сокровищ!");
                }
            }

            // Для шахтеров - добыча руды
            if (data.getGuild() == GuildType.MINER && isOre(blockType)) {
                int amount = 1;

                // Бонус за глубокий сланец
                if (blockType.name().startsWith("DEEPSLATE_")) {
                    amount = 2;
                }

                data.addContribution(amount);

                taskPlayer.sendActionBar(Component.text("§a+" + amount + " Contribution §7(Добыча руды)"));

                plugin.getLogger().info(taskPlayer.getName() + " gained +" + amount + " Contribution from mining " + blockType);
                plugin.getDatabaseManager().savePlayerData(data);

                checkRankUp((Player) taskPlayer, data, oldRank);
            }

            // Для фермеров - сбор урожая
            if (data.getGuild() == GuildType.FARMER && isCrop(blockType) && isFullyGrown(block)) {
                data.addContribution(1);

                taskPlayer.sendActionBar(Component.text("§a+1 Contribution §7(Сбор урожая)"));

                plugin.getLogger().info(taskPlayer.getName() + " gained +1 Contribution from farming " + blockType);
                plugin.getDatabaseManager().savePlayerData(data);

                checkRankUp((Player) taskPlayer, data, oldRank);
            }
        });
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();

        plugin.getTaskScheduler().runAtEntity(player, (taskPlayer) -> {
            PlayerData data = plugin.getGuildManager().getPlayerData(taskPlayer.getUniqueId());

            if (data == null || !data.hasGuild()) return;

            Rank oldRank = data.getRank();

            // Для кузнецов - зачарование
            if (data.getGuild() == GuildType.SMITH) {
                int amount = 5;

                // Больше опыта за редкие чары
                if (event.getExpLevelCost() > 20) {
                    amount = 10;
                }

                data.addContribution(amount);

                taskPlayer.sendMessage("§a+" + amount + " Contribution §7(Зачарование предмета)");

                plugin.getLogger().info(taskPlayer.getName() + " gained +" + amount + " Contribution from enchanting");
                plugin.getDatabaseManager().savePlayerData(data);

                checkRankUp((Player) taskPlayer, data, oldRank);
            }
        });
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {

            plugin.getTaskScheduler().runAtEntity(player, (taskPlayer) -> {
                PlayerData data = plugin.getGuildManager().getPlayerData(taskPlayer.getUniqueId());

                if (data == null || !data.hasGuild()) return;

                Rank oldRank = data.getRank();

                // Для кузнецов - крафт
                if (data.getGuild() == GuildType.SMITH) {

                    // Получаем результат крафта
                    ItemStack result = event.getCurrentItem();
                    if (result == null) return;

                    Material resultType = result.getType();

                    // Проверяем, является ли предмет ПРОСТЫМ (НЕ ДАЕМ Contribution)
                    if (isSimpleCraft(resultType)) {
                        taskPlayer.sendActionBar(Component.text("§7Простой крафт не дает Contribution"));
                        return;
                    }

                    // Если предмет не в списке простых - ДАЕМ Contribution
                    data.addContribution(1);
                    taskPlayer.sendActionBar(Component.text("§a+1 Contribution §7(Крафт)"));

                    plugin.getDatabaseManager().savePlayerData(data);
                    checkRankUp((Player) taskPlayer, data, oldRank);
                }
            });
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Проверяем, перешел ли игрок в новый чанк
        int oldX = event.getFrom().getChunk().getX();
        int oldZ = event.getFrom().getChunk().getZ();
        int newX = event.getTo().getChunk().getX();
        int newZ = event.getTo().getChunk().getZ();

        if (oldX != newX || oldZ != newZ) {
            // Игрок перешел в новый чанк
            plugin.getTaskScheduler().runAtEntity(player, (taskPlayer) -> {
                PlayerData data = plugin.getGuildManager().getPlayerData(taskPlayer.getUniqueId());

                if (data == null || !data.hasGuild()) return;

                Rank oldRank = data.getRank();

                // Для картографов - исследование новых чанков
                if (data.getGuild() == GuildType.CARTOGRAPHER) {
                    long chunkKey = ((long) taskPlayer.getWorld().hashCode()) << 32 |
                            (((long) newX) & 0xffffffffL) << 32 |
                            (((long) newZ) & 0xffffffffL);

                    if (!exploredChunks.contains(chunkKey)) {
                        exploredChunks.add(chunkKey);

                        data.addContribution(1);

                        taskPlayer.sendActionBar(Component.text("§a+1 Contribution §7(Исследование нового чанка)"));

                        plugin.getLogger().info(taskPlayer.getName() + " gained +1 Contribution from exploring");
                        plugin.getDatabaseManager().savePlayerData(data);

                        checkRankUp((Player) taskPlayer, data, oldRank);
                    }
                }
            });
        }
    }

    /**
     * Проверка на повышение ранга
     */
    public void checkRankUp(Player player, PlayerData data, Rank oldRank) {
        Rank newRank = data.getRank();

        if (oldRank != null && newRank != null && newRank.ordinal() > oldRank.ordinal()) {
            player.sendMessage(" ");
            player.sendMessage("§6§l⚡ ПОВЫШЕНИЕ РАНГА! ⚡");
            player.sendMessage("§7Ваш новый ранг: " + data.getGuild().getColorCode() +
                    newRank.getDisplayName());
            player.sendMessage(" ");
            plugin.getAPIImpl().fireRankUp(player, oldRank, newRank);

            player.playSound(player.getLocation(),
                    org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

            if (data.isMaster()) {
                player.sendMessage("§6§lТЕПЕРЬ ВАМ ДОСТУПЕН ЭКСКЛЮЗИВНЫЙ ПРЕДМЕТ!");
                player.sendMessage("§7Зайдите в меню гильдии (/guild) чтобы получить его!");
            }
        }
    }

    /**
     * Проверка, является ли крафт ПРОСТЫМ (НЕ ДАЕМ Contribution)
     */
    private boolean isSimpleCraft(Material material) {
        return simpleCrafts.contains(material);
    }

    /**
     * Проверка на руду
     */
    private boolean isOre(Material material) {
        return material.name().endsWith("_ORE") ||
                material == Material.ANCIENT_DEBRIS ||
                material == Material.NETHER_QUARTZ_ORE ||
                material == Material.DEEPSLATE_COAL_ORE ||
                material == Material.DEEPSLATE_COPPER_ORE ||
                material == Material.DEEPSLATE_DIAMOND_ORE ||
                material == Material.DEEPSLATE_EMERALD_ORE ||
                material == Material.DEEPSLATE_GOLD_ORE ||
                material == Material.DEEPSLATE_IRON_ORE ||
                material == Material.DEEPSLATE_LAPIS_ORE ||
                material == Material.DEEPSLATE_REDSTONE_ORE ||
                material == Material.NETHER_GOLD_ORE ||
                material == Material.GILDED_BLACKSTONE;
    }

    private ItemStack createTreasureMap(Location location) {
        ItemStack map = new ItemStack(Material.MAP);
        ItemMeta meta = map.getItemMeta();
        meta.setDisplayName("§b§lКарта сокровищ");

        List<String> lore = new ArrayList<>();
        lore.add("§7Ведите к спрятанному сокровищу!");
        lore.add("§7Радиус: §e500 блоков");
        meta.setLore(lore);

        map.setItemMeta(meta);
        return map;
    }

    /**
     * Проверка на редкие блоки для карт
     */
    private boolean isRareBlock(Material material) {
        return material == Material.DIAMOND_ORE ||
                material == Material.EMERALD_ORE ||
                material == Material.ANCIENT_DEBRIS ||
                material == Material.DEEPSLATE_DIAMOND_ORE ||
                material == Material.DEEPSLATE_EMERALD_ORE ||
                material == Material.CHEST ||
                material == Material.SPAWNER;
    }

    /**
     * Проверка на культуры (для фермеров)
     */
    private boolean isCrop(Material material) {
        return material == Material.WHEAT ||
                material == Material.CARROTS ||
                material == Material.POTATOES ||
                material == Material.BEETROOTS ||
                material == Material.SWEET_BERRY_BUSH ||
                material == Material.MELON ||
                material == Material.PUMPKIN ||
                material == Material.SUGAR_CANE ||
                material == Material.BAMBOO ||
                material == Material.CACTUS ||
                material == Material.NETHER_WART ||
                material == Material.COCOA ||
                material == Material.KELP_PLANT ||
                material == Material.KELP;
    }

    /**
     * Проверка на готовность культуры
     */
    private boolean isFullyGrown(Block block) {
        if (block.getBlockData() instanceof Ageable ageable) {
            return ageable.getAge() >= ageable.getMaximumAge();
        }

        Material type = block.getType();
        return type == Material.SUGAR_CANE ||
                type == Material.CACTUS ||
                type == Material.BAMBOO ||
                type == Material.KELP ||
                type == Material.KELP_PLANT;
    }
}