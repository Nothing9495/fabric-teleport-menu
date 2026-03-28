package io.wispforest.owo.ui.component;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1836;
import net.minecraft.class_2291;
import net.minecraft.class_2291.class_7215;
import net.minecraft.class_2561;
import net.minecraft.class_308;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_5684;
import net.minecraft.class_7225;
import net.minecraft.class_765;
import net.minecraft.class_7923;
import net.minecraft.class_811;
import net.minecraft.class_918;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class ItemComponent extends BaseComponent {

    protected static final Matrix4f ITEM_SCALING = new Matrix4f().scaling(16, -16, 16);

    protected final class_4597.class_4598 entityBuffers;
    protected final class_918 itemRenderer;
    protected class_1799 stack;
    protected boolean showOverlay = false;
    protected boolean setTooltipFromStack = false;

    protected ItemComponent(class_1799 stack) {
        this.entityBuffers = class_310.method_1551().method_22940().method_23000();
        this.itemRenderer = class_310.method_1551().method_1480();
        this.stack = stack;
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
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        final boolean notSideLit = !this.itemRenderer.method_4019(this.stack, null, null, 0).method_24304();
        if (notSideLit) {
            class_308.method_24210();
        }

        var matrices = context.method_51448();
        matrices.method_22903();

        // Translate to the root of the component
        matrices.method_46416(this.x, this.y, 100);

        // Scale according to component size and translate to the center
        matrices.method_22905(this.width / 16f, this.height / 16f, 1);
        matrices.method_22904(8.0, 8.0, 0.0);

        // Vanilla scaling and y inversion
        if (notSideLit) {
            matrices.method_22905(16, -16, 16);
        } else {
            matrices.method_34425(ITEM_SCALING);
        }

        var client = class_310.method_1551();

        this.itemRenderer.method_23178(this.stack, class_811.field_4317, class_765.field_32767, class_4608.field_21444, matrices, entityBuffers, client.field_1687, 0);
        this.entityBuffers.method_22993();

        // Clean up
        matrices.method_22909();

        if (this.showOverlay) {
            context.method_51431(client.field_1772, this.stack, this.x, this.y);
        }
        if (notSideLit) {
            class_308.method_24211();
        }
    }

    protected void updateTooltipForStack() {
        if (!this.setTooltipFromStack) return;

        if (!this.stack.method_7960()) {
            class_310 client = class_310.method_1551();
            this.tooltip(tooltipFromItem(this.stack, class_1792.class_9635.method_59528(client.field_1687), client.field_1724, null));
        } else {
            this.tooltip((List<class_5684>) null);
        }
    }

    public ItemComponent setTooltipFromStack(boolean setTooltipFromStack) {
        this.setTooltipFromStack = setTooltipFromStack;
        this.updateTooltipForStack();

        return this;
    }

    public boolean setTooltipFromStack() {
        return setTooltipFromStack;
    }

    public ItemComponent stack(class_1799 stack) {
        this.stack = stack;
        this.updateTooltipForStack();

        return this;
    }

    public class_1799 stack() {
        return this.stack;
    }

    public ItemComponent showOverlay(boolean drawOverlay) {
        this.showOverlay = drawOverlay;
        return this;
    }

    public boolean showOverlay() {
        return this.showOverlay;
    }

    /**
     * Obtain the full item stack tooltip, including custom components
     * provided via {@link net.minecraft.class_1792#method_32346(class_1799)}
     *
     * @param stack   The item stack from which to obtain the tooltip
     * @param context the tooltip context
     * @param player  The player to use for context, may be {@code null}
     * @param type    The tooltip type - {@code null} to fall back to the default provided by
     *                {@link net.minecraft.class_315#field_1827}
     */
    public static List<class_5684> tooltipFromItem(class_1799 stack, class_1792.class_9635 context, @Nullable class_1657 player, @Nullable class_1836 type) {
        if (type == null) {
            type = class_310.method_1551().field_1690.field_1827 ? class_1836.field_41071 : class_1836.field_41070;
        }

        var tooltip = new ArrayList<class_5684>();
        stack.method_7950(context, player, type)
                .stream()
                .map(class_2561::method_30937)
                .map(class_5684::method_32662)
                .forEach(tooltip::add);

        stack.method_32347().ifPresent(data -> {
            tooltip.add(1, Objects.requireNonNullElseGet(
                    TooltipComponentCallback.EVENT.invoker().getComponent(data),
                    () -> class_5684.method_32663(data)
            ));
        });

        return tooltip;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "show-overlay", UIParsing::parseBool, this::showOverlay);
        UIParsing.apply(children, "set-tooltip-from-stack", UIParsing::parseBool, this::setTooltipFromStack);

        UIParsing.apply(children, "item", UIParsing::parseIdentifier, itemId -> {
            Owo.debugWarn(Owo.LOGGER, "Deprecated <item> property populated on item component - migrate to <stack> instead");

            var item = class_7923.field_41178.method_17966(itemId).orElseThrow(() -> new UIModelParsingException("Unknown item " + itemId));
            this.stack(item.method_7854());
        });

        UIParsing.apply(children, "stack", $ -> $.getTextContent().strip(), stackString -> {
            try {
                var result = new class_2291(class_7225.class_7874.method_46761(Stream.of(class_7923.field_41178.method_46771())))
                    .method_9789(new StringReader(stackString));

                var stack = new class_1799(result.comp_628());
                stack.method_59692(result.comp_2439());

                this.stack(stack);
            } catch (CommandSyntaxException cse) {
                throw new UIModelParsingException("Invalid item stack", cse);
            }
        });
    }
}
