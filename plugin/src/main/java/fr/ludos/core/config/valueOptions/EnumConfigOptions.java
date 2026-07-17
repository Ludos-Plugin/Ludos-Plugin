package fr.ludos.core.config.valueOptions;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public final class EnumConfigOptions<T extends Enum<T>> extends ValueConfigOptions<T> {
	private final @NotNull Class<T> clazz;
	private final @Nullable T defaultValue;

	public EnumConfigOptions(@NotNull String name, @NotNull String key, @Nullable String emptyValue, @NotNull Class<T> clazz, @Nullable T defaultValue) {
		super(name, key, emptyValue);
		this.clazz = Objects.requireNonNull(clazz);
		this.defaultValue = defaultValue;
	}
	public EnumConfigOptions(@NotNull String name, @NotNull String key, @Nullable String emptyValue, @NotNull Class<T> clazz) {
		this(name, key, emptyValue, clazz, null);
	}

	@Override
	public T getDefaultValue() {
		return defaultValue != null
			? defaultValue
			: clazz.getEnumConstants()[0];
	}
	@Override
	public @NotNull Set<@NotNull String> getValidOptions(CommandSender player) {
		return Arrays.stream(clazz.getEnumConstants())
			.map(Enum::name)
			.collect(Collectors.toSet());
	}

	@Override
	public T getValueOrNull(ConfigurationSection config) {
		return fromString(config.getString(key()));
	}

	@Override
	protected boolean setValue(T value, ConfigurationSection config) {
		if (value == null) {
			config.set(key(), null);
			return false;
		}
		config.set(key(), value.name());
		return true;
	}

	@Override
	protected T fromString(String value) {
		if (value == null) return null;
		try {
			return Enum.valueOf(clazz, value);
		} catch (Exception e) {
			return null;
		}
	}
	@Override
	protected String toString(T value) {
		if (value == null) return null;
		return value.name();
	}
}
