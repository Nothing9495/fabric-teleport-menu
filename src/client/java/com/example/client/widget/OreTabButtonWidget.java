package com.example.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Tab header styled like the other Ore widgets: the selected tab keeps a filled
 * background plus an accent underline, the other tabs stay transparent and dim.
 */
public class OreTabButtonWidget extends AbstractOreButtonWidget {
	private final boolean selected;
	private final Consumer<OreTabButtonWidget> onPress;

	public OreTabButtonWidget(int width, int height, Component message, boolean selected, Consumer<OreTabButtonWidget> onPress) {
		super(width, height, message);
		this.selected = selected;
		this.onPress = onPress;
	}

	@Override
	public void onPress() {
		this.onPress.accept(this);
	}

	@Override
	protected void drawContent(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, int width, int height, boolean hovered, boolean active) {
		if (this.selected) {
			guiGraphics.fill(x, y, x + width, y + height, 0x66425773);
			guiGraphics.fill(x, y + height - 2, x + width, y + height, 0xFFC7D5ED);
		}

		int textColor;
		if (this.selected) {
			textColor = 0xFFF6FAFF;
		} else if (hovered && active) {
			textColor = 0xFFEAF1FF;
		} else {
			textColor = 0xFF9FAEC2;
		}

		guiGraphics.drawCenteredString(
			minecraft.font,
			this.getMessage(),
			x + (width / 2),
			y + ((height - minecraft.font.lineHeight) / 2),
			textColor
		);
	}
}
