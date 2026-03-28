package io.wispforest.owo.ui.core;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.client.OwoClient;
import io.wispforest.owo.mixin.ui.DrawContextInvoker;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import io.wispforest.owo.ui.util.NinePatchTexture;
import io.wispforest.owo.util.pond.OwoTessellatorExtension;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_293;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_4597;
import net.minecraft.class_5250;
import net.minecraft.class_5684;
import net.minecraft.class_757;
import net.minecraft.class_8001;
import net.minecraft.client.render.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class OwoUIDrawContext extends class_332 {

    @Deprecated
    public static final class_2960 PANEL_TEXTURE = class_2960.method_60655("owo", "textures/gui/panel.png");
    @Deprecated
    public static final class_2960 DARK_PANEL_TEXTURE = class_2960.method_60655("owo", "textures/gui/dark_panel.png");
    @Deprecated
    public static final class_2960 PANEL_INSET_TEXTURE = class_2960.method_60655("owo", "textures/gui/panel_inset.png");

    public static final class_2960 PANEL_NINE_PATCH_TEXTURE = class_2960.method_60655("owo", "panel/default");
    public static final class_2960 DARK_PANEL_NINE_PATCH_TEXTURE = class_2960.method_60655("owo", "panel/dark");
    public static final class_2960 PANEL_INSET_NINE_PATCH_TEXTURE = class_2960.method_60655("owo", "panel/inset");

    private boolean recording = false;

    private OwoUIDrawContext(class_310 client, class_4597.class_4598 vertexConsumers) {
        super(client, vertexConsumers);
    }

    public static OwoUIDrawContext of(class_332 context) {
        var owoContext = new OwoUIDrawContext(class_310.method_1551(), context.method_51450());
        ((DrawContextInvoker) owoContext).owo$setScissorStack(((DrawContextInvoker) context).owo$getScissorStack());
        ((DrawContextInvoker) owoContext).owo$setMatrices(((DrawContextInvoker) context).owo$getMatrices());

        return owoContext;
    }

    public static UtilityScreen utilityScreen() {
        return UtilityScreen.get();
    }

    public void recordQuads() {
        recording = true;
    }

    public boolean recording() {
        return recording;
    }

    public void submitQuads() {
        recording = false;

        var extension = ((OwoTessellatorExtension) class_289.method_1348());

        var buffer = extension.owo$getStoredBuilder();

        extension.owo$setStoredBuilder(null);

        class_286.method_43433(buffer.method_60800());
    }

    /**
     * Draw the outline of a rectangle
     *
     * @param x      The x-coordinate of top-left corner of the rectangle
     * @param y      The y-coordinate of top-left corner of the rectangle
     * @param width  The width of the rectangle
     * @param height The height of the rectangle
     * @param color  The color of the rectangle
     */
    public void drawRectOutline(int x, int y, int width, int height, int color) {
        this.method_25294(x, y, x + width, y + 1, color);
        this.method_25294(x, y + height - 1, x + width, y + height, color);

        this.method_25294(x, y + 1, x + 1, y + height - 1, color);
        this.method_25294(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    /**
     * Draw a filled rectangle with a gradient
     *
     * @param x                The x-coordinate of top-left corner of the rectangle
     * @param y                The y-coordinate of top-left corner of the rectangle
     * @param width            The width of the rectangle
     * @param height           The height of the rectangle
     * @param topLeftColor     The color at the rectangle's top left corner
     * @param topRightColor    The color at the rectangle's top right corner
     * @param bottomRightColor The color at the rectangle's bottom right corner
     * @param bottomLeftColor  The color at the rectangle's bottom left corner
     */
    public void drawGradientRect(int x, int y, int width, int height, int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor) {
        var buffer = class_289.method_1348().method_60827(class_293.class_5596.field_27382, class_290.field_1576);
        var matrix = this.method_51448().method_23760().method_23761();

        buffer.method_22918(matrix, x + width, y, 0).method_39415(topRightColor);
        buffer.method_22918(matrix, x, y, 0).method_39415(topLeftColor);
        buffer.method_22918(matrix, x, y + height, 0).method_39415(bottomLeftColor);
        buffer.method_22918(matrix, x + width, y + height, 0).method_39415(bottomRightColor);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShader(class_757::method_34540);
        class_286.method_43433(buffer.method_60800());

        RenderSystem.disableBlend();
    }

    /**
     * Draw a panel that looks like the background of a vanilla
     * inventory screen
     *
     * @param x      The x-coordinate of top-left corner of the panel
     * @param y      The y-coordinate of top-left corner of the panel
     * @param width  The width of the panel
     * @param height The height of the panel
     * @param dark   Whether to use the dark version of the panel texture
     */
    public void drawPanel(int x, int y, int width, int height, boolean dark) {
        NinePatchTexture.draw(dark ? DARK_PANEL_NINE_PATCH_TEXTURE : PANEL_NINE_PATCH_TEXTURE, this, x, y, width, height);
    }

    public void drawSpectrum(int x, int y, int width, int height, boolean vertical) {
        var buffer = class_289.method_1348().method_60827(class_293.class_5596.field_27382, class_290.field_1576);
        var matrix = this.method_51448().method_23760().method_23761();

        buffer.method_22918(matrix, x, y, 0).method_22915(1f, 1f, 1f, 1f);
        buffer.method_22918(matrix, x, y + height, 0).method_22915(vertical ? 0f : 1f, 1f, 1f, 1f);
        buffer.method_22918(matrix, x + width, y + height, 0).method_22915(0f, 1f, 1f, 1f);
        buffer.method_22918(matrix, x + width, y, 0).method_22915(vertical ? 1f : 0f, 1f, 1f, 1f);

        OwoClient.HSV_PROGRAM.use();
        class_286.method_43433(buffer.method_60800());
    }

    public void drawText(class_2561 text, float x, float y, float scale, int color) {
        drawText(text, x, y, scale, color, TextAnchor.TOP_LEFT);
    }

    public void drawText(class_2561 text, float x, float y, float scale, int color, TextAnchor anchorPoint) {
        final var textRenderer = class_310.method_1551().field_1772;

        this.method_51448().method_22903();
        this.method_51448().method_22905(scale, scale, 1);

        switch (anchorPoint) {
            case TOP_RIGHT -> x -= textRenderer.method_27525(text) * scale;
            case BOTTOM_LEFT -> y -= textRenderer.field_2000 * scale;
            case BOTTOM_RIGHT -> {
                x -= textRenderer.method_27525(text) * scale;
                y -= textRenderer.field_2000 * scale;
            }
        }


        this.method_51439(textRenderer, text, (int) (x * (1 / scale)), (int) (y * (1 / scale)), color, false);
        this.method_51448().method_22909();
    }

    public enum TextAnchor {
        TOP_RIGHT, BOTTOM_RIGHT, TOP_LEFT, BOTTOM_LEFT
    }

    public void drawLine(int x1, int y1, int x2, int y2, double thiccness, Color color) {
        var offset = new Vector2d(x2 - x1, y2 - y1).perpendicular().normalize().mul(thiccness * .5d);

        var buffer = class_289.method_1348().method_60827(class_293.class_5596.field_27382, class_290.field_1576);
        var matrix = this.method_51448().method_23760().method_23761();
        int vColor = color.argb();

        buffer.method_22918(matrix, (float) (x1 + offset.x), (float) (y1 + offset.y), 0).method_39415(vColor);
        buffer.method_22918(matrix, (float) (x1 - offset.x), (float) (y1 - offset.y), 0).method_39415(vColor);
        buffer.method_22918(matrix, (float) (x2 - offset.x), (float) (y2 - offset.y), 0).method_39415(vColor);
        buffer.method_22918(matrix, (float) (x2 + offset.x), (float) (y2 + offset.y), 0).method_39415(vColor);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(class_757::method_34540);

        class_286.method_43433(buffer.method_60800());
    }

    public void drawCircle(int centerX, int centerY, int segments, double radius, Color color) {
        drawCircle(centerX, centerY, 0, 360, segments, radius, color);
    }

    public void drawCircle(int centerX, int centerY, double angleFrom, double angleTo, int segments, double radius, Color color) {
        Preconditions.checkArgument(angleFrom < angleTo, "angleFrom must be less than angleTo");

        var buffer = class_289.method_1348().method_60827(class_293.class_5596.field_27381, class_290.field_1576);
        var matrix = this.method_51448().method_23760().method_23761();

        double angleStep = Math.toRadians(angleTo - angleFrom) / segments;
        int vColor = color.argb();

        buffer.method_22918(matrix, centerX, centerY, 0).method_39415(vColor);

        for (int i = segments; i >= 0; i--) {
            double theta = Math.toRadians(angleFrom) + i * angleStep;
            buffer.method_22918(matrix, (float) (centerX - Math.cos(theta) * radius), (float) (centerY - Math.sin(theta) * radius), 0)
                    .method_39415(vColor);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(class_757::method_34540);

        class_286.method_43433(buffer.method_60800());
    }

    public void drawRing(int centerX, int centerY, int segments, double innerRadius, double outerRadius, Color innerColor, Color outerColor) {
        drawRing(centerX, centerY, 0d, 360d, segments, innerRadius, outerRadius, innerColor, outerColor);
    }

    public void drawRing(int centerX, int centerY, double angleFrom, double angleTo, int segments, double innerRadius, double outerRadius, Color innerColor, Color outerColor) {
        Preconditions.checkArgument(angleFrom < angleTo, "angleFrom must be less than angleTo");
        Preconditions.checkArgument(innerRadius < outerRadius, "innerRadius must be less than outerRadius");

        var buffer = class_289.method_1348().method_60827(class_293.class_5596.field_27380, class_290.field_1576);
        var matrix = this.method_51448().method_23760().method_23761();

        double angleStep = Math.toRadians(angleTo - angleFrom) / segments;
        int inColor = innerColor.argb();
        int outColor = outerColor.argb();

        for (int i = 0; i <= segments; i++) {
            double theta = Math.toRadians(angleFrom) + i * angleStep;

            buffer.method_22918(matrix, (float) (centerX - Math.cos(theta) * outerRadius), (float) (centerY - Math.sin(theta) * outerRadius), 0)
                    .method_39415(outColor);
            buffer.method_22918(matrix, (float) (centerX - Math.cos(theta) * innerRadius), (float) (centerY - Math.sin(theta) * innerRadius), 0)
                    .method_39415(inColor);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(class_757::method_34540);

        class_286.method_43433(buffer.method_60800());
    }

    public void drawTooltip(class_327 textRenderer, int x, int y, List<class_5684> components) {
        ((DrawContextInvoker) this).owo$renderTooltipFromComponents(textRenderer, components, x, y, class_8001.field_41687);
    }

    // --- debug rendering ---

    /**
     * Draw the area around the given rectangle which
     * the given insets describe
     *
     * @param x      The x-coordinate of top-left corner of the rectangle
     * @param y      The y-coordinate of top-left corner of the rectangle
     * @param width  The width of the rectangle
     * @param height The height of the rectangle
     * @param insets The insets to draw around the rectangle
     * @param color  The color to draw the inset area with
     */
    public void drawInsets(int x, int y, int width, int height, Insets insets, int color) {
        this.method_25294(x - insets.left(), y - insets.top(), x + width + insets.right(), y, color);
        this.method_25294(x - insets.left(), y + height, x + width + insets.right(), y + height + insets.bottom(), color);

        this.method_25294(x - insets.left(), y, x, y + height, color);
        this.method_25294(x + width, y, x + width + insets.right(), y + height, color);
    }

    /**
     * Draw the element inspector for the given tree, detailing the position,
     * bounding box, margins and padding of each component
     *
     * @param root        The root component of the hierarchy to draw
     * @param mouseX      The x-coordinate of the mouse pointer
     * @param mouseY      The y-coordinate of the mouse pointer
     * @param onlyHovered Whether to only draw the inspector for the hovered widget
     */
    public void drawInspector(ParentComponent root, double mouseX, double mouseY, boolean onlyHovered) {
        RenderSystem.disableDepthTest();
        var client = class_310.method_1551();
        var textRenderer = client.field_1772;

        var children = new ArrayList<Component>();
        if (!onlyHovered) {
            root.collectDescendants(children);
        } else if (root.childAt((int) mouseX, (int) mouseY) != null) {
            children.add(root.childAt((int) mouseX, (int) mouseY));
        }

        for (var child : children) {
            if (child instanceof ParentComponent parentComponent) {
                this.drawInsets(parentComponent.x(), parentComponent.y(), parentComponent.width(),
                        parentComponent.height(), parentComponent.padding().get().inverted(), 0xA70CECDD);
            }

            final var margins = child.margins().get();
            this.drawInsets(child.x(), child.y(), child.width(), child.height(), margins, 0xA7FFF338);
            drawRectOutline(child.x(), child.y(), child.width(), child.height(), 0xFF3AB0FF);

            if (onlyHovered) {

                int inspectorX = child.x() + 1;
                int inspectorY = child.y() + child.height() + child.margins().get().bottom() + 1;
                int inspectorHeight = textRenderer.field_2000 * 2 + 4;

                if (inspectorY > client.method_22683().method_4502() - inspectorHeight) {
                    inspectorY -= child.fullSize().height() + inspectorHeight + 1;
                    if (inspectorY < 0) inspectorY = 1;
                    if (child instanceof ParentComponent parentComponent) {
                        inspectorX += parentComponent.padding().get().left();
                        inspectorY += parentComponent.padding().get().top();
                    }
                }

                final var nameText = class_2561.method_30163(child.getClass().getSimpleName() + (child.id() != null ? " '" + child.id() + "'" : ""));
                final var descriptor = class_2561.method_43470(child.x() + "," + child.y() + " (" + child.width() + "," + child.height() + ")"
                        + " <" + margins.top() + "," + margins.bottom() + "," + margins.left() + "," + margins.right() + "> ");
                if (child instanceof ParentComponent parentComponent) {
                    var padding = parentComponent.padding().get();
                    descriptor.method_27693(" >" + padding.top() + "," + padding.bottom() + "," + padding.left() + "," + padding.right() + "<");
                }

                int width = Math.max(textRenderer.method_27525(nameText), textRenderer.method_27525(descriptor));
                method_25294(inspectorX, inspectorY, inspectorX + width + 3, inspectorY + inspectorHeight, 0xA7000000);
                drawRectOutline(inspectorX, inspectorY, width + 3, inspectorHeight, 0xA7000000);

                this.method_51439(textRenderer, nameText, inspectorX + 2, inspectorY + 2, 0xFFFFFF, false);
                this.method_51439(textRenderer, descriptor, inspectorX + 2, inspectorY + textRenderer.field_2000 + 2, 0xFFFFFF, false);
            }
        }

        RenderSystem.enableDepthTest();
    }

    public static class UtilityScreen extends class_437 {

        private static UtilityScreen INSTANCE;

        private class_437 linkSourceScreen = null;

        private UtilityScreen() {
            super(class_2561.method_43473());
        }

        public static UtilityScreen get() {
            if (INSTANCE == null) {
                INSTANCE = new UtilityScreen();

                final var client = class_310.method_1551();
                INSTANCE.method_25423(
                        client,
                        client.method_22683().method_4486(),
                        client.method_22683().method_4502()
                );
            }

            return INSTANCE;
        }

        /**
         * Set the screen to which the game should return after the {@link net.minecraft.class_407}
         * opened by {@link #method_25430(class_2583)} to {@code screen}
         *
         * @see #method_25430(class_2583)
         */
        public void setLinkSource(class_437 screen) {
            this.linkSourceScreen = screen;
        }

        /**
         * Invoke {@link #setLinkSource(class_437)} with the current screen. Used by the default text click handler
         * in {@link io.wispforest.owo.ui.component.LabelComponent}
         */
        public void captureLinkSource() {
            this.setLinkSource(this.field_22787.field_1755);
        }

        @ApiStatus.Internal
        public @Nullable class_437 getAndClearLinkSource() {
            var source = this.linkSourceScreen;
            this.linkSourceScreen = null;

            return source;
        }

        /**
         * Since the vanilla implementation of this method always returns to the screen the method was invoked on
         * (which, here, would be the utility screen which is not what we want), either {@link #captureLinkSource()}
         * or {@link #setLinkSource(class_437)} must be called prior to invoking this method
         */
        @Override
        public boolean method_25430(@Nullable class_2583 style) {
            return super.method_25430(style);
        }

        static {
            WindowResizeCallback.EVENT.register((client, window) -> {
                if (INSTANCE == null) return;
                INSTANCE.method_25423(client, window.method_4486(), window.method_4502());
            });
        }
    }
}
