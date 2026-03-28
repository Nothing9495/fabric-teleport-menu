package com.example.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;

import java.util.UUID;
import java.util.function.Consumer;

public class OrePlayerEntryButtonWidget extends AbstractOreButtonWidget {
	private final UUID playerId;
	private final String playerName;
	private final Consumer<UUID> onPress;

	public OrePlayerEntryButtonWidget(int width, int height, UUID playerId, String playerName, Consumer<UUID> onPress) {
		super(width, height, Component.literal(playerName));
		this.playerId = playerId;
		this.playerName = playerName == null ? "" : playerName;
		this.onPress = onPress;
	}

	@Override
	public void onPress() {
		this.onPress.accept(this.playerId);
	}

	@Override
	protected void drawContent(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, int width, int height, boolean hovered, boolean active) {
		ClientPacketListener connection = minecraft.getConnection();
		PlayerInfo playerInfo = connection != null ? connection.getPlayerInfo(this.playerId) : null;

		PlayerSkin skin = playerInfo != null
			? playerInfo.getSkin()
			: DefaultPlayerSkin.get(this.playerId);

		int latency = playerInfo != null ? playerInfo.getLatency() : -1;
		Component subtitle = latency >= 0
			? Component.translatable("screen.teleportmenu.players.ping", latency)
			: Component.translatable("screen.teleportmenu.players.ping_unknown");

		int avatarSize = 24;
		int avatarX = x + 6;
		int avatarY = y + (height - avatarSize) / 2;
		PlayerFaceRenderer.draw(guiGraphics, skin, avatarX, avatarY, avatarSize);

		int textX = avatarX + avatarSize + 8;
		int textWidth = Math.max(0, width - (textX - x) - 8);
		String title = minecraft.font.plainSubstrByWidth(this.playerName, textWidth);

		guiGraphics.drawString(minecraft.font, title, textX, y + 6, this.titleColor(active, hovered), true);
		guiGraphics.drawString(minecraft.font, subtitle, textX, y + 18, latencyColor(latency), false);
	}

	private static int latencyColor(int latency) {
		if (latency < 0) {
			return 0xA6ADB8;
		}
		if (latency <= 80) {
			return 0x98E19F;
		}
		if (latency <= 180) {
			return 0xF5D48A;
		}
		return 0xF29C9C;
	}
}
