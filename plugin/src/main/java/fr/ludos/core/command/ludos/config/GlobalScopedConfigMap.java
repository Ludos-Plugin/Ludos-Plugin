package fr.ludos.core.command.ludos.config;

import java.util.Set;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.command.ludos.config.ludos.LudosConfigMap;
import fr.ludos.core.command.ludos.config.player.PlayerConfigMap;
import fr.ludos.core.config.ConfigOptionsMap;

/**
 * Config Options Map for Globally scoped configuration.
 * This is used to limit the subsequent config options to Global configuration.
 */
public class GlobalScopedConfigMap extends ConfigOptionsMap {
	public GlobalScopedConfigMap(Ludos ludos) {
		super(null, Set.of(
			LudosConfigMap.INSTANCE,
			GroupConfigMap.INSTANCE,
			ludos.getGameManager().configMap,
			ludos.getRoleManager().configMap,
			PlayerConfigMap.INSTANCE
		));
	}
}
