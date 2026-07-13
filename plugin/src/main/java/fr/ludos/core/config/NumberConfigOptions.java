package fr.ludos.core.config;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class NumberConfigOptions extends ValueConfigOptions<Integer> {
	private final static Set<String> NUMBERS = new HashSet<>() {{add("1"); add("2"); add("3");}};
	private final @Nullable Set<@NotNull String> suggestions;
	private final @Nullable Integer defaultValue;
	private final boolean unsigned;

	public NumberConfigOptions(@NotNull String name, @NotNull String key, @Nullable String emptyValue, @NotNull Integer defaultValue, @Nullable Set<@NotNull Integer> suggestions, boolean unsigned) {
		super(name, key, emptyValue);
		this.defaultValue = Objects.requireNonNull(defaultValue);
		this.suggestions = suggestions != null
			? suggestions.stream().map(i -> i.toString()).collect(Collectors.toSet())
			: null;
		this.unsigned = unsigned;
	}
	public NumberConfigOptions(@NotNull String name, @NotNull String key, @Nullable String emptyValue, @NotNull Integer defaultValue, @Nullable Set<@NotNull Integer> suggestions) {
		this(name, key, emptyValue, defaultValue, suggestions, false);
	}
	public NumberConfigOptions(@NotNull String name, @NotNull String key, @Nullable String emptyValue, @NotNull Integer defaultValue, boolean unsigned) {
		this(name, key, emptyValue, defaultValue, null, unsigned);
	}
	public NumberConfigOptions(@NotNull String name, @NotNull String key, @Nullable String emptyValue, @NotNull Integer defaultValue) {
		this(name, key, emptyValue, defaultValue, null);
	}

	@Override
	public boolean isValidOption(String opt, CommandSender player) {
		return fromString(opt) != null;
	}

	@Override
	public @NotNull Set<@NotNull String> getActualOptions(CommandSender player) {
		return suggestions != null
			? suggestions
			: NUMBERS;
	}

	@Override
	public @Nullable Integer getEmptyValue() {
		return defaultValue;
	}
	@Override
	protected Integer fromString(String value) {
		if (value == null || value.equals(emptyValue())) return null;
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
	protected String toString(Integer value) {
		if (value == null) return emptyValue();
		return value.toString();
	}

}
