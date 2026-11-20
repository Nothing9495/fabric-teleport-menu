package com.example.client.i18n;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientI18n {
	private static final Map<String, Map<String, String>> TRANSLATIONS = new ConcurrentHashMap<>();

	static {
		loadBundledLanguage("zh_cn");
		loadBundledLanguage("en_us");
	}

	private ClientI18n() {
	}

	public static void onJoin(String remoteAddress) {
		// UI localization is bundled in the UI jar, no server fetch needed.
	}

	public static void applyServerTranslations(Map<String, Map<String, String>> packs, long version) {
		if (packs == null || packs.isEmpty()) {
			return;
		}

		for (Map.Entry<String, Map<String, String>> pack : packs.entrySet()) {
			if (pack.getValue() == null || pack.getValue().isEmpty()) {
				continue;
			}
			Map<String, String> merged = new HashMap<>(TRANSLATIONS.getOrDefault(pack.getKey(), Map.of()));
			merged.putAll(pack.getValue());
			TRANSLATIONS.put(pack.getKey(), Map.copyOf(merged));
		}
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
		String selectedLanguage = "zh_cn";
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft != null && minecraft.getLanguageManager() != null && minecraft.getLanguageManager().getSelected() != null) {
			selectedLanguage = minecraft.getLanguageManager().getSelected();
		}

		String value = getFrom(selectedLanguage, key);
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

	private static void loadBundledLanguage(String languageCode) {
		String resourcePath = "/assets/teleportmenu/lang/" + languageCode + ".json";
		try (InputStream input = ClientI18n.class.getResourceAsStream(resourcePath)) {
			if (input == null) {
				return;
			}
			String raw = new String(input.readAllBytes(), StandardCharsets.UTF_8);
			JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
			Map<String, String> map = new HashMap<>();
			for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
				if (entry.getValue().isJsonPrimitive()) {
					map.put(entry.getKey(), entry.getValue().getAsString());
				}
			}
			TRANSLATIONS.put(languageCode, Map.copyOf(map));
		} catch (Exception ignored) {
		}
	}
}
