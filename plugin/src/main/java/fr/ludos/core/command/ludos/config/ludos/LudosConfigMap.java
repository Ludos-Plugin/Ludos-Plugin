package fr.ludos.core.command.ludos.config.ludos;

import java.util.Set;

import fr.ludos.core.config.ConfigOptionsMap;

/**
 * Config Options Map for Plugin configuration.
 */
public final class LudosConfigMap extends ConfigOptionsMap {
	public static final LudosConfigMap INSTANCE = new LudosConfigMap();

	private LudosConfigMap() {
		super("ludos", Set.of());
	}
}