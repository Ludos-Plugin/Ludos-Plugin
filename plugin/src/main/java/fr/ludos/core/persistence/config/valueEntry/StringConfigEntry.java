package fr.ludos.core.persistence.config.valueEntry;

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.serializer.Serializer;
import fr.ludos.core.persistence.serializer.StringSerializer;

/**
 * {@link ValueConfigEntry} for {@link String}s.
 */
public final class StringConfigEntry extends ValueConfigEntry<String, String> {
	private final @NotNull Set<@NotNull String> values;
	private final @Nullable String defaultValue;

	public StringConfigEntry(@NotNull String name, @NotNull String key, @Nullable String emptyValue, @NotNull Set<@NotNull String> values, String defaultValue) {
		super(name, key, emptyValue);
		this.values = Objects.requireNonNull(values);
		this.defaultValue = defaultValue;
	}
	public StringConfigEntry(@NotNull String name, @NotNull String key, @Nullable String emptyValue, Set<String> values) {
		this(name, key, emptyValue, values, null);
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public @NotNull Set<@NotNull String> getValidOptions(CommandSender player) {
		return values;
	}
	@Override
	protected Serializer<String, String> getSerializer() {
		return StringSerializer.INSTANCE;
	}
}
