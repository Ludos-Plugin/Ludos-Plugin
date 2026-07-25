package fr.ludos.core.persistence.config.valueEntry;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.serializer.IntegerSerializer;
import fr.ludos.core.persistence.serializer.Serializer;

/**
 * {@link ConfigEntry} for {@link Number}s.
 */
public class IntegerConfigEntry extends ConfigEntry<Integer, Integer> {
	private final static Set<String> NUMBERS = new HashSet<>() {{add("1"); add("2"); add("3");}};
	private final IntegerSerializer serializer;
	private final @Nullable Set<@NotNull String> suggestions;
	private final @Nullable Integer defaultValue;

	public IntegerConfigEntry(@NotNull String name, @NotNull String key, @NotNull Integer defaultValue, @Nullable Set<@NotNull Integer> suggestions, boolean unsigned) {
		super(name, key);
		this.defaultValue = Objects.requireNonNull(defaultValue);
		this.serializer = unsigned ? IntegerSerializer.UNSIGNED : IntegerSerializer.SIGNED;
		this.suggestions = suggestions != null
			? suggestions.stream().map(i -> i.toString()).collect(Collectors.toSet())
			: null;
	}
	public IntegerConfigEntry(@NotNull String name, @NotNull String key, @NotNull Integer defaultValue, @Nullable Set<@NotNull Integer> suggestions) {
		this(name, key, defaultValue, suggestions, false);
	}
	public IntegerConfigEntry(@NotNull String name, @NotNull String key, @NotNull Integer defaultValue, boolean unsigned) {
		this(name, key, defaultValue, null, unsigned);
	}
	public IntegerConfigEntry(@NotNull String name, @NotNull String key, @NotNull Integer defaultValue) {
		this(name, key, defaultValue, null);
	}

	@Override
	public @Nullable Integer getDefaultValue() {
		return defaultValue;
	}

	@Override
	public @NotNull Set<@NotNull String> options(CommandSender player) {
		return suggestions != null
			? suggestions
			: NUMBERS;
	}
	@Override
	protected Serializer<Integer, Integer> getSerializer() {
		return serializer;
	}
}
