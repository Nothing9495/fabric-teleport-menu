package com.example.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class OreIconListEntryButtonWidget extends AbstractOreButtonWidget {
	private final ItemStack icon;
	private final String title;
	private final String subtitle;
	private final String rightTop;
	private final String rightBottom;
	private final Consumer<OreIconListEntryButtonWidget> onPress;

	public OreIconListEntryButtonWidget(
		int width,
		int height,
		ItemStack icon,
		String title,
		String subtitle,
		String rightTop,
		String rightBottom,
		Consumer<OreIconListEntryButtonWidget> onPress
	) {
		super(width, height, Component.literal(title == null ? "" : title));
		this.icon = icon == null ? ItemStack.EMPTY : icon.copy();
		this.title = title == null ? "" : title;
		this.subtitle = subtitle == null ? "" : subtitle;
		this.rightTop = rightTop == null ? "" : rightTop;
		this.rightBottom = rightBottom == null ? "" : rightBottom;
		this.onPress = onPress;
	}

	@Override
	public void onPress() {
		this.onPress.accept(this);
	}

	@Override
	protected void drawContent(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, int width, int height, boolean hovered, boolean active) {
		int iconX = x + 8;
		int iconY = y + (height - 16) / 2;
		if (!this.icon.isEmpty()) {
			guiGraphics.renderItem(this.icon, iconX, iconY);
		}

		int rightSectionWidth = 76;
		int textX = iconX + 22;
		int textWidth = Math.max(0, width - (textX - x) - rightSectionWidth - 8);

		String renderedTitle = minecraft.font.plainSubstrByWidth(this.title, textWidth);
		String renderedSubtitle = minecraft.font.plainSubstrByWidth(this.subtitle, textWidth);
		guiGraphics.drawString(minecraft.font, renderedTitle, textX, y + 6, this.titleColor(active, hovered), true);
		guiGraphics.drawString(minecraft.font, renderedSubtitle, textX, y + 18, 0xA8B6C9, false);

		int rightX = x + width - rightSectionWidth;
		if (!this.rightTop.isBlank()) {
			String rt = minecraft.font.plainSubstrByWidth(this.rightTop, rightSectionWidth - 6);
			guiGraphics.drawString(minecraft.font, rt, rightX, y + 6, 0xEAF1FF, false);
		}
		if (!this.rightBottom.isBlank()) {
			String rb = minecraft.font.plainSubstrByWidth(this.rightBottom, rightSectionWidth - 6);
			guiGraphics.drawString(minecraft.font, rb, rightX, y + 18, 0xC7D5ED, false);
		}
	}
}
