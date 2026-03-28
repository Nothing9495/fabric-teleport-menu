package io.wispforest.owo.ui.component;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.util.pond.OwoEntityRenderDispatcherExtension;
import net.minecraft.class_1068;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_156;
import net.minecraft.class_1664;
import net.minecraft.class_2487;
import net.minecraft.class_2522;
import net.minecraft.class_2535;
import net.minecraft.class_2598;
import net.minecraft.class_2960;
import net.minecraft.class_308;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_634;
import net.minecraft.class_640;
import net.minecraft.class_746;
import net.minecraft.class_765;
import net.minecraft.class_7833;
import net.minecraft.class_7923;
import net.minecraft.class_7965;
import net.minecraft.class_7975;
import net.minecraft.class_8675;
import net.minecraft.class_8685;
import net.minecraft.class_898;
import net.minecraft.class_9782;
import net.minecraft.client.network.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

public class EntityComponent<E extends class_1297> extends BaseComponent {

    protected final class_898 dispatcher;
    protected final class_4597.class_4598 entityBuffers;
    protected final E entity;

    protected float mouseRotation = 0;
    protected float scale = 1;
    protected boolean lookAtCursor = false;
    protected boolean allowMouseRotation = false;
    protected boolean scaleToFit = false;
    protected boolean showNametag = false;
    protected Consumer<class_4587> transform = matrixStack -> {};

    protected EntityComponent(Sizing sizing, E entity) {
        final var client = class_310.method_1551();
        this.dispatcher = client.method_1561();
        this.entityBuffers = client.method_22940().method_23000();

        this.entity = entity;

        this.sizing(sizing);
    }

    @SuppressWarnings("DataFlowIssue")
    protected EntityComponent(Sizing sizing, class_1299<E> type, @Nullable class_2487 nbt) {
        final var client = class_310.method_1551();
        this.dispatcher = client.method_1561();
        this.entityBuffers = client.method_22940().method_23000();

        this.entity = type.method_5883(client.field_1687);
        if (nbt != null) entity.method_5651(nbt);
        entity.method_30634(client.field_1724.method_23317(), client.field_1724.method_23318(), client.field_1724.method_23321());

        this.sizing(sizing);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        var matrices = context.method_51448();
        matrices.method_22903();

        matrices.method_46416(x + this.width / 2f, y + this.height / 2f, 100);
        matrices.method_22905(75 * this.scale * this.width / 64f, -75 * this.scale * this.height / 64f, 75 * this.scale);

        matrices.method_46416(0, entity.method_17682() / -2f, 0);

        this.transform.accept(matrices);

        if (this.lookAtCursor) {
            float xRotation = (float) Math.toDegrees(Math.atan((mouseY - this.y - this.height / 2f) / 40f));
            float yRotation = (float) Math.toDegrees(Math.atan((mouseX - this.x - this.width / 2f) / 40f));

            if (this.entity instanceof class_1309 living) {
                living.field_6259 = -yRotation;
            }

            this.entity.field_5982 = -yRotation;
            this.entity.field_6004 = xRotation * .65f;

            // We make sure the xRotation never becomes 0, as the lighting otherwise becomes very unhappy
            if (xRotation == 0) xRotation = .1f;
            matrices.method_22907(class_7833.field_40714.rotationDegrees(xRotation * .15f));
            matrices.method_22907(class_7833.field_40716.rotationDegrees(yRotation * .15f));
        } else {
            matrices.method_22907(class_7833.field_40714.rotationDegrees(35));
            matrices.method_22907(class_7833.field_40716.rotationDegrees(-45 + this.mouseRotation));
        }

        var dispatcher = (OwoEntityRenderDispatcherExtension) this.dispatcher;
        dispatcher.owo$setCounterRotate(true);
        dispatcher.owo$setShowNametag(this.showNametag);

        RenderSystem.setShaderLights(new Vector3f(.15f, 1, 0), new Vector3f(.15f, -1, 0));
        this.dispatcher.method_3948(false);
        this.dispatcher.method_3954(this.entity, 0, 0, 0, 0, 0, matrices, this.entityBuffers, class_765.field_32767);
        this.dispatcher.method_3948(true);
        this.entityBuffers.method_22993();
        class_308.method_24211();

        matrices.method_22909();

        dispatcher.owo$setCounterRotate(false);
        dispatcher.owo$setShowNametag(true);
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        if (this.allowMouseRotation && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.mouseRotation += deltaX;

            super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
            return true;
        } else {
            return super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
        }
    }

    public E entity() {
        return this.entity;
    }

    public EntityComponent<E> allowMouseRotation(boolean allowMouseRotation) {
        this.allowMouseRotation = allowMouseRotation;
        return this;
    }

    public boolean allowMouseRotation() {
        return this.allowMouseRotation;
    }

    public EntityComponent<E> lookAtCursor(boolean lookAtCursor) {
        this.lookAtCursor = lookAtCursor;
        return this;
    }

    public boolean lookAtCursor() {
        return this.lookAtCursor;
    }

    public EntityComponent<E> scale(float scale) {
        this.scale = scale;
        return this;
    }

    public float scale() {
        return this.scale;
    }

    public EntityComponent<E> scaleToFit(boolean scaleToFit) {
        this.scaleToFit = scaleToFit;

        if (scaleToFit) {
            float xScale = .5f / entity.method_17681();
            float yScale = .5f / entity.method_17682();

            this.scale(Math.min(xScale, yScale));
        }

        return this;
    }

    public boolean scaleToFit() {
        return this.scaleToFit;
    }

    public EntityComponent<E> transform(Consumer<class_4587> transform) {
        this.transform = transform;
        return this;
    }

    public Consumer<class_4587> transform() {
        return transform;
    }

    public EntityComponent<E> showNametag(boolean showNametag) {
        this.showNametag = showNametag;
        return this;
    }

    public boolean showNametag() {
        return showNametag;
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return source == FocusSource.MOUSE_CLICK;
    }

    public static RenderablePlayerEntity createRenderablePlayer(GameProfile profile) {
        return new RenderablePlayerEntity(profile);
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.apply(children, "scale", UIParsing::parseFloat, this::scale);
        UIParsing.apply(children, "look-at-cursor", UIParsing::parseBool, this::lookAtCursor);
        UIParsing.apply(children, "mouse-rotation", UIParsing::parseBool, this::allowMouseRotation);
        UIParsing.apply(children, "scale-to-fit", UIParsing::parseBool, this::scaleToFit);
    }

    public static EntityComponent<?> parse(Element element) {
        UIParsing.expectAttributes(element, "type");
        var entityId = UIParsing.parseIdentifier(element.getAttributeNode("type"));
        var entityType = class_7923.field_41177.method_17966(entityId).orElseThrow(() -> new UIModelParsingException("Unknown entity type " + entityId));

        class_2487 nbt = null;
        if (element.hasAttribute("nbt")) {
            try {
                nbt = class_2522.method_10718(element.getAttribute("nbt"));
            } catch (CommandSyntaxException cse) {
                throw new UIModelParsingException("Invalid NBT compound", cse);
            }
        }

        return new EntityComponent<>(Sizing.content(), entityType, nbt);
    }

    public static class RenderablePlayerEntity extends class_746 {

        protected class_8685 skinTextures;

        protected RenderablePlayerEntity(GameProfile profile) {
            super(class_310.method_1551(),
                    class_310.method_1551().field_1687,
                    new class_634(class_310.method_1551(),
                            new class_2535(class_2598.field_11942),
                            new class_8675(
                                    profile, new class_7975(class_7965.field_41434, false, Duration.ZERO, ""),
                                    class_310.method_1551().field_1687.method_30349().method_40316(),
                                    class_310.method_1551().field_1687.method_45162(),
                                    "Wisp Forest Enterprises", null, null, Map.of(), null, false, Map.of(), class_9782.field_51977
                    )),
                    null, null, false, false
            );

            this.skinTextures = class_1068.method_4648(profile.getId());
            class_156.method_18349().execute(() -> {
                var completeProfile = class_310.method_1551().method_1495().fetchProfile(profile.getId(), false).profile();

                this.skinTextures = class_1068.method_52854(completeProfile);
                this.field_3937.method_1582().method_52863(completeProfile).thenAccept(textures -> {
                    this.skinTextures = textures;
                });
            });
        }

        @Override
        public class_8685 method_52814() {
            return this.skinTextures;
        }

        @Override
        public boolean method_7348(class_1664 modelPart) {
            return true;
        }

        @Nullable
        @Override
        protected class_640 method_3123() {
            return null;
        }
    }
}
