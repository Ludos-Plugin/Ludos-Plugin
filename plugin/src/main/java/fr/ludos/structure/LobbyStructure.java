package fr.ludos.structure;

import java.util.Map;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class LobbyStructure extends Structure {
	private final Map<Location, BlockData> blocksData;
	private final Location entranceLocation;

	public LobbyStructure(Location location, Map<Location, BlockData> blocksData, Location entranceLocation) {
		super(location);
		this.blocksData = blocksData;
		this.entranceLocation = entranceLocation.clone();
	}

	@Override
	public Location getEntranceLocation() {
		return entranceLocation.clone();
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
			origin.setY(275);
			origin.setX(origin.getBlockX());
			origin.setZ(origin.getBlockZ());

			HashMap<Location, BlockData> oldBlocks = new HashMap<>();

			Material floorBorder = Material.POLISHED_BLACKSTONE;
			Material floorCenter = Material.BLUE_CONCRETE;
			Material wall = Material.BLACK_CONCRETE;
			Material pillar = Material.POLISHED_BLACKSTONE_BRICKS;
			Material window = Material.CYAN_STAINED_GLASS;
			Material roof = Material.BLACKSTONE;
			Material accent = Material.SEA_LANTERN;

			int radius = 5;

			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					for (int y = 0; y <= 4; y++) {
						Location blockLoc = origin.clone().add(x, y, z);
						setBlock(blockLoc, Material.AIR, oldBlocks);
					}
				}
			}

			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					Location blockLoc = origin.clone().add(x, 0, z);
					setBlock(blockLoc, (Math.abs(x) == radius || Math.abs(z) == radius) ? floorBorder : floorCenter, oldBlocks);
				}
			}

			for (int x = -radius + 1; x <= radius - 1; x++) {
				for (int z = -radius + 1; z <= radius - 1; z++) {
					for (int y = 1; y <= 3; y++) {
						setBlock(origin.clone().add(x, y, z), Material.AIR, oldBlocks);
					}
				}
			}

			for (int x = -radius; x <= radius; x++) {
				for (int y = 1; y <= 3; y++) {
					Location north = origin.clone().add(x, y, -radius);
					Location south = origin.clone().add(x, y, radius);
					if (Math.abs(x) == 2 && y == 2) {
						setBlock(north, window, oldBlocks);
					} else {
						setBlock(north, wall, oldBlocks);
					}
					setBlock(south, wall, oldBlocks);
				}
			}

			for (int z = -radius + 1; z <= radius - 1; z++) {
				for (int y = 1; y <= 3; y++) {
					Location west = origin.clone().add(-radius, y, z);
					Location east = origin.clone().add(radius, y, z);
					if (Math.abs(z) == 2 && y == 2) {
						setBlock(west, window, oldBlocks);
						setBlock(east, window, oldBlocks);
					} else {
						setBlock(west, wall, oldBlocks);
						setBlock(east, wall, oldBlocks);
					}
				}
			}

			for (int x = -radius; x <= radius; x++) {
				setBlock(origin.clone().add(x, 4, -radius), roof, oldBlocks);
				setBlock(origin.clone().add(x, 4, radius), roof, oldBlocks);
			}

			for (int z = -radius + 1; z <= radius - 1; z++) {
				setBlock(origin.clone().add(-radius, 4, z), roof, oldBlocks);
				setBlock(origin.clone().add(radius, 4, z), roof, oldBlocks);
			}

			for (int x = -radius + 1; x <= radius - 1; x++) {
				for (int z = -radius + 1; z <= radius - 1; z++) {
					setBlock(origin.clone().add(x, 4, z), Material.POLISHED_BLACKSTONE_SLAB, oldBlocks);
				}
			}

			for (int dx : new int[]{-2, 2}) {
				for (int dz : new int[]{-2, 2}) {
					setBlock(origin.clone().add(dx, 0, dz), accent, oldBlocks);
				}
			}

			for (int y = 1; y <= 3; y += 2) {
				setBlock(origin.clone().add(-radius, y, -radius), pillar, oldBlocks);
				setBlock(origin.clone().add(radius, y, -radius), pillar, oldBlocks);
				setBlock(origin.clone().add(-radius, y, radius), pillar, oldBlocks);
				setBlock(origin.clone().add(radius, y, radius), pillar, oldBlocks);
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
