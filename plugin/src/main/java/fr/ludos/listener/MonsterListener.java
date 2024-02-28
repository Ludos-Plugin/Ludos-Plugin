package fr.ludos.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * MonsterTargetListener is a Bukkit event listener that cancels targeting events for specific entities.
 * In this case, it prevents zombies and skeletons from targeting a specified player.
 * @author feur25
 * @version 1.0
 * @see org.bukkit.entity.EntityType
 * @see org.bukkit.entity.Player
 * @see org.bukkit.event.EventHandler
 * @see org.bukkit.event.Listener
 * @see org.bukkit.event.entity.EntityTargetEvent
 */

public class MonsterListener implements Listener {

	private Player playerToIgnore;

	/**
	 * Constructs a new MonsterTargetListener with the specified player to ignore.
	 *
	 * @param playerToIgnore The player that will be ignored by targeted zombies and skeletons.
	 */

	public MonsterListener(Player playerToIgnore) {
		this.playerToIgnore = playerToIgnore;
	}

	/**
	 * Handles EntityTargetEvent to cancel targeting for zombies and skeletons towards the specified player.
	 *
	 * @param event The EntityTargetEvent triggered when an entity targets another entity.
	 */

	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.getEntityType() == EntityType.ZOMBIE || event.getEntityType() == EntityType.SKELETON) {
			if (event.getTarget() instanceof Player) {
				Player targetPlayer = (Player) event.getTarget();
				if (targetPlayer.equals(playerToIgnore)) {
					event.setCancelled(true);
				}
			}
		}
	}
}
