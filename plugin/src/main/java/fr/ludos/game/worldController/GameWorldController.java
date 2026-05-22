package fr.ludos.game.worldController;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.Utility;
import fr.ludos.game.Game;
import fr.ludos.game.GameProcessBase;
import fr.ludos.game.areaController.GameAreaController;
import fr.ludos.game.lobbyController.GameLobbyController;
import fr.ludos.group.Group;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.TriState;

public sealed abstract class GameWorldController extends GameProcessBase permits SingleWorldController, MultiWorldController {
	private final HashSet<World> scheduledToFlush = new HashSet<>();
	private BukkitTask flushTask;

	@Override
	protected final JavaPlugin getPlugin() {
		return getGame().getPlugin();
	}

	private final Game game;
	protected final Game getGame() {
		return game;
	}

	private final GameLobbyController lobbyController;
	public GameLobbyController getLobbyController() {
		return this.lobbyController;
	}

	private final GameAreaController areaController;
	public GameAreaController getAreaController() {
		return this.areaController;
	}

	private World world;
	public World getWorld() {
		return this.world;
	}

	private final Location returnLocation;
	public final Location getReturnLocation() {
		return returnLocation;
	}

	@Override
	public boolean isClear() {
		return super.isClear() && scheduledToFlush.isEmpty() && getLobbyController().isClear() && getAreaController().isClear();
	}

	@Override
	protected final void onStart() {
		super.onStart();

		onWorldStart();

		lobbyController.stop();
	}

	protected void onWorldStart() {}

	@Override
	protected final void onStop() {
		lobbyController.stop();
		areaController.stop();

		onWorldStop();

		super.onStop();
	}

	protected void onWorldStop() {}

	protected GameWorldController(Game game, GameLobbyController lobbyController, GameAreaController areaController, Location returnLocation) {
		this.game = Objects.requireNonNull(game, "Game cannot be null");
		this.lobbyController = lobbyController;
		this.areaController = areaController;

		this.returnLocation = Objects.requireNonNull(returnLocation, "Return location cannot be null");
	}

	public static Location pickInitialLocation(Group group) {
		Optional<Player> any = group.getOnlinePlayers().stream().filter(Player::isOnline).findFirst();
		if (any.isPresent()) return any.get().getLocation();
		return Bukkit.getWorlds().get(0).getSpawnLocation();
	}

	public World transferToNewWorld(WorldCreator creator) {
		World tempWorld = this.world;

		lobbyController.stop();
		areaController.stop();

		for (Player player : getGame().getGroup().getOnlinePlayers()) {
			player.sendMessage(
				Component.text("Loading world...")
					.color(NamedTextColor.YELLOW)
					.decorate(TextDecoration.BOLD)
			);
		}

		creator.keepSpawnLoaded(TriState.FALSE);
		World world = creator.createWorld();
		world.setKeepSpawnInMemory(false);
		world.setAutoSave(false);

		this.world = world;

		areaController.start();
		lobbyController.start();

		return tempWorld;
	}

	public void transferToWorld(World world, Location location) {
		Location tempLoc = location.clone();
		tempLoc.setWorld(world);

		transferToWorld(tempLoc);
	}
	public void transferToWorld(Location location) {
		List<Player> playersInWorld = world.getPlayers();

		if (playersInWorld.size() > 0) {
			for (Player player : playersInWorld) {
				if (player.isDead()) {
					player.spigot().respawn();
				}
				player.teleport(location);
			}
		}
	}

	public boolean flushWorld(World world, boolean evacuate) {
		if (evacuate) {
			transferToWorld(getReturnLocation());
		}

		if (world == null) return true;

		world.setAutoSave(false);
		world.setKeepSpawnInMemory(false);
		boolean unloaded = Bukkit.unloadWorld(world, false);

		if (unloaded) {
			Bukkit.getWorlds().remove(world);
			deleteWorld(world);
		}

		return unloaded;
	}

	public void scheduleFlushWorld(World world, boolean evacuate) {
		if (getPlugin().isEnabled()) {
			scheduledToFlush.add(world);

			if (flushTask == null || flushTask.isCancelled()) {
				flushTask = new BukkitRunnable() {
					public void run() {
						List<World> deleted = new ArrayList<>();
						for (World world : scheduledToFlush) {
							if (world == null) {
								deleted.add(world);
								continue;
							}
							if (flushWorld(world, evacuate)) {
								deleted.add(world);
							}
						}
						scheduledToFlush.removeAll(deleted);

						if (scheduledToFlush.size() == 0) {
							this.cancel();
							flushTask = null;
						}
					}
				}.runTaskTimer(getPlugin(), 0, 20);
			}
		}
		else {
			flushWorld(world, evacuate);
		}
	}

	public static void deleteWorld(World world) {
		if (world == null) return;

		File folder = world.getWorldFolder();
		if (!folder.isDirectory()) return;

		Utility.deleteRecursive(folder);

		File worldFolder = new File(Bukkit.getWorldContainer().getAbsolutePath() + "/" + world.getName());
		worldFolder.delete();
	}
}
