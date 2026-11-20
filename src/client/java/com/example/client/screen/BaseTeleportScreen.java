package com.example.client.screen;

import com.example.client.widget.OreMenuButtonWidget;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

abstract class BaseTeleportScreen extends BaseOwoScreen<FlowLayout> {
	protected static final int MAIN_BUTTON_WIDTH = 248;
	protected static final int BUTTON_HEIGHT = 24;
	protected static final int PLAYER_ENTRY_HEIGHT = 36;

	protected BaseTeleportScreen(Component title) {
		super(title);
	}

	@Override
	protected OwoUIAdapter<FlowLayout> createAdapter() {
		return OwoUIAdapter.create(this, Containers::verticalFlow);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		this.renderBlurredBackground(delta);
		this.renderTransparentBackground(guiGraphics);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected void configureRoot(FlowLayout rootComponent) {
		rootComponent.sizing(Sizing.fill(100), Sizing.fill(100));
		rootComponent.padding(Insets.of(8));
		rootComponent.gap(10);
		rootComponent.horizontalAlignment(HorizontalAlignment.CENTER);
		rootComponent.verticalAlignment(VerticalAlignment.CENTER);
	}

	protected io.wispforest.owo.ui.core.Component menuButton(Component text, ItemStack icon, Runnable onPress) {
		OreMenuButtonWidget widget = new OreMenuButtonWidget(MAIN_BUTTON_WIDTH, BUTTON_HEIGHT, text, icon, button -> onPress.run());
		var wrapped = Components.wrapVanillaWidget(widget);
		wrapped.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(BUTTON_HEIGHT));
		return wrapped;
	}

	protected io.wispforest.owo.ui.core.Component menuButton(Component text, ResourceLocation icon, Runnable onPress) {
		OreMenuButtonWidget widget = new OreMenuButtonWidget(MAIN_BUTTON_WIDTH, BUTTON_HEIGHT, text, icon, button -> onPress.run());
		var wrapped = Components.wrapVanillaWidget(widget);
		wrapped.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(BUTTON_HEIGHT));
		return wrapped;
	}
}
