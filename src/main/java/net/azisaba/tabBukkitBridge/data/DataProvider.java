package net.azisaba.tabBukkitBridge.data;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

public interface DataProvider<T, R> extends Predicate<T>, Function<T, R> {
    @Override
    boolean test(@Nullable T t);

    @Nullable
    @Override
    R apply(@Nullable T t);
}
