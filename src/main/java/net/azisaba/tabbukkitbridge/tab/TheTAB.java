package net.azisaba.tabbukkitbridge.tab;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import net.azisaba.tabbukkitbridge.BukkitBridge;
import net.azisaba.tabbukkitbridge.data.DataKey;
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
        try {
            Class.forName("me.neznamy.tab.shared.placeholders.conditions.Condition").getMethod("finishSetups").invoke(null);
        } catch (ReflectiveOperationException e) {
            BukkitBridge.plugin.getLogger().warning("Failed to execute Condition#finishSetups");
            e.printStackTrace();
        }
        if (enabled) return;
        TabAPI.getInstance().getEventBus().register(TabLoadEvent.class, e -> enable());
        enabled = true;
    }
}
