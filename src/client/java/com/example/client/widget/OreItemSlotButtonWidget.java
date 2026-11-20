package com.example.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class OreItemSlotButtonWidget extends AbstractOreButtonWidget {
	private final ItemStack stack;
	private final Consumer<OreItemSlotButtonWidget> onPress;

	public OreItemSlotButtonWidget(int size, ItemStack stack, Consumer<OreItemSlotButtonWidget> onPress) {
		super(size, size, Component.empty());
		this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
		this.onPress = onPress;
	}

	public ItemStack stack() {
		return this.stack.copy();
	}

	@Override
	public void onPress() {
		this.onPress.accept(this);
	}

	@Override
	protected void drawContent(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, int width, int height, boolean hovered, boolean active) {
		if (this.stack.isEmpty()) {
			return;
		}

		int itemX = x + ((width - 16) / 2);
		int itemY = y + ((height - 16) / 2);
		guiGraphics.renderItem(this.stack, itemX, itemY);
		guiGraphics.renderItemDecorations(minecraft.font, this.stack, itemX, itemY);
	}
}
