package net.azisaba.tabBukkitBridge.data.providers;

import net.azisaba.tabBukkitBridge.data.DataKey;
import net.azisaba.tabBukkitBridge.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ServerDataProvider {
    public static void register(Plugin plugin) {
        DataKey.TPS.register(new ServerTPSDataProvider(plugin));
        DataKey.TPS_FORMATTED.register(d -> true, d -> {
            if (d == null) return "20.00";
            return Util.format(d);
        });
        DataKey.PLAYER_COUNT.register(p -> true, p -> Bukkit.getOnlinePlayers().size());
        DataKey.SAFE_PLAYER_COUNT.register(p -> true, p -> (int) Bukkit.getOnlinePlayers().stream().filter(pl -> !DataKey.VANISHED.get(pl)).count());
    }
}
