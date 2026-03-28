package com.example.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class OreMenuButtonWidget extends AbstractOreButtonWidget {
	private final ItemStack icon;
	private final Consumer<OreMenuButtonWidget> onPress;

	public OreMenuButtonWidget(int width, int height, Component message, ItemStack icon, Consumer<OreMenuButtonWidget> onPress) {
		super(width, height, message);
		this.icon = icon.copy();
		this.onPress = onPress;
	}

	@Override
	public void onPress() {
		this.onPress.accept(this);
	}

	@Override
	protected void drawContent(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, int width, int height, boolean hovered, boolean active) {
		int iconX = x + 6;
		int iconY = y + (height - 16) / 2;
		guiGraphics.renderItem(this.icon, iconX, iconY);

		int textX = x + 28;
		int textY = y + (height - minecraft.font.lineHeight) / 2;
		String title = minecraft.font.plainSubstrByWidth(this.getMessage().getString(), Math.max(0, width - 34));
		guiGraphics.drawString(minecraft.font, title, textX, textY, this.titleColor(active, hovered), true);
	}
}
