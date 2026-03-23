package fr.ludos.game;

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

public abstract class GameAreaController extends GameProcessBase {
	@Override
	protected final JavaPlugin getPlugin() {
		return getGame().getPlugin();
	}

	private final Game game;
	protected final Game getGame() {
		return game;
	}

	private final Location returnLocation;
	public final Location getReturnLocation() {
		return returnLocation;
	}

	private Location initialLocation;
	public final Location getInitialLocation() {
		return initialLocation;
	}

	private @Nullable WorldCreator worldCreator;

	private World world;
	public final World getWorld() {
		return world;
	}

	private boolean isOwnWorld;
	public final boolean isOwnWorld() {
		return isOwnWorld;
	}

	protected GameAreaController(Game game, Location returnLocation) {
		if (game == null) {
			throw new IllegalArgumentException("Game cannot be null");
		}

		this.game = game;
		this.returnLocation = returnLocation;
	}

	public final GameAreaController withinWorld(WorldCreator worldCreator) {
		if (isStarted()) return this;
		this.initialLocation = null;
		this.world = null;
		this.worldCreator = worldCreator;
		this.isOwnWorld = true;
		return this;
	}
	public final GameAreaController withinWorld(World world, boolean isOwnWorld) {
		if (isStarted()) return this;
		this.initialLocation = world.getSpawnLocation();
		this.world = world;
		this.worldCreator = null;
		this.isOwnWorld = isOwnWorld;
		return this;
	}
	public final GameAreaController startingAt(Location initialLocation) {
		if (isStarted()) return this;
		this.initialLocation = initialLocation;
		this.world = initialLocation.getWorld();
		this.worldCreator = null;
		this.isOwnWorld = false;
		return this;
	}

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
		if (isOwnWorld && worldCreator != null && world == null) {
			World world = worldCreator.createWorld();
			world.setAutoSave(false);
			world.setKeepSpawnInMemory(false);
			this.world = world;

			this.initialLocation = world.getSpawnLocation();
		}

		onAreaInit();
	}

	@Override
	public final void onDeinit() {
		onAreaDeinit();

		Location returnLocation = getReturnLocation();

		if (world != null && isOwnWorld) {
			World tempWorld = world;

			new BukkitRunnable() {
				public void run() {
					List<Player> playersInWorld = tempWorld.getPlayers();
					if (playersInWorld.size() > 0) {
						for (Player player : playersInWorld) {
							if (player.isDead()) {
								player.spigot().respawn();
							}
							player.teleport(returnLocation);
						}
						return;
					}

					tempWorld.setAutoSave(false);
					tempWorld.setKeepSpawnInMemory(false);
					boolean unloaded = Bukkit.unloadWorld(tempWorld, false);

					if (unloaded) {
						Utility.deleteWorld(tempWorld);
						this.cancel();
					}
				}
			}.runTaskTimer(getPlugin(), 0, 20);

			world = null;
		}
	}

	protected void onAreaInit() { }
	protected void onAreaDeinit() { }

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
