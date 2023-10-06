package net.azisaba.tabbukkitbridge.util;

import net.azisaba.tabbukkitbridge.data.Skip;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.function.Function;
import java.util.function.Predicate;

public class Util {
    private static final DecimalFormat FORMATTER_COMMAS = new DecimalFormat("#,###.00");
    private static final DecimalFormat FORMATTER = new DecimalFormat("#.00");

    @Contract(pure = true)
    @NotNull
    public static <T> Predicate<T> isPluginEnabledPredicate(@NotNull String plugin) {
        return v -> Bukkit.getPluginManager().isPluginEnabled(plugin);
    }

    @Contract(value = "null, _ -> null", pure = true)
    public static <T, R> R nonNullMap(@Nullable T t, @NotNull Function<@NotNull T, R> function) {
        if (t == null) return null;
        return function.apply(t);
    }

    @NotNull
    public static <T, R> Function<T, R> nonNullMapper(@NotNull Function<@NotNull T, R> function) {
        return t -> {
            if (t == null) throw Skip.SKIP;
            return function.apply(t);
        };
    }

    @NotNull
    public static String format(double d) {
        return FORMATTER.format(d);
    }

    @NotNull
    public static String formatWithCommas(double d) {
        return FORMATTER_COMMAS.format(d);
    }
}
