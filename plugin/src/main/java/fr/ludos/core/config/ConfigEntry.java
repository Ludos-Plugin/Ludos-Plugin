package fr.ludos.core.config;

public final record ConfigEntry<T>(String key, ConfigOptions<T> options) {
	public ConfigEntry(String key, ConfigOptions<T> options) {
		this.key = key;
		this.options = options;
	}
}
