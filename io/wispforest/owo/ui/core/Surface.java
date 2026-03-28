package io.wispforest.owo.ui.core;

import D;
import F;
import I;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.client.OwoClient;
import io.wispforest.owo.mixin.ScreenAccessor;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.NinePatchTexture;
import java.util.List;
import net.minecraft.class_1041;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_293;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_766;
import net.minecraft.class_8002;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface Surface {

    Surface PANEL = (context, component) -> {
        context.drawPanel(component.x(), component.y(), component.width(), component.height(), false);
    };

    Surface DARK_PANEL = (context, component) -> {
        context.drawPanel(component.x(), component.y(), component.width(), component.height(), true);
    };

    Surface PANEL_INSET = (context, component) -> {
        NinePatchTexture.draw(OwoUIDrawContext.PANEL_INSET_NINE_PATCH_TEXTURE, context, component);
    };

    Surface VANILLA_TRANSLUCENT = (context, component) -> {
        context.drawGradientRect(
                component.x(), component.y(), component.width(), component.height(),
                0xC0101010, 0xC0101010, 0xD0101010, 0xD0101010
        );
    };

    Surface OPTIONS_BACKGROUND = Surface.panorama(ScreenAccessor.owo$ROTATING_PANORAMA_RENDERER(), false)
            .and(Surface.blur(5, 10));

    Surface TOOLTIP = (context, component) -> {
        context.method_51741(() -> {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            class_8002.method_47946(context, component.x() + 4, component.y() + 4, component.width() - 8, component.height() - 8, 0);
        });
    };

    static Surface blur(float quality, float size) {
        return (context, component) -> {
            var buffer = class_289.method_1348().method_60827(class_293.class_5596.field_27382, class_290.field_1592);
            var matrix = context.method_51448().method_23760().method_23761();

            buffer.method_22918(matrix, component.x(), component.y(), 0);
            buffer.method_22918(matrix, component.x(), component.y() + component.height(), 0);
            buffer.method_22918(matrix, component.x() + component.width(), component.y() + component.height(), 0);
            buffer.method_22918(matrix, component.x() + component.width(), component.y(), 0);

            OwoClient.BLUR_PROGRAM.setParameters(16, quality, size);
            OwoClient.BLUR_PROGRAM.use();
            class_286.method_43433(buffer.method_60800());
        };
    }

    static Surface vanillaPanorama(boolean alwaysVisible) {
        return panorama(new class_766(ScreenAccessor.owo$PANORAMA_RENDERER()), alwaysVisible);
    }

    static Surface panorama(class_766 renderer, boolean alwaysVisible) {
        return (context, component) -> {
            if (!alwaysVisible && class_310.method_1551().field_1687 != null) return;

            var client = class_310.method_1551();

            int prevX = GlStateManager.class_1040.method_35330();
            int prevY = GlStateManager.class_1040.method_35331();
            int prevWidth = GlStateManager.class_1040.method_35332();
            int prevHeight = GlStateManager.class_1040.method_35333();

            var window = client.method_22683();
            var scale = window.method_4495();

            var x = component.x();
            var y = component.y();
            var width = component.width();
            var height = component.height();

            RenderSystem.viewport(
                    (int) (x * scale),
                    (int) (window.method_4506() - (y * scale) - height * scale),
                    class_3532.method_15340((int) (width * scale), 0, window.method_4489()),
                    class_3532.method_15340((int) (height * scale), 0, window.method_4506())
            );

            var delta = client.method_60646().method_60638();

            RenderSystem.disableDepthTest();

            renderer.method_3317(context, width, height, 1.0F, delta);

            RenderSystem.enableDepthTest();

            RenderSystem.viewport(prevX, prevY, prevWidth, prevHeight);
        };
    }

    Surface BLANK = (context, component) -> {};

    static Surface flat(int color) {
        return (context, component) -> context.method_25294(component.x(), component.y(), component.x() + component.width(), component.y() + component.height(), color);
    }

    static Surface outline(int color) {
        return (context, component) -> context.drawRectOutline(component.x(), component.y(), component.width(), component.height(), color);
    }

    static Surface tiled(class_2960 texture, int textureWidth, int textureHeight) {
        return (context, component) -> {
            context.method_25290(texture, component.x(), component.y(), 0, 0, component.width(), component.height(), textureWidth, textureHeight);
        };
    }

    static Surface panelWithInset(int insetWidth) {
        return Surface.PANEL.and((context, component) -> {
            NinePatchTexture.draw(
                    OwoUIDrawContext.PANEL_INSET_NINE_PATCH_TEXTURE,
                    context,
                    component.x() + insetWidth,
                    component.y() + insetWidth,
                    component.width() - insetWidth * 2,
                    component.height() - insetWidth * 2
            );
        });
    }

    void draw(OwoUIDrawContext context, ParentComponent component);

    default Surface and(Surface surface) {
        return (context, component) -> {
            this.draw(context, component);
            surface.draw(context, component);
        };
    }

    static Surface parse(Element surfaceElement) {
        var children = UIParsing.<Element>allChildrenOfType(surfaceElement, Node.ELEMENT_NODE);
        var surface = BLANK;

        for (var child : children) {
            surface = switch (child.getNodeName()) {
                case "panel" -> surface.and(child.getAttribute("dark").equalsIgnoreCase("true")
                        ? DARK_PANEL
                        : PANEL);
                case "tiled" -> {
                    UIParsing.expectAttributes(child, "texture-width", "texture-height");
                    yield surface.and(tiled(
                            UIParsing.parseIdentifier(child),
                            UIParsing.parseUnsignedInt(child.getAttributeNode("texture-width")),
                            UIParsing.parseUnsignedInt(child.getAttributeNode("texture-height")))
                    );
                }
                case "blur" -> {
                    UIParsing.expectAttributes(child, "size", "quality");
                    yield surface.and(blur(
                            UIParsing.parseFloat(child.getAttributeNode("quality")),
                            UIParsing.parseFloat(child.getAttributeNode("size"))
                    ));
                }
                case "panel-with-inset" -> surface.and(panelWithInset(UIParsing.parseUnsignedInt(child)));
                case "options-background" -> surface.and(OPTIONS_BACKGROUND);
                case "vanilla-translucent" -> surface.and(VANILLA_TRANSLUCENT);
                case "panel-inset" -> surface.and(PANEL_INSET);
                case "tooltip" -> surface.and(TOOLTIP);
                case "outline" -> surface.and(outline(Color.parseAndPack(child)));
                case "flat" -> surface.and(flat(Color.parseAndPack(child)));
                default -> throw new UIModelParsingException("Unknown surface type '" + child.getNodeName() + "'");
            };
        }

        return surface;
    }
}
