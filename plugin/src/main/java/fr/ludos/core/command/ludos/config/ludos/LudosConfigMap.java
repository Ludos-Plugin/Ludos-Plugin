package fr.ludos.core.command.ludos.config.ludos;

import java.util.Set;

import fr.ludos.core.Ludos;
import fr.ludos.core.persistence.config.ConfigEntriesMap;

/**
 * Config Options Map for Plugin configuration.
 */
public final class LudosConfigMap extends ConfigEntriesMap {
	public static final LudosConfigMap INSTANCE = new LudosConfigMap();

	private LudosConfigMap() {
		super(Ludos.NAMESPACE, Set.of());
	}
}