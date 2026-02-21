package ru.guildforge.guilds;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import ru.guildforge.GuildForge;
import ru.guildforge.data.PlayerData;

public class AlchemistEffect implements Listener {

    private final GuildForge plugin;
    private final GuildManager guildManager;

    public AlchemistEffect(GuildForge plugin) {
        this.plugin = plugin;
        this.guildManager = plugin.getGuildManager();
    }

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Проверяем, что это зелье
        if (!item.getType().toString().contains("POTION")) return;

        PlayerData data = guildManager.getPlayerData(player.getUniqueId());

        // Проверяем, что игрок в гильдии алхимиков
        if (data == null || data.getGuild() != GuildType.ALCHEMIST) return;

        // Получаем ранг игрока
        Rank rank = data.getRank();

        // Получаем эффекты зелья
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null || !meta.hasCustomEffects()) {
            return;
        }

        // Отменяем стандартное применение зелья
        event.setCancelled(true);

        // Удаляем зелье из руки
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        // Определяем множитель на основе ранга
        double multiplier = getMultiplierByRank(rank);

        // Применяем эффекты с увеличенной длительностью
        for (PotionEffect effect : meta.getCustomEffects()) {
            int newDuration = (int) (effect.getDuration() * multiplier);

            // Ограничиваем максимальную длительность (1 час = 72000 тиков)
            if (newDuration > 72000) {
                newDuration = 72000;
            }

            player.addPotionEffect(new PotionEffect(
                    effect.getType(),
                    newDuration,
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.hasParticles(),
                    effect.hasIcon()
            ));
        }

        // Звук
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK,
                org.bukkit.SoundCategory.PLAYERS,
                1.0f, 1.0f);

        // Сообщение
        String message = plugin.getMessageManager().getMessage("alchemist.potion_boosted")
                .replace("%multiplier%", String.valueOf(multiplier));
        player.sendMessage(message);
    }

    /**
     * Определяет множитель длительности зелий на основе ранга алхимика
     * @param rank ранг игрока
     * @return множитель (от 2.5 до 4.0)
     */
    private double getMultiplierByRank(Rank rank) {
        // Используем minContribution для определения множителя
        int minContribution = rank.getMinContribution();

        if (minContribution >= 1500) {
            return 4.0;      // Старейшина/Верховный/Хранитель: x4
        } else if (minContribution >= 500) {
            return 3.5;      // Мастер: x3.5
        } else if (minContribution >= 100) {
            return 3.0;      // Подмастерье/Зельевар и т.д.: x3
        } else {
            return 2.5;      // Ученик/Стажёр: x2.5
        }
    }

    /**
     * Альтернативный метод с прямой проверкой названия ранга
     * @param rank ранг игрока
     * @return множитель
     */
    private double getMultiplierByRankName(Rank rank) {
        String name = rank.name();

        if (name.contains("ELDER") || name.contains("HIGH") || name.contains("KEEPER")) {
            return 4.0;  // Высшие ранги
        } else if (name.contains("MASTER")) {
            return 3.5;  // Мастера
        } else if (name.contains("JOURNEYMAN") || name.contains("BREWER") ||
                name.contains("PATHFINDER") || name.contains("MINER")) {
            return 3.0;  // Средние ранги
        } else {
            return 2.5;  // Начальные ранги
        }
    }
}