package fr.ludos.game.worldController;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.Utility;
import fr.ludos.game.Game;
import fr.ludos.game.TwoStepGameProcessBase;

public abstract class GameWorldController extends TwoStepGameProcessBase {
	@Override
	protected final JavaPlugin getPlugin() {
		return getGame().getPlugin();
	}

	private final Game game;
	protected final Game getGame() {
		return game;
	}

	public abstract World getWorld();

	private final Location returnLocation;
	public final Location getReturnLocation() {
		return returnLocation;
	}
	private final UUID returnWorldUUID;
	public final UUID getReturnWorldUUID() {
		return returnWorldUUID;
	}

	protected GameWorldController(Game game, Location returnLocation) {
		this.game = Objects.requireNonNull(game, "Game cannot be null");
		this.returnLocation = Objects.requireNonNull(returnLocation, "Return location cannot be null");
		this.returnWorldUUID = returnLocation.getWorld().getUID();
	}

	public static void deleteWorld(World world) {
		if (world == null) {
			throw new IllegalArgumentException("World cannot be null");
		}

		File folder = world.getWorldFolder();

		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("World folder is not a directory");
		}

		Bukkit.unloadWorld(world, false);
		Utility.deleteRecursive(folder);

		File worldFolder = new File(Bukkit.getWorldContainer().getAbsolutePath() + "/" + world.getName());
		worldFolder.delete();

		Bukkit.getWorlds().remove(world);
	}
}
