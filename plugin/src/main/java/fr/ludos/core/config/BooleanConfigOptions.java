package fr.ludos.core.config;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class BooleanConfigOptions extends ValueConfigOptions<Boolean> {
	private final static String falseString = "false";
	private final static String trueString = "true";
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
	protected Boolean fromString(String value) {
		switch (value) {
			case falseString:
				return false;
			case trueString:
				return true;
			default:
				return null;
		}
	}

	@Override
	protected String toString(Boolean value) {
		if (value == null) return null;
		return value
			? trueString
			: falseString;
	}

	@Override
	protected @NotNull Set<@NotNull String> getActualOptions(CommandSender sender) {
		return Set.of(falseString, trueString);
	}

}
