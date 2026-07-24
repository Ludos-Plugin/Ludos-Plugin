package fr.ludos.core.persistence.serializer;

import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;

/**
 * .
 */
public final class UUIDSerializer implements Serializer<UUID, String> {
	public final static UUIDSerializer INSTANCE = new UUIDSerializer();

	private UUIDSerializer() {}

	@Override
	public UUID parse(String primitive) {
		try {
			return UUID.fromString(primitive);
		} catch (Exception e) {
			return null;
		}
	}
	@Override
	public String serialize(UUID value) {
		return value.toString();
	}

	@Override
	public String getPrimitive(String key, ConfigurationSection config) {
		return config.getString(key);
	}

	@Override
	public UUID fromString(String string) {
		return parse(string);
	}
	@Override
	public String toString(UUID value) {
		return serialize(value);
	}
}
