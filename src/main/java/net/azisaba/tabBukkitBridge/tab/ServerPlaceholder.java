package net.azisaba.tabBukkitBridge.tab;

import java.util.function.Supplier;

public class ServerPlaceholder extends AbstractPlaceholder implements me.neznamy.tab.api.placeholder.ServerPlaceholder {
    private final Supplier<Object> supplier;

    public ServerPlaceholder(String identifier, int refresh, Supplier<Object> supplier) {
        super(identifier, refresh);
        this.supplier = supplier;
    }

    @Override
    public void updateValue(Object value) {
    }

    @Override
    public Object request() {
        return supplier.get();
    }
}
