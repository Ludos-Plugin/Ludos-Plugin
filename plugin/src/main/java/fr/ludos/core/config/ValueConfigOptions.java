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
	public static final String DEFAULT_EMPTY_VALUE = "default";
	private final @NotNull String name;
	public @NotNull String getName() {
		return name;
	}

	private final @NotNull String key;
	public @NotNull String key() {
		return key;
	}

	private final @NotNull String emptyValue;
	public final @NotNull String emptyValue() {
		return emptyValue;
	}

	@Override
	public ConfigOptions options() {
		return this;
	}

	public ValueConfigOptions(@NotNull String name, @NotNull String key, @Nullable String emptyValue) {
		this.name = ObjectUtils.requireNonEmpty(name);
		this.key = ObjectUtils.requireNonEmpty(key);
		this.emptyValue = (emptyValue != null && ! emptyValue.isBlank()) ? emptyValue : DEFAULT_EMPTY_VALUE;
	}

	public abstract @Nullable T getDefaultValue();
	public final @Nullable String getDefaultStringValue() {
		return toString(getDefaultValue());
	}

	public final @Nullable String getStringValueOrNull(ConfigurationSection config) {
		return config != null ? config.getString(key) : null;
	}
	public final @Nullable String getStringValueOrNull(ConfigurationSection config, ConfigurationSection fallback) {
		String first = config != null ? config.getString(key) : null;
		if (first != null) return first;

		String second = fallback != null ? fallback.getString(key) : null;
		if (second != null) return second;

		return null;
	}
	public final @Nullable String getStringValueOrDefault(ConfigurationSection config) {
		String found = getStringValueOrNull(config);
		if (found != null) return found;

		return getDefaultStringValue();
	}
	public final @Nullable String getStringValueOrDefault(ConfigurationSection config, ConfigurationSection fallback) {
		String found = getStringValueOrNull(config, fallback);
		if (found != null) return found;

		return getDefaultStringValue();
	}

	protected abstract @Nullable T fromString(String value);
	protected abstract @Nullable String toString(T value);

	public final @Nullable T getValueOrNull(ConfigurationSection config) {
		String found = getStringValueOrNull(config);
		if (found != null) return fromString(found);

		return null;
	}
	public final @Nullable T getValueOrDefault(ConfigurationSection config) {
		T found = getValueOrNull(config);
		if (found != null) return found;

		return getDefaultValue();
	}

	public final @Nullable T getValueOrNull(ConfigurationSection config, ConfigurationSection fallback) {
		String found = getStringValueOrNull(config, fallback);
		if (found != null) return fromString(found);

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

	protected void notifyUnset(CommandSender sender) {
		sender.sendMessage(getName() + " reset");
	}

	@Override
	public boolean isValidOption(String option, CommandSender sender) {
		return getActualOptions(sender).contains(option);
	}

	protected abstract @NotNull Set<@NotNull String> getActualOptions(CommandSender sender);
	@Override
	public @NotNull Set<@NotNull String> getOptions(CommandSender sender) {
		Set<String> options = getActualOptions(sender).stream()
			.collect(Collectors.toCollection(HashSet::new));

		options.add(emptyValue);

		return options;
	}

	public boolean set(@NotNull String[] args, CommandSender sender, ConfigurationSection config) {
		if (args.length == 0) {
			sender.sendMessage(getStringValueOrDefault(config));
			return false;
		}

		String value = args[0];
		if (value.equals(emptyValue)) {
			config.set(key, null);
			notifyUnset(sender);
			return true;
		}

		if (! isValidOption(value, sender)) return false;

		config.set(key, value);
		notifySet(value, sender);
		return true;
	}
	protected void notifySet(String value, CommandSender sender) {
		sender.sendMessage(getName() + " set to " + value);
	}
}
