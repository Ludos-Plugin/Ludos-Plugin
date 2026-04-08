package fr.ludos.game.sheepwars;

import java.util.EnumSet;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public final class SheepwarsMonsterSpawnListener implements Listener {

	private static final EnumSet<EntityType> MONSTER_TYPES = EnumSet.of(
		EntityType.ZOMBIE,
		EntityType.SKELETON,
		EntityType.SPIDER
	);

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (!WorldManager.ACTIVE_WORLD_NAME.equals(event.getLocation().getWorld().getName())) {
			return;
		}

		if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
			return;
		}

		if (MONSTER_TYPES.contains(event.getEntityType())) {
			event.setCancelled(false);
		}
	}
}