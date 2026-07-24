package fr.ludos.core.persistence.config.valueEntry;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.serializer.BooleanSerializer;
import fr.ludos.core.persistence.serializer.Serializer;

/**
 * {@link ValueConfigEntry} for {@link Boolean}s.
 */
public class BooleanConfigEntry extends ValueConfigEntry<Boolean, Boolean> {
	private final boolean defaultValue;

	public BooleanConfigEntry(@NotNull String name, @NotNull String key, Boolean defaultValue, String emptyValue) {
		super(name, key, emptyValue);
		this.defaultValue = defaultValue;
	}
	public BooleanConfigEntry(@NotNull String name, @NotNull String key, Boolean defaultValue) {
		this(name, key, defaultValue, null);
	}

	@Override
	public Boolean getDefaultValue() {
		return defaultValue;
	}
	@Override
	public @NotNull Set<@NotNull String> getValidOptions(CommandSender sender) {
		return Set.of(BooleanSerializer.FALSE_STRING, BooleanSerializer.TRUE_STRING);
	}
	@Override
	protected Serializer<Boolean, Boolean> getSerializer() {
		return BooleanSerializer.INSTANCE;
	}
}
