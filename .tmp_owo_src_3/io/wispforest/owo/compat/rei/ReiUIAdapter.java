package io.wispforest.owo.compat.rei;

import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.ScissorStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_364;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReiUIAdapter<T extends ParentComponent> extends Widget {

    public static final Point LAYOUT = new Point(-69, -69);

    public final OwoUIAdapter<T> adapter;

    public ReiUIAdapter(Rectangle bounds, BiFunction<Sizing, Sizing, T> rootComponentMaker) {
        this.adapter = OwoUIAdapter.createWithoutScreen(bounds.x, bounds.y, bounds.width, bounds.height, rootComponentMaker);
        this.adapter.inspectorZOffset = 900;

        if (class_310.method_1551().field_1755 != null) {
            ScreenEvents.remove(class_310.method_1551().field_1755).register(screen -> this.adapter.dispose());
        }
    }

    public void prepare() {
        this.adapter.inflateAndMount();
    }

    public T rootComponent() {
        return this.adapter.rootComponent;
    }

    public <W extends WidgetWithBounds> ReiWidgetComponent wrap(W widget) {
        return new ReiWidgetComponent(widget);
    }

    public <W extends WidgetWithBounds> ReiWidgetComponent wrap(Function<Point, W> widgetFactory, Consumer<W> widgetConfigurator) {
        var widget = widgetFactory.apply(LAYOUT);
        widgetConfigurator.accept(widget);
        return new ReiWidgetComponent(widget);
    }

    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return this.adapter.method_25405(mouseX, mouseY);
    }

    @Override
    public boolean method_25402(double mouseX, double mouseY, int button) {
        return this.adapter.method_25402(mouseX - this.adapter.x(), mouseY - this.adapter.y(), button);
    }

    @Override
    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.adapter.method_25401(mouseX - this.adapter.x(), mouseY - this.adapter.y(), horizontalAmount, verticalAmount);
    }

    @Override
    public boolean method_25406(double mouseX, double mouseY, int button) {
        return this.adapter.method_25406(mouseX - this.adapter.x(), mouseY - this.adapter.y(), button);
    }

    @Override
    public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.adapter.method_25403(mouseX - this.adapter.x(), mouseY - this.adapter.y(), button, deltaX, deltaY);
    }

    @Override
    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        return this.adapter.method_25404(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean method_16803(int keyCode, int scanCode, int modifiers) {
        return this.adapter.method_16803(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean method_25400(char chr, int modifiers) {
        return this.adapter.method_25400(chr, modifiers);
    }

    @Override
    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        ScissorStack.push(this.adapter.x(), this.adapter.y(), this.adapter.width(), this.adapter.height(), context.method_51448());
        this.adapter.method_25394(context, mouseX, mouseY, partialTicks);
        ScissorStack.pop();

        context.method_51452();
    }

    @Override
    public List<? extends class_364> method_25396() {
        return List.of();
    }
}
