package io.wispforest.owo.mixin.ui.layers;

import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_465.class)
public interface HandledScreenAccessor {

    @Accessor("x")
    int owo$getRootX();

    @Accessor("y")
    int owo$getRootY();

}
