package fr.ludos.core.command.ludos.config;

import java.util.Set;

import fr.ludos.core.command.ludos.config.game.GameConfigMap;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.command.ludos.config.player.PlayerConfigMap;
import fr.ludos.core.command.ludos.config.role.RoleConfigMap;
import fr.ludos.core.config.ConfigOptions;
import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.group.Group;

/**
 * {@link ConfigOptionsMap} for {@link Group}-scoped configuration.
 * This is used to limit the subsequent {@link ConfigOptions} to the scope of a single {@link Group}.
 */
public class GroupScopedConfigMap extends ConfigOptionsMap {
	public static final GroupScopedConfigMap INSTANCE = new GroupScopedConfigMap();

	public GroupScopedConfigMap() {
		super(null, Set.of(GroupConfigMap.INSTANCE, GameConfigMap.INSTANCE, RoleConfigMap.INSTANCE, PlayerConfigMap.INSTANCE));
	}
}
