package fr.ludos.core.persistence;

import javax.annotation.Nullable;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionProvider;
import fr.ludos.core.persistence.serializer.Serializer;
import fr.ludos.core.role.Role;

/**
 * An interface for easier Persistence management for a type T value.
 * @param <T> The type of value to persist
 */
public interface PersistentEntry<T> {
	public String key();
	public Serializer<T, ?> getSerializer();
	public T defaultValue();

	public static <T> PersistentEntry<T> of(Serializer<T, ?> serializer, String key, T defaultValue) {
		return new PersistentEntry<T>() {
			@Override
			public Serializer<T, ?> getSerializer() {
				return serializer;
			}
			@Override
			public String key() {
				return key;
			}
			@Override
			public T defaultValue() {
				return defaultValue;
			}
		};
	}

	public default PersistentAccessor<T> asAccessor(ConfigSectionProvider provider, CommandSender sender) {
		return new PersistentAccessor<>(this, provider, sender);
	}

	public default @Nullable T getOrNull(ConfigurationSection config) {
		return getSerializer().get(key(), config);
	}
	public default @Nullable T getOrDefault(ConfigurationSection config) {
		T found = getOrNull(config);
		if (found != null) return found;

		return defaultValue();
	}

	public default @Nullable T getOrNull(ConfigurationSection config, ConfigurationSection fallback) {
		T first = getOrNull(config);
		if (first != null) return first;

		T second = getOrNull(fallback);
		if (second != null) return second;

		return null;
	}
	public default @Nullable T getOrDefault(ConfigurationSection config, ConfigurationSection fallback) {
		T found = getOrNull(config, fallback);
		if (found != null) return found;

		return defaultValue();
	}

	public default @Nullable T getOrNull(ConfigurationSection scopedConfig, ConfigurationSection config, ConfigurationSection fallback) {
		T first = getOrNull(scopedConfig);
		if (first != null) return first;

		T second = getOrNull(config);
		if (second != null) return second;

		T third = getOrNull(fallback);
		if (third != null) return third;

		return null;
	}
	public default @Nullable T getOrDefault(ConfigurationSection scopedConfig, ConfigurationSection config, ConfigurationSection fallback) {
		T found = getOrNull(scopedConfig, config, fallback);
		if (found != null) return found;

		return defaultValue();
	}

	public default void set(T value, ConfigurationSection config) {
		getSerializer().set(key(), value, config);
	}

	public default void unset(ConfigurationSection config) {
		getSerializer().unset(key(), config);
	}

	public default T getPluginConfig(Ludos ludos) {
		return getOrDefault(ludos.getPluginConfig());
	}
	public default T getGroupConfig(Group group) {
		return getOrDefault(group.getGroupConfig(), group.getManager().getGlobalGroupConfig());
	}
	public default T getGameConfig(Group group, Game.Builder game) {
		return getOrDefault(group.getGameConfig(game), game.getManager().getGlobalGameConfig(game));
	}
	public default T getRoleConfig(Group group, Role.Builder role) {
		return getOrDefault(group.getRoleConfig(role), role.getLudos().getGlobalRoleConfig(role));
	}
	public default T getRoleConfig(OfflinePlayer player, Ludos ludos, Role.Builder role) {
		ConfigurationSection playerScopedConfig = ludos.getPlayerRoleConfig(player, role);
		ConfigurationSection globalScopedConfig = ludos.getGlobalRoleConfig(role);
		Group group = ludos.getGroupManager().getGroupOfPlayer(player);
		if (group == null) {
			return getOrDefault(playerScopedConfig, globalScopedConfig);
		}
		return getOrDefault(playerScopedConfig, group.getRoleConfig(role), globalScopedConfig);
	}
	public default T getPlayerConfig(OfflinePlayer player, Ludos ludos) {
		ConfigurationSection playerScopedConfig = ludos.getPlayerConfig(player);
		ConfigurationSection globalScopedConfig = ludos.getGlobalPlayerConfig();
		Group group = ludos.getGroupManager().getGroupOfPlayer(player);
		if (group == null) {
			return getOrDefault(playerScopedConfig, globalScopedConfig);
		}
		return getOrDefault(playerScopedConfig, group.getPlayerConfig(), globalScopedConfig);
	}
}
