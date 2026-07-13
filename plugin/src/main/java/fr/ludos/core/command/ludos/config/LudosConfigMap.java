package fr.ludos.core.command.ludos.config;

import java.util.Set;

import fr.ludos.core.command.ludos.game.GameConfigMap;
import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.group.GroupConfigMap;

public class LudosConfigMap extends ConfigOptionsMap {
	public static final LudosConfigMap instance = new LudosConfigMap();

	public LudosConfigMap() {
		super(null, Set.of(GroupConfigMap.instance, GameConfigMap.instance));
	}
}
