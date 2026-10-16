package fr.ludos.core.area;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents an empty, indefinite area, with no limits.
 */
public class VoidArea extends Area {
	private VoidArea(World world, JavaPlugin plugin) {
		super(world, plugin);
	}

	@Override
	public Location getCenter() {
		return new Location(world(), 0, 0, 0);
	}

	@Override
	public Location pickRandom(double startFactor, double endFactor) {
		return world().getSpawnLocation();
	}

	@Override
	public Location constrain(Location location) {
		return location;
	}

	@Override
	public boolean isInside(Location location) {
		return true;
	}
}
