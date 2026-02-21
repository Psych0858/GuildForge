package ru.guildforge.tasks;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FoliaTaskScheduler implements PlatformScheduler {
    private final Plugin plugin;
    private final GlobalRegionScheduler globalScheduler;
    private final AsyncScheduler asyncScheduler;
    private final RegionScheduler regionScheduler;

    public FoliaTaskScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.globalScheduler = plugin.getServer().getGlobalRegionScheduler();
        this.asyncScheduler = plugin.getServer().getAsyncScheduler();
        this.regionScheduler = plugin.getServer().getRegionScheduler();
    }

    @Override
    public void runGlobal(Runnable task) {
        globalScheduler.run(plugin, scheduledTask -> task.run());
    }

    @Override
    public void runGlobalLater(Runnable task, long delayTicks) {
        globalScheduler.runDelayed(plugin, scheduledTask -> task.run(), delayTicks);
    }

    @Override
    public void runGlobalTimer(Runnable task, long delayTicks, long periodTicks) {
        globalScheduler.runAtFixedRate(plugin, scheduledTask -> task.run(), delayTicks, periodTicks);
    }

    @Override
    public void runAsync(Runnable task) {
        asyncScheduler.runNow(plugin, scheduledTask -> task.run());
    }

    @Override
    public void runAsyncLater(Runnable task, long delay, TimeUnit unit) {
        asyncScheduler.runDelayed(plugin, scheduledTask -> task.run(), delay, unit);
    }

    @Override
    public void runAtEntity(Entity entity, Consumer<Entity> task) {
        entity.getScheduler().run(plugin, scheduledTask -> task.accept(entity), null);
    }

    @Override
    public void runAtEntityLater(Entity entity, Consumer<Entity> task, long delayTicks) {
        entity.getScheduler().runDelayed(plugin, scheduledTask -> task.accept(entity), null, delayTicks);
    }

    @Override
    public void runAtLocation(Location location, Runnable task) {
        regionScheduler.run(plugin, location, scheduledTask -> task.run());
    }

    @Override
    public void runAtWorld(World world, int chunkX, int chunkZ, Runnable task) {
        regionScheduler.run(plugin, world, chunkX, chunkZ, scheduledTask -> task.run());
    }

    @Override
    public void cancelAllTasks() {
        // Folia не имеет глобальной отмены, каждая задача отменяется отдельно
        plugin.getLogger().warning("Folia: global task cancellation not supported");
    }
}