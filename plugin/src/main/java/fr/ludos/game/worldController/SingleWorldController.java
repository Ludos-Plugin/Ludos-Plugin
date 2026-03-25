package fr.ludos.game.worldController;

import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.game.Game;
import net.kyori.adventure.util.TriState;

public class SingleWorldController extends GameWorldController {
	private final WorldCreator worldCreator;

	private World world;
	@Override
	public final World getWorld() {
		return world;
	}

	public SingleWorldController(Game game, WorldCreator worldCreator, Location returnLocation) {
		super(game, returnLocation);
		this.worldCreator = Objects.requireNonNull(worldCreator, "WorldCreator cannot be null");
	}


	@Override
	protected void onInit() {
		if (world != null) {
			throw new IllegalStateException("World has already been initialized");
		}

		worldCreator.keepSpawnLoaded(TriState.FALSE);
		World world = worldCreator.createWorld();
		world.setKeepSpawnInMemory(false);
		world.setAutoSave(false);

		this.world = world;
	}

	@Override
	protected void onDeinit() {
		if (world == null) {
			throw new IllegalStateException("World has not been initialized");
		}

		Location returnLocation = getReturnLocation();

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
					deleteWorld(tempWorld);
					this.cancel();
				}
			}
		}.runTaskTimer(getPlugin(), 0, 20);

		world = null;
	}
}
