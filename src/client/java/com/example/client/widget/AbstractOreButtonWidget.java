package com.example.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

abstract class AbstractOreButtonWidget extends AbstractButton {
	private static final int BG_IDLE_TOP = 0x33202A35;
	private static final int BG_IDLE_BOTTOM = 0x33243240;
	private static final int BG_HOVER_TOP = 0x6638485D;
	private static final int BG_HOVER_BOTTOM = 0x66425773;
	private static final int BG_DISABLED_TOP = 0x22181C23;
	private static final int BG_DISABLED_BOTTOM = 0x22181C23;

	protected AbstractOreButtonWidget(int width, int height, Component message) {
		super(0, 0, width, height, message);
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		int x = this.getX();
		int y = this.getY();
		int width = this.getWidth();
		int height = this.getHeight();
		boolean hovered = this.isHoveredOrFocused();

		int topColor;
		int bottomColor;
		if (!this.active) {
			topColor = BG_DISABLED_TOP;
			bottomColor = BG_DISABLED_BOTTOM;
		} else if (hovered) {
			topColor = BG_HOVER_TOP;
			bottomColor = BG_HOVER_BOTTOM;
		} else {
			topColor = BG_IDLE_TOP;
			bottomColor = BG_IDLE_BOTTOM;
		}

		guiGraphics.fillGradient(x, y, x + width, y + height, topColor, bottomColor);
		if (hovered && this.active) {
			guiGraphics.fill(x, y, x + width, y + 1, 0x90FFFFFF);
		}

		this.drawContent(guiGraphics, Minecraft.getInstance(), x, y, width, height, hovered, this.active);
	}

	protected int titleColor(boolean active, boolean hovered) {
		if (!active) {
			return 0x8D98A6;
		}

		return hovered ? 0xF6FAFF : 0xEAF1FF;
	}

	protected abstract void drawContent(
		GuiGraphics guiGraphics,
		Minecraft minecraft,
		int x,
		int y,
		int width,
		int height,
		boolean hovered,
		boolean active
	);

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		this.defaultButtonNarrationText(narrationElementOutput);
	}
}
