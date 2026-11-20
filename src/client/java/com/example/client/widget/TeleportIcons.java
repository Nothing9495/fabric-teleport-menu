package com.example.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Texture-based UI icons sourced from the shared game-icon pack. Each PNG is a
 * 32x32 monochrome glyph baked in a fixed light tint, living under
 * assets/teleportmenu/textures/gui/icons.
 */
public final class TeleportIcons {
	private static final int TEXTURE_SIZE = 32;

	// Navigation / actions
	public static final ResourceLocation BACK = icon("back");
	public static final ResourceLocation CLOSE = icon("close");
	public static final ResourceLocation CONFIRM = icon("confirm");
	public static final ResourceLocation CANCEL = icon("cancel");
	public static final ResourceLocation ADD = icon("add");
	public static final ResourceLocation RENAME = icon("rename");
	public static final ResourceLocation DELETE = icon("delete");
	public static final ResourceLocation QUERY = icon("query");
	public static final ResourceLocation PLUS = icon("plus");
	public static final ResourceLocation MINUS = icon("minus");
	public static final ResourceLocation SET = icon("set");
	public static final ResourceLocation RESET = icon("reset");
	public static final ResourceLocation SELECT = icon("select");
	public static final ResourceLocation MANAGE = icon("manage");
	public static final ResourceLocation EXPAND = icon("expand");
	public static final ResourceLocation LEADERBOARD = icon("leaderboard");
	public static final ResourceLocation COIN = icon("coin");
	public static final ResourceLocation SHOP = icon("shop");
	public static final ResourceLocation WHITELIST = icon("whitelist");
	public static final ResourceLocation INVENTORY = icon("inventory");
	public static final ResourceLocation BOXSHOP = icon("boxshop");

	// Main menu cards
	public static final ResourceLocation PLAYERS = icon("players");
	public static final ResourceLocation HOMES = icon("homes");
	public static final ResourceLocation TERRITORIES = icon("territories");
	public static final ResourceLocation ECONOMY = icon("economy");
	public static final ResourceLocation ADMIN = icon("admin");

	private TeleportIcons() {
	}

	private static ResourceLocation icon(String name) {
		return ResourceLocation.fromNamespaceAndPath("teleportmenu", "textures/gui/icons/" + name + ".png");
	}

	/**
	 * Draws the whole texture scaled into a {@code size x size} box at (x, y).
	 * Icons carry a fixed tint, so disabled buttons only fade the alpha.
	 */
	public static void draw(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int size, boolean active) {
		RenderSystem.enableBlend();
		float alpha = active ? 1.0f : 0.45f;
		guiGraphics.setColor(1.0f, 1.0f, 1.0f, alpha);
		guiGraphics.blit(texture, x, y, size, size, 0.0f, 0.0f, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
		guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.disableBlend();
	}
}
