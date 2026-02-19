package ru.guildforge.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import ru.guildforge.GuildForge;
import ru.guildforge.data.PlayerData;
import ru.guildforge.guilds.GuildType;

import java.util.HashSet;
import java.util.Set;

public class VanillaBlockListener implements Listener {
    private final GuildForge plugin;

    // Список руд для шахтёров
    private final Set<Material> ORES = new HashSet<>();

    // Список культур для фермеров
    private final Set<Material> CROPS = new HashSet<>();

    public VanillaBlockListener(GuildForge plugin) {
        this.plugin = plugin;
        initOres();
        initCrops();
    }

    private void initOres() {
        // Обычные руды
        ORES.add(Material.COAL_ORE);
        ORES.add(Material.IRON_ORE);
        ORES.add(Material.GOLD_ORE);
        ORES.add(Material.DIAMOND_ORE);
        ORES.add(Material.EMERALD_ORE);
        ORES.add(Material.LAPIS_ORE);
        ORES.add(Material.REDSTONE_ORE);
        ORES.add(Material.NETHER_QUARTZ_ORE);
        ORES.add(Material.NETHER_GOLD_ORE);
        ORES.add(Material.ANCIENT_DEBRIS);

        // Глубинные руды
        ORES.add(Material.DEEPSLATE_COAL_ORE);
        ORES.add(Material.DEEPSLATE_IRON_ORE);
        ORES.add(Material.DEEPSLATE_GOLD_ORE);
        ORES.add(Material.DEEPSLATE_DIAMOND_ORE);
        ORES.add(Material.DEEPSLATE_EMERALD_ORE);
        ORES.add(Material.DEEPSLATE_LAPIS_ORE);
        ORES.add(Material.DEEPSLATE_REDSTONE_ORE);
    }

    private void initCrops() {
        CROPS.add(Material.WHEAT);
        CROPS.add(Material.CARROTS);
        CROPS.add(Material.POTATOES);
        CROPS.add(Material.BEETROOTS);
        CROPS.add(Material.NETHER_WART);
        CROPS.add(Material.SWEET_BERRY_BUSH);
        CROPS.add(Material.COCOA);
        CROPS.add(Material.SUGAR_CANE);
        CROPS.add(Material.CACTUS);
        CROPS.add(Material.MELON);
        CROPS.add(Material.PUMPKIN);
        CROPS.add(Material.KELP);
        CROPS.add(Material.BAMBOO);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();

        PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
        if (data == null || !data.hasGuild()) return;

        GuildType guild = data.getGuild();

        // ===== ШАХТЁРЫ =====
        if (guild == GuildType.MINER && ORES.contains(type)) {
            int points = getMinerPoints(type);
            plugin.getGuildManager().addContribution(player, points);
            player.sendActionBar("§8⛏ +" + points + " очков шахтёра");
        }

        // ===== ФЕРМЕРЫ =====
        else if (guild == GuildType.FARMER && CROPS.contains(type)) {
            // Для культур проверяем, созрели ли они
            if (isFullyGrown(block)) {
                int points = getFarmerPoints(type);
                plugin.getGuildManager().addContribution(player, points);
                player.sendActionBar("§a🌾 +" + points + " очков фермера");
            }
        }
    }

    /**
     * Проверяет, созрела ли культура
     */
    private boolean isFullyGrown(Block block) {
        BlockData data = block.getBlockData();

        if (data instanceof Ageable) {
            Ageable ageable = (Ageable) data;
            return ageable.getAge() == ageable.getMaximumAge();
        }

        // Для культур без Ageable (кактус, тростник, тыква, арбуз)
        Material type = block.getType();
        return type == Material.CACTUS ||
                type == Material.SUGAR_CANE ||
                type == Material.MELON ||
                type == Material.PUMPKIN ||
                type == Material.KELP ||
                type == Material.BAMBOO;
    }

    /**
     * Очки для шахтёра в зависимости от руды
     */
    private int getMinerPoints(Material ore) {
        switch (ore) {
            case Material.ANCIENT_DEBRIS:
                return 10;
            case Material.DIAMOND_ORE:
            case Material.DEEPSLATE_DIAMOND_ORE:
            case Material.EMERALD_ORE:
            case Material.DEEPSLATE_EMERALD_ORE:
                return 5;
            case Material.GOLD_ORE:
            case Material.DEEPSLATE_GOLD_ORE:
            case Material.NETHER_GOLD_ORE:
                return 3;
            case Material.IRON_ORE:
            case Material.DEEPSLATE_IRON_ORE:
            case Material.LAPIS_ORE:
            case Material.DEEPSLATE_LAPIS_ORE:
            case Material.REDSTONE_ORE:
            case Material.DEEPSLATE_REDSTONE_ORE:
                return 2;
            default:
                return 1;
        }
    }

    /**
     * Очки для фермера в зависимости от культуры
     */
    private int getFarmerPoints(Material crop) {
        switch (crop) {
            case Material.NETHER_WART:
            case Material.MELON:
            case Material.PUMPKIN:
                return 3;
            case Material.WHEAT:
            case Material.CARROTS:
            case Material.POTATOES:
            case Material.BEETROOTS:
                return 2;
            default:
                return 1;
        }
    }

    // ===== БЛОКИРОВКА ПОЧИНКИ (было в оригинале) =====

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        ItemStack result = event.getItem();
        if (result.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
            if (meta != null && meta.hasStoredEnchant(org.bukkit.enchantments.Enchantment.MENDING)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            // TODO: Проверка на книгу починки
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.CHEST ||
                event.getInventory().getType() == InventoryType.BARREL) {
            // TODO: Проверка лута на книгу починки
        }
    }
}