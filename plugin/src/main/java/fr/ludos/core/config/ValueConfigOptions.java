package fr.ludos.core.config;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;

public abstract class ValueConfigOptions<T> extends ConfigOptions implements ConfigEntryInterface {
	public static final String DEFAULT_PLACEHOLDER_VALUE = "default";
	private final @NotNull String name;
	public @NotNull String getName() {
		return name;
	}

	private final @NotNull String key;
	public @NotNull String key() {
		return key;
	}

	private final @NotNull String placeholderValue;
	public final @NotNull String placeholderValue() {
		return placeholderValue;
	}

	@Override
	public ConfigOptions options() {
		return this;
	}

	public ValueConfigOptions(@NotNull String name, @NotNull String key, @Nullable String placeholderValue) {
		this.name = ObjectUtils.requireNonEmpty(name);
		this.key = ObjectUtils.requireNonEmpty(key);
		this.placeholderValue = (placeholderValue != null && ! placeholderValue.isBlank()) ? placeholderValue : DEFAULT_PLACEHOLDER_VALUE;
	}

	public final @Nullable T getValueOrDefault(ConfigurationSection config) {
		T found = getValueOrNull(config);
		if (found != null) return found;

		return getDefaultValue();
	}

	public final @Nullable T getValueOrNull(ConfigurationSection config, ConfigurationSection fallback) {
		T first = getValueOrNull(config);
		if (first != null) return first;

		T second = getValueOrNull(config);
		if (second != null) return second;

		return null;
	}
	public final @Nullable T getValueOrDefault(ConfigurationSection config, ConfigurationSection fallback) {
		T found = getValueOrNull(config, fallback);
		if (found != null) return found;

		return getDefaultValue();
	}

	public final @Nullable T getPluginConfig(Ludos ludos) {
		return getValueOrDefault(ludos.getPluginConfig());
	}
	public final @Nullable T getGroupConfig(Group group) {
		return getValueOrDefault(group.getGroupConfig(), group.getLudos().getGroupConfig());
	}
	public final @Nullable T getGameConfig(Group group, Game.Builder game) {
		return getValueOrDefault(group.getGameConfig(game), group.getLudos().getGameConfig(game));
	}

	@Override
	public @NotNull Set<@NotNull String> getOptions(CommandSender sender) {
		Set<String> options = getValidOptions(sender).stream()
			.collect(Collectors.toCollection(HashSet::new));

		options.add(placeholderValue);

		return options;
	}

	public boolean set(@NotNull String[] args, CommandSender sender, ConfigurationSection config) {
		if (args.length == 0) {
			sender.sendMessage(defaultMessage(config));
			return false;
		}

		if (isDefaultArgs(args, sender)) {
			setValueAndNotify(null, sender, config);
			return true;
		}

		setValueAndNotify(parseValueFromArgs(args, sender), sender, config);
		return true;
	}

	protected boolean setValue(T value, ConfigurationSection config) {
		config.set(key, value);
		return value != null;
	}
	protected void setValueAndNotify(T value, CommandSender sender, ConfigurationSection config) {
		boolean wasValue = setValue(value, config);

		String stringValue = toString(value);
		if (! wasValue || stringValue == null) {
			notifyUnset(sender);
		} else {
			notifySet(stringValue, sender);
		}
	}

	public boolean isDefaultArgs(@NotNull String[] args, CommandSender sender) {
		return args[0].equals(placeholderValue);
	}
	public T parseValueFromArgs(@NotNull String[] args, CommandSender sender) {
		return fromString(args[0]);
	}

	protected void notifyUnset(CommandSender sender) {
		sender.sendMessage(getName() + " reset");
	}
	protected void notifySet(String value, CommandSender sender) {
		sender.sendMessage(getName() + " set to " + value);
	}

	public String defaultMessage(ConfigurationSection config) {
		String returnString = toString(getValueOrNull(config));
		if (returnString.equals(placeholderValue)) {
			return returnString + " (" + toString(getDefaultValue()) + ")";
		}
		return returnString;
	}

	public abstract @Nullable T getDefaultValue();
	protected abstract @NotNull Set<@NotNull String> getValidOptions(CommandSender sender);

	public abstract @Nullable T getValueOrNull(ConfigurationSection config);

	protected abstract @Nullable T fromString(String value);
	protected abstract @Nullable String toString(T value);
}
