package fr.ludos.core.item.texture;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.inventory.ItemStack;

public final class CustomModelRegistry {
	private static final ConcurrentHashMap<String, Integer> registry = new ConcurrentHashMap<>();

	public static int register(String key, TextureProvider provider) {
		return registry.computeIfAbsent(key, k -> provider.calculateCustomModelData(k));
	}

	public static <T extends TextureProvider> void apply(ItemStack item, T textureProvider) {
		if (item == null || item.getItemMeta() == null || textureProvider == null) return;

		String id = textureProvider.getTextureId();
		Integer modelId = registry.get(id);

		if (modelId == null) return;

		var meta = item.getItemMeta();
		meta.setCustomModelData(modelId);
		item.setItemMeta(meta);
	}

	public static void apply(ItemStack item, String id) {
		if (item == null || item.getItemMeta() == null) return;

		Integer modelId = registry.get(id);

		if (modelId == null) return;

		var meta = item.getItemMeta();
		meta.setCustomModelData(modelId);
		item.setItemMeta(meta);
	}

	public static Integer get(String id) {
		return registry.get(id);
	}

	public static boolean has(String id) {
		return registry.containsKey(id);
	}

	public static Set<String> getAllKeys() {
		return registry.keySet();
	}

	public static void clear() {
		registry.clear();
	}
}