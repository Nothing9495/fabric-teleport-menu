package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.util.pond.OwoEntityRenderDispatcherExtension;
import net.minecraft.class_1297;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_7833;
import net.minecraft.class_898;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_898.class)
public class EntityRenderDispatcherMixin implements OwoEntityRenderDispatcherExtension {

    private boolean owo$showNametag = true;
    private boolean owo$counterRotate = false;

    @Override
    public void owo$setShowNametag(boolean showNametag) {
        this.owo$showNametag = showNametag;
    }

    @Override
    public boolean owo$showNametag() {
        return this.owo$showNametag;
    }

    @Override
    public void owo$setCounterRotate(boolean counterRotate) {
        this.owo$counterRotate = counterRotate;
    }

    @Override
    public boolean owo$counterRotate() {
        return this.owo$counterRotate;
    }

    @Shadow public class_4184 camera;

    @Inject(method = "renderFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionf;)V", shift = At.Shift.AFTER))
    private void cancelFireRotation(class_4587 matrices, class_4597 vertexConsumers, class_1297 entity, Quaternionf rotation, CallbackInfo ci) {
        if (!this.owo$counterRotate) return;
        matrices.method_22907(class_7833.field_40716.rotationDegrees(this.camera.method_19330() + 170));
        matrices.method_22904(0, 0, .1);
    }
}
