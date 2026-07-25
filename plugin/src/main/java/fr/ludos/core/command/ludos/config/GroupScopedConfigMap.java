package fr.ludos.core.command.ludos.config;

import java.util.Set;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.command.ludos.config.player.PlayerConfigMap;
import fr.ludos.core.group.Group;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import fr.ludos.core.persistence.config.ConfigRootMap;

/**
 * {@link ConfigNodeMap} for {@link Group}-scoped configuration.
 * This is used to limit the subsequent {@link ConfigNode} to the scope of a single {@link Group}.
 */
public class GroupScopedConfigMap extends ConfigRootMap {
	public GroupScopedConfigMap(Ludos ludos) {
		super(Set.of(
			GroupConfigMap.INSTANCE,
			ludos.getGameManager().configMap,
			ludos.getRoleManager().configMap,
			PlayerConfigMap.INSTANCE
		));
	}
}
