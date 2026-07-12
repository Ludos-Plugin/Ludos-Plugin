package fr.ludos.core.config;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class EnumConfigOptions<T extends Enum<T>> extends ConfigOptions<T> {
	private final @NotNull Class<T> clazz;
	private final @Nullable T defaultValue;

	public EnumConfigOptions(@NotNull String name, @NotNull Class<T> clazz, @Nullable T defaultValue) {
		super(name);
		this.clazz = Objects.requireNonNull(clazz);
		this.defaultValue = defaultValue;
	}
	public EnumConfigOptions(@NotNull String name, @NotNull Class<T> clazz) {
		this(name, clazz, null);
	}

	@Override
	public T getDefaultValue() {
		return defaultValue != null
			? defaultValue
			: clazz.getEnumConstants()[0];
	}
	@Override
	public @NotNull Set<@NotNull String> getOptions(CommandSender player) {
		return Arrays.stream(clazz.getEnumConstants())
			.map(Enum::name)
			.collect(Collectors.toSet());
	}

	@Override
	protected T fromString(String value) {
		try {
			return Enum.valueOf(clazz, value);
		} catch (Exception e) {
			return null;
		}
	}
	@Override
	protected String toString(T value) {
		return value.name();
	}
}
