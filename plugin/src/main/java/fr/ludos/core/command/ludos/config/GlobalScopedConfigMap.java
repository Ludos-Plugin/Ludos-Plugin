package fr.ludos.core.command.ludos.config;

import java.util.Set;

import fr.ludos.core.command.ludos.config.game.GameConfigMap;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.command.ludos.config.ludos.LudosConfigMap;
import fr.ludos.core.command.ludos.config.player.PlayerConfigMap;
import fr.ludos.core.command.ludos.config.role.RoleConfigMap;
import fr.ludos.core.config.ConfigOptionsMap;

public class GlobalScopedConfigMap extends ConfigOptionsMap {
	public static final GlobalScopedConfigMap INSTANCE = new GlobalScopedConfigMap();

	public GlobalScopedConfigMap() {
		super(null, Set.of(LudosConfigMap.INSTANCE, GroupConfigMap.INSTANCE, GameConfigMap.INSTANCE, RoleConfigMap.INSTANCE, PlayerConfigMap.INSTANCE));
	}
}
