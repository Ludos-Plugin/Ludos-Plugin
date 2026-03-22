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
	public Location getReturnLocation() {
		return returnLocation;
	}

	private @Nullable Location initialLocation;
	public @Nullable Location getInitialLocation() {
		return initialLocation;
	}

	private final @Nullable WorldCreator worldCreator;
	public @Nullable WorldCreator getWorldCreator() {
		return worldCreator;
	}
	private @Nullable World world;
	public @Nullable World getWorld() {
		return world;
	}

	private GameAreaController(Game game, Location returnLocation, @Nullable Location initialLocation, @Nullable WorldCreator worldCreator) {
		if (game == null) {
			throw new IllegalArgumentException("Game cannot be null");
		}

		this.game = game;
		this.returnLocation = returnLocation;
		this.initialLocation = initialLocation;
		this.worldCreator = worldCreator;
	}
	protected GameAreaController(Game game, Location returnLocation, Location initialLocation) {
		this(game, returnLocation, initialLocation, null);
	}
	protected GameAreaController(Game game, Location returnLocation, WorldCreator worldCreator) {
		this(game, returnLocation, null, worldCreator);
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
		if (worldCreator != null) {
			this.world = worldCreator.createWorld();
			this.world.setAutoSave(false);
			this.world.setKeepSpawnInMemory(false);

			this.initialLocation = world.getSpawnLocation();
		}

		onAreaInit();
	}

	@Override
	public final void onDeinit() {
		onAreaDeinit();

		Location returnLocation = getReturnLocation();
		World tempWorld = world;

		if (world != null) {
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
