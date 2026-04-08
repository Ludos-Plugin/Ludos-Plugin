package fr.ludos.game.sheepwars;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

import net.kyori.adventure.util.TriState;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldManager {

	public static final String SOURCE_WORLD_NAME = "sheep_wars_with_water";
	public static final String ACTIVE_WORLD_NAME = "sheepwars_active";
	public static final String LOBBY_WORLD_KEY = "lobby-world";
	public static final String DEFAULT_LOBBY_WORLD_NAME = "world";

	private final JavaPlugin plugin;
	private final AtomicInteger operationToken = new AtomicInteger();

	@Nullable
	private volatile World activeWorld;

	public WorldManager(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Nullable
	public World getActiveWorld() {
		World world = activeWorld;
		if (world != null) {
			return world;
		}

		return Bukkit.getWorld(ACTIVE_WORLD_NAME);
	}

	public void loadGameWorld(String templateName, @Nullable Runnable callback) {
		Objects.requireNonNull(templateName, "templateName");

		final int token = operationToken.incrementAndGet();

		Bukkit.getScheduler().runTask(plugin, () -> {
			if (operationToken.get() != token) {
				return;
			}

			World existingWorld = Bukkit.getWorld(ACTIVE_WORLD_NAME);
			if (existingWorld != null) {
				Location fallbackLocation = resolveFallbackLocation(existingWorld);
				if (fallbackLocation == null) {
					plugin.getLogger().warning("Active SheepWars world is already loaded, but no fallback world exists to unload it safely.");
					return;
				}

				ejectPlayers(existingWorld, fallbackLocation);
				Bukkit.unloadWorld(existingWorld, false);
			}

			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				if (operationToken.get() != token) {
					return;
				}

				try {
					copyTemplateWorld(templateName);
				} catch (IOException e) {
					plugin.getLogger().severe("Failed to prepare SheepWars world from template '" + templateName + "': " + e.getMessage());
					e.printStackTrace();
					return;
				}

				Bukkit.getScheduler().runTask(plugin, () -> {
					if (operationToken.get() != token) {
						return;
					}

					WorldCreator creator = new WorldCreator(ACTIVE_WORLD_NAME);
					creator.keepSpawnLoaded(TriState.FALSE);
					creator.generator(new VoidGenerator());

					World world = Bukkit.createWorld(creator);
					if (world == null) {
						plugin.getLogger().severe("Failed to load SheepWars active world: " + ACTIVE_WORLD_NAME);
						deleteWorldFolderAsync(token);
						return;
					}

					world.setAutoSave(false);
					world.setDifficulty(Difficulty.NORMAL);
					world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
					world.setTime(1000L);
					world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
					world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
					world.setStorm(false);
					world.setThundering(false);

					activeWorld = world;

					if (callback != null && operationToken.get() == token) {
						callback.run();
					}
				});
			});
		});
	}

	public void destroyWorld(@Nullable Location fallbackLocation) {
		final int token = operationToken.incrementAndGet();
		World world = Bukkit.getWorld(ACTIVE_WORLD_NAME);
		if (world == null) {
			deleteWorldFolderAsync(token);
			return;
		}

		Bukkit.getScheduler().runTask(plugin, () -> {
			if (operationToken.get() != token) {
				return;
			}

			Location destination = fallbackLocation != null ? fallbackLocation : resolveFallbackLocation(world);
			if (destination == null) {
				plugin.getLogger().warning("No fallback location available to eject players from SheepWars world.");
				return;
			}

			ejectPlayers(world, destination);

			if (!Bukkit.unloadWorld(world, false)) {
				plugin.getLogger().warning("Unable to unload SheepWars world: " + ACTIVE_WORLD_NAME);
				return;
			}

			activeWorld = null;
			deleteWorldFolderAsync(token);
		});
	}

	private void ejectPlayers(World world, Location destination) {
		Location safeDestination = destination.clone();
		for (Player player : new ArrayList<>(world.getPlayers())) {
			if (player.isOnline()) {
				player.setSpectatorTarget(null);
				player.setGameMode(GameMode.ADVENTURE);
				player.teleport(safeDestination);
			}
		}
	}

	private void copyTemplateWorld(String templateName) throws IOException {
		Path worldContainer = Bukkit.getWorldContainer().toPath();
		Path sourceWorld = worldContainer.resolve(templateName);
		Path activeWorld = worldContainer.resolve(ACTIVE_WORLD_NAME);

		if (!Files.exists(sourceWorld)) {
			throw new IOException("Template world not found: " + sourceWorld);
		}

		deleteDirectory(activeWorld);
		Files.createDirectories(activeWorld);

		Files.walkFileTree(sourceWorld, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path relative = sourceWorld.relativize(dir);
				Path targetDirectory = activeWorld.resolve(relative);
				Files.createDirectories(targetDirectory);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String fileName = file.getFileName().toString();
				if ("uid.dat".equalsIgnoreCase(fileName) || "session.lock".equalsIgnoreCase(fileName)) {
					return FileVisitResult.CONTINUE;
				}

				Path relative = sourceWorld.relativize(file);
				Path targetFile = activeWorld.resolve(relative);
				Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
				return FileVisitResult.CONTINUE;
			}
		});

		Files.deleteIfExists(activeWorld.resolve("uid.dat"));
		Files.deleteIfExists(activeWorld.resolve("session.lock"));
	}

	private void deleteWorldFolderAsync(int token) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			if (operationToken.get() != token) {
				return;
			}

			try {
				deleteDirectory(Bukkit.getWorldContainer().toPath().resolve(ACTIVE_WORLD_NAME));
			} catch (IOException e) {
				plugin.getLogger().warning("Failed to delete SheepWars world folder: " + e.getMessage());
				e.printStackTrace();
			}
		});
	}

	private void deleteDirectory(Path path) throws IOException {
		if (!Files.exists(path)) {
			return;
		}

		Files.walkFileTree(path, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.deleteIfExists(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.deleteIfExists(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	@Nullable
	private Location resolveFallbackLocation(World excludedWorld) {
		Location lobbyLocation = resolveLobbySpawnLocation();
		if (lobbyLocation != null) {
			return lobbyLocation;
		}

		for (World world : Bukkit.getWorlds()) {
			if (world != excludedWorld) {
				return world.getSpawnLocation();
			}
		}

		return null;
	}

	@Nullable
	private Location resolveLobbySpawnLocation() {
		FileConfiguration config = plugin.getConfig();
		String lobbyWorldName = config.getString(LOBBY_WORLD_KEY, DEFAULT_LOBBY_WORLD_NAME);
		World lobbyWorld = Bukkit.getWorld(lobbyWorldName);
		if (lobbyWorld != null) {
			return lobbyWorld.getSpawnLocation();
		}

		if (!Bukkit.getWorlds().isEmpty()) {
			return Bukkit.getWorlds().get(0).getSpawnLocation();
		}

		return null;
	}
}