package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.SpriteUtilInvoker;
import org.w3c.dom.Element;

import java.util.Map;
import net.minecraft.class_1058;
import net.minecraft.class_2960;
import net.minecraft.class_4730;

public class SpriteComponent extends BaseComponent {

    protected final class_1058 sprite;
    protected boolean blend = false;

    protected SpriteComponent(class_1058 sprite) {
        this.sprite = sprite;
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return this.sprite.method_45851().method_45807();
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return this.sprite.method_45851().method_45815();
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        SpriteUtilInvoker.markSpriteActive(this.sprite);

        if (this.blend) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }

        context.method_25298(this.x, this.y, 0, this.width, this.height, this.sprite);

        if (this.blend) {
            RenderSystem.disableBlend();
        }
    }

    public SpriteComponent blend(boolean blend) {
        this.blend = blend;
        return this;
    }

    public boolean blend() {
        return this.blend;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "blend", UIParsing::parseBool, this::blend);
    }
    public static SpriteComponent parse(Element element) {
        UIParsing.expectAttributes(element, "atlas", "sprite");

        var atlas = UIParsing.parseIdentifier(element.getAttributeNode("atlas"));
        var sprite = UIParsing.parseIdentifier(element.getAttributeNode("sprite"));

        return Components.sprite(new class_4730(atlas, sprite));
    }
}
