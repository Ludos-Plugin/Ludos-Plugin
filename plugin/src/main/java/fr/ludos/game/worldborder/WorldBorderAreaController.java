package fr.ludos.game.worldborder;

import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.Utility;
import fr.ludos.game.Game;
import fr.ludos.game.GameAreaController;

public class WorldBorderAreaController extends GameAreaController {

	private final WorldBorderLocationOption locationOption;

	private Location fallbackLocation;
	private double fallbackBorderSize;

	private Location gameLocation;

	public World getWorld() {
		return gameLocation.getWorld();
	}

	private final WorldBorderAreaOption areaOption;
	public final int getAreaDiameter() {
		return areaOption.getSize();
	}
	private final int getAreaRadius() {
		return getAreaDiameter() / 2;
	}


	public WorldBorderAreaController(Game game, WorldBorderLocationOption location, WorldBorderAreaOption area) {
		super(game);

		this.locationOption = location;
		this.areaOption = area;
	}

	@Override
	protected void onStart() {
		World world = fallbackLocation.getWorld();
		if (world == null) {
			throw new IllegalStateException("Initial location world is null");
		}

		switch (locationOption) {
			case random:
				gameLocation = Utility.getRandomBiomeLocation(fallbackLocation, 2500, 0, 200, fallbackLocation, 5, null);
				break;
			default:
				gameLocation = fallbackLocation.clone();
				break;
		}

		double highestY = world.getHighestBlockYAt(gameLocation) + 1;
		gameLocation.setY(highestY);

		WorldBorder border = world.getWorldBorder();
		border.setCenter(gameLocation);
		border.setSize(getAreaDiameter(), 3);
	}

	@Override
	protected void onStop() {
		resetBorder();
	}


	public void resetBorder() {
		if (fallbackLocation == null || fallbackBorderSize == 0) return;

		WorldBorder border = fallbackLocation.getWorld().getWorldBorder();
		border.setCenter(fallbackLocation);
		border.setSize(fallbackBorderSize, 0);

		fallbackLocation = null;
		fallbackBorderSize = 0;
	}

	@Override
	public void setup(Location base) {
		this.fallbackLocation = base;
		this.fallbackBorderSize = base.getWorld().getWorldBorder().getSize();
	}

	@Override
	public Location pickRandom(double startFactor, double endFactor) {
		int areaRadius = getAreaRadius();
		return Utility.getGroundedLocationAround(gameLocation, (int)(areaRadius * startFactor), (int)(areaRadius * endFactor), gameLocation);
	}
	@Override
	public Location constrain(Location location) {
		WorldBorder border = getWorld().getWorldBorder();
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
		return gameLocation;
	}
	@Override
	public boolean isInside(Location location) {
		return getWorld().getWorldBorder().isInside(location);
	}
}
