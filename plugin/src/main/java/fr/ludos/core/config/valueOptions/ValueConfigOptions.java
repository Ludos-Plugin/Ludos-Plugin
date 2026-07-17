package fr.ludos.core.config.valueOptions;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.config.ConfigEntryInterface;
import fr.ludos.core.config.ConfigOptions;
import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;
import fr.ludos.core.role.Role;

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

		T second = getValueOrNull(fallback);
		if (second != null) return second;

		return null;
	}
	public final @Nullable T getValueOrDefault(ConfigurationSection config, ConfigurationSection fallback) {
		T found = getValueOrNull(config, fallback);
		if (found != null) return found;

		return getDefaultValue();
	}

	public final @Nullable T getValueOrNull(ConfigurationSection scopedConfig, ConfigurationSection config, ConfigurationSection fallback) {
		T first = getValueOrNull(scopedConfig);
		if (first != null) return first;

		T second = getValueOrNull(config);
		if (second != null) return second;

		T third = getValueOrNull(fallback);
		if (third != null) return third;

		return null;
	}
	public final @Nullable T getValueOrDefault(ConfigurationSection scopedConfig, ConfigurationSection config, ConfigurationSection fallback) {
		T found = getValueOrNull(scopedConfig, config, fallback);
		if (found != null) return found;

		return getDefaultValue();
	}

	public final @Nullable T getPluginConfig(Ludos ludos) {
		return getValueOrDefault(ludos.getPluginConfig());
	}
	public final @Nullable T getGroupConfig(Group group) {
		return getValueOrDefault(group.getGroupConfig(), group.getLudos().getGlobalGroupConfig());
	}
	public final @Nullable T getGameConfig(Group group, Game.Builder game) {
		return getValueOrDefault(group.getGameConfig(game), group.getLudos().getGlobalGameConfig(game));
	}
	public final @Nullable T getRoleConfig(Group group, Role.Builder role) {
		return getValueOrDefault(group.getRoleConfig(role), group.getLudos().getGlobalRoleConfig(role));
	}
	public final @Nullable T getRoleConfig(OfflinePlayer player, Ludos ludos, Role.Builder role) {
		ConfigurationSection playerScopedConfig = ludos.getPlayerRoleConfig(player, role);
		ConfigurationSection globalScopedConfig = ludos.getGlobalRoleConfig(role);
		Group group = Group.getGroupOfPlayer(player);
		if (group == null) {
			return getValueOrDefault(playerScopedConfig, globalScopedConfig);
		}
		return getValueOrDefault(playerScopedConfig, group.getRoleConfig(role), globalScopedConfig);
	}
	public final @Nullable T getPlayerConfig(OfflinePlayer player, Ludos ludos) {
		ConfigurationSection playerScopedConfig = ludos.getPlayerConfig(player);
		ConfigurationSection globalScopedConfig = ludos.getGlobalPlayerConfig();
		Group group = Group.getGroupOfPlayer(player);
		if (group == null) {
			return getValueOrDefault(playerScopedConfig, globalScopedConfig);
		}
		return getValueOrDefault(playerScopedConfig, group.getPlayerConfig(), globalScopedConfig);
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
			sender.sendMessage(getterMessage(config));
			return false;
		}

		if (isDefaultArgs(args, sender)) {
			unsetValue(config);
			notifyUnset(sender);
			return true;
		}

		T parsed = parseValueFromArgs(args, sender);
		if (parsed == null) return false;

		if (! setValue(parsed, config)) return false;

		notifySet(parsed, sender);
		return true;
	}

	/**
	 * Unset/reset the value in the given {@link ConfigurationSection}.
	 * @param config The Configuration section to use as a root path for the unsetting.
	 * @return Whether or not the operation was successful.
	 */
	protected boolean unsetValue(ConfigurationSection config) {
		config.set(key, null);
		return true;
	}
	/**
	 * Set the new value in the given {@link ConfigurationSection}.
	 * @param value The new value to set in the given {@link ConfigurationSection}
	 * @param config The Configuration section to use as a root path for the setting.
	 * @return Whether or not the operation was successful, if the given {@code value} is null, return false.
	 */
	protected boolean setValue(T value, ConfigurationSection config) {
		if (value == null) return false;

		config.set(key, value);
		return true;
	}

	/**
	 * Function to determine whether a set of arguments will reset the active Config Options value.
	 * @param args The arguments passed for the command performed
	 * @param sender The Command Sender who performed the command
	 * @return
	 * Whether or not these args correspond to a "reset" command for this option<br/>
	 * This usually means that the args are equal to [{@link #placeholderValue}, ...]
	 * For example, with a boolean Config Options, <code>ludos config global player guidebook_message default</code>, the value will be reset.
	 */
	public boolean isDefaultArgs(@NotNull String[] args, CommandSender sender) {
		return args[0].equals(placeholderValue);
	}
	/**
	 * Parse the given args as a native T type.
	 * @param args The arguments passed for the command performed
	 * @param sender The Command Sender who performed the command
	 * @return A valid instance of T if the args were valid, or null.
	 */
	public T parseValueFromArgs(@NotNull String[] args, CommandSender sender) {
		return fromString(args[0]);
	}

	protected void notifyUnset(CommandSender sender) {
		sender.sendMessage(getName() + " reset");
	}
	protected void notifySet(T value, CommandSender sender) {
		String parsed = toString(value);
		if (parsed == null) {
			sender.sendMessage(getName() + " set to irrepresentable value");
		}

		sender.sendMessage(getName() + " set to " + parsed);
	}

	/**
	 * The message sent to the command sender, when no option value was given.</br>
	 * We use this to give the current set value to the user.
	 * @param config The Configuration section to use as a root path for the fetching.
	 * @return A more detailed, if necessary, value to return to the player.
	 */
	public String getterMessage(ConfigurationSection config) {
		String valueString = toString(getValueOrNull(config));
		return getterMessage(valueString);
	}
	/**
	 * The message sent to the command sender, when no option value was given.</br>
	 * We use this to give the current set value to the user.
	 * @param value The parsed String value that was fetched.
	 * @return A more detailed, if necessary, value to return to the player.
	 */
	public String getterMessage(String value) {
		if (value == null) {
			String defaultValue = toString(getDefaultValue());
			return defaultValue != null
				? placeholderValue + " (" + defaultValue + ")"
				: placeholderValue;
		}
		return value;
	}

	/**
	 * The default value that will be returned when fetching the value, when the option was not set, or after it was reset, using {@link #placeholderValue}.</br>
	 * Using {@link #getDefaultValue} with {@link #getDefaultValue} should NEVER result in null, for the sake of sender messages.
	 * @return The default value. Do not return null, unless the {@link #getValueOrDefault} call-sites are null-proof.
	 */
	public abstract @NotNull T getDefaultValue();
	/**
	 * Gets the valid options available for this parameter. Does not include {@link #placeholderValue}.
	 * @param sender The Command Sender who performed the command
	 * @return The valid options (except for {@link #placeholderValue}) available to the {@code sender} for passing as the next argument in the chain.
	 */
	public abstract @NotNull Set<@NotNull String> getValidOptions(CommandSender sender);

	/**
	 * Fetches the currently set value of this ConfigOptions in the given {@link ConfigurationSection}.</br>
	 * Returns null if the value was not set, or if it was reset, using {@link #placeholderValue}.
	 * @param config The Configuration section to use as a root path for the fetching.
	 * @return The value found in the config, or null.
	 */
	public abstract @Nullable T getValueOrNull(ConfigurationSection config);

	/**
	 * Function to convert a single String value to a native T type value.</br>
	 * If the given String value is invalid/non-parsable, return null.</br>
	 * @param value The representative String value to parse to a native T type.
	 * @return The parsed T value or null if an invalid/non-parsable String `value` was given.
	 */
	protected abstract @Nullable T fromString(String value);
	/**
	 * Function to convert a T native value to a parsed String value.</br>
	 * If the given T value is invalid/null, return null.
	 * @param value The T native value type to represent as a string
	 * @return The representative String or null if an invalid/null T `value` was given.
	 */
	protected abstract @NotNull String toString(T value);
}
