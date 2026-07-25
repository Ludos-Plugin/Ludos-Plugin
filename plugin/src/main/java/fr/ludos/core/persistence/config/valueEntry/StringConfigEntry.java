package fr.ludos.core.persistence.config.valueEntry;

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.serializer.Serializer;
import fr.ludos.core.persistence.serializer.StringSerializer;

/**
 * {@link ConfigEntry} for {@link String}s.
 */
public class StringConfigEntry extends ConfigEntry<String, String> {
	private final @NotNull Set<@NotNull String> values;
	private final @Nullable String defaultValue;

	public StringConfigEntry(@NotNull String name, @NotNull String key, @NotNull Set<@NotNull String> values, String defaultValue) {
		super(name, key);
		this.values = Objects.requireNonNull(values);
		this.defaultValue = defaultValue;
	}
	public StringConfigEntry(@NotNull String name, @NotNull String key, @NotNull Set<@NotNull String> values) {
		this(name, key, values, null);
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public @NotNull Set<@NotNull String> options(CommandSender player) {
		return values;
	}
	@Override
	protected Serializer<String, String> getSerializer() {
		return StringSerializer.INSTANCE;
	}
}
