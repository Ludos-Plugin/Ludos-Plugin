package fr.ludos.core.lobby;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.google.common.base.Predicate;

import fr.ludos.core.PlayerList;
import fr.ludos.core.Utility;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameProcessBase;
import fr.ludos.core.structure.Structure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import net.kyori.adventure.title.TitlePart;

/**
 * Manager of the 'lobby' phase of a {@link Game}'s startup sequence, used to buffer the loading-in of Players into the {@link Game}'s managed {@link World}.
 */
public final class Lobby extends GameProcessBase {
	/**
	 * How much of a {@link Player}'s state should be cleared/reset in and out of a {@link Lobby}.
	 */
	public enum ClearMode {
		NONE,
		STATE {
			@Override
			public void handlePlayer(Player player) {
				Utility.resetPlayerState(player);
			}
		},
		ALL {
			@Override
			public void handlePlayer(Player player) {
				Utility.resetPlayer(player);
			}
		};

		public void handlePlayer(Player player) {}
	}

	private final Game game;
	@Override
	protected final JavaPlugin plugin() {
		return this.game.plugin();
	}

	private final Location location;

	private final Structure.Builder structureBuilder;
	public final Structure.Builder structureBuilder() {
		return this.structureBuilder;
	}

	private Structure structure;
	public final Structure getStructure() {
		return this.structure;
	}

	private final ClearMode clearMode;
	private final GameMode gameMode;
	private final Duration waitDuration;
	private final PlayerList selectedPlayers;
	private final Predicate<Lobby> startCondition;
	private final Runnable endFunction;
	private final Component waitingText;
	private final Component startingText;

	private Set<OfflinePlayer> players;

	private BukkitTask joinLobbyTask;
	private BukkitTask startGameTask;
	private Set<OfflinePlayer> finalPlayersSelection;


	private Lobby(Structure.Builder structureBuilder, Location location, Game game, ClearMode clearMode, GameMode gameMode, Duration waitDuration, PlayerList selectedPlayers, Predicate<Lobby> startCondition, Runnable endFunction, Component waitingText, Component startingText) {
		this.structureBuilder = structureBuilder;
		this.game = game;
		this.location = location;
		this.clearMode = clearMode;
		this.gameMode = gameMode;
		this.waitDuration = waitDuration;
		this.selectedPlayers = selectedPlayers;
		this.startCondition = startCondition;
		this.endFunction = endFunction;
		this.waitingText = waitingText;
		this.startingText = startingText;
	}

	public static Builder within(Game game) {
		return new Builder(game);
	}

	@Override
	public boolean isClear() {
		return ! isStarted() &&
			(joinLobbyTask == null || joinLobbyTask.isCancelled()) &&
			(startGameTask == null || startGameTask.isCancelled());
	}

	@Override
	protected void onStart() {
		super.onStart();

		structure = structureBuilder.build(location);

		startWaitSequence();
	}

	@Override
	public void onStop() {
		super.onStop();

		players = null;
		structure.destroy();

		structure = null;

		if (joinLobbyTask != null) {
			joinLobbyTask.cancel();
		}
		joinLobbyTask = null;

		if (startGameTask != null) {
			startGameTask.cancel();
		}
		startGameTask = null;
	}

	private void startWaitSequence() {
		Lobby lobby = this;
		joinLobbyTask = new BukkitRunnable() {
			@Override
			public void run() {
				finalPlayersSelection = selectedPlayers.getPlayers().stream().collect(Collectors.toSet());

				Set<OfflinePlayer> allPlayersToTeleport = finalPlayersSelection.stream()
					.filter(player ->
						!player.isOnline() ||
						!player.getPlayer().getWorld().equals(location.getWorld()) ||
						!structure.contains(player.getPlayer().getBoundingBox())
					)
					.collect(Collectors.toSet());

				if (startCondition.apply(lobby) && allPlayersToTeleport.isEmpty()) {
					players = finalPlayersSelection;
					startGameTask = startOnTimer(
						startingText,
						(int) waitDuration.toSeconds(),
						endFunction
					);
					cancel();
					return;
				}

				Location structureEntrance = structure.getEntranceLocation();
				for (OfflinePlayer waitedPlayer : allPlayersToTeleport) {
					Player player = waitedPlayer.getPlayer();
					if (player == null) continue;
					if (! player.isOnline()) continue;

					Location randomizedEntrance = structure.randomizedEntranceLocation();
					Vector lookingDirection = structureEntrance.toVector().subtract(randomizedEntrance.toVector());
					if (lookingDirection.lengthSquared() >= 0) {
						try {
							lookingDirection.normalize();
							randomizedEntrance.setDirection(lookingDirection);
						} finally { }
					}
					player.teleport(randomizedEntrance);
					clearMode.handlePlayer(player);
					if (gameMode != null) player.setGameMode(gameMode);
				}


				Set<Player> waitingPlayers = Utility.getOnline(finalPlayersSelection.stream())
					.filter(player -> player.getWorld().equals(location.getWorld()) )
					.collect(Collectors.toSet());

				for (Player waitingPlayer : waitingPlayers) {
					waitingPlayer.sendTitlePart(
						TitlePart.TIMES,
						Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ZERO)
					);
					waitingPlayer.sendTitlePart(
						TitlePart.TITLE,
						waitingText
					);
				}
			}
		}.runTaskTimer(plugin(), 0, 20);
	}

	private final BukkitTask startOnTimer(Component text, int seconds, Runnable onFinish) {
		if (onFinish == null) return null;

		return new BukkitRunnable() {
			int timeLeft = seconds;

			@Override
			public void run() {
				if (timeLeft <= 0) {
					stop();

					onFinish.run();
					return;
				}

				for (OfflinePlayer offlinePlayer : players) {
					Player player = offlinePlayer.getPlayer();
					if (player == null) continue;

					player.showTitle(
						Title.title(
							text,
							Component.text(timeLeft),
							Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ZERO)
						)
					);
				}

				timeLeft--;
			}
		}.runTaskTimer(plugin(), 0, 20L);
	}


	@EventHandler
	public void onPlayerDieInLobby(PlayerDeathEvent event) {
		Player player = event.getPlayer();
		if (players == null || ! players.contains(player)) return;

		event.setCancelled(true);
		player.playEffect(EntityEffect.TOTEM_RESURRECT);
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (! (event.getEntity() instanceof Player player)) return;
		if (players == null || ! players.contains(player)) return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onBreakBlock(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (players == null || ! players.contains(player)) return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (! finalPlayersSelection.contains(player)) return;

		startGameTask.cancel();
		startGameTask = null;
		startWaitSequence();
	}

	/**
	 * Builder for {@link Lobby}.
	 */
	public static final class Builder {
		private final Game game;

		public Builder(Game game) {
			this.game = Objects.requireNonNull(game);
		}

		private Location location;
		public Builder at(Location location) {
			this.location = location;
			return this;
		}

		private Structure.Builder structureBuilder = null;
		private final Structure.Builder getStructureBuilder() {
			return structureBuilder != null
				? structureBuilder
				: new LobbyStructure.Builder();
		}
		public Builder asStructure(Structure.Builder builder) {
			structureBuilder = builder;
			return this;
		}

		private ClearMode clearMode = ClearMode.NONE;
		public Builder clear(ClearMode clear) {
			this.clearMode = clear;
			return this;
		}

		private GameMode gameMode = GameMode.SURVIVAL;
		public Builder setGameMode(@Nullable GameMode gameMode) {
			this.gameMode = gameMode;
			return this;
		}

		private Duration waitDuration = null;
		private final Duration getWaitDuration() {
			return waitDuration != null
				? waitDuration
				: Duration.ofSeconds(10);
		}

		public Builder wait(Duration duration) {
			waitDuration = duration;
			return this;
		}

		private PlayerList players;
		public Builder waitFor(PlayerList players) {
			this.players = players;
			return this;
		}

		private List<Predicate<Lobby>> lobbyStartConditions = new ArrayList<>();
		private boolean canStart(Lobby lobby) {
			for (Predicate<Lobby> cond : new ArrayList<>(lobbyStartConditions)) {
				if (! cond.test(lobby)) return false;
			}
			return true;
		}
		public Builder waitUntil(Predicate<Lobby> condition) {
			this.lobbyStartConditions.add(condition);
			return this;
		}
		public Builder dontWaitUntil(Predicate<Lobby> condition) {
			this.lobbyStartConditions.remove(condition);
			return this;
		}

		private List<Runnable> endFunctions = new ArrayList<>();
		private void onEnd() {
			for (Runnable func : new ArrayList<>(endFunctions)) func.run();
		}
		public Builder then(Runnable endFunction) {
			endFunctions.add(endFunction);
			return this;
		}
		public Builder thenDont(Runnable endFunction) {
			endFunctions.remove(endFunction);
			return this;
		}

		private static final Component DEFAULT_WAITING_TEXT = Component.text("Waiting for players to join...");
		private Component waitingText;
		private Component getWaitingText() {
			return waitingText != null
				? waitingText
				: DEFAULT_WAITING_TEXT;
		}
		public Builder showWhenWaiting(Component text) {
			this.waitingText = text;
			return this;
		}

		private static final Component DEFAULT_STARTING_TEXT = Component.text("Starting Game");
		private Component startingText;
		private Component getStartingText() {
			return startingText != null
				? startingText
				: DEFAULT_STARTING_TEXT;
		}
		public Builder showOnStart(Component text) {
			this.startingText = text;
			return this;
		}

		public Lobby build(World world) {
			Location loc = location;
			if (loc.getWorld() != Objects.requireNonNull(world)) {
				loc = loc.clone();
				loc.setWorld(world);
			}

			return new Lobby(
				getStructureBuilder(),
				loc,
				game,
				clearMode,
				gameMode,
				getWaitDuration(),
				players,
				this::canStart,
				this::onEnd,
				getWaitingText(),
				getStartingText()
			);
		}
	}
}
