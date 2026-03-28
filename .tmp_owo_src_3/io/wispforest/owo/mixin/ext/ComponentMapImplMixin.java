package io.wispforest.owo.mixin.ext;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.owo.ext.DerivedComponentMap;
import net.minecraft.class_9323;
import net.minecraft.class_9335;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_9335.class)
public class ComponentMapImplMixin {
    @ModifyExpressionValue(method = "copy", at = @At(value = "FIELD", target = "Lnet/minecraft/component/ComponentMapImpl;baseComponents:Lnet/minecraft/component/ComponentMap;"))
    private class_9323 reWrapDerived(class_9323 original) {
        return DerivedComponentMap.reWrapIfNeeded(original);
    }
}
