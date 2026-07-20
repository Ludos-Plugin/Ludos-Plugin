package fr.ludos.core.command.ludos.config;

import java.util.Set;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.command.ludos.config.player.PlayerConfigMap;
import fr.ludos.core.config.ConfigOptions;
import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.group.Group;

/**
 * {@link ConfigOptionsMap} for {@link Group}-scoped configuration.
 * This is used to limit the subsequent {@link ConfigOptions} to the scope of a single {@link Group}.
 */
public class GroupScopedConfigMap extends ConfigOptionsMap {
	public GroupScopedConfigMap(Ludos ludos) {
		super(null, Set.of(
			GroupConfigMap.INSTANCE,
			ludos.getGameManager().configMap,
			ludos.getRoleManager().configMap,
			PlayerConfigMap.INSTANCE
		));
	}
}
