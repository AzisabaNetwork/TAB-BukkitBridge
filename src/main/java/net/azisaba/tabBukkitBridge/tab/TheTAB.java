package net.azisaba.tabBukkitBridge.tab;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import net.azisaba.tabBukkitBridge.data.DataKey;
import org.bukkit.entity.Player;

public class TheTAB {
    private static boolean enabled = false;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void enable() {
        for (DataKey dataKey : DataKey.values()) {
            for (Object identifier : dataKey.getPlaceholders()) {
                TabAPI.getInstance().getPlaceholderManager().registerPlayerPlaceholder(
                        "%" + identifier + "%",
                        50 * 20,
                        p -> dataKey.get(dataKey.playerToT((Player) p.getPlayer()))
                );
            }
        }
        if (enabled) return;
        TabAPI.getInstance().getEventBus().register(TabLoadEvent.class, e -> enable());
        enabled = true;
    }
}
