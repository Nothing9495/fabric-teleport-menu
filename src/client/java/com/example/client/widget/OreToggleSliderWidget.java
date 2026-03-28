package com.example.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

public class OreToggleSliderWidget extends AbstractOreButtonWidget {
	private boolean enabled;
	private final Consumer<Boolean> onToggle;

	public OreToggleSliderWidget(int width, int height, boolean enabled, Consumer<Boolean> onToggle) {
		super(width, height, net.minecraft.network.chat.Component.empty());
		this.enabled = enabled;
		this.onToggle = onToggle;
	}

	@Override
	public void onPress() {
		this.enabled = !this.enabled;
		this.onToggle.accept(this.enabled);
	}

	@Override
	protected void drawContent(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, int width, int height, boolean hovered, boolean active) {
		int trackColor = this.enabled ? 0xAA2F9E44 : 0xAA7A2F2F;
		int knobColor = this.enabled ? 0xFFE5FFF0 : 0xFFFFE5E5;

		int trackX = x + 4;
		int trackY = y + 5;
		int trackW = Math.max(20, width - 8);
		int trackH = Math.max(10, height - 10);
		guiGraphics.fill(trackX, trackY, trackX + trackW, trackY + trackH, trackColor);

		int knobSize = trackH - 2;
		int knobX = this.enabled ? trackX + trackW - knobSize - 1 : trackX + 1;
		int knobY = trackY + 1;
		guiGraphics.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, knobColor);
	}
}
