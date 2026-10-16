package fr.ludos.core.world;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.core.Utility;
import fr.ludos.core.area.Area;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameProcessBase;
import fr.ludos.core.lobby.Lobby;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.TriState;

/**
 * Manages a dedicated world for the {@link Game} process and its related area.
 */
public final class WorldManager extends GameProcessBase {
	private final Builder builder;
	@Override
	protected JavaPlugin plugin() {
		return this.builder.game.plugin();
	}

	private Map<Integer, BukkitTask> flushTasks = new HashMap<>();

	private World world;
	public final World getWorld() {
		return this.world;
	}

	private Area area;
	public Area getArea() {
		return area;
	}
	public final WorldManager mutateArea(Consumer<Area.Builder<?>> config) {
		if (builder.areaBuilder == null) return this;
		config.accept(builder.areaBuilder);
		return this;
	}

	public boolean isAreaStarted() {
		return area != null && area.isStarted();
	}

	private Lobby lobby;
	public Lobby getLobby() {
		return lobby;
	}
	public final WorldManager mutateLobby(Consumer<Lobby.Builder> config) {
		if (builder.lobbyBuilder == null) return this;
		config.accept(builder.lobbyBuilder);
		return this;
	}

	public boolean isLobbyStarted() {
		return lobby != null && lobby.isStarted();
	}


	private WorldManager(Builder builder) {
		this.builder = builder;
	}

	public static Builder within(Game game) {
		return new Builder(game);
	}

	public boolean transfer(Consumer<Builder> config) {
		World oldWorld = world;

		config.accept(builder);

		startProcesses();

		scheduleFlushWorld(oldWorld, false);

		return true;
	}

	@Override
	public boolean isClear() {
		return ! isStarted() &&
			(flushTasks.size() == 0) &&
			(area == null || area.isClear()) &&
			(lobby == null || lobby.isClear());
	}

	private void startProcesses() {
		builder.game.group().sendMessage(
			Component.text("Loading world...")
				.color(NamedTextColor.YELLOW)
				.decorate(TextDecoration.BOLD)
		);

		if (builder.world != null) {
			this.world = builder.world;
		} else if (builder.worldCreator != null) {
			builder.worldCreator.keepSpawnLoaded(TriState.FALSE);

			world = builder.worldCreator.createWorld();
		} else throw new NullArgumentException("world/worldCreator");
		world.setKeepSpawnInMemory(false);
		world.setAutoSave(false);

		builder.configure(world);

		stopProcesses();

		if (builder.areaBuilder != null) {
			area = builder.areaBuilder.build(world);
			area.start();
		}
		if (builder.lobbyBuilder != null) {
			lobby = builder.lobbyBuilder.build(world);
			lobby.start();
		}
	}
	@Override
	protected void onStart() {
		super.onStart();
		startProcesses();
	}

	private void stopProcesses() {
		if (lobby != null) lobby.stop();
		lobby = null;

		if (area != null) area.stop();
		area = null;
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
			builder.game.returnAllPlayers();
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
		if (plugin().isEnabled()) {
			BukkitTask task = new BukkitRunnable() {
				public void run() {
					if (flushWorld(world, evacuate)) {
						cancel();
						flushTasks.remove(this.getTaskId());
					}
				}
			}.runTaskTimer(plugin(), 0, 20);
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

		File worldFolder = new File(Bukkit.getWorldContainer().getAbsolutePath() + '/' + world.getName());
		worldFolder.delete();
	}

	/**
	 * Builder for {@link WorldManager}.
	 */
	public static final class Builder {
		private final Game game;

		public Builder(Game game) {
			this.game = Objects.requireNonNull(game);
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

		private Consumer<World> worldConfig = null;

		public final Builder config(Consumer<World> config) {
			this.worldConfig = config;
			return this;
		}

		public final World configure(World world) {
			if (this.worldConfig != null) {
				this.worldConfig.accept(world);
			}
			return world;
		}

		public WorldManager build() {
			return new WorldManager(this);
		}
	}
}