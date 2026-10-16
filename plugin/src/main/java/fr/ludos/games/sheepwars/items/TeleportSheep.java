package fr.ludos.games.sheepwars.items;

import javax.annotation.Nullable;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Teleport Sheep - Teleports the launcher to the impact location.
 */
public class TeleportSheep extends AbstractSheep {

	public TeleportSheep(String name, String description, Material material) {
		super(name, description, material);
	}

	@Override
	public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
		org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.PURPLE);

		startSheepTimer(sheep, () -> {
			teleportOwnerToLocation(sheep.getLocation(), launcher);
		}, launcher);
	}

	/**
	 * Teleports the owner to the target location safely.
	 *
	 * @param center The target location
	 * @param owner The player to teleport
	 */
	protected void teleportOwnerToLocation(Location center, Player owner) {
		Location safeLoc = center.clone();
		safeLoc.setY(center.getWorld().getHighestBlockYAt(center) + 1);
		safeLoc.setYaw(owner.getLocation().getYaw());
		safeLoc.setPitch(owner.getLocation().getPitch());

		owner.getWorld().spawnParticle(Particle.PORTAL, owner.getLocation(), 50);
		owner.teleport(safeLoc);
		owner.getWorld().playSound(owner.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
		owner.getWorld().spawnParticle(Particle.PORTAL, owner.getLocation(), 50);
	}
}
