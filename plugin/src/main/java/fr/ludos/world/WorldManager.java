package fr.ludos.world;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.Utility;
import fr.ludos.area.Area;
import fr.ludos.game.GameProcessBase;
import fr.ludos.lobby.Lobby;
import net.kyori.adventure.util.TriState;

public final class WorldManager extends GameProcessBase {
	private final Builder builder;
	@Override
	protected JavaPlugin getPlugin() {
		return this.builder.plugin;
	}

	private Map<Integer, BukkitTask> flushTasks = new HashMap<>();

	private Location returnLocation;
	public final Location getReturnLocation() {
		return this.returnLocation;
	}

	private World world;
	public final World getWorld() {
		return this.world;
	}

	@Nullable
	private Area area;
	@Nullable
	public final Area getArea() {
		return this.area;
	}
	public final WorldManager mutateArea(Consumer<Area.Builder<?>> config) {
		if (area == null) return this;
		area.mutate(config);
		return this;
	}

	public boolean isAreaStarted() {
		return area != null && area.isStarted();
	}

	@Nullable
	private Lobby lobby;
	@Nullable
	public final Lobby getLobby() {
		return this.lobby;
	}
	public final WorldManager mutateLobby(Consumer<Lobby.Builder> config) {
		if (lobby == null) return this;
		lobby.mutate(config);
		return this;
	}

	public boolean isLobbyStarted() {
		return lobby != null && lobby.isStarted();
	}


	private WorldManager(Builder builder) {
		this.builder = builder;

		this.area = builder.areaBuilder != null
			? builder.areaBuilder.build()
			: null;

		this.lobby = builder.lobbyBuilder != null
			? builder.lobbyBuilder.build()
			: null;
	}

	public static Builder within(JavaPlugin plugin, Location returnLocation) {
		return new Builder(plugin, returnLocation);
	}

	public boolean transfer(Consumer<Builder> config) {
		World oldWorld = world;

		config.accept(builder);

		stopProcesses();
		startProcesses();

		scheduleFlushWorld(oldWorld, false);

		return true;
	}

	@Override
	public boolean isClear() {
		Area area = getArea();
		Lobby lobby = getLobby();

		return ! isStarted() &&
			(flushTasks.size() == 0) &&
			(area == null || area.isClear()) &&
			(lobby == null || lobby.isClear());
	}

	private void startProcesses() {
		if (builder.world != null) {
			this.world = builder.world;
		} else if (builder.worldCreator != null) {
			builder.worldCreator.keepSpawnLoaded(TriState.FALSE);

			world = builder.worldCreator.createWorld();
			world.setKeepSpawnInMemory(false);
			world.setAutoSave(false);
		} else throw new NullArgumentException("world/worldCreator");

		builder.worldConfig.accept(world);

		returnLocation = builder.returnLocation;

		if (area != null) {
			area.mutate(area -> area
				.in(world)
			).start();
		}
		if (lobby != null) {
			lobby.mutate(lobby -> lobby
				.in(world)
			).start();
		}
	}
	@Override
	protected void onStart() {
		super.onStart();
		startProcesses();
	}

	private void stopProcesses() {
		if (lobby != null) lobby.stop();
		if (area != null) area.stop();
	}
	@Override
	public void onStop() {
		stopProcesses();
		scheduleFlushWorld(true);
	}

	public boolean flushWorld(boolean evacuate) {
		return flushWorld(this.world, evacuate);
	}
	private boolean flushWorld(World world, boolean evacuate) {
		if (evacuate) {
			List<Player> playersInWorld = world.getPlayers();

			if (playersInWorld.size() > 0) {
				for (Player player : playersInWorld) {
					if (player.isDead()) {
						player.spigot().respawn();
					}
					player.teleport(returnLocation);
				}
			}
		}

		if (world == null) return true;

		world.setAutoSave(false);
		world.setKeepSpawnInMemory(false);
		boolean unloaded = Bukkit.unloadWorld(world, false);

		if (unloaded) {
			Bukkit.getWorlds().remove(world);
			deleteWorld(world);

			world = null;
		}

		return unloaded;
	}

	public TriState scheduleFlushWorld(boolean evacuate) {
		return scheduleFlushWorld(this.world, evacuate);
	}
	private TriState scheduleFlushWorld(World world, boolean evacuate) {
		if (world == null) return TriState.TRUE;
		if (getPlugin().isEnabled()) {
			BukkitTask task = new BukkitRunnable() {
				public void run() {
					if (flushWorld(world, evacuate)) {
						cancel();
						flushTasks.remove(this.getTaskId());
					}
				}
			}.runTaskTimer(getPlugin(), 0, 20);
			flushTasks.put(task.getTaskId(), task);
			return TriState.NOT_SET;
		}
		else {
			return TriState.byBoolean(flushWorld(world, evacuate));
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


	public static final class Builder {
		private final JavaPlugin plugin;
		private Location returnLocation;

		public Builder(JavaPlugin plugin, Location returnLocation) {
			this.plugin = Objects.requireNonNull(plugin);
			this.returnLocation = Objects.requireNonNull(returnLocation);
		}

		private World world;
		private WorldCreator worldCreator;

		public Builder of(World world) {
			this.world = world;
			this.worldCreator = null;
			return this;
		}

		public Builder of(WorldCreator creator) {
			this.world = null;
			this.worldCreator = creator;
			return this;
		}

		@Nullable
		private Area.Builder<?> areaBuilder;
		public final Builder inArea(Area.Builder<?> area) {
			this.areaBuilder = area;
			return this;
		}
		public final Builder mutateArea(Consumer<Area.Builder<?>> config) {
			if (areaBuilder == null) return this;
			config.accept(areaBuilder);
			return this;
		}

		@Nullable
		private Lobby.Builder lobbyBuilder;
		public final Builder withLobby(Lobby.Builder lobby) {
			this.lobbyBuilder = lobby;
			return this;
		}
		public final Builder mutateLobby(Consumer<Lobby.Builder> config) {
			if (lobbyBuilder == null) return this;
			config.accept(lobbyBuilder);
			return this;
		}

		private Consumer<World> worldConfig = ignored -> {};

		public final Builder config(Consumer<World> config) {
			this.worldConfig = config;
			return this;
		}

		public WorldManager build() {
			return new WorldManager(this);
		}
	}
}