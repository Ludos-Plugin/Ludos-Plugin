package fr.ludos.core.area;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.World;

import fr.ludos.core.game.GameProcessBase;

/**
 * Manager for a game area, responsible for setting up the area and managing requests from Game processes.
 */
public abstract class Area extends GameProcessBase {
	public abstract Builder<? extends Area> getBuilder();
	public final Area mutate(Consumer<Builder<?>> config) {
		config.accept(getBuilder());
		return this;
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

		protected Location location;
		public Builder<T> at(Location location) {
			this.location = location;
			return this;
		}
		public Builder<T> in(World world) {
			this.location = world.getSpawnLocation();
			return this;
		}

		public final Builder<T> mutate(Consumer<Builder<T>> config) {
			config.accept(this);
			return this;
		}

		public abstract T build();
	}
}
