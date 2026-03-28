package io.wispforest.owo.mixin;

import io.wispforest.owo.util.pond.OwoItemExtensions;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_3222;
import net.minecraft.class_3225;
import net.minecraft.class_3468;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_3225.class)
public class ServerPlayerInteractionManagerMixin {

    @Inject(method = "interactItem", at = @At("RETURN"))
    private void incrementUseState(class_3222 player, class_1937 world, class_1799 stack, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        if (!((OwoItemExtensions) stack.method_7909()).owo$shouldTrackUsageStat() || !cir.getReturnValue().method_36360()) {
            return;
        }

        player.method_7259(class_3468.field_15372.method_14956(stack.method_7909()));
    }

}
