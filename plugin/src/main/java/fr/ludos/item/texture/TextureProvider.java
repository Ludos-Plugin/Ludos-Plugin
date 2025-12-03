package fr.ludos.item.texture;

import java.util.regex.Pattern;

import org.bukkit.Material;

public interface TextureProvider {
    String getTextureId();
    String getTexturePath();

    Material getMaterialForVariant(String variant);

    String getPathRessource();
    Pattern modelPattern();

    String buildTextureKey(String variant, String mode);

    int calculateCustomModelData(String key);
}