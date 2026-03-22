package fr.ludos.game.worldborder;

import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.Utility;
import fr.ludos.game.Game;
import fr.ludos.game.GameAreaController;

public class WorldBorderAreaController extends GameAreaController {

	private final WorldBorderLocationOption locationOption;

	private double initialBorderSize;

	private Location gameLocation;

	private final WorldBorderAreaOption areaOption;
	public final int getAreaDiameter() {
		return areaOption.getSize();
	}
	private final int getAreaRadius() {
		return getAreaDiameter() / 2;
	}


	public WorldBorderAreaController(Game game, Location returnLocation, WorldCreator worldCreator, WorldBorderLocationOption location, WorldBorderAreaOption area) {
		super(game, returnLocation, worldCreator);

		this.locationOption = location;
		this.areaOption = area;
	}

	@Override
	protected void onAreaInit() {
		initialBorderSize = getInitialLocation().getWorld().getWorldBorder().getSize();
	}

	@Override
	protected void onAreaStart() {
		Location initialLocation = getInitialLocation();
		World world = getWorld();

		switch (locationOption) {
			case random:
				gameLocation = Utility.getRandomBiomeLocation(initialLocation, 2500, 0, 200, initialLocation, 5, null);
				break;
			default:
				gameLocation = initialLocation.clone();
				break;
		}

		double highestY = world.getHighestBlockYAt(gameLocation) + 1;
		gameLocation.setY(highestY);

		setBorder(gameLocation, getAreaDiameter(), 3);
	}

	@Override
	protected void onAreaStop() {
		resetBorder();
	}

	public void setBorder(Location center, double size, long time) {
		if (initialBorderSize != 0) return;

		Location initialLocation = getInitialLocation();
		WorldBorder border = initialLocation.getWorld().getWorldBorder();

		initialBorderSize = border.getSize();

		border.setCenter(center);
		border.setSize(size, time);
	}

	public void resetBorder(long time) {
		if (initialBorderSize == 0) return;

		Location initialLocation = getInitialLocation();
		WorldBorder border = initialLocation.getWorld().getWorldBorder();

		border.setCenter(initialLocation);
		border.setSize(initialBorderSize, time);

		initialBorderSize = 0;
	}
	public void resetBorder() {
		resetBorder(0);
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
