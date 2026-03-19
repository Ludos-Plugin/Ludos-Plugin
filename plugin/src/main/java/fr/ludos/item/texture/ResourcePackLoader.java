package fr.ludos.item.texture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import com.google.gson.*;

public final class ResourcePackLoader {
    // private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final JavaPlugin plugin;
    private final Path baseResourcePath;

    private final ConcurrentHashMap<String, ModelData> models = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Material, List<ModelData>> materialModels = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Material, TextureProvider> materialProviders = new ConcurrentHashMap<>();
    
    public ResourcePackLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.baseResourcePath = plugin.getDataFolder().toPath().resolve("resourcepack/assets/minecraft/models/item");
    }
    
    public <T extends TextureProvider> void load(Collection<T> providers) {
        models.clear();
        materialModels.clear();
        materialProviders.clear();
        
        providers.forEach(this::loadProvider);
        // generateOverrides();
        plugin.getLogger().info("Loaded " + models.size() + " models from " + providers.size() + " providers");
    }
    
    private <T extends TextureProvider> void loadProvider(T provider) {
        Path providerPath = baseResourcePath.resolve(provider.getTexturePath());
        if (!Files.exists(providerPath)) return;
        
        try (Stream<Path> files = Files.list(providerPath)) {
            files.filter(Files::isRegularFile)
                 .filter(fileName -> fileName.toString().endsWith(".json"))
                 .forEach(pathFile -> processModel(pathFile, provider));
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load models for " + provider.getTextureId() + ": " + e.getMessage());
        }
    }
    
    private <T extends TextureProvider> void processModel(Path modelPath, T provider) {
        String fileName = modelPath.getFileName().toString();
        
        var matcher = Pattern.compile(provider.model_pattern()).matcher(fileName);
        
        if (!matcher.matches()) return;
        
        String materialBase = matcher.group(1).toLowerCase();
        String numericSuffix = matcher.group(2);
        String mode = matcher.group(3).toLowerCase();

        String variant = materialBase + numericSuffix;
        String key = provider.buildTextureKey(variant, mode);
        
        try {
            Material material = provider.getMaterialForVariant(materialBase);
            int modelId = CustomModelRegistry.register(key, provider);
            ModelData modelData = new ModelData(provider.getTextureId(), variant, mode, material, modelId, modelPath, provider.getTexturePath());
            
            models.put(key, modelData);
            materialModels.computeIfAbsent(material, k -> new ArrayList<>()).add(modelData);
            materialProviders.put(material, provider);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to process " + fileName + ": " + e.getMessage());
        }
    }
    
    public <T extends TextureProvider> Optional<ModelData> getModel(T provider, String variant, String mode) {
        return Optional.ofNullable(models.get(provider.buildTextureKey(variant, mode)));
    }
    
    public <T extends TextureProvider> Optional<ModelData> findBestModel(T provider, String variant, String mode) {
        return getModel(provider, variant, mode)
            .or(() -> !"normal".equals(mode) ? getModel(provider, variant, "normal") : Optional.empty())
            .or(() -> getModel(provider, variant.replaceAll("[0-9]+$", ""), mode))
            .or(() -> !"normal".equals(mode) ? getModel(provider, variant.replaceAll("[0-9]+$", ""), "normal") : Optional.empty());
    }
    
    public int getModelCount() { 
        return models.size();
    }
    
    // private void generateOverrides() {
    //     materialModels.forEach((material, modelList) -> {
    //         if (modelList.isEmpty()) return;

    //         TextureProvider provider = materialProviders.get(material);

    //         if (provider != null) {
    //             updateOverrideFile(material, modelList, provider);
    //         }
    //     });
    // }
    
    // private void updateOverrideFile(Material material, List<ModelData> modelList, TextureProvider provider) {
    //     if (modelList.isEmpty()) return;
        
    //     String pathSuffix = provider.getPathRessource();
    //     Path overridePath = baseResourcePath.resolve(material.name().toLowerCase() + pathSuffix + ".json");
        
    //     try {
    //         JsonObject root = createBaseOverrideStructure(material, pathSuffix);
    //         JsonArray overrides = root.getAsJsonArray("overrides");
            
    //         modelList.stream()
    //             .sorted(Comparator.comparing(ModelData::modelId))
    //             .forEach(model -> addOverride(overrides, model));
            
    //         Files.writeString(overridePath, GSON.toJson(root));
            
    //     } catch (Exception e) {
    //         plugin.getLogger().warning("Failed to update override for " + material + ": " + e.getMessage());
    //     }
    // }
    
    // private JsonObject createBaseOverrideStructure(Material material, String pathSuffix) throws IOException {
    //     Path overridePath = baseResourcePath.resolve(material.name().toLowerCase() + pathSuffix + ".json");
        
    //     if (Files.exists(overridePath)) {
    //         return GSON.fromJson(Files.readString(overridePath), JsonObject.class);
    //     }
        
    //     JsonObject root = new JsonObject();
    //     root.addProperty("parent", "item/handheld");
        
    //     JsonObject textures = new JsonObject();
    //     textures.addProperty("layer0", "minecraft:item/" + material.name().toLowerCase() + pathSuffix);
    //     root.add("textures", textures);
    //     root.add("overrides", new JsonArray());
        
    //     return root;
    // }
    
    // private void addOverride(JsonArray overrides, ModelData model) {
    //     JsonObject override = new JsonObject();
    //     JsonObject predicate = new JsonObject();

    //     predicate.addProperty("custom_model_data", model.modelId());
    //     override.add("predicate", predicate);
        
    //     String modelName = capitalize(model.variant()) + capitalize(model.mode());
    //     override.addProperty("model", "item/" + model.providerPath() + "/" + modelName);
        
    //     overrides.add(override);
    // }
    
    // private static String capitalize(String str) {
    //     return str.isEmpty() ? str : str.substring(0, 1).toUpperCase() + str.substring(1);
    // }
    
    public record ModelData(String itemType, String variant, String mode, Material material,
        int modelId, Path modelPath, String providerPath) {}
}