package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.util.pond.OwoEntityRenderDispatcherExtension;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_7833;
import net.minecraft.class_897;
import net.minecraft.class_898;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_897.class)
public class EntityRendererMixin<T extends class_1297> {

    @Shadow
    @Final
    protected class_898 dispatcher;

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void cancelLabel(T entity, class_2561 text, class_4587 matrices, class_4597 vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
        if (((OwoEntityRenderDispatcherExtension) this.dispatcher).owo$showNametag()) return;
        ci.cancel();
    }

    @Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionf;)V", shift = At.Shift.AFTER))
    private void adjustLabelRotation(T entity, class_2561 text, class_4587 matrices, class_4597 vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
        if (!((OwoEntityRenderDispatcherExtension) this.dispatcher).owo$counterRotate()) return;

        matrices.method_22907(new Quaternionf(this.dispatcher.method_24197()).invert());
        matrices.method_22907(class_7833.field_40716.rotationDegrees(180));
    }

}
