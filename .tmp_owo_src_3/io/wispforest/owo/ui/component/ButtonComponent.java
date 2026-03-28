package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.mixin.ui.access.ButtonWidgetAccessor;
import io.wispforest.owo.mixin.ui.access.ClickableWidgetAccessor;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.NinePatchTexture;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_8001;
import net.minecraft.class_9110;

public class ButtonComponent extends class_4185 {

    public static final class_2960 ACTIVE_TEXTURE = class_2960.method_60655("owo", "button/active");
    public static final class_2960 HOVERED_TEXTURE = class_2960.method_60655("owo", "button/hovered");
    public static final class_2960 DISABLED_TEXTURE = class_2960.method_60655("owo", "button/disabled");

    protected Renderer renderer = Renderer.VANILLA;
    protected boolean textShadow = true;

    protected ButtonComponent(class_2561 message, Consumer<ButtonComponent> onPress) {
        super(0, 0, 0, 0, message, button -> onPress.accept((ButtonComponent) button), class_4185.field_40754);
        this.sizing(Sizing.content());
    }

    @Override
    public void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
        this.renderer.draw((OwoUIDrawContext) context, this, delta);

        var textRenderer = class_310.method_1551().field_1772;
        int color = this.field_22763 ? 0xffffff : 0xa0a0a0;

        if (this.textShadow) {
            context.method_27534(textRenderer, this.method_25369(), this.method_46426() + this.field_22758 / 2, this.method_46427() + (this.field_22759 - 8) / 2, color);
        } else {
            context.method_51439(textRenderer, this.method_25369(), (int) (this.method_46426() + this.field_22758 / 2f - textRenderer.method_27525(this.method_25369()) / 2f), (int) (this.method_46427() + (this.field_22759 - 8) / 2f), color, false);
        }

        var tooltip = ((ClickableWidgetAccessor) this).owo$getTooltip();
        if (this.field_22762 && tooltip.method_56137() != null)
            context.method_51436(textRenderer, tooltip.method_56137().method_47405(class_310.method_1551()), class_8001.field_41687, mouseX, mouseY);
    }

    public ButtonComponent onPress(Consumer<ButtonComponent> onPress) {
        ((ButtonWidgetAccessor) this).owo$setOnPress(button -> onPress.accept((ButtonComponent) button));
        return this;
    }

    public ButtonComponent renderer(Renderer renderer) {
        this.renderer = renderer;
        return this;
    }

    public Renderer renderer() {
        return this.renderer;
    }

    public ButtonComponent textShadow(boolean textShadow) {
        this.textShadow = textShadow;
        return this;
    }

    public boolean textShadow() {
        return this.textShadow;
    }

    public ButtonComponent active(boolean active) {
        this.field_22763 = active;
        return this;
    }

    public boolean active() {
        return this.field_22763;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "text", UIParsing::parseText, this::method_25355);
        UIParsing.apply(children, "text-shadow", UIParsing::parseBool, this::textShadow);
        UIParsing.apply(children, "renderer", Renderer::parse, this::renderer);
    }

    protected CursorStyle owo$preferredCursorStyle() {
        return CursorStyle.HAND;
    }

    @FunctionalInterface
    public interface Renderer {
        Renderer VANILLA = (matrices, button, delta) -> {
            RenderSystem.enableDepthTest();

            var texture = button.field_22763
                    ? button.field_22762 ? HOVERED_TEXTURE : ACTIVE_TEXTURE
                    : DISABLED_TEXTURE;
            NinePatchTexture.draw(texture, matrices, button.method_46426(), button.method_46427(), button.field_22758, button.field_22759);
        };

        static Renderer flat(int color, int hoveredColor, int disabledColor) {
            return (context, button, delta) -> {
                RenderSystem.enableDepthTest();

                if (button.field_22763) {
                    if (button.field_22762) {
                        context.method_25294(button.method_46426(), button.method_46427(), button.method_46426() + button.field_22758, button.method_46427() + button.field_22759, hoveredColor);
                    } else {
                        context.method_25294(button.method_46426(), button.method_46427(), button.method_46426() + button.field_22758, button.method_46427() + button.field_22759, color);
                    }
                } else {
                    context.method_25294(button.method_46426(), button.method_46427(), button.method_46426() + button.field_22758, button.method_46427() + button.field_22759, disabledColor);
                }
            };
        }

        static Renderer texture(class_2960 texture, int u, int v, int textureWidth, int textureHeight) {
            return (context, button, delta) -> {
                int renderV = v;
                if (!button.field_22763) {
                    renderV += button.field_22759 * 2;
                } else if (button.method_49606()) {
                    renderV += button.field_22759;
                }

                RenderSystem.enableDepthTest();
                context.method_25290(texture, button.method_46426(), button.method_46427(), u, renderV, button.field_22758, button.field_22759, textureWidth, textureHeight);
            };
        }

        void draw(OwoUIDrawContext context, ButtonComponent button, float delta);

        static Renderer parse(Element element) {
            var children = UIParsing.<Element>allChildrenOfType(element, Node.ELEMENT_NODE);
            if (children.size() > 1)
                throw new UIModelParsingException("'renderer' declaration may only contain a single child");

            var rendererElement = children.get(0);
            return switch (rendererElement.getNodeName()) {
                case "vanilla" -> VANILLA;
                case "flat" -> {
                    UIParsing.expectAttributes(rendererElement, "color", "hovered-color", "disabled-color");
                    yield flat(
                            Color.parseAndPack(rendererElement.getAttributeNode("color")),
                            Color.parseAndPack(rendererElement.getAttributeNode("hovered-color")),
                            Color.parseAndPack(rendererElement.getAttributeNode("disabled-color"))
                    );
                }
                case "texture" -> {
                    UIParsing.expectAttributes(rendererElement, "texture", "u", "v", "texture-width", "texture-height");
                    yield texture(
                            UIParsing.parseIdentifier(rendererElement.getAttributeNode("texture")),
                            UIParsing.parseUnsignedInt(rendererElement.getAttributeNode("u")),
                            UIParsing.parseUnsignedInt(rendererElement.getAttributeNode("v")),
                            UIParsing.parseUnsignedInt(rendererElement.getAttributeNode("texture-width")),
                            UIParsing.parseUnsignedInt(rendererElement.getAttributeNode("texture-height"))
                    );
                }
                default ->
                        throw new UIModelParsingException("Unknown button renderer '" + rendererElement.getNodeName() + "'");
            };
        }
    }
}
