package fr.ludos.core.persistence.config.valueEntry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.persistence.serializer.Serializer;
import fr.ludos.core.persistence.serializer.StringSetSerializer;

/**
 * {@link ValueConfigEntry} for a typed {@link Set} of values.
 * @param <T> The type of data, stored inside the parsed Set
 */
public abstract class SetConfigEntry<T> extends ValueConfigEntry<Set<T>, List<String>> {
	private final StringSetSerializer<T> serializer;

	public SetConfigEntry(@NotNull String name, @NotNull String key, StringSetSerializer<T> serializer, String placeholderValue) {
		super(name, key, placeholderValue);
		this.serializer = serializer;
	}
	public SetConfigEntry(@NotNull String name, @NotNull String key, Serializer<T, String> serializer, String placeholderValue) {
		this(name, key, new StringSetSerializer<>(serializer), placeholderValue);
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
		Set<T> res = Arrays.stream(args)
			.map((s) -> serializer.getSerializer().fromString(s))
			.filter(Objects::nonNull)
			.filter((v) -> validateValue(v, sender))
			.collect(Collectors.toSet());
		if (res.isEmpty()) return null;
		return res;
	}

	public boolean validateValue(T value, CommandSender sender) {
		return true;
	}

	@Override
	protected Serializer<Set<T>, List<String>> getSerializer() {
		return serializer;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		Set<String> options = options(sender);
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
}
