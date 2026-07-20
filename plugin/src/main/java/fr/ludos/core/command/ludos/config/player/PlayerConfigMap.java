package fr.ludos.core.command.ludos.config.player;

import java.util.Set;

import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.config.valueOptions.BooleanConfigOptions;

/**
 * Config Options Map for Player-specific configuration.
 */
public final class PlayerConfigMap extends ConfigOptionsMap {
	public static final BooleanConfigOptions GUIDEBOOK_MESSAGE =
		new BooleanConfigOptions("Show guidebook message on login", "guidebook_message", true);

	public static final PlayerConfigMap INSTANCE = new PlayerConfigMap();

	private PlayerConfigMap() {
		super("player", Set.of(GUIDEBOOK_MESSAGE));
	}
}