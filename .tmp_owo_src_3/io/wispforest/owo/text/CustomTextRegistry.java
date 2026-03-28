package io.wispforest.owo.text;

import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_7417;

public final class CustomTextRegistry {

    private static final Map<String, Entry<?>> TYPES = new HashMap<>();

    private CustomTextRegistry() {}

    public static void register(class_7417.class_8823<?> type, String triggerField) {
        TYPES.put(type.comp_1984(), new Entry<>(triggerField, type));
    }

    @ApiStatus.Internal
    public static Map<String, Entry<?>> typesMap() {
        return TYPES;
    }

    public record Entry<C extends class_7417>(String triggerField, class_7417.class_8823<C> type) {}
}
