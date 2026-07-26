package fr.ludos.core.persistence;

import javax.annotation.Nullable;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;
import fr.ludos.core.persistence.serializer.Serializer;
import fr.ludos.core.role.Role;

/**
 *
 * @param <T>
 */
public interface PersistentEntry<T> {
	public String key();
	public Serializer<T, ?> getSerializer();
	public T defaultValue();

	public default @Nullable T getValueOrNull(ConfigurationSection config) {
		return getSerializer().get(key(), config);
	}
	public default @Nullable T getValueOrDefault(ConfigurationSection config) {
		T found = getValueOrNull(config);
		if (found != null) return found;

		return defaultValue();
	}

	public default @Nullable T getValueOrNull(ConfigurationSection config, ConfigurationSection fallback) {
		T first = getValueOrNull(config);
		if (first != null) return first;

		T second = getValueOrNull(fallback);
		if (second != null) return second;

		return null;
	}
	public default @Nullable T getValueOrDefault(ConfigurationSection config, ConfigurationSection fallback) {
		T found = getValueOrNull(config, fallback);
		if (found != null) return found;

		return defaultValue();
	}

	public default @Nullable T getValueOrNull(ConfigurationSection scopedConfig, ConfigurationSection config, ConfigurationSection fallback) {
		T first = getValueOrNull(scopedConfig);
		if (first != null) return first;

		T second = getValueOrNull(config);
		if (second != null) return second;

		T third = getValueOrNull(fallback);
		if (third != null) return third;

		return null;
	}
	public default @Nullable T getValueOrDefault(ConfigurationSection scopedConfig, ConfigurationSection config, ConfigurationSection fallback) {
		T found = getValueOrNull(scopedConfig, config, fallback);
		if (found != null) return found;

		return defaultValue();
	}

	public default void setValue(T value, ConfigurationSection config) {
		getSerializer().set(key(), value, config);
	}

	public default void unsetValue(ConfigurationSection config) {
		getSerializer().unset(key(), config);
	}

	public default T getPluginConfig(Ludos ludos) {
		return getValueOrDefault(ludos.getPluginConfig());
	}
	public default T getGroupConfig(Group group) {
		return getValueOrDefault(group.getGroupConfig(), group.getManager().getGlobalGroupConfig());
	}
	public default T getGameConfig(Group group, Game.Builder game) {
		return getValueOrDefault(group.getGameConfig(game), game.getManager().getGlobalGameConfig(game));
	}
	public default T getRoleConfig(Group group, Role.Builder role) {
		return getValueOrDefault(group.getRoleConfig(role), role.getLudos().getGlobalRoleConfig(role));
	}
	public default T getRoleConfig(OfflinePlayer player, Ludos ludos, Role.Builder role) {
		ConfigurationSection playerScopedConfig = ludos.getPlayerRoleConfig(player, role);
		ConfigurationSection globalScopedConfig = ludos.getGlobalRoleConfig(role);
		Group group = ludos.getGroupManager().getGroupOfPlayer(player);
		if (group == null) {
			return getValueOrDefault(playerScopedConfig, globalScopedConfig);
		}
		return getValueOrDefault(playerScopedConfig, group.getRoleConfig(role), globalScopedConfig);
	}
	public default T getPlayerConfig(OfflinePlayer player, Ludos ludos) {
		ConfigurationSection playerScopedConfig = ludos.getPlayerConfig(player);
		ConfigurationSection globalScopedConfig = ludos.getGlobalPlayerConfig();
		Group group = ludos.getGroupManager().getGroupOfPlayer(player);
		if (group == null) {
			return getValueOrDefault(playerScopedConfig, globalScopedConfig);
		}
		return getValueOrDefault(playerScopedConfig, group.getPlayerConfig(), globalScopedConfig);
	}
}
