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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
	protected void onSetup() {
		if (world != null) {
			throw new IllegalStateException("World has already been initialized");
		}

		for (Player player : getGame().getGroup().getOnlinePlayers()) {
			player.sendMessage(
				Component.text("Loading world...")
					.color(NamedTextColor.YELLOW)
					.decorate(TextDecoration.BOLD)
			);
		}

		worldCreator.keepSpawnLoaded(TriState.FALSE);
		World world = worldCreator.createWorld();
		world.setKeepSpawnInMemory(false);
		world.setAutoSave(false);

		this.world = world;
	}

	private boolean evacuateWorld(World world) {
		List<Player> playersInWorld = world.getPlayers();

		// Load the world
		Bukkit.getWorld(getReturnWorldUUID());

		if (playersInWorld.size() > 0) {
			for (Player player : playersInWorld) {
				if (player.isDead()) {
					player.spigot().respawn();
				}
				player.teleport(getReturnLocation());
			}
		}

		world.setAutoSave(false);
		world.setKeepSpawnInMemory(false);
		boolean unloaded = Bukkit.unloadWorld(world, false);

		if (unloaded) {
			deleteWorld(world);
		}

		return unloaded;
	}

	@Override
	protected void onSetdown() {
		if (world == null) {
			throw new IllegalStateException("World has not been initialized");
		}

		World tempWorld = world;
		world = null;


		if (getPlugin().isEnabled()) {
			new BukkitRunnable() {
				public void run() {
					if (evacuateWorld(tempWorld)) {
						this.cancel();
					}
				}
			}.runTaskTimer(getPlugin(), 0, 20);
		}
		else {
			evacuateWorld(tempWorld);
		}
	}
}
