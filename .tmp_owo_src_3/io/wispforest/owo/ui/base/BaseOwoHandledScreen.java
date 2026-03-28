package io.wispforest.owo.ui.base;

import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.ui.SlotAccessor;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.inject.GreedyInputComponent;
import io.wispforest.owo.ui.util.DisposableScreen;
import io.wispforest.owo.ui.util.UIErrorToast;
import io.wispforest.owo.util.pond.OwoSlotExtension;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1735;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_437;
import net.minecraft.class_465;

public abstract class BaseOwoHandledScreen<R extends ParentComponent, S extends class_1703> extends class_465<S> implements DisposableScreen {

    /**
     * The UI adapter of this screen. This handles
     * all user input as well as setting up GL state for rendering
     * and managing component focus
     */
    protected OwoUIAdapter<R> uiAdapter = null;

    /**
     * Whether this screen has encountered an unrecoverable
     * error during its lifecycle and should thus close
     * itself on the next frame
     */
    protected boolean invalid = false;

    protected BaseOwoHandledScreen(S handler, class_1661 inventory, class_2561 title) {
        super(handler, inventory, title);
    }

    /**
     * Initialize the UI adapter for this screen. Usually
     * the body of this method will simply consist of a call
     * to {@link OwoUIAdapter#create(class_437, BiFunction)}
     *
     * @return The UI adapter for this screen to use
     */
    protected abstract @NotNull OwoUIAdapter<R> createAdapter();

    /**
     * Build the component hierarchy of this screen,
     * called after the adapter and root component have been
     * initialized by {@link #createAdapter()}
     *
     * @param rootComponent The root component created
     *                      in the previous initialization step
     */
    protected abstract void build(R rootComponent);

    @Override
    protected void method_25426() {
        super.method_25426();

        if (this.invalid) return;

        // Check whether this screen was already initialized
        if (this.uiAdapter != null) {
            // If it was, only resize the adapter instead of recreating it - this preserves UI state
            this.uiAdapter.moveAndResize(0, 0, this.field_22789, this.field_22790);
            // Re-add it as a child to circumvent vanilla clearing them
            this.method_37063(this.uiAdapter);
        } else {
            try {
                this.uiAdapter = this.createAdapter();
                this.build(this.uiAdapter.rootComponent);

                this.uiAdapter.inflateAndMount();
            } catch (Exception error) {
                Owo.LOGGER.warn("Could not initialize owo screen", error);
                UIErrorToast.report(error);
                this.invalid = true;
            }
        }
    }

    /**
     * Disable the slot at the given index. Note
     * that this is hard override and the slot cannot
     * re-enable itself
     *
     * @param index The index of the slot to disable
     */
    protected void disableSlot(int index) {
        this.disableSlot(this.field_2797.field_7761.get(index));
    }

    /**
     * Disable the given slot. Note that
     * this is hard override and the slot cannot
     * re-enable itself
     */
    protected void disableSlot(class_1735 slot) {
        ((OwoSlotExtension) slot).owo$setDisabledOverride(true);
    }

    /**
     * Enable the slot at the given index. Note
     * that this is an override and cannot enable
     * a slot that is disabled through its own will
     *
     * @param index The index of the slot to enable
     */
    protected void enableSlot(int index) {
        this.enableSlot(this.field_2797.field_7761.get(index));
    }

    /**
     * Enable the given slot. Note that
     * this is an override and cannot enable
     * a slot that is disabled through its own will
     */
    protected void enableSlot(class_1735 slot) {
        ((OwoSlotExtension) slot).owo$setDisabledOverride(false);
    }

    /**
     * @return whether the given slot is enabled or disabled
     * using the {@link OwoSlotExtension} disabling functionality
     */
    protected boolean isSlotEnabled(int index) {
        return isSlotEnabled(this.field_2797.field_7761.get(index));
    }

    /**
     * @return whether the given slot is enabled or disabled
     * using the {@link OwoSlotExtension} disabling functionality
     */
    protected boolean isSlotEnabled(class_1735 slot) {
        return !((OwoSlotExtension) slot).owo$getDisabledOverride();
    }

    /**
     * Wrap the slot at the given index in this screen's
     * handler into a component, so it can be managed by the UI system
     *
     * @param index The index the slot occupies in the handler's slot list
     * @return The wrapped slot
     */
    protected SlotComponent slotAsComponent(int index) {
        return new SlotComponent(index);
    }

    /**
     * A convenience shorthand for querying a component from the adapter's
     * root component via {@link ParentComponent#childById(Class, String)}
     */
    protected <C extends Component> C component(Class<C> expectedClass, String id) {
        return this.uiAdapter.rootComponent.childById(expectedClass, id);
    }

    /**
     * Compute a stream of all components for which to
     * generate exclusion areas in a recipe viewer overlay.
     * Called by the REI and EMI plugins
     */
    @ApiStatus.OverrideOnly
    public Stream<Component> componentsForExclusionAreas() {
        if (this.method_25396().isEmpty()) return Stream.of();

        var rootComponent = uiAdapter.rootComponent;
        var children = new ArrayList<Component>();

        rootComponent.collectDescendants(children);
        children.remove(rootComponent);

        return children.stream().filter(component -> !(component instanceof ParentComponent parent) || parent.surface() != Surface.BLANK);
    }

    @Override
    public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {}

    @Override
    public void method_25394(class_332 vanillaContext, int mouseX, int mouseY, float delta) {
        var context = OwoUIDrawContext.of(vanillaContext);
        if (!this.invalid) {
            super.method_25394(context, mouseX, mouseY, delta);

            if (this.uiAdapter.enableInspector) {
                context.method_51448().method_46416(0, 0, 500);

                for (int i = 0; i < this.field_2797.field_7761.size(); i++) {
                    var slot = this.field_2797.field_7761.get(i);
                    if (!slot.method_7682()) continue;

                    context.drawText(class_2561.method_43470("H:" + i),
                        this.field_2776 + slot.field_7873 + 15, this.field_2800 + slot.field_7872 + 9, .5f, 0x0096FF,
                        OwoUIDrawContext.TextAnchor.BOTTOM_RIGHT
                    );
                    context.drawText(class_2561.method_43470("I:" + slot.method_34266()),
                        this.field_2776 + slot.field_7873 + 15, this.field_2800 + slot.field_7872 + 15, .5f, 0x5800FF,
                        OwoUIDrawContext.TextAnchor.BOTTOM_RIGHT
                    );
                }

                context.method_51448().method_46416(0, 0, -500);
            }

            this.method_2380(context, mouseX, mouseY);
        } else {
            this.method_25419();
        }
    }

    @Override
    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) == 0
            && this.uiAdapter.rootComponent.focusHandler().focused() instanceof GreedyInputComponent inputComponent
            && inputComponent.onKeyPress(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.method_25404(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.uiAdapter.method_25403(mouseX, mouseY, button, deltaX, deltaY) || super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Nullable
    @Override
    public class_364 method_25399() {
        return this.uiAdapter;
    }

    @Override
    public void method_25432() {
        super.method_25432();
        if (this.uiAdapter != null) {
            this.uiAdapter.cursorAdapter.applyStyle(CursorStyle.NONE);
        }
    }

    @Override
    public void dispose() {
        if (this.uiAdapter != null) this.uiAdapter.dispose();
    }

    @Override
    protected void method_2389(class_332 context, float delta, int mouseX, int mouseY) {}

    public class SlotComponent extends BaseComponent {

        protected final class_1735 slot;
        protected boolean didDraw = false;

        protected SlotComponent(int index) {
            this.slot = BaseOwoHandledScreen.this.field_2797.method_7611(index);
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            this.didDraw = true;

            int[] scissor = new int[4];
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, scissor);

            ((OwoSlotExtension) this.slot).owo$setScissorArea(PositionedRectangle.of(
                scissor[0], scissor[1], scissor[2], scissor[3]
            ));
        }

        @Override
        public void update(float delta, int mouseX, int mouseY) {
            super.update(delta, mouseX, mouseY);

            ((OwoSlotExtension) this.slot).owo$setDisabledOverride(!this.didDraw);

            this.didDraw = false;
        }

        @Override
        public void drawTooltip(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            if (!this.slot.method_7681()) {
                super.drawTooltip(context, mouseX, mouseY, partialTicks, delta);
            }
        }

        @Override
        public boolean shouldDrawTooltip(double mouseX, double mouseY) {
            return super.shouldDrawTooltip(mouseX, mouseY);
        }

        @Override
        protected int determineHorizontalContentSize(Sizing sizing) {
            return 16;
        }

        @Override
        protected int determineVerticalContentSize(Sizing sizing) {
            return 16;
        }

        @Override
        public void updateX(int x) {
            super.updateX(x);
            ((SlotAccessor) this.slot).owo$setX(x - BaseOwoHandledScreen.this.field_2776);
        }

        @Override
        public void updateY(int y) {
            super.updateY(y);
            ((SlotAccessor) this.slot).owo$setY(y - BaseOwoHandledScreen.this.field_2800);
        }
    }
}
