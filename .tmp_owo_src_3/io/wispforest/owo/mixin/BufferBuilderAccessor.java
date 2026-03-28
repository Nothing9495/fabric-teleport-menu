package io.wispforest.owo.mixin;

import net.minecraft.class_287;
import net.minecraft.class_293;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_287.class)
public interface BufferBuilderAccessor {
    @Accessor("building") boolean isBuilding();

    @Accessor("format") class_293 getFormat();

    @Accessor("drawMode") class_293.class_5596 getDrawMode();
}
