package net.azisaba.tabbukkitbridge.data.providers;

import net.azisaba.tabbukkitbridge.data.DataProvider;
import net.azisaba.tabbukkitbridge.util.Schedulers;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Measures TPS of the main thread (Paper/Spigot) or the global region (Folia).
 */
public class ServerTPSDataProvider implements DataProvider<Void, Double>, Runnable {
    private static final int INTERVAL = 40;
    // read from other threads (player threads on Folia)
    private volatile double tps = 20;
    private long previousTime = System.currentTimeMillis();

    public ServerTPSDataProvider(@NotNull Plugin plugin) {
        Schedulers.runGlobalTimer(plugin, this, INTERVAL, INTERVAL);
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
