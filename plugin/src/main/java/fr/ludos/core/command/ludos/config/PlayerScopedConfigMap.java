package fr.ludos.core.command.ludos.config;

import java.util.Set;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.ludos.config.player.PlayerConfigMap;
import fr.ludos.core.config.ConfigOptionsMap;

/**
 * Config Options Map for Player-scoped configuration.<br>
 * This is used to limit the subsequent config options to the scope of a single Player.
 */
public class PlayerScopedConfigMap extends ConfigOptionsMap {
	public PlayerScopedConfigMap(Ludos ludos) {
		super(null, Set.of(
			ludos.getRoleManager().configMap,
			PlayerConfigMap.INSTANCE
		));
	}
}
