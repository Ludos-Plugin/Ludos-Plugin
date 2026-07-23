package fr.ludos.core.command.ludos.config.player;

import java.util.Set;

import fr.ludos.core.Ludos;
import fr.ludos.core.persistence.config.ConfigEntriesMap;
import fr.ludos.core.persistence.config.valueEntry.BooleanConfigEntry;

/**
 * Config Options Map for Player-specific configuration.
 */
public final class PlayerConfigMap extends ConfigEntriesMap {
	public static final BooleanConfigEntry GUIDEBOOK_MESSAGE =
		new BooleanConfigEntry("Show guidebook message on login", "guidebook_message", true);

	public static final PlayerConfigMap INSTANCE = new PlayerConfigMap();

	private PlayerConfigMap() {
		super(Ludos.PLAYER_NAMESPACE, Set.of(GUIDEBOOK_MESSAGE));
	}
}