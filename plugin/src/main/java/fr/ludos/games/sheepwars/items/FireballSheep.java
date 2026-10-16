package fr.ludos.games.sheepwars.items;

import javax.annotation.Nullable;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Fireball Sheep - Spawns a fireball from the sky on impact.
 */
public class FireballSheep extends AbstractSheep {

	public FireballSheep(String name, String description, Material material) {
		super(name, description, material);
	}

	@Override
	public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
		org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.CYAN);

		startSheepTimer(sheep, () -> {
			fireballFromSky(sheep.getLocation());
		}, launcher);
	}

	/**
	 * Spawns a fireball that falls from the sky.
	 *
	 * @param center The target location
	 */
	protected void fireballFromSky(Location center) {
		World world = center.getWorld();
		Location spawnLoc = center.clone().add(0, 30, 0);

		org.bukkit.entity.Fireball fireball = world.spawn(spawnLoc, org.bukkit.entity.Fireball.class);
		fireball.setDirection(new Vector(0, -1, 0));
		fireball.setYield(3.0f);

		world.playSound(center, Sound.ENTITY_GHAST_WARN, 1.0f, 0.5f);
	}
}
