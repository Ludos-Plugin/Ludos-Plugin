package fr.ludos.core.config;

/**
 * An entry in a {@link ConfigOptionsMap}.
 */
public interface ConfigEntryInterface {
	public String key();
	public ConfigOptions options();
}
