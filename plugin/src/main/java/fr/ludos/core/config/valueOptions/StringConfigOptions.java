package fr.ludos.core.config.valueOptions;

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public final class StringConfigOptions extends ValueConfigOptions<String> {
	private final @NotNull Set<@NotNull String> values;
	private final @Nullable String defaultValue;

	public StringConfigOptions(@NotNull String name, @NotNull String key, @Nullable String emptyValue, @NotNull Set<@NotNull String> values, String defaultValue) {
		super(name, key, emptyValue);
		this.values = Objects.requireNonNull(values);
		this.defaultValue = defaultValue;
	}
	public StringConfigOptions(@NotNull String name, @NotNull String key, @Nullable String emptyValue, Set<String> values) {
		this(name, key, emptyValue, values, null);
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String getValueOrNull(ConfigurationSection config) {
		return config.getString(key());
	}

	@Override
	public @NotNull Set<@NotNull String> getValidOptions(CommandSender player) {
		return values;
	}
	@Override
	protected String fromString(String value) {
		if (value == null) return null;
		return value;
	}
	@Override
	protected String toString(String value) {
		if (value == null) return null;
		return value;
	}
}
