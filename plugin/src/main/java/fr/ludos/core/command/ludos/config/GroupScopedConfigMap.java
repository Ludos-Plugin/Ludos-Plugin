package fr.ludos.core.command.ludos.config;

import java.util.Set;

import fr.ludos.core.command.ludos.config.game.GameConfigMap;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.command.ludos.config.player.PlayerConfigMap;
import fr.ludos.core.command.ludos.config.role.RoleConfigMap;
import fr.ludos.core.config.ConfigOptionsMap;

public class GroupScopedConfigMap extends ConfigOptionsMap {
	public static final GroupScopedConfigMap INSTANCE = new GroupScopedConfigMap();

	public GroupScopedConfigMap() {
		super(null, Set.of(GroupConfigMap.INSTANCE, GameConfigMap.INSTANCE, RoleConfigMap.INSTANCE, PlayerConfigMap.INSTANCE));
	}
}
