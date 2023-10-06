package net.azisaba.tabbukkitbridge.tab;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;

import java.util.function.Function;

public class PlayerPlaceholder extends AbstractPlaceholder implements me.neznamy.tab.api.placeholder.PlayerPlaceholder {
    private final Function<TabPlayer, Object> function;

    public PlayerPlaceholder(String identifier, int refresh, Function<TabPlayer, Object> function) {
        super(identifier, refresh);
        this.function = function;
    }

    @Override
    public void updateValue(TabPlayer player, Object value) {
    }

    @Override
    public Object request(TabPlayer p) {
        try {
            return function.apply(p);
        } catch (Exception e) {
            TabAPI.getInstance().logError("Player placeholder " + identifier + " generated an error when setting for player " + p.getName(), e);
            return "ERROR";
        }
    }
}
