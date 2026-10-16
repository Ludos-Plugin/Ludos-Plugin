package fr.ludos.core.area;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.core.game.GameProcessBase;

/**
 * Manager for a game area, responsible for setting up the area and managing requests from Game processes.
 */
public abstract class Area extends GameProcessBase {
	private final World world;
	public final World world() {
		return world;
	}
	private final JavaPlugin plugin;
	@Override
	protected JavaPlugin plugin() {
		return plugin;
	}

	public Area(World world, JavaPlugin plugin) {
		this.world = Objects.requireNonNull(world);
		this.plugin = Objects.requireNonNull(plugin);
	}

	public abstract Location getCenter();

	public abstract Location pickRandom(double startFactor, double endFactor);
	public Location pickRandomStartingAt(double startFactor) {
		return pickRandom(startFactor, 1.0);
	}
	public Location pickRandomEndingAt(double endFactor) {
		return pickRandom(0.0, endFactor);
	}

	public abstract Location constrain(Location location);
	public abstract boolean isInside(Location location);

	/**
	 *
	 * Builder for an {@link Area}.
	 * @param <T> The type of Area that will be built.
	 */
	public static abstract class Builder<T extends Area> {
		public Builder(JavaPlugin plugin) {
			this.plugin = plugin;
		}

		private final JavaPlugin plugin;
		public JavaPlugin plugin() {
			return plugin;
		}

		public abstract T build(World world);
	}
}
