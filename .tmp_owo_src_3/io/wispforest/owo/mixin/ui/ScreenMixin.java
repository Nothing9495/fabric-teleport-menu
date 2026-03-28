package io.wispforest.owo.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.class_437;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(class_437.class)
public class ScreenMixin {

    @ModifyExpressionValue(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;shouldCloseOnEsc()Z", ordinal = 0))
    private boolean dontCloseOwoScreens(boolean original) {
        //noinspection ConstantValue
        if ((Object) this instanceof BaseOwoScreen<?> || (Object) this instanceof BaseOwoHandledScreen<?, ?>) {
            return false;
        }

        return original;
    }



    @ModifyArg(method = "method_61039", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"), index = 0)
    private @Nullable class_437 injectProperLinkSource(@Nullable class_437 screen) {
        if ((Object) this != OwoUIDrawContext.utilityScreen()) return screen;
        return OwoUIDrawContext.utilityScreen().getAndClearLinkSource();
    }
}
