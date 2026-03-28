package io.wispforest.owo.compat.rei;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;

public class ReiWidgetComponent extends BaseComponent {

    private final WidgetWithBounds widget;

    protected ReiWidgetComponent(WidgetWithBounds widget) {
        this.widget = widget;

        var bounds = widget.getBounds();
        this.horizontalSizing.set(Sizing.fixed(bounds.getWidth()));
        this.verticalSizing.set(Sizing.fixed(bounds.getHeight()));

        this.mouseEnter().subscribe(() -> {
            this.focusHandler().focus(this, FocusSource.KEYBOARD_CYCLE);
        });

        this.mouseLeave().subscribe(() -> {
            this.focusHandler().focus(null, null);
        });
    }

    @Override
    public void mount(ParentComponent parent, int x, int y) {
        super.mount(parent, x, y);
        this.applyToWidget();
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        this.widget.method_25394(context, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawFocusHighlight(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {}

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return this.widget.getBounds().getWidth();
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return this.widget.getBounds().getHeight();
    }

    @Override
    public void updateX(int x) {
        super.updateX(x);
        this.applyToWidget();
    }

    @Override
    public void updateY(int y) {
        super.updateY(y);
        this.applyToWidget();
    }

    private void applyToWidget() {
        var bounds = this.widget.getBounds();

        bounds.x = this.x;
        bounds.y = this.y;

        bounds.width = this.width;
        bounds.height = this.height;
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        return this.widget.method_25402(this.x + mouseX, this.y + mouseY, button)
                | super.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public boolean onMouseUp(double mouseX, double mouseY, int button) {
        return this.widget.method_25406(this.x + mouseX, this.y + mouseY, button)
                | super.onMouseUp(mouseX, mouseY, button);
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        return this.widget.method_25401(this.x + mouseX, this.y + mouseY, 0, amount)
                | super.onMouseScroll(mouseX, mouseY, amount);
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        return this.widget.method_25403(this.x + mouseX, this.y + mouseY, button, deltaX, deltaY)
                | super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
    }

    @Override
    public boolean onCharTyped(char chr, int modifiers) {
        return this.widget.method_25400(chr, modifiers)
                | super.onCharTyped(chr, modifiers);
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        return this.widget.method_25404(keyCode, scanCode, modifiers)
                | super.onKeyPress(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }
}
