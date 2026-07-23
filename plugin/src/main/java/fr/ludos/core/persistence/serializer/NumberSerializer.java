package fr.ludos.core.persistence.serializer;

import org.bukkit.configuration.ConfigurationSection;

/**
 * .
 */
public final class NumberSerializer implements Serializer<Integer, Integer> {
	public static final NumberSerializer UNSIGNED = new NumberSerializer(true);
	public static final NumberSerializer SIGNED = new NumberSerializer(false);

	private final boolean unsigned;

	private NumberSerializer(boolean unsigned) {
		this.unsigned = unsigned;
	}

	@Override
	public Integer parse(Integer primitive) {
		return primitive;
	}
	@Override
	public Integer serialize(Integer value) {
		return value;
	}

	@Override
	public Integer getPrimitive(String key, ConfigurationSection config) {
		if (! config.isInt(key)) return null;
		return config.getInt(key);
	}

	@Override
	public Integer fromString(String value) {
		if (value == null) return null;
		try {
			if (unsigned) {
				return Integer.parseUnsignedInt(value);
			} else {
				return Integer.parseInt(value);
			}
		} catch (Exception e) {
			return null;
		}
	}
	@Override
	public String toString(Integer value) {
		if (value == null) return null;
		return value.toString();
	}
}