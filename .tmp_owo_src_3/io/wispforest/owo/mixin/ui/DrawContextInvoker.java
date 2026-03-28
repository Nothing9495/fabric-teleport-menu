package io.wispforest.owo.mixin.ui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_5684;
import net.minecraft.class_8000;

@Mixin(class_332.class)
public interface DrawContextInvoker {

    @Invoker("drawTooltip")
    void owo$renderTooltipFromComponents(class_327 textRenderer, List<class_5684> components, int x, int y, class_8000 positioner);

    @Accessor("matrices")
    class_4587 owo$getMatrices();

    @Mutable
    @Accessor("matrices")
    void owo$setMatrices(class_4587 matrices);

    @Accessor("scissorStack")
    class_332.class_8214 owo$getScissorStack();

    @Mutable
    @Accessor("scissorStack")
    void owo$setScissorStack(class_332.class_8214 scissorStack);
}
