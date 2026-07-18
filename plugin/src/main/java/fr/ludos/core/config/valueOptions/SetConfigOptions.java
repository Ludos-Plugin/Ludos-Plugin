package fr.ludos.core.config.valueOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SetConfigOptions<T> extends ValueConfigOptions<Set<T>> {

	public SetConfigOptions(@NotNull String name, @NotNull String key, String placeholderValue) {
		super(name, key, placeholderValue);
	}

	public String getterMessage(String value) {
		if (value == null) return placeholderValue();
		return value;
	}

	@Override
	public Set<T> getDefaultValue() {
		return Collections.emptySet();
	}

	@Override
	public boolean isDefaultArgs(@NotNull String[] args, CommandSender sender) {
		return args.length == 1 && args[0].equals(placeholderValue());
	}
	@Override
	public Set<T> parseValueFromArgs(@NotNull String[] args, CommandSender sender) {
		return Arrays.stream(args)
			.map(this::parseSingleValueFromArg)
			.filter(a -> validateParsedValueFromArg(a, sender))
			.collect(Collectors.toSet());
	}

	@Override
	protected boolean setValue(Set<T> value, ConfigurationSection config) {
		if (value == null || value.isEmpty()) return false;

		config.set(
			key(),
			value.stream()
				.map(this::parseSingleValueToString)
				.collect(Collectors.toList())
		);
		return true;
	}
	@Override
	public Set<T> getValueOrNull(ConfigurationSection config) {
		if (! config.isList(key())) return null;
		List<String> underlying = config.getStringList(key());

		if (underlying.isEmpty()) return null;

		return underlying.stream()
			.map(this::parseSingleValueFromArg)
			.collect(Collectors.toSet());
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		Set<String> options = getOptions(sender);
		if (args.length <= 1) {
			return options.stream().toList();
		}

		if (args[0].equals(placeholderValue())) {
			return Collections.emptyList();
		}

		options.remove(placeholderValue());

		for (int i = 0; i < args.length - 1; i++) {
			options.remove(args[i]);
		}
		return options.stream().toList();
	}

	@Override
	public Set<T> fromString(String value) {
		if (value == null) return null;
		return Arrays.stream(value.split(" "))
			.map(this::parseSingleValueFromArg)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}
	@Override
	public String toString(Set<T> value) {
		if (value == null) return null;
		if (value.isEmpty()) return placeholderValue();
		return value.stream()
			.map(this::parseSingleValueToString)
			.filter(Objects::nonNull)
			.collect(Collectors.joining(", "));
	}

	public abstract T parseSingleValueFromArg(@NotNull String arg);
	public abstract boolean validateParsedValueFromArg(T argValue, CommandSender sender);
	public abstract String parseSingleValueToString(T value);
}
