package fr.ludos.core.command.ludos.config;

import java.util.Set;

import fr.ludos.core.command.ludos.config.player.PlayerConfigMap;
import fr.ludos.core.command.ludos.config.role.RoleConfigMap;
import fr.ludos.core.config.ConfigOptionsMap;

public class PlayerScopedConfigMap extends ConfigOptionsMap {
	public static final PlayerScopedConfigMap INSTANCE = new PlayerScopedConfigMap();

	public PlayerScopedConfigMap() {
		super(null, Set.of(RoleConfigMap.INSTANCE, PlayerConfigMap.INSTANCE));
	}
}
