package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.inject.GreedyInputComponent;
import net.minecraft.class_2561;
import net.minecraft.class_7528;
import net.minecraft.class_7529;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(class_7529.class)
public abstract class EditBoxWidgetMixin extends class_7528 implements GreedyInputComponent {

    public EditBoxWidgetMixin(int i, int j, int k, int l, class_2561 text) {
        super(i, j, k, l, text);
    }

    @Override
    public void onFocusGained(Component.FocusSource source) {
        super.onFocusGained(source);
        this.method_25365(true);
    }

}
