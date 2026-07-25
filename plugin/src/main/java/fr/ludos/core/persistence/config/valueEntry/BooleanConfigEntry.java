package fr.ludos.core.persistence.config.valueEntry;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.serializer.BooleanSerializer;
import fr.ludos.core.persistence.serializer.Serializer;

/**
 * {@link ConfigEntry} for {@link Boolean}s.
 */
public class BooleanConfigEntry extends ConfigEntry<Boolean, Boolean> {
	private final boolean defaultValue;

	public BooleanConfigEntry(@NotNull String name, @NotNull String key, Boolean defaultValue) {
		super(name, key);
		this.defaultValue = defaultValue;
	}

	@Override
	public Boolean getDefaultValue() {
		return defaultValue;
	}
	@Override
	public @NotNull Set<@NotNull String> options(CommandSender sender) {
		return Set.of(BooleanSerializer.FALSE_STRING, BooleanSerializer.TRUE_STRING);
	}
	@Override
	protected Serializer<Boolean, Boolean> getSerializer() {
		return BooleanSerializer.INSTANCE;
	}
}
