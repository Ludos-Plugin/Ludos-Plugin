package fr.ludos.game;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class GameAreaController extends GameProcessBase {
	private final Game game;
	protected final Game getGame() {
		return game;
	}

	@Override
	protected final JavaPlugin getPlugin() {
		return getGame().getPlugin();
	}

	public GameAreaController(Game game) {
		if (game == null) {
			throw new IllegalArgumentException("Game cannot be null");
		}

		this.game = game;
	}


	public abstract void setup(Location base);

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
}
