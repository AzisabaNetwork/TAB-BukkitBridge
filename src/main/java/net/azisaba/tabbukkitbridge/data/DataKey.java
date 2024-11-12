package net.azisaba.tabbukkitbridge.data;

import net.azisaba.tabbukkitbridge.BukkitBridge;
import net.azisaba.tabbukkitbridge.data.providers.CachedDataProvider;
import net.azisaba.tabbukkitbridge.data.providers.EssentialsDataProvider;
import net.azisaba.tabbukkitbridge.data.providers.LuckPermsDataProvider;
import net.azisaba.tabbukkitbridge.data.providers.PlayerDataProvider;
import net.azisaba.tabbukkitbridge.data.providers.ServerDataProvider;
import net.azisaba.tabbukkitbridge.data.providers.VaultDataProvider;
import net.azisaba.tabbukkitbridge.event.DataKeyRegisterEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class DataKey<T, R> {
    private static final List<DataKey<?, ?>> DATA_TYPES = new ArrayList<>();
    public static final DataKey<Void, Double> TPS = new DataKey<Void, Double>(p -> null, 20.0).placeholders("tps", "tps_double");
    public static final DataKey<Double, String> TPS_FORMATTED = new DataKey<>(p -> TPS.get(null), "20.00").placeholders("tps_formatted");
    public static final DataKey<Player, String> WORLD = new DataKey<Player, String>("world").placeholders("world", "player_world");
    public static final DataKey<Player, Boolean> ESSENTIALS_VANISHED = new DataKey<Player, Boolean>(false).placeholders("essentials_vanished");
    public static final DataKey<Player, Boolean> VANISHED = new DataKey<Player, Boolean>(false).placeholders("vanished");
    public static final DataKey<Player, Boolean> ESSENTIALS_AFK = new DataKey<Player, Boolean>(false).placeholders("essentials_afk");
    public static final DataKey<Player, Boolean> AFK = new DataKey<Player, Boolean>(false).placeholders("afk");
    public static final DataKey<Player, Boolean> DISGUISED = new DataKey<Player, Boolean>(false).placeholders("disguised");
    public static final DataKey<Player, Boolean> INVISIBLE = new DataKey<Player, Boolean>(false).placeholders("invisible");
    public static final DataKey<Player, String> LUCKPERMS_PRIMARY_GROUP = new DataKey<Player, String>("default").placeholders("luckperms_primary_group");
    public static final DataKey<Player, String> PRIMARY_GROUP = new DataKey<Player, String>("default").placeholders("primary_group");
    public static final DataKey<Player, Integer> PRIMARY_GROUP_WEIGHT = new DataKey<Player, Integer>(0).placeholders("primary_group_weight");
    public static final DataKey<Player, String> VAULT_PREFIX = new DataKey<Player, String>("").placeholders("vault_prefix");
    public static final DataKey<Player, String> PREFIX_CACHED = new DataKey<Player, String>("").placeholders("prefix_cached");
    public static final DataKey<Player, String> PREFIX = new DataKey<Player, String>("").placeholders("prefix");
    public static final DataKey<Player, String> VAULT_SUFFIX = new DataKey<Player, String>("").placeholders("vault_suffix");
    public static final DataKey<Player, String> SUFFIX_CACHED = new DataKey<Player, String>("").placeholders("suffix_cached");
    public static final DataKey<Player, String> SUFFIX = new DataKey<Player, String>("").placeholders("suffix");
    public static final DataKey<Player, Double> MONEY = new DataKey<Player, Double>(0.0).placeholders("money");
    public static final DataKey<Player, String> MONEY_FORMATTED = new DataKey<Player, String>("0.00").placeholders("money_formatted");
    public static final DataKey<Player, String> DISPLAY_NAME = new DataKey<Player, String>("").placeholders("display_name");
    public static final DataKey<Player, String> NAME = new DataKey<Player, String>("").placeholders("name");
    public static final DataKey<Player, Double> POSITION_X = new DataKey<Player, Double>(0.0).placeholders("position_x");
    public static final DataKey<Player, Double> POSITION_Y = new DataKey<Player, Double>(0.0).placeholders("position_y");
    public static final DataKey<Player, Double> POSITION_Z = new DataKey<Player, Double>(0.0).placeholders("position_z");
    public static final DataKey<Player, String> GAMEMODE = new DataKey<Player, String>("survival").placeholders("gamemode");
    public static final DataKey<Player, String> TEAM_NAME = new DataKey<Player, String>("").placeholders("team_name");
    public static final DataKey<Player, String> TEAM_DISPLAY_NAME = new DataKey<Player, String>("").placeholders("team_display_name");
    public static final DataKey<Player, String> TEAM_SUFFIX = new DataKey<Player, String>("").placeholders("team_suffix");
    public static final DataKey<Player, String> TEAM_PREFIX = new DataKey<Player, String>("").placeholders("team_prefix");
    public static final DataKey<Player, String> TEAM_COLOR_NAME = new DataKey<Player, String>("").placeholders("team_color_name");
    public static final DataKey<Player, String> TEAM_COLOR = new DataKey<Player, String>("").placeholders("team_color");
    public static final DataKey<Player, String> TEAM_NAMETAG_VISIBILITY = new DataKey<Player, String>("").placeholders("team_nametag_visibility");
    public static final DataKey<Player, Integer> PLAYER_COUNT = new DataKey<Player, Integer>(0).placeholders("player_count");
    public static final DataKey<Player, Integer> SAFE_PLAYER_COUNT = new DataKey<Player, Integer>(0).placeholders("safe_player_count");

    private final Function<Player, T> ptFunction;
    private final R defaultValue;
    private final List<DataProvider<T, R>> providers = new ArrayList<>();
    private final Set<String> placeholders = new HashSet<>();

    public DataKey(@NotNull Function<Player, T> ptFunction, @NotNull R defaultValue) {
        this.ptFunction = ptFunction;
        this.defaultValue = Objects.requireNonNull(defaultValue);
        DATA_TYPES.add(this);
    }

    /**
     * This constructor works only if <code>T</code> is instance of Player.
     * @param defaultValue default value
     */
    @SuppressWarnings("unchecked")
    public DataKey(@NotNull R defaultValue) {
        this(p -> (T) p, defaultValue);
    }

    private DataKey<T, R> placeholders(@NotNull String@NotNull... placeholders) {
        this.placeholders.addAll(Arrays.asList(placeholders));
        return this;
    }

    /**
     * Registers a new provider. This method is not thread-safe.
     * @param condition condition to trigger provider
     * @param provider data provider
     * @param additionalKeys keys
     */
    @SafeVarargs
    public final void register(@NotNull Predicate<@Nullable T> condition, @NotNull Function<@Nullable T, @Nullable R> provider, @NotNull DataKey<T, R>@NotNull... additionalKeys) {
        DataProvider<T, R> dataProvider = new DataProvider<T, R>() {
            @Override
            public boolean test(T t) {
                return condition.test(t);
            }

            @Override
            public R apply(T t) {
                return provider.apply(t);
            }
        };
        register(dataProvider, additionalKeys);
    }

    /**
     * Registers a new provider. this method is not thread-safe.
     * @param provider data provider
     * @param additionalKeys keys
     */
    @SafeVarargs
    public final void register(@NotNull DataProvider<T, R> provider, @NotNull DataKey<T, R>@NotNull... additionalKeys) {
        registerProvider(provider);
        for (DataKey<T, R> key : additionalKeys) {
            key.registerProvider(provider);
        }
    }

    protected void registerProvider(@NotNull DataProvider<T, R> provider) {
        Bukkit.getPluginManager().callEvent(new DataKeyRegisterEvent(DataKeyRegisterEvent.State.PRE, this));
        providers.add(provider);
        Bukkit.getPluginManager().callEvent(new DataKeyRegisterEvent(DataKeyRegisterEvent.State.POST, this));
    }

    @NotNull
    public R get(@Nullable T t) {
        for (DataProvider<T, R> provider : providers) {
            if (provider.test(t)) {
                try {
                    R value = provider.apply(t);
                    if (value != null) return value;
                    return defaultValue;
                } catch (Skip ignored) {
                } catch (Exception e) {
                    BukkitBridge.plugin.getLogger().warning("Provider threw exception");
                    e.printStackTrace(BukkitBridge.ERR_PRINT_STREAM);
                }
            }
        }
        return defaultValue;
    }

    @NotNull
    public String getAsString(@Nullable T t) {
        return String.valueOf(get(t));
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public String setPlaceholder(@Nullable Object o, @NotNull String s) throws ClassCastException {
        for (String placeholder : placeholders) {
            s = s.replace("%" + placeholder + "%", String.valueOf(get((T) o)));
        }
        return s;
    }

    @NotNull
    public Set<String> getPlaceholders() {
        return placeholders;
    }

    public static void registerAllProviders(@NotNull Plugin plugin) {
        // Priority: top -> bottom (top would be evaluated at first, and bottom would be evaluated at last)
        LuckPermsDataProvider.register();
        EssentialsDataProvider.register();
        VaultDataProvider.register();
        PlayerDataProvider.register();
        ServerDataProvider.register(plugin);
        CachedDataProvider.register(plugin);
    }

    @NotNull
    public static String setPlaceholders(@NotNull Player player, @NotNull String s) {
        for (DataKey<?, ?> dataKey : DATA_TYPES) {
            s = dataKey.setPlaceholder(dataKey.ptFunction.apply(player), s);
        }
        return s;
    }

    public T playerToT(Player player) {
        return ptFunction.apply(player);
    }

    @NotNull
    public R getByPlayer(Player player) {
        return get(ptFunction.apply(player));
    }

    @NotNull
    public static List<DataKey<?, ?>> values() {
        return Collections.unmodifiableList(DATA_TYPES);
    }
}
