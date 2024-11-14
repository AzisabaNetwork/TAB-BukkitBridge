package net.azisaba.tabbukkitbridge;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.clip.placeholderapi.PlaceholderAPI;
import net.azisaba.tabbukkitbridge.data.DataKey;
import net.azisaba.tabbukkitbridge.tab.TheTAB;
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
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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

    public static final String CHANNEL_NAME = "tab:bridge-6";

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
        Bukkit.getScheduler().runTaskTimer(this, () -> Bukkit.getOnlinePlayers().forEach(BukkitBridge::updatePlaceholders), 20, 20);
        TheTAB.enable();
    }

    private void registerEvents() {
        registerEvent(PlayerJoinEvent.class, e -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            writePlayerJoinResponse(out, e.getPlayer());
            e.getPlayer().sendPluginMessage(this, CHANNEL_NAME, out.toByteArray());
        });
        try {
            registerEvent(PlayerChangedWorldEvent.class, e -> {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                writePlayerJoinResponse(out, e.getPlayer());
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
        async(() -> {
            if (type.equals("PlayerJoin")) {
                writePlayerJoinResponse(out, player);
            }
            if (type.equals("Permission")) {
                String permission = in.readUTF();
                out.writeByte(2);
                out.writeUTF(permission);
                out.writeBoolean(player.hasPermission(permission));
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

    private static void processGroup(@NotNull Player player, @NotNull ByteArrayDataOutput out) {
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

    private static final Map<Map.Entry<UUID, String>, String> STORED_PLACEHOLDERS = new ConcurrentHashMap<>();

    private static void updatePlaceholders(@NotNull Player player) {
        getPlaceholders(player).forEach((key, value) -> {
            Map.Entry<UUID, String> entry = new AbstractMap.SimpleEntry<>(player.getUniqueId(), key);
            String stored = STORED_PLACEHOLDERS.get(entry);
            if (stored == null || !stored.equals(value)) {
                STORED_PLACEHOLDERS.put(entry, value);
                ByteArrayDataOutput out2 = ByteStreams.newDataOutput();
                writeRegisterPlaceholder(out2, key);
                player.sendPluginMessage(plugin, CHANNEL_NAME, out2.toByteArray());
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                writeUpdatePlaceholder(out, key, value);
                player.sendPluginMessage(plugin, CHANNEL_NAME, out.toByteArray());
            }
        });
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        writeVanished(out, player);
        player.sendPluginMessage(plugin, CHANNEL_NAME, out.toByteArray());
    }

    private static Map<String, String> getPlaceholders(@NotNull Player player) {
        Map<String, String> placeholders = new HashMap<>();
        for (DataKey<?, ?> dataKey : DataKey.values()) {
            String value = String.valueOf(dataKey.getByPlayer(player));
            for (String placeholder : dataKey.getPlaceholders()) {
                placeholders.put(placeholder, value);
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholders.putAll(getPlaceholderAPI(player));
        }
        return placeholders;
    }

    private static Map<String, String> getPlaceholderAPI(@NotNull Player player) {
        Map<String, String> placeholders = new HashMap<>();
        for (String identifier : PlaceholderAPI.getRegisteredIdentifiers()) {
            String value = PlaceholderAPI.setPlaceholders(player, "%" + identifier + "%");
            placeholders.put(identifier, value);
        }
        return placeholders;
    }

    private static void writeVanished(@NotNull ByteArrayDataOutput out, @NotNull Player player) {
        out.writeByte(7); // packet id
        out.writeBoolean(DataKey.VANISHED.getByPlayer(player));
    }

    private static void writeUpdatePlaceholder(@NotNull ByteArrayDataOutput out, @NotNull String identifier, @NotNull String value) {
        out.writeByte(8); // packet id
        out.writeUTF("%" + identifier + "%");
        out.writeUTF(value);
    }

    private static void writePlayerJoinResponse(@NotNull ByteArrayDataOutput out, Player player) {
        out.writeByte(9); // packet id
        out.writeUTF(player.getWorld().getName());
        //processGroup(player, out);
        Map<String, String> placeholders = getPlaceholders(player);
        out.writeInt(placeholders.size());
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeUTF(entry.getValue());
        }
        out.writeInt(player.getGameMode().getValue());
        STORED_PLACEHOLDERS.keySet().removeIf(e -> e.getKey().equals(player.getUniqueId()));
    }

    private static void writeRegisterPlaceholder(@NotNull ByteArrayDataOutput out, @NotNull String identifier) {
        out.writeByte(10); // packet id
        out.writeUTF("%" + identifier + "%");
    }
}
