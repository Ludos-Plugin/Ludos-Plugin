package fr.ludos.core.persistence.serializer;

import org.bukkit.configuration.ConfigurationSection;

/**
 * .
 * @param <TComplex>
 * @param <TPrimitive>
 */
public interface Serializer<TComplex, TPrimitive extends Object> {
	public default TComplex get(String key, ConfigurationSection config) {
		TPrimitive got = getPrimitive(key, config);
		if (got == null) return null;
		return parse(got);
	}
	public default boolean set(String key, TComplex value, ConfigurationSection config) {
		TPrimitive serialized = serialize(value);
		if (serialized == null) return false;
		config.set(key, serialized);
		return true;
	}
	public default boolean unset(String key, ConfigurationSection config) {
		config.set(key, null);
		return true;
	}

	public TComplex parse(TPrimitive primitive);
	public TPrimitive serialize(TComplex value);
	public TPrimitive getPrimitive(String key, ConfigurationSection config);

	public TComplex fromString(String string);
	public String toString(TComplex value);
}