package net.azisaba.tabbukkitbridge.data.providers;

import net.azisaba.tabbukkitbridge.data.DataKey;
import net.azisaba.tabbukkitbridge.util.Schedulers;
import net.azisaba.tabbukkitbridge.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CachedDataProvider {
    private static final long EXPIRE_MILLIS = 60 * 1000;
    private static final Map<UUID, CachedValue> prefixCache = new ConcurrentHashMap<>();
    private static final Map<UUID, CachedValue> suffixCache = new ConcurrentHashMap<>();

    public static void register(@NotNull Plugin plugin) {
        // remove expired entries (including players who left) every minute
        Schedulers.runGlobalTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            prefixCache.values().removeIf(v -> v.isExpired(now));
            suffixCache.values().removeIf(v -> v.isExpired(now));
        }, 20 * 60, 20 * 60);
        DataKey.PREFIX_CACHED.register(p -> true, Util.nonNullMapper(p -> getOrCompute(prefixCache, p, DataKey.PREFIX)));
        DataKey.SUFFIX_CACHED.register(p -> true, Util.nonNullMapper(p -> getOrCompute(suffixCache, p, DataKey.SUFFIX)));
    }

    @NotNull
    private static String getOrCompute(@NotNull Map<UUID, CachedValue> cache, @NotNull Player player, @NotNull DataKey<Player, String> key) {
        long now = System.currentTimeMillis();
        CachedValue cached = cache.get(player.getUniqueId());
        if (cached != null && !cached.isExpired(now)) return cached.value;
        // compute outside of the map to avoid holding the lock while calling providers
        String value = key.get(player);
        cache.put(player.getUniqueId(), new CachedValue(value, now + EXPIRE_MILLIS));
        return value;
    }

    private static final class CachedValue {
        private final String value;
        private final long expiresAt;

        private CachedValue(@NotNull String value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        private boolean isExpired(long now) {
            return now >= expiresAt;
        }
    }
}
