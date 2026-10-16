package fr.ludos.core.structure;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Random;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

/**
 * A {@link BoundingBoxStructure} built using a standard Minecraft .NBT structure file, and acquired through Bukkit's own Resource system (see {@link JavaPlugin#getResource(String filename)}).
 */
public class NBTFileStructure extends BoundingBoxStructure {
	private final Location origin;
	private final Location entrance;
	private final BoundingBox boundingBox;

	public NBTFileStructure(Structure structure, Location location, @Nullable Vector entranceOffset, StructureRotation rotation, Mirror mirror, int palette, float integrity, Random random) {
		super(location);
		Objects.requireNonNull(structure);

		this.origin = location.clone();

		switch (rotation) {
			case NONE:
				break;
			case CLOCKWISE_180:
				entranceOffset.setZ(- entranceOffset.getZ());
				entranceOffset.setX(- entranceOffset.getX());
				break;
			case CLOCKWISE_90:
				double clockwise_x = entranceOffset.getX();
				entranceOffset.setX(- entranceOffset.getZ());
				entranceOffset.setZ(clockwise_x);
				break;
			case COUNTERCLOCKWISE_90:
				double counter_x = entranceOffset.getX();
				entranceOffset.setX(entranceOffset.getZ());
				entranceOffset.setZ(- counter_x);
				break;
		}
		switch (mirror) {
			case NONE:
				break;
			case FRONT_BACK:
				entranceOffset.setZ(- entranceOffset.getZ());
				break;
			case LEFT_RIGHT:
				entranceOffset.setX(- entranceOffset.getX());
				break;
		}
		final BlockVector size = structure.getSize();
		final Location center = origin.clone().add(size.getX() / 2.0, size.getY() / 2.0, size.getZ() / 2.0);
		entrance = center.add(entranceOffset);

		this.boundingBox = new BoundingBox(
			origin.getX(), origin.getY(), origin.getZ(),
			origin.getX() + size.getX(),
			origin.getY() + size.getY(),
			origin.getZ() + size.getZ()
		);

		structure.place(origin, false, rotation, mirror, palette, integrity, random);
	}

	@Override
	public Location getEntranceLocation() {
		return entrance;
	}

	@Override
	public Location randomizedEntranceLocation() {
		return entrance;
	}

	@Override
	public void destroy() {
		int minX = (int) Math.floor(boundingBox.getMinX());
		int minY = (int) Math.floor(boundingBox.getMinY());
		int minZ = (int) Math.floor(boundingBox.getMinZ());
		int maxX = (int) Math.ceil(boundingBox.getMaxX());
		int maxY = (int) Math.ceil(boundingBox.getMaxY());
		int maxZ = (int) Math.ceil(boundingBox.getMaxZ());
		for (int x = minX; x < maxX; x++) {
			for (int y = minY; y < maxY; y++) {
				for (int z = minZ; z < maxZ; z++) {
					origin.getWorld().getBlockAt(x, y, z).setType(Material.AIR, false);
				}
			}
		}
	}

	@Override
	public BoundingBox getBoundingBox() {
		return boundingBox.clone();
	}


	/**
	 * Builder for {@link NBTFileStructure}.
	 */
	public static class Builder extends BoundingBoxStructure.Builder {
		private final Structure structure;
		public final Structure structure() {
			return structure;
		}

		private Vector entranceOffset = null;
		private StructureRotation rotation = StructureRotation.NONE;
		private Mirror mirror = Mirror.NONE;
		private int palette = -1;
		private float integrity = 1.0f;
		private Random random = null;

		public Builder(JavaPlugin plugin, String resourcePath) {
			InputStream resource = plugin.getResource(resourcePath);
			if (resource == null) {
				throw new IllegalArgumentException("NBT structure resource not found: " + resourcePath);
			}

			try {
				structure = Bukkit.getStructureManager().loadStructure(resource);
			} catch (IOException exception) {
				throw new IllegalArgumentException("Unable to load NBT structure", exception);
			}
		}

		public Builder offsetEntrance(@Nullable Vector entranceOffset) {
			this.entranceOffset = entranceOffset;
			return this;
		}

		public Builder rotate(StructureRotation rotation) {
			this.rotation = rotation;
			return this;
		}

		public Builder mirror(Mirror mirror) {
			this.mirror = mirror;
			return this;
		}

		public Builder withPalette(int palette) {
			this.palette = palette;
			return this;
		}

		public Builder withIntegrity(float integrity) {
			this.integrity = integrity;
			return this;
		}

		public Builder pristine() {
			this.integrity = 1.0f;
			return this;
		}

		public Builder withRandom(Random random) {
			this.random = random;
			return this;
		}

		public Builder withRNGSeed(long seed) {
			this.random = new Random(seed);
			return this;
		}

		public NBTFileStructure build(Location location) {
			if (location == null || location.getWorld() == null) {
				throw new IllegalArgumentException("A structure location with a world is required");
			}

			return new NBTFileStructure(structure, location, entranceOffset, rotation, mirror, palette, integrity, random != null ? random : new Random());
		}
	}
}
