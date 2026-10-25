package net.azisaba.tabbukkitbridge.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Scheduler wrapper that works on both Folia and Paper/Spigot.
 * <ul>
 *     <li>Folia: global region scheduler, entity scheduler and async scheduler</li>
 *     <li>Paper/Spigot: {@link org.bukkit.scheduler.BukkitScheduler}</li>
 * </ul>
 */
public final class Schedulers {
    private static final boolean FOLIA = detectFolia();

    private Schedulers() {}

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    /**
     * Runs the task repeatedly on the global region (Folia) or the main thread (Paper/Spigot).
     * Do not touch world or entity state from this task on Folia.
     */
    public static void runGlobalTimer(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks, long periodTicks) {
        if (FOLIA) {
            // Folia requires delay and period to be at least 1
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), Math.max(1, delayTicks), Math.max(1, periodTicks));
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        }
    }

    /**
     * Runs the task on the thread that owns the entity. On Folia, the task is dropped if the entity is removed
     * (e.g. the player left) before it runs. On Paper/Spigot, the task runs immediately if called from the main thread.
     */
    public static void runForEntity(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Runnable task) {
        if (FOLIA) {
            entity.getScheduler().run(plugin, t -> task.run(), null);
        } else if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void cancelAll(@NotNull Plugin plugin) {
        if (FOLIA) {
            Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
            Bukkit.getAsyncScheduler().cancelTasks(plugin);
        } else {
            Bukkit.getScheduler().cancelTasks(plugin);
        }
    }
}
