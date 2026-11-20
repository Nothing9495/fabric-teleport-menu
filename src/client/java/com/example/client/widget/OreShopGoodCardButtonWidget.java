package com.example.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class OreShopGoodCardButtonWidget extends AbstractOreButtonWidget {
	private final ItemStack icon;
	private final String title;
	private final String stockText;
	private final String priceText;
	private final Consumer<OreShopGoodCardButtonWidget> onPress;

	public OreShopGoodCardButtonWidget(
		int width,
		int height,
		ItemStack icon,
		String title,
		String stockText,
		String priceText,
		Consumer<OreShopGoodCardButtonWidget> onPress
	) {
		super(width, height, Component.literal(title == null ? "" : title));
		this.icon = icon == null ? ItemStack.EMPTY : icon.copy();
		this.title = title == null ? "" : title;
		this.stockText = stockText == null ? "" : stockText;
		this.priceText = priceText == null ? "" : priceText;
		this.onPress = onPress;
	}

	@Override
	public void onPress() {
		this.onPress.accept(this);
	}

	@Override
	protected void drawContent(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, int width, int height, boolean hovered, boolean active) {
		if (!this.icon.isEmpty()) {
			int iconX = x + (width - 16) / 2;
			int iconY = y + 8;
			guiGraphics.renderItem(this.icon, iconX, iconY);
		}

		String titleText = minecraft.font.plainSubstrByWidth(this.title, Math.max(0, width - 10));
		guiGraphics.drawCenteredString(minecraft.font, titleText, x + (width / 2), y + 30, this.titleColor(active, hovered));

		String stock = minecraft.font.plainSubstrByWidth(this.stockText, Math.max(0, width - 10));
		guiGraphics.drawCenteredString(minecraft.font, stock, x + (width / 2), y + 44, 0xB6C3D8);

		String price = minecraft.font.plainSubstrByWidth(this.priceText, Math.max(0, width - 10));
		guiGraphics.drawCenteredString(minecraft.font, price, x + (width / 2), y + 56, 0x98E19F);
	}
}
