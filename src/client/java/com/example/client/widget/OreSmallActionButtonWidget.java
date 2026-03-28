package com.example.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class OreSmallActionButtonWidget extends AbstractOreButtonWidget {
	private final Consumer<OreSmallActionButtonWidget> onPress;

	public OreSmallActionButtonWidget(int width, int height, Component message, Consumer<OreSmallActionButtonWidget> onPress) {
		super(width, height, message);
		this.onPress = onPress;
	}

	@Override
	public void onPress() {
		this.onPress.accept(this);
	}

	@Override
	protected void drawContent(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, int width, int height, boolean hovered, boolean active) {
		guiGraphics.drawCenteredString(
			minecraft.font,
			this.getMessage(),
			x + (width / 2),
			y + ((height - minecraft.font.lineHeight) / 2),
			this.titleColor(active, hovered)
		);
	}
}
