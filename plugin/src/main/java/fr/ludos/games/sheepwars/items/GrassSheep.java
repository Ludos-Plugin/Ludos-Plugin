package fr.ludos.games.sheepwars.items;

import java.util.HashSet;

import javax.annotation.Nullable;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Grass Sheep - Transforms connected structure blocks to dirt using flood fill algorithm.
 */
public class GrassSheep extends AbstractSheep {

	public GrassSheep(String name, String description, Material material) {
		super(name, description, material);
	}

	@Override
	public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
		org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.GREEN);

		startSheepTimer(sheep, () -> {
			transformStructureToDirt(sheep.getLocation());
		}, launcher);
	}

	private void transformStructureToDirt(Location center) {
		HashSet<Block> visited = new HashSet<>();
		java.util.LinkedList<Block> toVisit = new java.util.LinkedList<>();

		// Start with blocks in a 5x5x5 area around center
		for (int x = -2; x <= 2; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -2; z <= 2; z++) {
					Block b = center.clone().add(x, y, z).getBlock();
					if (b.getType() != Material.AIR && b.getType() != Material.CAVE_AIR) {
						toVisit.add(b);
					}
				}
			}
		}

		int maxBlocks = 10000;
		int blocksTransformed = 0;

		BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

		while (!toVisit.isEmpty() && blocksTransformed < maxBlocks) {
			Block current = toVisit.poll();

			if (visited.contains(current)) continue;
			if (current.getType() == Material.AIR || current.getType() == Material.CAVE_AIR) continue;
			if (current.getLocation().distance(center) > 100) continue;

			visited.add(current);
			current.setType(Material.DIRT);
			blocksTransformed++;

			for (BlockFace face : faces) {
				Block neighbor = current.getRelative(face);
				if (!visited.contains(neighbor) && neighbor.getType() != Material.AIR && neighbor.getType() != Material.CAVE_AIR) {
					toVisit.add(neighbor);
				}
			}
		}

		center.getWorld().playSound(center, Sound.BLOCK_GRAVEL_BREAK, 1.0f, 0.5f);
	}
}
