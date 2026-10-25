package net.azisaba.tabbukkitbridge.data.providers;

import me.clip.placeholderapi.PlaceholderAPI;
import net.azisaba.tabbukkitbridge.BukkitBridge;
import net.azisaba.tabbukkitbridge.data.DataKey;
import net.azisaba.tabbukkitbridge.data.Skip;
import net.azisaba.tabbukkitbridge.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provider for EssentialsC (net.godlycow.org.essc), which is a different plugin from EssentialsX.
 * <p>
 * EssentialsC does not expose AFK/vanish in its public API ({@code EssentialsCAPI}), so this calls
 * {@code EssentialsC#getVanishManager().isVanished(Player)} and {@code EssentialsC#getAfkManager().isAFK(Player)}
 * via reflection. If that fails, it falls back to the PlaceholderAPI expansion ({@code %essc_..._boolean%}).
 */
public class EssentialsCDataProvider {
    private static final String PLUGIN_NAME = "EssentialsC";
    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    public static void register() {
        DataKey.ESSENTIALS_VANISHED.register(Util.isPluginEnabledPredicate(PLUGIN_NAME), Util.nonNullMapper(p ->
                query(p, "getVanishManager", "isVanished", "essc_vanish_boolean")), DataKey.VANISHED);
        DataKey.ESSENTIALS_AFK.register(Util.isPluginEnabledPredicate(PLUGIN_NAME), Util.nonNullMapper(p ->
                query(p, "getAfkManager", "isAFK", "essc_afk_boolean")), DataKey.AFK);
    }

    private static boolean query(@NotNull Player player, @NotNull String managerGetter, @NotNull String checker, @NotNull String papiPlaceholder) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        if (plugin == null) throw Skip.SKIP;
        try {
            Object manager = getMethod(plugin.getClass(), managerGetter).invoke(plugin);
            // manager is null if the module is disabled in EssentialsC's config
            if (manager != null) {
                return (Boolean) getMethod(manager.getClass(), checker, Player.class).invoke(manager, player);
            }
        } catch (ReflectiveOperationException | ClassCastException e) {
            if (WARNED.add(managerGetter)) {
                BukkitBridge.plugin.getLogger().warning("Failed to call EssentialsC#" + managerGetter + "()." + checker
                        + "(Player), falling back to PlaceholderAPI (%" + papiPlaceholder + "%)");
                e.printStackTrace(BukkitBridge.ERR_PRINT_STREAM);
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            String placeholder = "%" + papiPlaceholder + "%";
            String value = PlaceholderAPI.setPlaceholders(player, placeholder);
            if (!value.equals(placeholder)) return Boolean.parseBoolean(value);
        }
        throw Skip.SKIP;
    }

    @NotNull
    private static Method getMethod(@NotNull Class<?> clazz, @NotNull String name, @NotNull Class<?>... parameterTypes) throws NoSuchMethodException {
        String key = clazz.getName() + "#" + name;
        Method method = METHOD_CACHE.get(key);
        if (method == null) {
            method = clazz.getMethod(name, parameterTypes);
            METHOD_CACHE.put(key, method);
        }
        return method;
    }
}
