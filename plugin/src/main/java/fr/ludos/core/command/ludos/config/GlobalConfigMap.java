package fr.ludos.core.command.ludos.config;

import java.util.Set;

import fr.ludos.core.command.ludos.game.GameConfigMap;
import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.group.GroupConfigMap;

public class GlobalConfigMap extends ConfigOptionsMap {
	public static final GlobalConfigMap INSTANCE = new GlobalConfigMap();

	public GlobalConfigMap() {
		super(null, Set.of(LudosConfigMap.INSTANCE, GroupConfigMap.INSTANCE, GameConfigMap.INSTANCE));
	}
}
