package fr.ludos.core.persistence.config.valueEntry;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.serializer.EnumSerializer;
import fr.ludos.core.persistence.serializer.Serializer;

/**
 * {@link ConfigEntry} for {@link Enum} values.
 * @param <T> The Enum type of the values
 */
public class EnumConfigEntry<T extends Enum<T>> extends ConfigEntry<T, String> {
	private final @NotNull Class<T> clazz;
	private final EnumSerializer<T> serializer;
	private final @Nullable T defaultValue;

	public EnumConfigEntry(@NotNull String name, @NotNull String key, @NotNull Class<T> clazz, @Nullable T defaultValue) {
		super(name, key);
		this.clazz = Objects.requireNonNull(clazz);
		this.serializer = Objects.requireNonNull(new EnumSerializer<>(clazz));
		this.defaultValue = defaultValue;
	}
	public EnumConfigEntry(@NotNull String name, @NotNull String key, @NotNull Class<T> clazz) {
		this(name, key, clazz, null);
	}

	@Override
	public T getDefaultValue() {
		return defaultValue != null
			? defaultValue
			: clazz.getEnumConstants()[0];
	}
	@Override
	public @NotNull Set<@NotNull String> options(CommandSender player) {
		return Arrays.stream(clazz.getEnumConstants())
			.map(Enum::name)
			.collect(Collectors.toSet());
	}
	@Override
	protected Serializer<T, String> getSerializer() {
		return serializer;
	}
}
