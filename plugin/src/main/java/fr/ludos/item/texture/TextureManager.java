package fr.ludos.item.texture;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import fr.ludos.Ludos;
import java.util.*;

public final class TextureManager implements CommandExecutor {
    private static TextureManager instance;
    private final Ludos plugin;
    private final ResourcePackLoader loader;
    private final List<TextureProvider> allProviders = new ArrayList<>();
    
    public TextureManager(Ludos plugin) {
        this.plugin = plugin;
        this.loader = new ResourcePackLoader(plugin);

        instance = this;

        initialize();
    }
    
    public static TextureManager getInstance() { return instance; }
    
    private void initialize() {
        loader.load(Collections.emptyList());
        plugin.getLogger().info("TextureManager initialized");
    }
    
    public <T extends TextureProvider> void registerProvider(T provider) {
        allProviders.add(provider);
        loader.load(allProviders);
    }
    
    public <T extends TextureProvider> void registerProviders(Collection<T> providers) {
        allProviders.addAll(providers);
        loader.load(allProviders);
    }
    
    public <T extends TextureProvider> void applyTexture(ItemStack item, T provider, String variant, String mode) {
        String key = provider.buildTextureKey(variant, mode);
        loader.findBestModel(provider, variant, mode)
              .ifPresentOrElse(
                  model -> CustomModelRegistry.apply(item, key),
                  () -> plugin.getLogger().warning("No model found for: " + key)
              );
    }
    
    public void reload() {
        CustomModelRegistry.clear();

        if (!allProviders.isEmpty()) {
            loader.load(allProviders);
        }

        plugin.getLogger().info("TextureManager reloaded with " + allProviders.size() + " providers");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ludos.admin")) return true;
        
        if (args.length == 0) {
            sender.sendMessage("§e/texture reload §7- Reload pack");
            sender.sendMessage("§e/texture info §7- Show pack info");
            sender.sendMessage("§e/texture debug §7- Show all registered keys");

            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                sender.sendMessage("§6Reloading resource pack...");
                reload();
                sender.sendMessage("§aResource pack reloaded!");
            }
            case "info" -> sender.sendMessage("§7Total models: §f" + loader.getModelCount());
            case "debug" -> {
                for (String key : CustomModelRegistry.getAllKeys()) {
                    Integer modelId = CustomModelRegistry.get(key);
                    sender.sendMessage("§7" + key + " §f-> §e" + modelId);
                }
            }
            default -> sender.sendMessage("§cUsage: /texture <reload|info|debug>");
        }
        return true;
    }
}