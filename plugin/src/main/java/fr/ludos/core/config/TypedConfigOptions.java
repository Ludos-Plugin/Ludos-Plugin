package fr.ludos.core.config;

import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public abstract class TypedConfigOptions<T> extends ConfigOptions {

	public TypedConfigOptions(@NotNull String name) {
		super(name);
	}

	public abstract @Nullable T getDefaultTypedValue();
	public final @Nullable String getDefaultValue() {
		return toString(getDefaultTypedValue());
	}
	public final @Nullable T getTypedValueOrDefault(String key, ConfigurationSection container) {
		String found = container.getString(key);
		if (found == null) return getDefaultTypedValue();

		return fromString(found);
	}

	protected abstract @Nullable T fromString(String value);
	protected abstract @Nullable String toString(T value);
}
