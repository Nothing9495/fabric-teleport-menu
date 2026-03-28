package io.wispforest.owo.mixin.offline;

import io.wispforest.owo.offline.DataSavedEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;
import net.minecraft.class_2985;
import net.minecraft.class_3222;

@Mixin(class_2985.class)
public class PlayerAdvancementTrackerMixin {
    @Shadow
    private class_3222 owner;

    @SuppressWarnings("unchecked")
    @ModifyArg(method = "save", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;encodeStart(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;", remap = false), index = 1)
    private Object onAdvancementsSaved(Object map) {
        DataSavedEvents.ADVANCEMENTS.invoker().onSaved(owner.method_5667(), ((ProgressMapAccessor) map).getMap());
        return map;
    }
}
