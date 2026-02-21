package ru.guildforge.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PaperTaskScheduler implements PlatformScheduler {
    private final Plugin plugin;

    public PaperTaskScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runGlobal(Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void runGlobalLater(Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    @Override
    public void runGlobalTimer(Runnable task, long delayTicks, long periodTicks) {
        Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    @Override
    public void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void runAsyncLater(Runnable task, long delay, TimeUnit unit) {
        long ticks = unit.toSeconds(delay) * 20;
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, ticks);
    }

    @Override
    public void runAtEntity(Entity entity, Consumer<Entity> task) {
        Bukkit.getScheduler().runTask(plugin, () -> task.accept(entity));
    }

    @Override
    public void runAtEntityLater(Entity entity, Consumer<Entity> task, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> task.accept(entity), delayTicks);
    }

    @Override
    public void runAtLocation(Location location, Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void runAtWorld(World world, int chunkX, int chunkZ, Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void cancelAllTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}