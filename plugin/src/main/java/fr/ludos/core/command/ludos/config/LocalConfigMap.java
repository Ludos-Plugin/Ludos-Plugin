package fr.ludos.core.command.ludos.config;

import java.util.Set;

import fr.ludos.core.command.ludos.game.GameConfigMap;
import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.group.GroupConfigMap;

public class LocalConfigMap extends ConfigOptionsMap {
	public static final LocalConfigMap INSTANCE = new LocalConfigMap();

	public LocalConfigMap() {
		super(null, Set.of(GroupConfigMap.INSTANCE, GameConfigMap.INSTANCE));
	}
}
