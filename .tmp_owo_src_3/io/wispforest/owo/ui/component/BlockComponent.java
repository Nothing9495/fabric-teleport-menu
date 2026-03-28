package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.mixin.ui.access.BlockEntityAccessor;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.class_2259;
import net.minecraft.class_2259.class_7211;
import net.minecraft.class_2464;
import net.minecraft.class_2487;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_308;
import net.minecraft.class_310;
import net.minecraft.class_4597.class_4598;
import net.minecraft.class_4608;
import net.minecraft.class_638;
import net.minecraft.class_765;
import net.minecraft.class_7833;
import net.minecraft.class_7923;
import net.minecraft.class_827;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.w3c.dom.Element;

public class BlockComponent extends BaseComponent {

    private final class_310 client = class_310.method_1551();

    private final class_2680 state;
    private final @Nullable class_2586 entity;

    protected BlockComponent(class_2680 state, @Nullable class_2586 entity) {
        this.state = state;
        this.entity = entity;
    }

    @Override
    @SuppressWarnings("NonAsciiCharacters")
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        context.method_51448().method_22903();

        context.method_51448().method_46416(x + this.width / 2f, y + this.height / 2f, 100);
        context.method_51448().method_22905(40 * this.width / 64f, -40 * this.height / 64f, 40);

        context.method_51448().method_22907(class_7833.field_40714.rotationDegrees(30));
        context.method_51448().method_22907(class_7833.field_40716.rotationDegrees(45 + 180));

        context.method_51448().method_22904(-.5, -.5, -.5);

        RenderSystem.runAsFancy(() -> {
            final var vertexConsumers = client.method_22940().method_23000();
            if (this.state.method_26217() != class_2464.field_11456) {
                this.client.method_1541().method_3353(
                        this.state, context.method_51448(), vertexConsumers,
                        class_765.field_32767, class_4608.field_21444
                );
            }

            if (this.entity != null) {
                var медведь = this.client.method_31975().method_3550(this.entity);
                if (медведь != null) {
                    медведь.method_3569(entity, partialTicks, context.method_51448(), vertexConsumers, class_765.field_32767, class_4608.field_21444);
                }
            }

            RenderSystem.setShaderLights(new Vector3f(-1.5f, -.5f, 0), new Vector3f(0, -1, 0));
            vertexConsumers.method_22993();
            class_308.method_24211();
        });

        context.method_51448().method_22909();
    }

    protected static void prepareBlockEntity(class_2680 state, class_2586 blockEntity, @Nullable class_2487 nbt) {
        if (blockEntity == null) return;

        var world = class_310.method_1551().field_1687;

        ((BlockEntityAccessor) blockEntity).owo$setCachedState(state);
        blockEntity.method_31662(world);

        if (nbt == null) return;

        final var nbtCopy = nbt.method_10553();

        nbtCopy.method_10569("x", 0);
        nbtCopy.method_10569("y", 0);
        nbtCopy.method_10569("z", 0);

        blockEntity.method_58690(nbtCopy, world.method_30349());
    }

    public static BlockComponent parse(Element element) {
        UIParsing.expectAttributes(element, "state");

        try {
            var result = class_2259.method_41957(class_7923.field_41175.method_46771(), element.getAttribute("state"), true);
            return Components.block(result.comp_622(), result.comp_624());
        } catch (CommandSyntaxException cse) {
            throw new UIModelParsingException("Invalid block state", cse);
        }
    }
}
