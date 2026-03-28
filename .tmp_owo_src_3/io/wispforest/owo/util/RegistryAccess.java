package io.wispforest.owo.util;

import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_6880;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval = true)
public final class RegistryAccess {

    private RegistryAccess() {}

    /**
     * @deprecated Use {@link class_2378#method_55841(class_2960)}
     */
    @Nullable
    public static <T> class_6880<T> getEntry(class_2378<T> registry, class_2960 id) {
        return registry.method_55841(id).orElse(null);
    }

    /**
     * @deprecated Use {@link class_2378#method_47983(T)}
     */
    @Nullable
    public static <T> class_6880<T> getEntry(class_2378<T> registry, T value) {
        return registry.method_47983(value);
    }
}
