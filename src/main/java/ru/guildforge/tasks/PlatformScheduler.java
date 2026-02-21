package ru.guildforge.tasks;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface PlatformScheduler {

    void runGlobal(Runnable task);

    void runGlobalLater(Runnable task, long delayTicks);

    void runGlobalTimer(Runnable task, long delayTicks, long periodTicks);

    void runAsync(Runnable task);

    void runAsyncLater(Runnable task, long delay, TimeUnit unit);

    void runAtEntity(Entity entity, Consumer<Entity> task);

    void runAtEntityLater(Entity entity, Consumer<Entity> task, long delayTicks);

    void runAtLocation(Location location, Runnable task);

    void runAtWorld(World world, int chunkX, int chunkZ, Runnable task);

    void cancelAllTasks();

    static PlatformScheduler create(Plugin plugin) {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            plugin.getLogger().info("✅ Folia detected! Using threaded scheduler.");
            return new FoliaTaskScheduler(plugin);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("✅ Paper/Bukkit detected! Using standard scheduler.");
            return new PaperTaskScheduler(plugin);
        }
    }
}