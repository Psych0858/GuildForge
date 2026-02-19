package ru.guildforge.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.guildforge.GuildForge;
import ru.guildforge.recipes.ExclusiveItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExclusiveItemListener implements Listener {

    private final GuildForge plugin;
    // Ключ: UUID игрока + Название предмета
    private final Map<String, Long> itemCooldowns = new HashMap<>();

    public ExclusiveItemListener(GuildForge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !ExclusiveItems.isExclusiveItem(item)) return;

        event.setCancelled(true);

        String itemName = item.getItemMeta().getDisplayName();
        String cooldownKey = player.getUniqueId().toString() + "_" + itemName;

        // Проверяем кулдаун для этого КОНКРЕТНОГО предмета у этого игрока
        if (itemCooldowns.containsKey(cooldownKey)) {
            long lastUse = itemCooldowns.get(cooldownKey);
            long now = System.currentTimeMillis();
            int cooldownHours = ExclusiveItems.getItemCooldownHours(item);
            long cooldownMillis = cooldownHours * 60 * 60 * 1000L;

            if (now - lastUse < cooldownMillis) {
                long remainingHours = (cooldownMillis - (now - lastUse)) / (60 * 60 * 1000);
                long remainingMinutes = ((cooldownMillis - (now - lastUse)) % (60 * 60 * 1000)) / (60 * 1000);

                player.sendMessage("§cЭтот предмет ещё не восстановился! Осталось: §e"
                        + remainingHours + "ч " + remainingMinutes + "м");
                return;
            }
        }

        // Используем предмет
        if (itemName.contains("Книга Починки")) {
            useMendingBook(player, item);
        } else if (itemName.contains("Вечный Флакон")) {
            useEternalPotion(player, item);
        } else if (itemName.contains("Карта Возвращения")) {
            useReturnMap(player, item);
        } else if (itemName.contains("Золотое Яблоко Изобилия")) {
            useGoldenApple(player, item);
        } else if (itemName.contains("Сердце Горы")) {
            useHeartOfMountain(player, item);
        }

        // Сохраняем кулдаун для этого КОНКРЕТНОГО предмета
        itemCooldowns.put(cooldownKey, System.currentTimeMillis());
    }

    // ... остальные методы useMendingBook, useEternalPotion и т.д. ...

    /**
     * Использование книги починки
     */
    private void useMendingBook(Player player, ItemStack book) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.ENCHANTED_BOOK) {
            player.sendMessage("§cНельзя применить книгу на книгу!");
            return;
        }

        if (item.getType() == Material.AIR) {
            player.sendMessage("§cВозьмите предмет, который хотите зачаровать!");
            return;
        }

        item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.MENDING, 1);

        book.setAmount(book.getAmount() - 1);
        player.getInventory().setItemInMainHand(item);

        player.sendMessage("§aВы успешно применили книгу починки!");
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
    }

    /**
     * Использование вечного флакона
     */
    private void useEternalPotion(Player player, ItemStack potion) {
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.REGENERATION, 400, 1));
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.ABSORPTION, 2400, 1));
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.RESISTANCE, 1200, 0));

        player.sendMessage("§dВы выпили вечный флакон!");
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_DRINK, 1.0f, 1.0f);

        // Предмет НЕ исчезает (вечный)
    }

    /**
     * Использование карты возвращения
     */
    private void useReturnMap(Player player, ItemStack map) {
        Location bedLocation = player.getBedSpawnLocation();

        if (bedLocation == null) {
            bedLocation = player.getWorld().getSpawnLocation();
        }

        player.teleportAsync(bedLocation).thenAccept(success -> {
            if (success) {
                player.sendMessage("§bКарта возвращения перенесла вас домой!");
                player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                map.setAmount(map.getAmount() - 1);
            }
        });
    }

    /**
     * Использование золотого яблока
     */
    private void useGoldenApple(Player player, ItemStack apple) {
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SATURATION)) {
            player.sendMessage("§cУ вас уже есть эффект изобилия!");
            return;
        }

        // 24 часа = 1728000 тиков
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SATURATION, 1728000, 0, true, true, true));

        player.sendMessage("§aВы съели золотое яблоко изобилия!");
        player.sendMessage("§7Теперь ваш голод не будет уменьшаться 24 часа!");
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);

        apple.setAmount(apple.getAmount() - 1);
    }

    /**
     * Использование сердца горы
     */
    private void useHeartOfMountain(Player player, ItemStack heart) {
        World world = player.getWorld();
        Location loc = player.getLocation();

        Material[] ores = {
                Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE,
                Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.LAPIS_ORE,
                Material.REDSTONE_ORE
        };

        int generated = 0;
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (generated >= 10) break;

                    Block block = loc.clone().add(x, y, z).getBlock();
                    if (block.getType() == Material.STONE || block.getType() == Material.DEEPSLATE) {
                        Material ore = ores[(int)(Math.random() * ores.length)];
                        block.setType(ore);
                        generated++;
                    }
                }
            }
        }

        player.sendMessage("§8Сердце горы пробудило руду вокруг вас!");
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);

        heart.setAmount(heart.getAmount() - 1);
    }
}