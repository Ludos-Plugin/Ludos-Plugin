package fr.ludos.core.persistence.data;

import java.util.Objects;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.serializer.Serializer;

/**
 * .
 * @param <T>
 */
public class DataEntry<T> {
	private final @NotNull String key;
	private final @NotNull Serializer<T, ?> serializer;

	public DataEntry(@NotNull String key, @NotNull Serializer<T, ?> serializer) {
		this.key = Objects.requireNonNull(key);
		this.serializer = Objects.requireNonNull(serializer);
	}

	public T get(ConfigurationSection config) {
		return serializer.get(key, config);
	}
	public void set(T value, ConfigurationSection section) {
		serializer.set(key, value, section);
	}
}
