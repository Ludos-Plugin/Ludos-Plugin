package fr.ludos.core.persistence.config;

import fr.ludos.core.persistence.config.valueEntry.ConfigEntry;

/**
 * The type of Interaction with a Persistence item. Mostly used by {@link ConfigEntry}
 */
public enum ConfigNodeOperation {
	get,
	set,
	reset;
}