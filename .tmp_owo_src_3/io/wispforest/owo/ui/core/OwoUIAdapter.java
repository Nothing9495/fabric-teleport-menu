package io.wispforest.owo.ui.core;

import F;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.Owo;
import io.wispforest.owo.renderdoc.RenderDoc;
import io.wispforest.owo.ui.util.CursorAdapter;
import org.lwjgl.glfw.GLFW;

import java.util.function.BiFunction;
import net.minecraft.class_1041;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_437;
import net.minecraft.class_6379;
import net.minecraft.class_6382;

/**
 * A UI adapter constitutes the main entrypoint to using owo-ui.
 * It takes care of rendering the UI tree correctly, handles input events
 * and cursor styling as well as the component inspector.
 * <p>
 * Additionally, the adapter implements all interfaces required for it
 * to be treated as a normal widget by the vanilla screen system - this means
 * even if you choose to not use {@link io.wispforest.owo.ui.base.BaseOwoScreen}
 * you can always simply add it as a widget and get most of the functionality
 * working out of the box
 *
 * @see io.wispforest.owo.ui.base.BaseOwoScreen
 */
public class OwoUIAdapter<R extends ParentComponent> implements class_364, class_4068, class_6379 {

    private static boolean isRendering = false;

    public final R rootComponent;
    public final CursorAdapter cursorAdapter;

    protected boolean disposed = false;
    protected boolean captureFrame = false;

    protected int x, y;
    protected int width, height;

    public boolean enableInspector = false;
    public boolean globalInspector = false;
    public int inspectorZOffset = 1000;

    protected OwoUIAdapter(int x, int y, int width, int height, R rootComponent) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.cursorAdapter = CursorAdapter.ofClientWindow();
        this.rootComponent = rootComponent;
    }

    /**
     * Create a UI adapter for the given screen. This also sets it up
     * to be rendered and receive input events, without needing you to
     * do any more setup
     *
     * @param screen             The screen for which to create an adapter
     * @param rootComponentMaker A function which will create the root component of this screen
     * @param <R>                The type of root component the created adapter will use
     * @return The new UI adapter, already set up for the given screen
     */
    public static <R extends ParentComponent> OwoUIAdapter<R> create(class_437 screen, BiFunction<Sizing, Sizing, R> rootComponentMaker) {
        var rootComponent = rootComponentMaker.apply(Sizing.fill(100), Sizing.fill(100));

        var adapter = new OwoUIAdapter<>(0, 0, screen.field_22789, screen.field_22790, rootComponent);
        screen.method_37063(adapter);
        screen.method_25395(adapter);

        return adapter;
    }

    /**
     * Create a new UI adapter without the specific context of a screen - use this
     * method when you want to embed owo-ui into a different context
     *
     * @param x                  The x-coordinate of the top-left corner of the root component
     * @param y                  The y-coordinate of the top-left corner of the root component
     * @param width              The width of the available area, in pixels
     * @param height             The height of the available area, in pixels
     * @param rootComponentMaker A function which will create the root component of the adapter
     * @param <R>                The type of root component the created adapter will use
     * @return The new UI adapter, ready for layout inflation
     */
    public static <R extends ParentComponent> OwoUIAdapter<R> createWithoutScreen(int x, int y, int width, int height, BiFunction<Sizing, Sizing, R> rootComponentMaker) {
        var rootComponent = rootComponentMaker.apply(Sizing.fill(100), Sizing.fill(100));
        return new OwoUIAdapter<>(x, y, width, height, rootComponent);
    }

    /**
     * Begin the layout process of the UI tree and
     * mount the tree once the layout is inflated
     * <p>
     * After this method has executed, this adapter is ready for rendering
     */
    public void inflateAndMount() {
        this.rootComponent.inflate(Size.of(this.width, this.height));
        this.rootComponent.mount(null, this.x, this.y);
    }

    public void moveAndResize(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.inflateAndMount();
    }

    /**
     * Dispose this UI adapter - this will destroy the cursor
     * objects held onto by this adapter and stop updating the cursor style
     * <p>
     * After this method has executed, this adapter can safely be garbage-collected
     */
    // TODO properly dispose root component
    public void dispose() {
        this.cursorAdapter.dispose();
        this.disposed = true;
    }

    /**
     * @return Toggle rendering of the inspector
     */
    public boolean toggleInspector() {
        return this.enableInspector = !this.enableInspector;
    }

    /**
     * @return Toggle the inspector between
     * hovered and global mode
     */
    public boolean toggleGlobalInspector() {
        return this.globalInspector = !this.globalInspector;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    @Override
    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        if (!(context instanceof OwoUIDrawContext)) context = OwoUIDrawContext.of(context);
        var owoContext = (OwoUIDrawContext) context;

        try {
            isRendering = true;

            if (this.captureFrame) RenderDoc.startFrameCapture();

            final var delta = class_310.method_1551().method_60646().method_60636();
            final var window = class_310.method_1551().method_22683();

            this.rootComponent.update(delta, mouseX, mouseY);

            RenderSystem.enableDepthTest();
            GlStateManager._enableScissorTest();

            GlStateManager._scissorBox(0, 0, window.method_4489(), window.method_4506());
            this.rootComponent.draw(owoContext, mouseX, mouseY, partialTicks, delta);

            GlStateManager._disableScissorTest();
            RenderSystem.disableDepthTest();

            this.rootComponent.drawTooltip(owoContext, mouseX, mouseY, partialTicks, delta);

            final var hovered = this.rootComponent.childAt(mouseX, mouseY);
            if (!disposed && hovered != null) {
                this.cursorAdapter.applyStyle(hovered.cursorStyle());
            }

            if (this.enableInspector) {
                context.method_51448().method_46416(0, 0, this.inspectorZOffset);
                owoContext.drawInspector(this.rootComponent, mouseX, mouseY, !this.globalInspector);
                context.method_51448().method_46416(0, 0, -this.inspectorZOffset);
            }

            if (this.captureFrame) RenderDoc.endFrameCapture();
        } finally {
            isRendering = false;
            this.captureFrame = false;
        }
    }

    @Override
    public boolean method_25405(double mouseX, double mouseY) {
        return this.rootComponent.isInBoundingBox(mouseX, mouseY);
    }

    @Override
    public void method_25365(boolean focused) {}

    @Override
    public boolean method_25370() {
        return true;
    }

    @Override
    public boolean method_25402(double mouseX, double mouseY, int button) {
        return this.rootComponent.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public boolean method_25406(double mouseX, double mouseY, int button) {
        return this.rootComponent.onMouseUp(mouseX, mouseY, button);
    }

    @Override
    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.rootComponent.onMouseScroll(mouseX, mouseY, verticalAmount);
    }

    @Override
    public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.rootComponent.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
    }

    @Override
    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (Owo.DEBUG && keyCode == GLFW.GLFW_KEY_LEFT_SHIFT) {
            if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                this.toggleInspector();
            } else if ((modifiers & GLFW.GLFW_MOD_ALT) != 0) {
                this.toggleGlobalInspector();
            }
        }

        if (Owo.DEBUG && keyCode == GLFW.GLFW_KEY_R && RenderDoc.isAvailable()) {
            if ((modifiers & GLFW.GLFW_MOD_ALT) != 0 && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                this.captureFrame = true;
            }
        }

        return this.rootComponent.onKeyPress(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean method_25400(char chr, int modifiers) {
        return this.rootComponent.onCharTyped(chr, modifiers);
    }

    @Override
    public class_6380 method_37018() {
        return class_6380.field_33784;
    }

    @Override
    public void method_37020(class_6382 builder) {}

    public static boolean isRendering() {
        return isRendering;
    }
}
