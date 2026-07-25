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

import fr.ludos.core.persistence.config.ConfigNodeOperation;
import fr.ludos.core.persistence.serializer.Serializer;
import fr.ludos.core.persistence.serializer.StringSetSerializer;

/**
 * {@link ConfigEntry} for a typed {@link Set} of values.
 * @param <T> The type of data, stored inside the parsed Set
 */
public abstract class SetConfigEntry<T> extends ConfigEntry<Set<T>, List<String>> {
	public static final String DEFAULT_PLACEHOLDER = "none";

	private final StringSetSerializer<T> serializer;
	private final String placeholder;

	public SetConfigEntry(@NotNull String name, @NotNull String key, StringSetSerializer<T> serializer, String placeholder) {
		super(name, key);
		this.serializer = serializer;
		this.placeholder = placeholder != null
			? placeholder
			: DEFAULT_PLACEHOLDER;
	}
	public SetConfigEntry(@NotNull String name, @NotNull String key, StringSetSerializer<T> serializer) {
		this(name, key, serializer, null);
	}

	public String getterMessage(String value) {
		if (value == null) return placeholder;
		return value;
	}

	@Override
	public Set<T> getDefaultValue() {
		return Collections.emptySet();
	}

	@Override
	public Set<T> parseValueFromArgs(@NotNull String[] args, CommandSender sender) {
		Set<T> res = Arrays.stream(args)
			.map((s) -> serializer.getSerializer().fromString(s))
			.filter(Objects::nonNull)
			.filter((v) -> validateSingleValue(v, sender))
			.collect(Collectors.toSet());
		if (res.isEmpty()) return null;
		if (! validateValue(res, sender)) return null;
		return res;
	}

	public boolean validateSingleValue(T value, CommandSender sender) {
		return true;
	}

	@Override
	protected Serializer<Set<T>, List<String>> getSerializer() {
		return serializer;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender, ConfigNodeOperation op) {
		Set<String> options = options(sender);
		if (args.length <= 1) {
			return options.stream().toList();
		}

		for (int i = 0; i < args.length - 1; i++) {
			options.remove(args[i]);
		}
		return options.stream().toList();
	}
}
