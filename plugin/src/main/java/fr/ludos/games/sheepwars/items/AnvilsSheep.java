package fr.ludos.games.sheepwars.items;

import javax.annotation.Nullable;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Anvils Sheep - Spawns a grid of anvils above the impact location.
 */
public class AnvilsSheep extends AbstractSheep {

	public AnvilsSheep(String name, String description, Material material) {
		super(name, description, material);
	}

	@Override
	public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
		org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.GRAY);

		startSheepTimer(sheep, () -> {
			spawnAnvils(sheep.getLocation(), 5);
		}, launcher);
	}

	/**
	 * Spawns a grid of anvils above the target location.
	 *
	 * @param center The center location
	 * @param radius The radius of the anvil grid
	 */
	protected void spawnAnvils(Location center, int radius) {
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				if (Math.sqrt(x*x + z*z) <= radius) {
					Block block = center.clone().add(x, 7, z).getBlock();

					if (block.getType() != Material.ANVIL)
						block.setType(Material.ANVIL);
				}
			}
		}
	}
}
