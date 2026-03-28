package io.wispforest.owo.ui.base;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Size;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5684;
import net.minecraft.class_9779;

@ApiStatus.Experimental
public abstract class BaseOwoTooltipComponent<R extends ParentComponent> implements class_5684 {

    protected final R rootComponent;
    protected int virtualWidth = 1000, virtualHeight = 1000;

    protected BaseOwoTooltipComponent(Supplier<R> components) {
        this.rootComponent = components.get();

        this.rootComponent.inflate(Size.of(this.virtualWidth, this.virtualHeight));
        this.rootComponent.mount(null, 0, 0);
    }

    @Override
    public void method_32666(class_327 textRenderer, int x, int y, class_332 context) {
        var client = class_310.method_1551();

        var tickCounter = class_310.method_1551().method_60646();

        this.rootComponent.moveTo(x, y);
        this.rootComponent.draw(OwoUIDrawContext.of(context), -1000, -1000, tickCounter.method_60637(false), tickCounter.method_60636());
    }

    @Override
    public int method_32661() {
        return this.rootComponent.fullSize().height();
    }

    @Override
    public int method_32664(class_327 textRenderer) {
        return this.rootComponent.fullSize().width();
    }
}
