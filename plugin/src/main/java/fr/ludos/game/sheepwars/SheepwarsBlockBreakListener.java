package fr.ludos.game.sheepwars;

import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class SheepwarsBlockBreakListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!WorldManager.ACTIVE_WORLD_NAME.equals(event.getBlock().getWorld().getName())) {
			return;
		}

		event.setDropItems(false);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (!WorldManager.ACTIVE_WORLD_NAME.equals(event.getLocation().getWorld().getName())) {
			return;
		}

		event.setYield(0.0F);
	}
}