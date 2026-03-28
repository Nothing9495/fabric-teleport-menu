package com.example.client.i18n;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientI18n {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Map<String, Map<String, String>> TRANSLATIONS = new ConcurrentHashMap<>();
	private static volatile String serverCacheKey = "default";
	private static volatile long serverVersion = 0L;

	private ClientI18n() {
	}

	public static void onJoin(String remoteAddress) {
		serverCacheKey = sanitize(remoteAddress);
		loadCache();
	}

	public static void applyServerTranslations(Map<String, Map<String, String>> packs, long version) {
		TRANSLATIONS.clear();
		for (Map.Entry<String, Map<String, String>> entry : packs.entrySet()) {
			TRANSLATIONS.put(entry.getKey(), Map.copyOf(entry.getValue()));
		}
		serverVersion = version;
		saveCache();
	}

	public static MutableComponent tr(String key, Object... args) {
		return Component.literal(text(key, args));
	}

	public static String text(String key, Object... args) {
		String template = resolve(key);
		Object[] normalized = normalizeArgs(args);
		try {
			return String.format(Locale.ROOT, template, normalized);
		} catch (Exception ignored) {
			return template;
		}
	}

	private static String resolve(String key) {
		String lang = "zh_cn";
		Minecraft mc = Minecraft.getInstance();
		if (mc != null && mc.getLanguageManager() != null && mc.getLanguageManager().getSelected() != null) {
			lang = mc.getLanguageManager().getSelected();
		}

		String value = getFrom(lang, key);
		if (value != null) {
			return value;
		}
		value = getFrom("zh_cn", key);
		if (value != null) {
			return value;
		}
		value = getFrom("en_us", key);
		return value != null ? value : key;
	}

	private static String getFrom(String language, String key) {
		Map<String, String> map = TRANSLATIONS.get(language);
		if (map == null) {
			return null;
		}
		return map.get(key);
	}

	private static Object[] normalizeArgs(Object[] args) {
		if (args == null || args.length == 0) {
			return new Object[0];
		}

		Object[] normalized = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg instanceof Component component) {
				normalized[i] = component.getString();
			} else {
				normalized[i] = Objects.toString(arg, "");
			}
		}
		return normalized;
	}

	private static Path cacheFile() {
		return FabricLoader.getInstance().getConfigDir().resolve("teleportmenu").resolve("lang-cache").resolve(serverCacheKey + ".json");
	}

	private static void loadCache() {
		Path file = cacheFile();
		if (!Files.exists(file)) {
			return;
		}
		try {
			String raw = Files.readString(file, StandardCharsets.UTF_8);
			JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
			serverVersion = root.has("version") ? root.get("version").getAsLong() : 0L;
			TRANSLATIONS.clear();
			if (root.has("packs") && root.get("packs").isJsonObject()) {
				JsonObject packsObj = root.getAsJsonObject("packs");
				for (Map.Entry<String, JsonElement> langEntry : packsObj.entrySet()) {
					if (!langEntry.getValue().isJsonObject()) {
						continue;
					}
					JsonObject values = langEntry.getValue().getAsJsonObject();
					Map<String, String> map = new HashMap<>();
					for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
						if (entry.getValue().isJsonPrimitive()) {
							map.put(entry.getKey(), entry.getValue().getAsString());
						}
					}
					TRANSLATIONS.put(langEntry.getKey(), Map.copyOf(map));
				}
			}
		} catch (Exception ignored) {
		}
	}

	private static void saveCache() {
		Path file = cacheFile();
		try {
			Files.createDirectories(file.getParent());
			JsonObject root = new JsonObject();
			root.addProperty("version", serverVersion);
			JsonObject packs = new JsonObject();
			for (Map.Entry<String, Map<String, String>> langEntry : TRANSLATIONS.entrySet()) {
				JsonObject values = new JsonObject();
				for (Map.Entry<String, String> entry : langEntry.getValue().entrySet()) {
					values.addProperty(entry.getKey(), entry.getValue());
				}
				packs.add(langEntry.getKey(), values);
			}
			root.add("packs", packs);
			Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
		} catch (IOException ignored) {
		}
	}

	private static String sanitize(String raw) {
		if (raw == null || raw.isBlank()) {
			return "default";
		}
		return raw.replaceAll("[^a-zA-Z0-9._-]", "_");
	}
}
