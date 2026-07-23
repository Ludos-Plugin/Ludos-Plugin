package fr.ludos.core.persistence.serializer;

import org.bukkit.configuration.ConfigurationSection;

/**
 * .
 */
public final class BooleanSerializer implements Serializer<Boolean, Boolean> {
	public final static BooleanSerializer INSTANCE = new BooleanSerializer();

	private BooleanSerializer() {}

	public static final String FALSE_STRING = "false";
	public static final String TRUE_STRING = "true";

	@Override
	public Boolean parse(Boolean primitive) {
		return primitive;
	}
	@Override
	public Boolean serialize(Boolean primitive) {
		return primitive;
	}

	@Override
	public Boolean getPrimitive(String key, ConfigurationSection config) {
		// Manual null check, otherwise 0 is returned
		if (! config.isBoolean(key)) return null;
		return config.getBoolean(key);
	}

	@Override
	public Boolean fromString(String value) {
		switch (value) {
			case FALSE_STRING:
				return false;
			case TRUE_STRING:
				return true;
			default:
				return null;
		}
	}
	@Override
	public String toString(Boolean value) {
		if (value == null) return null;
		return value
			? TRUE_STRING
			: FALSE_STRING;
	}
}