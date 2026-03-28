package io.wispforest.owo.mixin.ui.access;

import net.minecraft.class_342;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_342.class)
public interface TextFieldWidgetAccessor {
    @Accessor("drawsBackground")
    boolean owo$drawsBackground();
}
