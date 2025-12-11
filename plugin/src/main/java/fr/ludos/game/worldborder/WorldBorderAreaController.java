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
	public static final String borderWorldUUIDKey = "borderWorldUUID";
	public String borderWorldUUIDPath() {
		return getGame().getBuilder().getId() + '.' + borderWorldUUIDKey;
	}
	public static final String borderLocationKey = "borderLocation";
	public String borderLocationPath() {
		return getGame().getBuilder().getId() + '.' + borderLocationKey;
	}
	public static final String borderSizeKey = "borderSize";
	public String borderSizePath() {
		return getGame().getBuilder().getId() + '.' + borderSizeKey;
	}

	private final WorldBorderLocationOption locationOption;
	private Location initialLocation;
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
		World world = initialLocation.getWorld();
		if (world == null) {
			throw new IllegalStateException("Initial location world is null");
		}

		setCachedBorder(world);

		switch (locationOption) {
			case random:
				gameLocation = Utility.getGroundedLocationAround(initialLocation, 300, 2500, initialLocation);
				break;
			default:
			case here:
				gameLocation = initialLocation;
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


	@Nullable
	public UUID getCachedBorderWorldUID() {
		JavaPlugin plugin = getPlugin();
		String value = plugin.getConfig().getString(borderWorldUUIDPath());

		if (value == null) return null;
		return UUID.fromString(value);
	}
	@Nullable
	public Location getCachedBorderLocation() {
		JavaPlugin plugin = getPlugin();
		return plugin.getConfig().getLocation(borderLocationPath());
	}
	public double getCachedBorderSize() {
		JavaPlugin plugin = getPlugin();
		return plugin.getConfig().getDouble(borderSizePath());
	}
	public void setCachedBorder(World world) {
		JavaPlugin plugin = getPlugin();
		FileConfiguration config = plugin.getConfig();

		if (world == null) {
			config.set(borderWorldUUIDPath(), null);
			config.set(borderLocationPath(), null);
			config.set(borderSizePath(), null);
			plugin.saveConfig();
			return;
		}

		WorldBorder border = world.getWorldBorder();
		config.set(borderWorldUUIDPath(), world.getUID().toString());
		config.set(borderLocationPath(), border.getCenter());
		config.set(borderSizePath(), border.getSize());
		plugin.saveConfig();
	}
	public void resetBorder() {
		Location location = getCachedBorderLocation();
		if (location == null) return;

		World world = Bukkit.getWorld(getCachedBorderWorldUID());
		if (world == null) return;

		double size = getCachedBorderSize();
		WorldBorder border = world.getWorldBorder();
		border.setCenter(location);
		border.setSize(size, 0);

		setCachedBorder(null);
	}

	@Override
	public void setup(Location base) {
		this.initialLocation = base;
		this.gameLocation = base;
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
