package fr.ludos.core.persistence.config;

import javax.annotation.Nullable;

/**
 * A structure to represent a Configurable value (ex: The number of waves in a Raid), and its valid values (options).
 */
public interface ConfigNode extends ConfigRoot {
	public @Nullable String key();
}
