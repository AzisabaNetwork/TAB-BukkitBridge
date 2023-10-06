package net.azisaba.tabbukkitbridge.data;

public class Skip extends RuntimeException {
    private Skip() {}

    public static final Skip SKIP = new Skip();
}
