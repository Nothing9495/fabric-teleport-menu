package io.wispforest.owo.mixin.ui;

import Z;
import io.wispforest.owo.mixin.BufferBuilderAccessor;
import io.wispforest.owo.util.pond.OwoTessellatorExtension;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_293;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_289.class)
public class TessellatorMixin implements OwoTessellatorExtension {

    @Unique
    private boolean owo$skipBegin = false;

    @Unique
    @Nullable
    private class_287 bufferBuilder = null;

    @Inject(method = "begin", at = @At("HEAD"), cancellable = true)
    private void skipBegin(class_293.class_5596 drawMode, class_293 format, CallbackInfoReturnable<class_287> cir) {
        if(this.bufferBuilder == null) return;

        var bl = this.owo$skipBegin
                && builderAccessor().getDrawMode().equals(drawMode)
                && builderAccessor().getFormat().equals(format);

        if (!bl) return;

        this.owo$skipBegin = false;

        cir.setReturnValue(this.bufferBuilder);

        this.bufferBuilder = null;
    }

    @Override
    public void owo$skipNextBegin() {
        if(this.bufferBuilder != null && builderAccessor().isBuilding()) this.owo$skipBegin = true;
    }

    @Override
    public void owo$setStoredBuilder(class_287 builder) {
        this.bufferBuilder = builder;
    }

    @Override
    public class_287 owo$getStoredBuilder() {
        return this.bufferBuilder;
    }

    @Unique
    private BufferBuilderAccessor builderAccessor() {
        return (BufferBuilderAccessor) this.bufferBuilder;
    }
}
