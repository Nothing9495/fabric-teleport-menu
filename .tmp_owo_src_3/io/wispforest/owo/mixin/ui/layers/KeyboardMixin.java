package io.wispforest.owo.mixin.ui.layers;

import io.wispforest.owo.ui.layers.Layer.Instance;
import io.wispforest.owo.ui.layers.Layers;
import net.minecraft.class_309;
import net.minecraft.class_310;
import net.minecraft.class_364;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_309.class)
public class KeyboardMixin {

    @Inject(method = "method_1473", at = @At("HEAD"), cancellable = true)
    private static void captureScreenCharTyped(class_364 element, char character, int modifiers, CallbackInfo ci) {
        boolean handled = false;
        for (var instance : Layers.getInstances(class_310.method_1551().field_1755)) {
            handled = instance.adapter.method_25400(character, modifiers);
            if (handled) break;
        }

        if (handled) ci.cancel();
    }

    @Inject(method = "method_1458", at = @At("HEAD"), cancellable = true)
    private static void captureScreenCharTyped(class_364 element, int character, int modifiers, CallbackInfo ci) {
        boolean handled = false;
        for (var instance : Layers.getInstances(class_310.method_1551().field_1755)) {
            handled = instance.adapter.method_25400((char) character, modifiers);
            if (handled) break;
        }

        if (handled) ci.cancel();
    }

}
