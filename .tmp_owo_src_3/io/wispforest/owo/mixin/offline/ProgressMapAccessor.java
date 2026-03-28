package io.wispforest.owo.mixin.offline;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.class_167;
import net.minecraft.class_2960;

@Mixin(targets = "net/minecraft/advancement/PlayerAdvancementTracker$ProgressMap")
public interface ProgressMapAccessor {
    @Accessor
    Map<class_2960, class_167> getMap();
}
