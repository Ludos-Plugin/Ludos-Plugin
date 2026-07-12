package fr.ludos.core.area;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.core.Utility;
import fr.ludos.core.config.ConfigEntry;
import fr.ludos.core.config.NumberConfigOption;
import fr.ludos.core.game.Game;

public class WorldBorderArea extends Area {
	public static final NumberConfigOption configOption = new NumberConfigOption("WorldBorder Area diameter", Set.of(150, 250, 350), 150, true);
	public static final ConfigEntry<Integer> configEntry = new ConfigEntry<>(
		"area",
		WorldBorderArea.configOption
	);

	private final Builder builder;
	@Override
	public Builder getBuilder() {
		return this.builder;
	}

	@Override
	protected JavaPlugin getPlugin() {
		return this.builder.game.getPlugin();
	}

	private Location initialCenter;
	private double initialSize;

	private Location currentCenter;
	private double currentSize;


	private WorldBorderArea(Builder builder) {
		this.builder = builder;
	}

	public static Builder within(Game game, int diameter) {
		return new Builder(game, diameter);
	}

	@Override
	public boolean isClear() {
		WorldBorder border = builder.location.getWorld().getWorldBorder();

		return ! isStarted() &&
			border.getCenter().equals(initialCenter) &&
			border.getSize() == initialSize;
	}

	@Override
	protected void onStart() {
		super.onStart();

		WorldBorder border = builder.location.getWorld().getWorldBorder();

		initialCenter = border.getCenter();
		initialSize = border.getSize();

		setBorder(builder.location, builder.getAreaDiameter(), 3);
	}

	@Override
	public void onStop() {
		WorldBorder border = builder.location.getWorld().getWorldBorder();

		if (
			border.getCenter().equals(currentCenter) &&
			border.getSize() == currentSize
		) {
			setBorder(initialCenter, initialSize, 0);
		}
	}

	public void setBorder(Location center, double size, long time) {
		WorldBorder border = builder.location.getWorld().getWorldBorder();

		border.setCenter(builder.location);
		currentCenter = builder.location;

		border.setSize(size, time);
		currentSize = size;
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

	public static class Builder extends Area.Builder<WorldBorderArea> {
		private final Game game;
		private Integer diameter;

		public Builder(Game game, int diameter) {
			this.game = game;
			this.diameter = diameter;
		}

		public final int getAreaDiameter() {
			return diameter;
		}
		public final int getAreaRadius() {
			return diameter / 2;
		}

		@Override
		public WorldBorderArea build() {
			return new WorldBorderArea(this);
		}
	}
}
