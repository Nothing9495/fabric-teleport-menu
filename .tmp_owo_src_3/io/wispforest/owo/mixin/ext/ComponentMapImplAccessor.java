package io.wispforest.owo.mixin.ext;

import net.minecraft.class_9323;
import net.minecraft.class_9335;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_9335.class)
public interface ComponentMapImplAccessor {
    @Accessor("baseComponents")
    class_9323 owo$getBaseComponents();

    @Accessor("baseComponents")
    @Mutable
    void owo$setBaseComponents(class_9323 baseComponents);
}
