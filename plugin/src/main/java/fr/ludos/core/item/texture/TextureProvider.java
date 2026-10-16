package fr.ludos.core.item.texture;

import org.bukkit.Material;

/**
 * Provides texture metadata and material mapping for custom item variants.
 */
public interface TextureProvider {
	String getTextureId();
	String getTexturePath();

	Material getMaterialForVariant(String variant);

	String getPathRessource();
	String model_pattern();

	String buildTextureKey(String variant, String mode);

	int calculateCustomModelData(String key);
}