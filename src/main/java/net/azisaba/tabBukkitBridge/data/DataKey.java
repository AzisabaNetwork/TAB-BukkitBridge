package net.azisaba.tabBukkitBridge.data;

import net.azisaba.tabBukkitBridge.BukkitBridge;
import net.azisaba.tabBukkitBridge.data.providers.EssentialsDataProvider;
import net.azisaba.tabBukkitBridge.data.providers.LuckPermsDataProvider;
import net.azisaba.tabBukkitBridge.data.providers.PlayerDataProvider;
import net.azisaba.tabBukkitBridge.data.providers.ServerDataProvider;
import net.azisaba.tabBukkitBridge.data.providers.VaultDataProvider;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class DataKey<T, R> {
    private static final List<DataKey<?, ?>> DATA_TYPES = new ArrayList<>();
    public static final DataKey<Void, Double> TPS = new DataKey<Void, Double>(p -> null, 20.0).placeholders("tps");
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
    public static final DataKey<Player, String> PREFIX = new DataKey<Player, String>("").placeholders("prefix");
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
    public static final DataKey<Player, Integer> PLAYER_COUNT = new DataKey<Player, Integer>(0).placeholders("player_count");
    public static final DataKey<Player, Integer> SAFE_PLAYER_COUNT = new DataKey<Player, Integer>(0).placeholders("safe_player_count");

    private final Function<Player, T> ptFunction;
    private final R defaultValue;
    private final List<DataProvider<T, R>> providers = new ArrayList<>();
    private final Set<String> placeholders = new HashSet<>();

    private DataKey(@NotNull Function<Player, T> ptFunction, @NotNull R defaultValue) {
        this.ptFunction = ptFunction;
        this.defaultValue = Objects.requireNonNull(defaultValue);
        DATA_TYPES.add(this);
    }

    /**
     * This constructor works only if <code>T</code> is instance of Player.
     */
    @SuppressWarnings("unchecked")
    private DataKey(@NotNull R defaultValue) {
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
        providers.add(dataProvider);
        for (DataKey<T, R> key : additionalKeys) {
            key.register(dataProvider);
        }
    }

    /**
     * Registers a new provider. this method is not thread-safe.
     * @param provider data provider
     */
    @SafeVarargs
    public final void register(@NotNull DataProvider<T, R> provider, @NotNull DataKey<T, R>@NotNull... additionalKeys) {
        providers.add(provider);
        for (DataKey<T, R> key : additionalKeys) {
            key.register(provider);
        }
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

    public static void registerAllProviders(@NotNull Plugin plugin) {
        // Priority: top -> bottom (top would be evaluated at first, and bottom would be evaluated at last)
        LuckPermsDataProvider.register();
        EssentialsDataProvider.register();
        VaultDataProvider.register();
        PlayerDataProvider.register();
        ServerDataProvider.register(plugin);
    }

    @NotNull
    public static String setPlaceholders(@NotNull Player player, @NotNull String s) {
        for (DataKey<?, ?> dataKey : DATA_TYPES) {
            s = dataKey.setPlaceholder(dataKey.ptFunction.apply(player), s);
        }
        return s;
    }
}
