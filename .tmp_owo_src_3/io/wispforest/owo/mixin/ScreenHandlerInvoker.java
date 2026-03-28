package io.wispforest.owo.mixin;

import net.minecraft.class_1703;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_1703.class)
public interface ScreenHandlerInvoker {

    @Invoker("insertItem")
    boolean owo$insertItem(class_1799 stack, int startIndex, int endIndex, boolean fromLast);

}
