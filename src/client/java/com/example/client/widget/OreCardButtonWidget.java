package com.example.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class OreCardButtonWidget extends AbstractOreButtonWidget {
	private final ItemStack icon;
	private final Consumer<OreCardButtonWidget> onPress;

	public OreCardButtonWidget(int width, int height, Component message, ItemStack icon, Consumer<OreCardButtonWidget> onPress) {
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
		int iconSize = 30;
		int iconX = x + (width - iconSize) / 2;
		int iconY = y + 8;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(iconX, iconY, 0);
		guiGraphics.pose().scale(iconSize / 16f, iconSize / 16f, 1f);
		guiGraphics.renderItem(this.icon, 0, 0);
		guiGraphics.pose().popPose();

		int textY = y + height - minecraft.font.lineHeight - 8;
		String title = minecraft.font.plainSubstrByWidth(this.getMessage().getString(), Math.max(0, width - 10));
		guiGraphics.drawCenteredString(minecraft.font, title, x + (width / 2), textY, this.titleColor(active, hovered));
	}
}
