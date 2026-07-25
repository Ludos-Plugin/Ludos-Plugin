package fr.ludos.core.persistence.config.valueEntry;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeOperation;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import fr.ludos.core.persistence.serializer.Serializer;
import fr.ludos.core.role.Role;

/**
 * {@link ConfigNode} for flat, typed values.
 * @param <TComplex> The type of values, natively supported by this instance. Parsed to and from a String during command running.
 * @param <TPrimitive> The backing type that the value is converted to, before being set in the give {@link ConfigurationSection}
 */
public abstract class ConfigEntry<TComplex, TPrimitive> implements ConfigNode {
	public static final String DEFAULT = "default";

	private final @NotNull String name;
	public @NotNull String getName() {
		return name;
	}

	private final @NotNull String key;
	public @NotNull String key() {
		return key;
	}

	public ConfigEntry(@NotNull String name, @NotNull String key) {
		this.name = ObjectUtils.requireNonEmpty(name);
		this.key = ObjectUtils.requireNonEmpty(key);
	}

	protected abstract Serializer<TComplex, TPrimitive> getSerializer();

	public final @Nullable TComplex getValueOrNull(ConfigurationSection config) {
		return getSerializer().get(key, config);
	}
	public final @Nullable TComplex getValueOrDefault(ConfigurationSection config) {
		TComplex found = getValueOrNull(config);
		if (found != null) return found;

		return getDefaultValue();
	}

	public final @Nullable TComplex getValueOrNull(ConfigurationSection config, ConfigurationSection fallback) {
		TComplex first = getValueOrNull(config);
		if (first != null) return first;

		TComplex second = getValueOrNull(fallback);
		if (second != null) return second;

		return null;
	}
	public final @Nullable TComplex getValueOrDefault(ConfigurationSection config, ConfigurationSection fallback) {
		TComplex found = getValueOrNull(config, fallback);
		if (found != null) return found;

		return getDefaultValue();
	}

	public final @Nullable TComplex getValueOrNull(ConfigurationSection scopedConfig, ConfigurationSection config, ConfigurationSection fallback) {
		TComplex first = getValueOrNull(scopedConfig);
		if (first != null) return first;

		TComplex second = getValueOrNull(config);
		if (second != null) return second;

		TComplex third = getValueOrNull(fallback);
		if (third != null) return third;

		return null;
	}
	public final @Nullable TComplex getValueOrDefault(ConfigurationSection scopedConfig, ConfigurationSection config, ConfigurationSection fallback) {
		TComplex found = getValueOrNull(scopedConfig, config, fallback);
		if (found != null) return found;

		return getDefaultValue();
	}

	public final TComplex getPluginConfig(Ludos ludos) {
		return getValueOrDefault(ludos.getPluginConfig());
	}
	public final TComplex getGroupConfig(Group group) {
		return getValueOrDefault(group.getGroupConfig(), group.getManager().getGlobalGroupConfig());
	}
	public final TComplex getGameConfig(Group group, Game.Builder game) {
		return getValueOrDefault(group.getGameConfig(game), game.getManager().getGlobalGameConfig(game));
	}
	public final TComplex getRoleConfig(Group group, Role.Builder role) {
		return getValueOrDefault(group.getRoleConfig(role), role.getLudos().getGlobalRoleConfig(role));
	}
	public final TComplex getRoleConfig(OfflinePlayer player, Ludos ludos, Role.Builder role) {
		ConfigurationSection playerScopedConfig = ludos.getPlayerRoleConfig(player, role);
		ConfigurationSection globalScopedConfig = ludos.getGlobalRoleConfig(role);
		Group group = ludos.getGroupManager().getGroupOfPlayer(player);
		if (group == null) {
			return getValueOrDefault(playerScopedConfig, globalScopedConfig);
		}
		return getValueOrDefault(playerScopedConfig, group.getRoleConfig(role), globalScopedConfig);
	}
	public final TComplex getPlayerConfig(OfflinePlayer player, Ludos ludos) {
		ConfigurationSection playerScopedConfig = ludos.getPlayerConfig(player);
		ConfigurationSection globalScopedConfig = ludos.getGlobalPlayerConfig();
		Group group = ludos.getGroupManager().getGroupOfPlayer(player);
		if (group == null) {
			return getValueOrDefault(playerScopedConfig, globalScopedConfig);
		}
		return getValueOrDefault(playerScopedConfig, group.getPlayerConfig(), globalScopedConfig);
	}

	public boolean execute(@NotNull String[] args, CommandSender sender, ConfigSectionContext context, ConfigNodeOperation op) {
		if (! context.isAuthorized(sender, op)) return false;
		ConfigurationSection config = context.getConfig(sender);
		switch (op) {
			case get:
				return get(args, sender, config);
			case set:
				return set(args, sender, config);
			case reset:
				return unset(args, sender, config);
			default:
				return false;
		}
	}

	protected boolean get(String[] args, CommandSender sender, ConfigurationSection config) {
		if (args.length == 0) {
			sender.sendMessage(getterMessage(config));
			return true;
		}
		return false;
	}
	protected boolean set(String[] args, CommandSender sender, ConfigurationSection config) {
		TComplex parsed = parseValueFromArgs(args, sender);
		if (parsed == null) return false;

		if (getSerializer().set(key, parsed, config)) {
			notifySet(parsed, sender);
			return true;
		}
		return false;
	}
	protected boolean unset(String[] args, CommandSender sender, ConfigurationSection config) {
		if (args.length == 0) {
			if (getSerializer().unset(key, config)) {
				notifyUnset(sender);
				return true;
			}
		}
		return false;
	}

	@Override
	public List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender, ConfigNodeOperation mode) {
		switch (mode) {
			case get:
			case reset:
				return Collections.emptyList();
			case set:
				return setTabComplete(args, sender);
			default:
				return null;
		}
	}

	protected List<@NotNull String> setTabComplete(String[] args, CommandSender sender) {
		if (args.length <= 1) {
			return options(sender).stream().toList();
		}
		return null;
	}

	/**
	 * Parse the given args as a native T type.
	 * @param args The arguments passed for the command performed
	 * @param sender The Command Sender who performed the command
	 * @return A valid instance of T if the args were valid, or null.
	 */
	public TComplex parseValueFromArgs(@NotNull String[] args, CommandSender sender) {
		String val = args[0];
		TComplex parsed = getSerializer().fromString(val);
		if (! validateValue(parsed, sender)) return null;
		return parsed;
	}
	public boolean validateValue(TComplex value, CommandSender sender) {
		return true;
	}

	protected void notifyUnset(CommandSender sender) {
		sender.sendMessage(getName() + " reset");
	}
	protected void notifySet(TComplex value, CommandSender sender) {
		String parsed = getSerializer().toString(value);
		if (parsed == null) {
			sender.sendMessage(getName() + " set to irrepresentable value");
		}

		sender.sendMessage(getName() + " set to " + parsed);
	}

	/**
	 * The message sent to the command sender, when no option value was given.<br>
	 * We use this to give the current set value to the user.<br>
	 * Must not be null.
	 * @param config The Configuration section to use as a root path for the fetching.
	 * @return A more detailed, if necessary, value to return to the player.
	 */
	public @NotNull String getterMessage(ConfigurationSection config) {
		String valueString = getSerializer().toString(getValueOrNull(config));
		return getterMessage(valueString);
	}
	/**
	 * The message sent to the command sender, when no option value was given.<br>
	 * We use this to give the current set value to the user.
	 * @param value The parsed String value that was fetched.
	 * @return A more detailed, if necessary, value to return to the player.
	 */
	public String getterMessage(String value) {
		if (value == null) {
			String defaultValue = getSerializer().toString(getDefaultValue());
			return defaultValue != null
				? DEFAULT + " (" + defaultValue + ')'
				: DEFAULT;
		}
		return value;
	}

	public TPrimitive serialize(TComplex value) {
		return getSerializer().serialize(value);
	}
	public TComplex parse(TPrimitive primitive) {
		return getSerializer().parse(primitive);
	}
	public String toString(TComplex value) {
		return getSerializer().toString(value);
	}
	public TComplex fromString(String string) {
		return getSerializer().fromString(string);
	}

	/**
	 * The default value that will be returned when fetching the value, when the option was not set, or after it was reset, using {@link #placeholderValue}.<br>
	 * Using {@link #getDefaultValue} with {@link #getDefaultValue} should NEVER result in null, for the sake of sender messages.
	 * @return The default value. Do not return null, unless the {@link #getValueOrDefault} call-sites are null-proof.
	 */
	public abstract @NotNull TComplex getDefaultValue();
}
