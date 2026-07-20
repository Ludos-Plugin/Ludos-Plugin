package fr.ludos.core.config.valueOptions;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * {@link ValueConfigOptions} for {@link Boolean}s.
 */
public class BooleanConfigOptions extends ValueConfigOptions<Boolean> {
	public final static String FALSE_STRING = "false";
	public final static String TRUE_STRING = "true";

	private final boolean defaultValue;

	public BooleanConfigOptions(@NotNull String name, @NotNull String key, Boolean defaultValue, String emptyValue) {
		super(name, key, emptyValue);
		this.defaultValue = defaultValue;
	}
	public BooleanConfigOptions(@NotNull String name, @NotNull String key, Boolean defaultValue) {
		this(name, key, defaultValue, null);
	}

	@Override
	public Boolean getDefaultValue() {
		return defaultValue;
	}
	@Override
	public @NotNull Set<@NotNull String> getValidOptions(CommandSender sender) {
		return Set.of(FALSE_STRING, TRUE_STRING);
	}

	@Override
	public Boolean getValueOrNull(ConfigurationSection config) {
		if (! config.isBoolean(key())) return null;
		return config.getBoolean(key());
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
