package com.example.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.UUID;
import java.util.function.Consumer;

public class OreHomeEntryButtonWidget extends AbstractOreButtonWidget {
	private final UUID homeId;
	private final String homeName;
	private final int xPos;
	private final int yPos;
	private final int zPos;
	private final ItemStack icon;
	private final Consumer<UUID> onPress;

	public OreHomeEntryButtonWidget(int width, int height, UUID homeId, String homeName, int xPos, int yPos, int zPos, String iconItemId, Consumer<UUID> onPress) {
		super(width, height, Component.literal(homeName));
		this.homeId = homeId;
		this.homeName = homeName == null ? "" : homeName;
		this.xPos = xPos;
		this.yPos = yPos;
		this.zPos = zPos;
		this.icon = new ItemStack(resolveIcon(iconItemId));
		this.onPress = onPress;
	}

	@Override
	public void onPress() {
		this.onPress.accept(this.homeId);
	}

	@Override
	protected void drawContent(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, int width, int height, boolean hovered, boolean active) {
		int iconX = x + 8;
		int iconY = y + (height - 16) / 2;
		guiGraphics.renderItem(this.icon, iconX, iconY);

		int textX = iconX + 22;
		int textWidth = Math.max(0, width - (textX - x) - 8);
		String title = minecraft.font.plainSubstrByWidth(this.homeName, textWidth);
		Component subtitle = Component.literal("X: " + this.xPos + "  Y: " + this.yPos + "  Z: " + this.zPos);

		guiGraphics.drawString(minecraft.font, title, textX, y + 6, this.titleColor(active, hovered), true);
		guiGraphics.drawString(minecraft.font, subtitle, textX, y + 18, 0xA8B6C9, false);
	}

	private static Item resolveIcon(String iconItemId) {
		if (iconItemId == null || iconItemId.isBlank()) {
			return Items.COMPASS;
		}

		try {
			ResourceLocation id = ResourceLocation.parse(iconItemId);
			return BuiltInRegistries.ITEM.getOptional(id).orElse(Items.COMPASS);
		} catch (IllegalArgumentException ignored) {
			return Items.COMPASS;
		}
	}
}
