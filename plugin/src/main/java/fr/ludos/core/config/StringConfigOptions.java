package fr.ludos.core.config;

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class StringConfigOptions extends ConfigOptions<String> {
	private final @NotNull Set<@NotNull String> values;
	private final @Nullable String defaultValue;

	public StringConfigOptions(@NotNull String name, @NotNull Set<@NotNull String> values, String defaultValue) {
		super(name);
		this.values = Objects.requireNonNull(values);
		this.defaultValue = defaultValue;
	}
	public StringConfigOptions(@NotNull String name, Set<String> values) {
		this(name, values, null);
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}
	@Override
	public @NotNull Set<@NotNull String> getOptions(CommandSender player) {
		return values;
	}
	@Override
	protected String fromString(String value) {
		return value;
	}
	@Override
	protected String toString(String value) {
		return value;
	}
}
