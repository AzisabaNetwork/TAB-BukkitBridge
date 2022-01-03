package net.azisaba.tabBukkitBridge.data.providers;

import net.azisaba.tabBukkitBridge.data.DataProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerTPSDataProvider implements DataProvider<Void, Double>, Runnable {
    private static final int INTERVAL = 40;
    private double tps = 20;
    private long previousTime;

    public ServerTPSDataProvider(@NotNull Plugin plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, INTERVAL, INTERVAL);
    }

    @Override
    public boolean test(@Nullable Void unused) {
        return true;
    }

    @NotNull
    @Override
    public Double apply(@Nullable Void unused) {
        return tps;
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        long elapsed = time - previousTime;
        double elapsedSeconds = (double) elapsed / 1000;
        tps = INTERVAL / elapsedSeconds;
        previousTime = time;
    }
}
