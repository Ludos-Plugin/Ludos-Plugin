package fr.ludos.core.command.ludos.config;

import java.util.Set;

import fr.ludos.core.command.ludos.game.GameConfigMap;
import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.group.GroupConfigMap;

public class GlobalConfigMap extends ConfigOptionsMap {
	public static final GlobalConfigMap instance = new GlobalConfigMap();

	public GlobalConfigMap() {
		super(null, Set.of(LudosConfigMap.instance, GroupConfigMap.instance, GameConfigMap.instance));
	}
}
