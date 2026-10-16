package fr.ludos.core.persistence.serializer;

import java.util.Objects;

import org.bukkit.configuration.ConfigurationSection;

/**
 * .
 * @param <T>
 */
public final class EnumSerializer<T extends Enum<T>> implements Serializer<T, String> {
	private final Class<T> clazz;

	public EnumSerializer(Class<T> clazz) {
		this.clazz = Objects.requireNonNull(clazz);
	}

	@Override
	public T parse(String primitive) {
		return fromString(primitive);
	}
	@Override
	public String serialize(T value) {
		return toString(value);
	}

	@Override
	public String getPrimitive(String key, ConfigurationSection config) {
		return config.getString(key);
	}

	@Override
	public T fromString(String string) {
		if (string == null) return null;
		try {
			return Enum.valueOf(clazz, string);
		} catch (Exception e) {
			return null;
		}
	}
	@Override
	public String toString(T value) {
		if (value == null) return null;
		return value.name();
	}
}
