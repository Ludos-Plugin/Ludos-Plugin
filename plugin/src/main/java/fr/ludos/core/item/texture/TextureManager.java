package fr.ludos.core.item.texture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.Ludos;

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
		syncResourcePackFromPolymerSource();
		loader.load(Collections.emptyList());
		plugin.getLogger().info("TextureManager initialized");
	}

	private void syncResourcePackFromPolymerSource() {
		Path pluginDataFolder = plugin.getDataFolder().toPath().toAbsolutePath().normalize();
		Path pluginsDir = pluginDataFolder.getParent();

		if (pluginsDir == null) {
			plugin.getLogger().warning("Unable to resolve plugins directory from: " + pluginDataFolder);
			return;
		}

		Path serverRoot = pluginsDir.getParent();
		if (serverRoot == null) {
			serverRoot = Path.of(".").toAbsolutePath().normalize();
			plugin.getLogger().warning("Unable to resolve server root from plugins directory, fallback to: " + serverRoot);
		}

		Path sourceAssets = serverRoot.resolve("config/polymer/source_assets");
		Path destination = pluginDataFolder.resolve("resourcepack");

		if (!Files.isDirectory(sourceAssets)) {
			plugin.getLogger().warning("Polymer source assets not found at: " + sourceAssets);
			return;
		}

		try {
			deleteDirectory(destination);
			copyDirectory(sourceAssets, destination);
			plugin.getLogger().info("Resource pack synced from Polymer source assets");
		} catch (IOException e) {
			plugin.getLogger().warning("Failed to sync resource pack: " + e.getMessage());
		}
	}

	private static void copyDirectory(Path source, Path destination) throws IOException {
		try (Stream<Path> files = Files.walk(source)) {
			files.forEach(path -> {
				try {
					Path relative = source.relativize(path);
					Path target = destination.resolve(relative);

					if (Files.isDirectory(path)) {
						Files.createDirectories(target);
						return;
					}

					Files.createDirectories(target.getParent());
					Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				}
			});
		} catch (RuntimeException runtimeException) {
			if (runtimeException.getCause() instanceof IOException ioException) {
				throw ioException;
			}

			throw runtimeException;
		}
	}

	private static void deleteDirectory(Path directory) throws IOException {
		if (!Files.exists(directory)) {
			return;
		}

		try (Stream<Path> paths = Files.walk(directory)) {
			paths.sorted(Comparator.reverseOrder())
				 .forEach(path -> {
					 try {
						 Files.deleteIfExists(path);
					 } catch (IOException exception) {
						 throw new RuntimeException(exception);
					 }
				 });
		} catch (RuntimeException runtimeException) {
			if (runtimeException.getCause() instanceof IOException ioException) {
				throw ioException;
			}

			throw runtimeException;
		}
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
		syncResourcePackFromPolymerSource();
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