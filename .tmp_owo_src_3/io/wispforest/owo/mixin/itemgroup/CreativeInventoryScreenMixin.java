package io.wispforest.owo.mixin.itemgroup;

import I;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.OwoItemGroup.TabTextures;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupButtonWidget;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.util.CursorAdapter;
import io.wispforest.owo.util.pond.OwoCreativeInventoryScreenExtensions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1761;
import net.minecraft.class_1761.class_8128;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_481;
import net.minecraft.class_485;
import net.minecraft.class_746;
import net.minecraft.class_7699;

@Mixin(class_481.class)
public abstract class CreativeInventoryScreenMixin extends class_485<class_481.class_483> implements OwoCreativeInventoryScreenExtensions {

    @Shadow
    private static class_1761 selectedTab;

    @Shadow
    protected abstract void method_25426();

    @Shadow
    protected abstract boolean shouldShowOperatorTab(class_1657 player);

    @Shadow
    protected abstract boolean hasScrollbar();

    @Unique
    private final List<ItemGroupButtonWidget> owoButtons = new ArrayList<>();

    @Unique
    private class_7699 enabledFeatures = null;

    @Unique
    private final CursorAdapter cursorAdapter = CursorAdapter.ofClientWindow();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void captureFeatures(class_746 player, class_7699 enabledFeatures, boolean operatorTabEnabled, CallbackInfo ci) {
        this.enabledFeatures = enabledFeatures;
    }

    // ----------
    // Background
    // ----------

    @ModifyArg(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 0))
    private class_2960 injectCustomGroupTexture(class_2960 original) {
        if (!(selectedTab instanceof OwoItemGroup owoGroup) || owoGroup.getBackgroundTexture() == null) return original;
        return owoGroup.getBackgroundTexture();
    }

    // ----------------
    // Scrollbar slider
    // ----------------

    @ModifyArgs(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void injectCustomScrollbarTexture(Args args) {
        if (!(selectedTab instanceof OwoItemGroup owoGroup) || owoGroup.getScrollerTextures() == null) return;

        args.set(0, this.hasScrollbar() ? owoGroup.getScrollerTextures().enabled() : owoGroup.getScrollerTextures().disabled());
    }

    // -------------
    // Group headers
    // -------------

    @ModifyArg(method = "renderTabIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private class_2960 injectCustomTabTexture(class_2960 texture, @Local(argsOnly = true) class_1761 group) {
        if(!(group instanceof OwoItemGroup contextGroup) || contextGroup.getTabTextures() == null) return texture;

        var textures = contextGroup.getTabTextures();
        return contextGroup.method_47309() == class_1761.class_7915.field_41049
                ? selectedTab == contextGroup ? contextGroup.method_7743() == 0 ? textures.topSelectedFirstColumn() : textures.topSelected() : textures.topUnselected()
                : selectedTab == contextGroup ? contextGroup.method_7743() == 0 ? textures.bottomSelectedFirstColumn() : textures.bottomSelected() : textures.bottomUnselected();
    }

    @Inject(method = "renderTabIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;getIcon()Lnet/minecraft/item/ItemStack;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void renderOwoIcon(class_332 context, class_1761 group, CallbackInfo ci, boolean bl, boolean bl2, int i, int j, int k) {
        if (!(group instanceof OwoItemGroup owoGroup)) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        owoGroup.icon().render(context, j, k, 0, 0, 0);
        RenderSystem.disableBlend();
    }

    // -------------
    // oωo tab title
    // -------------

    @ModifyArg(method = "drawForeground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I"))
    private class_2561 injectTabNameAsTitle(class_2561 original) {
        if (!(selectedTab instanceof OwoItemGroup owoGroup) || !owoGroup.hasDynamicTitle() || owoGroup.selectedTabs().size() != 1) {
            return original;
        }

        var singleActiveTab = owoGroup.getTab(owoGroup.selectedTabs().iterator().nextInt());
        if (singleActiveTab.primary()) {
            return singleActiveTab.name();
        } else {
            return class_2561.method_43469(
                    "text.owo.itemGroup.tab_template",
                    owoGroup.method_7737(),
                    singleActiveTab.name()
            );
        }
    }

    // ---------------
    // oωo tab buttons
    // ---------------

    @Inject(at = @At("HEAD"), method = "setSelectedTab(Lnet/minecraft/item/ItemGroup;)V")
    private void setSelectedTab(class_1761 group, CallbackInfo ci) {
        this.owoButtons.forEach(this::method_37066);
        this.owoButtons.clear();

        if (group instanceof OwoItemGroup owoGroup) {
            int tabRootY = this.field_2800;

            final var tabStackHeight = owoGroup.getTabStackHeight();
            tabRootY -= 13 * (tabStackHeight - 4);

            if (owoGroup.shouldDisplaySingleTab() || owoGroup.tabs.size() > 1) {
                for (int tabIdx = 0; tabIdx < owoGroup.tabs.size(); tabIdx++) {
                    var tab = owoGroup.tabs.get(tabIdx);

                    int xOffset = this.field_2776 - 27 - (tabIdx / tabStackHeight) * 26;
                    int yOffset = tabRootY + 10 + (tabIdx % tabStackHeight) * 30;

                    var tabButton = new ItemGroupButtonWidget(xOffset, yOffset, 32, tab, owo$createSelectAction(owoGroup, tabIdx));
                    if (owoGroup.isTabSelected(tabIdx)) tabButton.isSelected = true;

                    this.owoButtons.add(tabButton);
                    this.method_37063(tabButton);
                }
            }

            final var buttonStackHeight = owoGroup.getButtonStackHeight();
            tabRootY = this.field_2800 - 13 * (buttonStackHeight - 4);

            var buttons = owoGroup.getButtons();
            for (int i = 0; i < buttons.size(); i++) {
                var buttonDefinition = buttons.get(i);

                int xOffset = this.field_2776 + 198 + (i / buttonStackHeight) * 26;
                int yOffset = tabRootY + 10 + (i % buttonStackHeight) * 30;

                var tabButton = new ItemGroupButtonWidget(xOffset, yOffset, 0, buttonDefinition, __ -> buttonDefinition.action().run());

                this.owoButtons.add(tabButton);
                this.method_37063(tabButton);
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void render(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean anyButtonHovered = false;

        for (var button : this.owoButtons) {
            if (button.trulyHovered()) {
                context.method_51434(
                        this.field_22793,
                        button.isTab() && ((OwoItemGroup) selectedTab).canSelectMultipleTabs()
                                ? List.of(button.method_25369(), class_2561.method_43471("text.owo.itemGroup.select_hint"))
                                : List.of(button.method_25369()),
                        mouseX,
                        mouseY
                );
                anyButtonHovered = true;
            }
        }

        this.cursorAdapter.applyStyle(anyButtonHovered ? CursorStyle.HAND : CursorStyle.NONE);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void disposeCursorAdapter(CallbackInfo ci) {
        this.cursorAdapter.dispose();
    }

    @Override
    public int owo$getRootX() {
        return this.field_2776;
    }

    @Override
    public int owo$getRootY() {
        return this.field_2800;
    }

    @Unique
    private Consumer<ItemGroupButtonWidget> owo$createSelectAction(OwoItemGroup group, int tabIdx) {
        return button -> {
            var context = new class_1761.class_8128(this.enabledFeatures, this.shouldShowOperatorTab(this.field_2797.player()), this.field_2797.player().method_37908().method_30349());
            if (class_437.method_25442()) {
                group.toggleTab(tabIdx, context);
            } else {
                group.selectSingleTab(tabIdx, context);
            }

            this.method_41843();
            button.isSelected = true;
        };
    }

    public CreativeInventoryScreenMixin(class_481.class_483 screenHandler, class_1661 playerInventory, class_2561 text) {
        super(screenHandler, playerInventory, text);
    }
}