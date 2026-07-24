package fr.ludos.core.persistence.serializer;

import org.bukkit.configuration.ConfigurationSection;

/**
 * .
 */
public final class DoubleSerializer implements Serializer<Double, Double> {
	public static final DoubleSerializer INSTANCE = new DoubleSerializer();

	private DoubleSerializer() {}

	@Override
	public Double parse(Double primitive) {
		return primitive;
	}
	@Override
	public Double serialize(Double value) {
		return value;
	}

	@Override
	public Double getPrimitive(String key, ConfigurationSection config) {
		if (! config.isDouble(key)) return null;
		return config.getDouble(key);
	}

	@Override
	public Double fromString(String value) {
		if (value == null) return null;
		try {
			return Double.parseDouble(value);
		} catch (Exception e) {
			return null;
		}
	}
	@Override
	public String toString(Double value) {
		if (value == null) return null;
		return value.toString();
	}
}