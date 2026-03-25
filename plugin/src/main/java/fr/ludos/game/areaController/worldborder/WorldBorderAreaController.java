package fr.ludos.game.areaController.worldborder;

import java.util.Objects;
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
import fr.ludos.game.areaController.GameAreaController;

public class WorldBorderAreaController extends GameAreaController {

	private final WorldBorderLocationOption locationOption;

	private Location initialBorderCenter;
	private double initialBorderSize;

	private Location gameLocation;

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
	protected void onAreaInit() {
		World world = getGame().getWorldController().getWorld();
		WorldBorder border = world.getWorldBorder();
		initialBorderCenter = border.getCenter();
		initialBorderSize = border.getSize();


		Location initialLocation = Objects.requireNonNull(world.getSpawnLocation());

		gameLocation = locationOption.getLocation(initialLocation);
		double highestY = gameLocation.getWorld().getHighestBlockYAt(gameLocation) + 1;
		gameLocation.setY(highestY);
	}

	@Override
	protected void onAreaStart() {
		setBorder(gameLocation, getAreaDiameter(), 3);
	}

	@Override
	protected void onAreaStop() {
		resetBorder();
	}

	public void setBorder(Location center, double size, long time) {
		resetBorder();

		WorldBorder border = getGame().getWorldController().getWorld().getWorldBorder();

		initialBorderCenter = border.getCenter();
		initialBorderSize = border.getSize();

		border.setCenter(center);
		border.setSize(size, time);
	}

	public void resetBorder(long time) {
		if (initialBorderCenter == null || initialBorderSize == 0) return;

		WorldBorder border = initialBorderCenter.getWorld().getWorldBorder();

		border.setCenter(initialBorderCenter);
		border.setSize(initialBorderSize, time);

		initialBorderCenter = null;
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
		if (initialBorderCenter == null) return null;
		World borderWorld = initialBorderCenter.getWorld();
		if (location.getWorld() != borderWorld) return null;

		WorldBorder border = borderWorld.getWorldBorder();
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
		return getGame().getWorldController().getWorld().getWorldBorder().isInside(location);
	}
}
