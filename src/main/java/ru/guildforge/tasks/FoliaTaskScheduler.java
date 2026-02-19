package ru.guildforge.tasks;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import ru.guildforge.GuildForge;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Класс-обёртка для работы с Folia scheduling
 * Позволяет безопасно выполнять задачи в многопоточной среде Folia
 */
public class FoliaTaskScheduler {
    private final GuildForge plugin;
    private final GlobalRegionScheduler globalScheduler;
    private final AsyncScheduler asyncScheduler;
    private final RegionScheduler regionScheduler;

    public FoliaTaskScheduler(GuildForge plugin) {
        this.plugin = plugin;
        this.globalScheduler = plugin.getServer().getGlobalRegionScheduler();
        this.asyncScheduler = plugin.getServer().getAsyncScheduler();
        this.regionScheduler = plugin.getServer().getRegionScheduler();
    }

    /**
     * Запустить задачу в глобальном регионе (не привязана к конкретному чанку)
     */
    public void runGlobal(Runnable task) {
        globalScheduler.run(plugin, scheduledTask -> task.run());
    }

    /**
     * Запустить задачу в глобальном регионе с задержкой
     */
    public void runGlobalLater(Runnable task, long delayTicks) {
        globalScheduler.runDelayed(plugin, scheduledTask -> task.run(), delayTicks);
    }

    /**
     * Запустить периодическую задачу в глобальном регионе
     */
    public void runGlobalTimer(Runnable task, long delayTicks, long periodTicks) {
        globalScheduler.runAtFixedRate(plugin, scheduledTask -> task.run(), delayTicks, periodTicks);
    }

    /**
     * Запустить асинхронную задачу
     */
    public void runAsync(Runnable task) {
        asyncScheduler.runNow(plugin, scheduledTask -> task.run());
    }

    /**
     * Запустить асинхронную задачу с задержкой
     */
    public void runAsyncLater(Runnable task, long delay, TimeUnit unit) {
        asyncScheduler.runDelayed(plugin, scheduledTask -> task.run(), delay, unit);
    }

    /**
     * Запустить задачу в регионе, где находится сущность
     */
    public void runAtEntity(Entity entity, Consumer<Entity> task) {
        EntityScheduler scheduler = entity.getScheduler();
        scheduler.run(plugin, scheduledTask -> task.accept(entity), null);
    }

    /**
     * Запустить задачу в регионе, где находится сущность, с задержкой
     */
    public void runAtEntityLater(Entity entity, Consumer<Entity> task, long delayTicks) {
        EntityScheduler scheduler = entity.getScheduler();
        scheduler.runDelayed(plugin, scheduledTask -> task.accept(entity), null, delayTicks);
    }

    /**
     * Запустить задачу в регионе, где находится локация
     */
    public void runAtLocation(Location location, Runnable task) {
        regionScheduler.run(plugin, location, scheduledTask -> task.run());
    }

    /**
     * Запустить задачу в регионе мира
     */
    public void runAtWorld(World world, int chunkX, int chunkZ, Runnable task) {
        regionScheduler.run(plugin, world, chunkX, chunkZ, scheduledTask -> task.run());
    }
}