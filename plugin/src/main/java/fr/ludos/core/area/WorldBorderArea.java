package fr.ludos.core.area;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.core.Utility;
import fr.ludos.core.persistence.config.valueEntry.IntegerConfigEntry;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Area that is delimited by a Minecraft {@link WorldBorder}.
 */
public class WorldBorderArea extends Area {
	public static final IntegerConfigEntry CONFIG =
		new IntegerConfigEntry(
			Component.text("Area diameter"),
			"area",
			150, Set.of(150, 250, 350),
			true
		) {
			@Override
			public AbstractItemBuilder<?> createItem(Player player) {
				return new ItemBuilder(Material.MAP);
			}
			protected String formatValueString(String value) {
				return super.formatValueString(value) + " blocks";
			};
		};

	private Location initialCenter;
	private double initialSize;

	private Location currentCenter;
	private double currentSize;


	private WorldBorderArea(Location center, int diameter, JavaPlugin plugin) {
		super(center.getWorld(), plugin);
		this.currentCenter = center;
		this.currentSize = diameter;
	}

	public static Builder within(int diameter, JavaPlugin plugin) {
		return within((World) null, diameter, plugin);
	}
	public static Builder within(World world, int diameter, JavaPlugin plugin) {
		return within(new Location(world, 0, 0, 0), diameter, plugin);
	}
	public static Builder within(Location location, int diameter, JavaPlugin plugin) {
		return new Builder(location, diameter, plugin);
	}

	@Override
	public boolean isClear() {
		return ! isStarted();
	}

	@Override
	protected void onStart() {
		super.onStart();

		WorldBorder border = world().getWorldBorder();

		initialCenter = border.getCenter();
		initialSize = border.getSize();

		setBorder(currentCenter, currentSize, 3);
	}

	@Override
	public void onStop() {
		WorldBorder border = world().getWorldBorder();

		if (
			border.getCenter().equals(currentCenter) &&
			border.getSize() == currentSize
		) {
			setBorder(initialCenter, initialSize, 0);
		}
	}

	public void setBorder(Location center, double size, long time) {
		WorldBorder border = world().getWorldBorder();

		currentCenter = center;
		border.setCenter(currentCenter);

		currentSize = size;
		border.setSize(currentSize, time);
	}

	@Override
	public Location pickRandom(double startFactor, double endFactor) {
		Location center = currentCenter;
		double radius = currentSize / 2;
		return Utility.getLocationAround(center, (int)(radius * startFactor), (int)(radius * endFactor), center);
	}
	@Override
	public Location constrain(Location location) {
		WorldBorder border = location.getWorld().getWorldBorder();
		double halfSize = border.getSize() / 2.0;

		double minX = border.getCenter().getX() - halfSize;
		double maxX = border.getCenter().getX() + halfSize;
		double x = Math.max(minX, Math.min(maxX, location.getX()));

		double minZ = border.getCenter().getZ() - halfSize;
		double maxZ = border.getCenter().getZ() + halfSize;
		double z = Math.max(minZ, Math.min(maxZ, location.getZ()));

		return new Location(location.getWorld(), x, location.getY(), z);
	}
	@Override
	public Location getCenter() {
		return currentCenter;
	}
	@Override
	public boolean isInside(Location location) {
		return location.getWorld().getWorldBorder().isInside(location);
	}

	/**
	 * Builder for a {@link WorldBorderArea}.
	 */
	public static class Builder extends Area.Builder<WorldBorderArea> {
		private Location center;
		private Integer diameter;

		public Builder(Location center, int diameter, JavaPlugin plugin) {
			super(plugin);
			this.center = center;
			this.diameter = diameter;
		}

		public final int getAreaDiameter() {
			return diameter;
		}
		public final int getAreaRadius() {
			return diameter / 2;
		}

		@Override
		public WorldBorderArea build(World world) {
			Location loc = center.clone();
			loc.setWorld(world);
			return new WorldBorderArea(loc, diameter, plugin());
		}
	}
}
