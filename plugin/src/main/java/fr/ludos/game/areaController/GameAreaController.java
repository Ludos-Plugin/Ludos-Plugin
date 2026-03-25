package fr.ludos.game.areaController;

import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.Utility;
import fr.ludos.game.Game;
import fr.ludos.game.GameProcessBase;

public abstract class GameAreaController extends GameProcessBase {
	@Override
	protected final JavaPlugin getPlugin() {
		return getGame().getPlugin();
	}

	private final Game game;
	protected final Game getGame() {
		return game;
	}

	protected GameAreaController(Game game) {
		if (game == null) {
			throw new IllegalArgumentException("Game cannot be null");
		}

		this.game = game;
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


	@Override
	public final void onStart() {
		onAreaStart();
	}

	@Override
	public final void onStop() {
		onAreaStop();
	}

	protected void onAreaStart() { }
	protected void onAreaStop() { }

	@Override
	public final void onInit() {
		onAreaInit();
	}

	@Override
	public final void onDeinit() {
		onAreaDeinit();
	}

	protected void onAreaInit() { }
	protected void onAreaDeinit() { }
}
