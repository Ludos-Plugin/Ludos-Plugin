package fr.ludos.lobby;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import fr.ludos.structure.Structure;

public class LobbyStructure extends BoundingBoxStructure {
	private final Map<Location, BlockData> blocksData;
	private final Location entranceLocation;

	private final static int radius = 5;
	private final static int height = 5;

	private final BoundingBox bb;

	private LobbyStructure(Location location, Map<Location, BlockData> blocksData, Location entranceLocation) {
		super(location);
		this.blocksData = blocksData;
		this.entranceLocation = entranceLocation;

		Vector vec = location.toVector();
		bb = new BoundingBox(
			vec.getX() - radius, vec.getY(), vec.getZ() - radius,
			vec.getX() + radius, vec.getY() + height, vec.getZ() + radius
		);
	}

	@Override
	public Location getEntranceLocation() {
		return entranceLocation.clone();
	}

	@Override
	public BoundingBox getBoundingBox() {
		return bb;
	}

	@Override
	public void destroy() {
		for (Map.Entry<Location, BlockData> backup : blocksData.entrySet()) {
			Location blockLocation = backup.getKey();
			BlockData blockData = backup.getValue();
			if (blockLocation == null || blockData == null) continue;
			blockLocation.getBlock().setBlockData(blockData, false);
		}
		blocksData.clear();
	}

	public static class Builder extends Structure.Builder {
		public Builder() { }

		@Override
		public Structure build(Location location) {
			Location origin = location.clone();

			origin.setY(location.getWorld().getMaxHeight() - 15);
			origin.setX(origin.getBlockX());
			origin.setZ(origin.getBlockZ());

			HashMap<Location, BlockData> oldBlocks = new HashMap<>();

			Material floorBorder = Material.BLUE_CONCRETE;
			Material floorCenter = Material.CYAN_STAINED_GLASS;
			Material wall = Material.BLACK_CONCRETE;
			Material pillar = Material.POLISHED_BLACKSTONE_BRICKS;
			Material window = Material.CYAN_STAINED_GLASS;
			Material roof = Material.BLACKSTONE;
			Material accent = Material.SEA_LANTERN;

			// Hollow out the structure
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					for (int y = 0; y <= height; y++) {
						Location blockLoc = origin.clone().add(x, y, z);
						setBlock(blockLoc, Material.AIR, oldBlocks);
					}
				}
			}

			// Bordered floor
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					Location blockLoc = origin.clone().add(x, 0, z);
					int absX = Math.abs(x);
					int absZ = Math.abs(z);
					setBlock(blockLoc, ((absX > 2 && absZ > 2) || absX == radius || absZ == radius) ? floorBorder : floorCenter, oldBlocks);
				}
			}

			// Floor Lights
			for (int dx : new int[]{-2, 2}) {
				for (int dz : new int[]{-2, 2}) {
					setBlock(origin.clone().add(dx, 0, dz), accent, oldBlocks);
				}
			}

			// North and South walls
			for (int x = -radius; x <= radius; x++) {
				for (int y = 1; y <= height - 1; y++) {
					Location north = origin.clone().add(x, y, -radius);
					Location south = origin.clone().add(x, y, radius);
					int absX = Math.abs(x);
					if ((absX >= 1 && absX <= 3) && (y >= 1 && y <= 3)) {
						setBlock(north, window, oldBlocks);
						setBlock(south, window, oldBlocks);
					} else {
						setBlock(north, wall, oldBlocks);
						setBlock(south, wall, oldBlocks);
					}
				}
			}

			// East and West walls
			for (int z = -radius + 1; z <= radius - 1; z++) {
				for (int y = 1; y <= height - 1; y++) {
					Location west = origin.clone().add(-radius, y, z);
					Location east = origin.clone().add(radius, y, z);
					int absZ = Math.abs(z);
					if ((absZ >= 1 && absZ <= 3) && (y >= 1 && y <= 3)) {
						setBlock(west, window, oldBlocks);
						setBlock(east, window, oldBlocks);
					} else {
						setBlock(west, wall, oldBlocks);
						setBlock(east, wall, oldBlocks);
					}
				}
			}

			// Roof
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					setBlock(origin.clone().add(x, height, z), Material.POLISHED_BLACKSTONE_SLAB, oldBlocks);
				}
			}

			for (int x = -radius; x <= radius; x++) {
				setBlock(origin.clone().add(x, height, -radius), roof, oldBlocks);
				setBlock(origin.clone().add(x, height, radius), roof, oldBlocks);
			}

			for (int z = -radius + 1; z <= radius - 1; z++) {
				setBlock(origin.clone().add(-radius, height, z), roof, oldBlocks);
				setBlock(origin.clone().add(radius, height, z), roof, oldBlocks);
			}


			int pillarDistance = radius - 2;
			for (int y = 1; y <= height - 1; y ++) {
				setBlock(origin.clone().add(-pillarDistance, y, -pillarDistance), pillar, oldBlocks);
				setBlock(origin.clone().add(pillarDistance, y, -pillarDistance), pillar, oldBlocks);
				setBlock(origin.clone().add(-pillarDistance, y, pillarDistance), pillar, oldBlocks);
				setBlock(origin.clone().add(pillarDistance, y, pillarDistance), pillar, oldBlocks);
			}

			Location entrance = origin.clone().add(0, 1, 0);
			return new LobbyStructure(origin, oldBlocks, entrance);
		}

		private void setBlock(Location location, Material material, HashMap<Location, BlockData> oldBlocks) {
			if (location == null) return;
			if (! oldBlocks.containsKey(location)) {
				oldBlocks.put(location, location.getBlock().getBlockData().clone());
			}
			location.getBlock().setType(material);
		}
	}
}
