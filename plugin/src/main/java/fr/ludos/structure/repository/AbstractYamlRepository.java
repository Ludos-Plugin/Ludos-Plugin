package fr.ludos.structure.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractYamlRepository<T> {
	private final JavaPlugin plugin;
	private final File folder;

	protected AbstractYamlRepository(JavaPlugin plugin, String folderName) {
		this.plugin = plugin;
		this.folder = new File(plugin.getDataFolder(), folderName);

		if (!folder.exists()) {
			folder.mkdirs();
		}
	}

	protected abstract String getRootKey();

	protected abstract void serialize(T value, YamlConfiguration config);

	protected abstract T deserialize(String key, YamlConfiguration config);

	public final void save(String key, T value) throws IOException {
		String normalizedKey = normalizeKey(key);
		File file = new File(folder, normalizedKey + ".yml");
		YamlConfiguration config = new YamlConfiguration();
		serialize(value, config);
		config.save(file);
	}

	public final Optional<T> find(String key) {
		String normalizedKey = normalizeKey(key);
		File file = new File(folder, normalizedKey + ".yml");
		if (!file.exists()) {
			return Optional.empty();
		}

		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		return Optional.ofNullable(deserialize(normalizedKey, config));
	}

	public final boolean delete(String key) {
		String normalizedKey = normalizeKey(key);
		File file = new File(folder, normalizedKey + ".yml");
		return file.exists() && file.delete();
	}

	public final List<String> listKeys() {
		if (!folder.exists()) {
			return new ArrayList<>();
		}

		File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
		if (files == null) {
			return new ArrayList<>();
		}

		return java.util.Arrays.stream(files)
			.map(File::getName)
			.map(name -> name.substring(0, name.length() - 4))
			.sorted()
			.collect(Collectors.toList());
	}

	protected final String normalizeKey(String key) {
		if (key == null) {
			throw new IllegalArgumentException("Key cannot be null");
		}

		String normalized = key.trim().toLowerCase(Locale.ROOT);
		if (normalized.isBlank()) {
			throw new IllegalArgumentException("Key cannot be blank");
		}

		if (!normalized.matches("[a-z0-9_-]+")) {
			throw new IllegalArgumentException("Key contains invalid characters");
		}

		return normalized;
	}

	protected final String rootPath(String suffix) {
		if (suffix == null || suffix.isBlank()) {
			return getRootKey();
		}

		return getRootKey() + "." + suffix;
	}

	protected final JavaPlugin getPlugin() {
		return plugin;
	}
}