package fr.ludos.core.persistence.serializer;

import org.bukkit.configuration.ConfigurationSection;

/**
 * .
 */
public final class StringSerializer implements Serializer<String, String> {
	public static final StringSerializer INSTANCE = new StringSerializer();

	private StringSerializer() {}

	@Override
	public String fromString(String string) {
		return string;
	}
	@Override
	public String toString(String value) {
		return value;
	}

	@Override
	public String parse(String primitive) {
		return primitive;
	}
	@Override
	public String serialize(String value) {
		return value;
	}

	@Override
	public String getPrimitive(String key, ConfigurationSection config) {
		return config.getString(key);
	}
}
