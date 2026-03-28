package com.example.client.widget;

import com.example.client.model.ItemManageEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class OreRegistryEntryButtonWidget extends AbstractOreButtonWidget {
	private final ItemManageEntry entry;
	private final boolean disabled;
	private final Consumer<ItemManageEntry> onPress;

	public OreRegistryEntryButtonWidget(int width, int height, ItemManageEntry entry, boolean disabled, Consumer<ItemManageEntry> onPress) {
		super(width, height, Component.literal(entry.registryId()));
		this.entry = entry;
		this.disabled = disabled;
		this.onPress = onPress;
	}

	@Override
	public void onPress() {
		this.onPress.accept(this.entry);
	}

	@Override
	protected void drawContent(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, int width, int height, boolean hovered, boolean active) {
		int iconX = x + 8;
		int iconY = y + (height - 16) / 2;
		if (!this.entry.iconStack().isEmpty()) {
			guiGraphics.renderItem(this.entry.iconStack(), iconX, iconY);
		}

		int textX = iconX + 22;
		int textWidth = Math.max(0, width - (textX - x) - 8);
		String title = minecraft.font.plainSubstrByWidth(this.entry.registryId(), textWidth);
		String subtitleText = minecraft.font.plainSubstrByWidth("材质: " + this.entry.materialLabel() + " | " + this.entry.displayName(), textWidth);
		Component subtitle = Component.literal(subtitleText);

		guiGraphics.drawString(minecraft.font, title, textX, y + 6, this.titleColor(active, hovered), false);
		guiGraphics.drawString(minecraft.font, subtitle, textX, y + 18, 0xB8C0CE, false);

		if (this.disabled) {
			guiGraphics.fill(x, y, x + width, y + height, 0x88C52020);
		}
	}
}
