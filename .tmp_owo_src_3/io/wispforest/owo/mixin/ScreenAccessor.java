package io.wispforest.owo.mixin;

import net.minecraft.class_437;
import net.minecraft.class_751;
import net.minecraft.class_766;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_437.class)
public interface ScreenAccessor {
    @Accessor("PANORAMA_RENDERER")
    static class_751 owo$PANORAMA_RENDERER() {
        throw new UnsupportedOperationException();
    }

    @Accessor("ROTATING_PANORAMA_RENDERER")
    static class_766 owo$ROTATING_PANORAMA_RENDERER() {
        throw new UnsupportedOperationException();
    }
}
