package net.azisaba.tabbukkitbridge.event;

import net.azisaba.tabbukkitbridge.data.DataKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DataKeyRegisterEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final State state;
    private final DataKey<?, ?> dataKey;

    public DataKeyRegisterEvent(@NotNull State state, @NotNull DataKey<?, ?> dataKey) {
        this.state = state;
        this.dataKey = Objects.requireNonNull(dataKey);
    }

    @NotNull
    public State getState() {
        return state;
    }

    @NotNull
    public DataKey<?, ?> getDataKey() {
        return dataKey;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public enum State {
        PRE,
        POST,
    }
}
