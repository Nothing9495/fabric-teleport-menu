package com.example.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Client-only UI preferences, stored in {@code config/teleportmenu-client.json}.
 * Nothing here is ever synchronized to the server: both switches only change how
 * this client renders the scoreboard and incoming chat.
 */
public final class ClientPreferences {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("teleportmenu-client.json");

	private static boolean hideMoneySidebar;
	private static boolean hideRewardTips;

	static {
		load();
	}

	private ClientPreferences() {
	}

	public static boolean hideMoneySidebar() {
		return hideMoneySidebar;
	}

	public static boolean hideRewardTips() {
		return hideRewardTips;
	}

	public static void setHideMoneySidebar(boolean hide) {
		hideMoneySidebar = hide;
		save();
	}

	public static void setHideRewardTips(boolean hide) {
		hideRewardTips = hide;
		save();
	}

	private static void load() {
		try {
			if (!Files.exists(FILE)) {
				return;
			}

			JsonObject root = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
			hideMoneySidebar = root.has("hideMoneySidebar") && root.get("hideMoneySidebar").getAsBoolean();
			hideRewardTips = root.has("hideRewardTips") && root.get("hideRewardTips").getAsBoolean();
		} catch (Exception ignored) {
			// a broken local config only costs the preferences, never the game
		}
	}

	private static void save() {
		try {
			JsonObject root = new JsonObject();
			root.addProperty("hideMoneySidebar", hideMoneySidebar);
			root.addProperty("hideRewardTips", hideRewardTips);

			Files.createDirectories(FILE.getParent());
			Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
		} catch (Exception ignored) {
			// ignore write failures
		}
	}
}
