package io.wispforest.owo.mixin;

import io.wispforest.owo.ui.util.MatrixStackTransformer;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(class_332.class)
public abstract class DrawContextMixin implements MatrixStackTransformer {

    @Shadow public abstract class_4587 getMatrices();

    @Override
    public class_4587 getMatrixStack() {
        return getMatrices();
    }
}
