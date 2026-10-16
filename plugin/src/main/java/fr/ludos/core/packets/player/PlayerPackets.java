package fr.ludos.core.packets.player;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Provides player-related packet utilities.
 *
 * Implementations should handle sending the necessary packets to make an
 * entity glow for a specific viewing player.
 */
public interface PlayerPackets {
	/**
	 * Set or unset the glow effect of a target entity for a specific viewer.
	 *
	 * @param target the entity to modify
	 * @param viewer the player who should see the change
	 * @param value  true to enable glow, false to disable
	 */
	void setGlowForPlayer(Entity target, Player viewer, boolean value);
}