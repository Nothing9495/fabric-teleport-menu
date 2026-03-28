package io.wispforest.owo.mixin.offline;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.class_167;
import net.minecraft.class_8782;

@Mixin(class_167.class)
public interface AdvancementProgressAccessor {
    @Accessor
    class_8782 getRequirements();

    @Accessor
    void setRequirements(class_8782 requirements);
}