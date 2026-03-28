package io.wispforest.owo.compat.rei;

import J;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.mixin.itemgroup.CreativeInventoryScreenAccessor;
import io.wispforest.owo.mixin.ui.access.BaseOwoHandledScreenAccessor;
import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.util.ScissorStack;
import io.wispforest.owo.util.pond.OwoCreativeInventoryScreenExtensions;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.client.registry.screen.OverlayRendererProvider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.class_1761;
import net.minecraft.class_437;
import net.minecraft.class_481;
import net.minecraft.class_7833;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fStack;
import java.util.ArrayList;
import java.util.Collections;

public class OwoReiPlugin implements REIClientPlugin {

    @SuppressWarnings("UnstableApiUsage")
    private static @Nullable OverlayRendererProvider.Sink renderSink = null;

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(class_481.class, screen -> {
            var group = CreativeInventoryScreenAccessor.owo$getSelectedTab();
            if (!(group instanceof OwoItemGroup owoGroup)) return Collections.emptySet();
            if (owoGroup.getButtons().isEmpty()) return Collections.emptySet();

            int x = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootX();
            int y = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootY();

            int stackHeight = owoGroup.getButtonStackHeight();
            y -= 13 * (stackHeight - 4);

            final var rectangles = new ArrayList<Rectangle>();
            for (int i = 0; i < owoGroup.getButtons().size(); i++) {
                int xOffset = x + 198 + (i / stackHeight) * 26;
                int yOffset = y + 10 + (i % stackHeight) * 30;
                rectangles.add(new Rectangle(xOffset, yOffset, 24, 24));
            }

            return rectangles;
        });

        zones.register(BaseOwoHandledScreen.class, screen -> {
            return ((BaseOwoHandledScreen<?, ?>) screen).componentsForExclusionAreas()
                    .map(rect -> new Rectangle(rect.x(), rect.y(), rect.width(), rect.height()))
                    .toList();
        });
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerDecider(new OverlayDecider() {
            @Override
            public <R extends class_437> boolean isHandingScreen(Class<R> screen) {
                return BaseOwoHandledScreen.class.isAssignableFrom(screen);
            }

            @Override
            @SuppressWarnings("UnstableApiUsage")
            public OverlayRendererProvider getRendererProvider() {
                return new OverlayRendererProvider() {
                    @Override
                    public void onApplied(Sink sink) {
                        renderSink = sink;
                    }

                    @Override
                    public void onRemoved() {
                        renderSink = null;
                    }
                };
            }
        });
    }

    static {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof BaseOwoHandledScreenAccessor accessor)) return;

            ScreenEvents.beforeRender(screen).register(($, context, mouseX, mouseY, tickDelta) -> {
                var root = accessor.owo$getUIAdapter().rootComponent;

                CallbackSurface surface;
                if (root.surface() instanceof CallbackSurface wrapped) {
                    surface = wrapped;
                } else {
                    surface = new CallbackSurface(root.surface());
                    root.surface(surface);
                }

                surface.callback = () -> {
                    if (renderSink == null) return;
                    renderOverlay($, () -> renderSink.render(context, mouseX, mouseY, tickDelta));
                };
            });

            ScreenEvents.afterRender(screen).register(($, matrices, mouseX, mouseY, tickDelta) -> {
                if (renderSink == null) return;
                renderOverlay($, () -> renderSink.lateRender(matrices, mouseX, mouseY, tickDelta));
            });
        });
    }

    private static void renderOverlay(class_437 screen, Runnable renderFunction) {
        if (REIRuntime.getInstance().getSearchTextField().getText().equals("froge")) {
            var modelView = RenderSystem.getModelViewStack();

            final var time = System.currentTimeMillis();
            float scale = .75f + (float) (Math.sin(time / 500d) * .5f);
            modelView.pushMatrix();
            modelView.translate(screen.field_22789 / 2f - scale / 2f * screen.field_22789, screen.field_22790 / 2f - scale / 2f * screen.field_22790, 0);
            modelView.scale(scale, scale, 1f);
            modelView.translate((float) (Math.sin(time / 1000d) * .75f) * screen.field_22789, (float) (Math.sin(time / 500d) * .75f) * screen.field_22790, 0);

            modelView.translate(screen.field_22789 / 2f, screen.field_22790 / 2f, 0);
            modelView.rotate(class_7833.field_40718.rotationDegrees((float) (time / 25d % 360d)));
            modelView.translate(screen.field_22789 / -2f, screen.field_22790 / -2f, 0);

            for (int i = 0; i < 20; i++) {
                modelView.pushMatrix();
                modelView.translate(screen.field_22789 / 2f, screen.field_22790 / 2f, 0);
                modelView.rotate(class_7833.field_40718.rotationDegrees(i * 18));
                modelView.translate(screen.field_22789 / -2f, screen.field_22790 / -2f, 0);

                RenderSystem.applyModelViewMatrix();
                ScissorStack.pushDirect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
                renderFunction.run();
                GlStateManager._enableScissorTest();
                ScissorStack.pop();
                modelView.popMatrix();
            }

            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
        } else {
            ScissorStack.pushDirect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            renderFunction.run();
            GlStateManager._enableScissorTest();
            ScissorStack.pop();
        }
    }

    private static class CallbackSurface implements Surface {
        public final Surface inner;
        public @NotNull Runnable callback = () -> {};

        private CallbackSurface(Surface inner) {
            this.inner = inner;
        }

        @Override
        public void draw(OwoUIDrawContext context, ParentComponent component) {
            this.inner.draw(context, component);
            this.callback.run();
        }
    }
}
