package net.azisaba.tabBukkitBridge.data.providers;

import net.azisaba.tabBukkitBridge.data.DataKey;
import net.azisaba.tabBukkitBridge.data.Skip;
import net.azisaba.tabBukkitBridge.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EssentialsDataProvider {
    public static void register() {
        DataKey.VANISHED.register(Util.isPluginEnabledPredicate("Essentials"), Util.nonNullMapper(p -> {
            try {
                Object essentials = Bukkit.getPluginManager().getPlugin("Essentials");
                Object user = essentials.getClass().getMethod("getUser", Player.class).invoke(essentials, p);
                return (boolean) user.getClass().getMethod("isVanished").invoke(user);
            } catch (ReflectiveOperationException e) {
                throw Skip.SKIP;
            }
        }));
    }
}
