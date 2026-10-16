package fr.ludos.games.sheepwars.items;

import javax.annotation.Nullable;

import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import fr.ludos.core.Ludos;

/**
 * All Aboard Sheep - The launcher rides the sheep and can control its direction.
 * Stacks nearby entities on landing.
 */
public class AllAboardSheep extends AbstractSheep {

	public AllAboardSheep(String name, String description, Material material) {
		super(name, description, material);
	}

	@Override
	public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
		org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(
			launcher, event, item, DyeColor.WHITE, null, 2);

		// Special configuration for riding sheep
		sheep.setInvulnerable(true);
		sheep.addPassenger(launcher);

		// Use riding timer instead of standard timer
		startRidingSheepTimer(sheep, launcher);
	}

	/**
	 * Starts a timer for a riding sheep that follows the player's view direction.
	 * Used specifically for AllAboardSheep.
	 *
	 * @param sheep The sheep entity being ridden
	 * @param rider The player riding the sheep
	 */
	protected void startRidingSheepTimer(org.bukkit.entity.Sheep sheep, Player rider) {
		new BukkitRunnable() {
			int flightTime = 0;
			final int maxFlightTime = 100; // 5 seconds

			@Override
			public void run() {
				if (sheep == null || sheep.isDead()) {
					this.cancel();
					return;
				}

				// If player is no longer riding or time is up
				if (!sheep.getPassengers().contains(rider) || flightTime >= maxFlightTime) {
					rider.leaveVehicle();
					sheep.remove();
					this.cancel();
					return;
				}

				// Keep sheep flying and follow player's view direction
				Vector direction = rider.getLocation().getDirection().multiply(0.8);
				direction.setY(Math.max(direction.getY(), -0.1)); // Prevent falling too fast
				sheep.setVelocity(direction);

				sheep.getLocation().getWorld().playEffect(sheep.getLocation(), Effect.SMOKE, 0);
				flightTime++;
			}
		}.runTaskTimer(JavaPlugin.getPlugin(Ludos.class), 0L, 1L);
	}
}
