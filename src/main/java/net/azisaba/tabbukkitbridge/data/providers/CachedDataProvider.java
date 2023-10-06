package net.azisaba.tabbukkitbridge.data.providers;

import net.azisaba.tabbukkitbridge.data.DataKey;
import net.azisaba.tabbukkitbridge.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CachedDataProvider {
    private static Map<UUID, String> prefixCache;
    private static Map<UUID, String> suffixCache;

    public static void register(@NotNull Plugin plugin) {
        prefixCache = new HashMap<UUID, String>() {
            @Override
            public String put(UUID key, String value) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> prefixCache.remove(key), 20 * 60);
                return super.put(key, value);
            }
        };
        suffixCache = new HashMap<UUID, String>() {
            @Override
            public String put(UUID key, String value) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> prefixCache.remove(key), 20 * 60);
                return super.put(key, value);
            }
        };
        DataKey.PREFIX_CACHED.register(p -> true, Util.nonNullMapper(p -> prefixCache.computeIfAbsent(p.getUniqueId(), u -> DataKey.PREFIX.get(p))));
        DataKey.SUFFIX_CACHED.register(p -> true, Util.nonNullMapper(p -> suffixCache.computeIfAbsent(p.getUniqueId(), u -> DataKey.SUFFIX.get(p))));
    }
}
