package net.azisaba.tabBukkitBridge;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.azisaba.tabBukkitBridge.data.DataKey;
import net.azisaba.tabBukkitBridge.tab.TheTAB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BukkitBridge extends JavaPlugin implements PluginMessageListener {
    public static BukkitBridge plugin;
    public static final PrintStream ERR_PRINT_STREAM = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {}
    }) {
        @Override
        public void println(@Nullable String x) {
            plugin.getLogger().warning(x);
        }

        @Override
        public void println(@Nullable Object x) {
            plugin.getLogger().warning(String.valueOf(x));
        }
    };

    public static final String CHANNEL_NAME = "tab:placeholders";

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        DataKey.registerAllProviders(this);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, CHANNEL_NAME, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, CHANNEL_NAME);
        registerEvents();
        try {
            TheTAB.enable();
        } catch (Throwable t) {
            // this is not an error, because bridge can still work.
            getLogger().info("Failed to enable TAB integration");
            t.printStackTrace();
        }
    }

    private void registerEvents() {
        registerEvent(PlayerJoinEvent.class, e -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Attribute");
            out.writeUTF("world");
            out.writeUTF(e.getPlayer().getWorld().getName());
            e.getPlayer().sendPluginMessage(this, CHANNEL_NAME, out.toByteArray());
        });
        try {
            registerEvent(PlayerChangedWorldEvent.class, e -> {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Attribute");
                out.writeUTF("world");
                out.writeUTF(e.getPlayer().getWorld().getName());
                e.getPlayer().sendPluginMessage(this, CHANNEL_NAME, out.toByteArray());
            });
        } catch (NoClassDefFoundError ignored) {}
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void registerEvent(@NotNull Class<T> clazz, Consumer<T> action) {
        Bukkit.getPluginManager().registerEvent(clazz, new Listener() {}, EventPriority.NORMAL, (l, e) -> action.accept((T) e), this);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals(CHANNEL_NAME)) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String type = in.readUTF(); // Type
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(type); // Type
        async(() -> {
            if (type.equals("Placeholder")) {
                String identifier = in.readUTF(); // Identifier
                out.writeUTF(identifier); // Identifier
                long start = System.nanoTime();
                out.writeUTF(DataKey.setPlaceholders(player, identifier));
                long time = System.nanoTime() - start;
                out.writeLong(time);
            }
            if (type.equals("Attribute")) {
                String attribute = in.readUTF(); // Attribute
                processAttribute(player, attribute, out);
            }
            if (type.equals("Group")) {
                processGroup(player, out);
            }
            return out.toByteArray();
        }).thenAccept(sync(data -> {
            if (data.length == 0) return;
            player.sendPluginMessage(this, channel, data);
        }));
    }

    private void processAttribute(@NotNull Player player, @NotNull String attribute, @NotNull ByteArrayDataOutput out) {
        out.writeUTF(attribute); // Attribute
        if (attribute.startsWith("hasPermission:")) {
            String perm = attribute.substring(14);
            out.writeUTF(Boolean.toString(player.hasPermission(perm)));
        }
        if (attribute.equalsIgnoreCase("invisible")) {
            out.writeUTF(DataKey.INVISIBLE.getAsString(player));
        }
        if (attribute.equalsIgnoreCase("disguised")) {
            out.writeUTF(DataKey.DISGUISED.getAsString(player));
        }
        if (attribute.equalsIgnoreCase("vanished")) {
            out.writeUTF(DataKey.VANISHED.getAsString(player));
        }
        if (attribute.equalsIgnoreCase("world")) {
            out.writeUTF(DataKey.WORLD.get(player));
        }
    }

    private void processGroup(@NotNull Player player, @NotNull ByteArrayDataOutput out) {
        out.writeUTF(DataKey.PRIMARY_GROUP.get(player)); // Primary group
    }

    @NotNull
    private <T> CompletableFuture<T> async(@NotNull Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, r -> Bukkit.getScheduler().runTaskAsynchronously(this, r));
    }

    @NotNull
    private <T> Consumer<T> sync(Consumer<T> action) {
        return t -> Bukkit.getScheduler().runTask(this, () -> action.accept(t));
    }
}
