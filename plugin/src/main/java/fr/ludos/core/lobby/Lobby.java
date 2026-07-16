package fr.ludos.core.lobby;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.base.Predicate;

import fr.ludos.core.Utility;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameProcessBase;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupConfigMap;
import fr.ludos.core.structure.Structure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import net.kyori.adventure.title.TitlePart;

public final class Lobby extends GameProcessBase {
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

	private final Builder builder;
	@Override
	protected JavaPlugin getPlugin() {
		return this.builder.game.getPlugin();
	}

	private Structure structure;
	public final Structure getStructure() {
		return this.structure;
	}

	private Set<OfflinePlayer> players;

	private BukkitTask joinLobbyTask;
	private BukkitTask startGameTask;


	private Lobby(Builder builder) {
		this.builder = builder;
	}

	public static Builder within(Game game) {
		return new Builder(game);
	}

	public Lobby mutate(Consumer<Builder> config) {
		config.accept(builder);
		return this;
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

		structure = builder.getStructureBuilder().build(builder.location);

		Lobby lobby = this;
		joinLobbyTask = new BukkitRunnable() {
			@Override
			public void run() {
				Set<OfflinePlayer> allPlayers = builder.getPlayers();

				Set<OfflinePlayer> allPlayersToTeleport = allPlayers.stream()
					.filter(player ->
						!player.isOnline() ||
						!player.getPlayer().getWorld().equals(builder.location.getWorld()) ||
						!structure.contains(player.getPlayer().getBoundingBox())
					)
					.collect(Collectors.toSet());

				if (builder.canStart(lobby) && allPlayersToTeleport.isEmpty()) {
					players = allPlayers;
					startGameTask = startOnTimer(
						builder.getStartingText(),
						(int) builder.getWaitDuration().toSeconds(),
						builder::onEnd
					);
					cancel();
					return;
				}

				for (OfflinePlayer waitedPlayer : allPlayersToTeleport) {
					Player player = waitedPlayer.getPlayer();
					if (player == null) continue;
					if (! player.isOnline()) continue;

					player.teleport(structure.getEntranceLocation());
					builder.clearMode.handlePlayer(player);
					if (builder.gameMode != null) player.setGameMode(builder.gameMode);
				}


				Set<Player> waitingPlayers = Utility.getOnline(allPlayers.stream())
					.filter(player -> player.getWorld().equals(builder.location.getWorld()) )
					.collect(Collectors.toSet());

				for (Player waitingPlayer : waitingPlayers) {
					waitingPlayer.sendTitlePart(
						TitlePart.TIMES,
						Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ZERO)
					);
					waitingPlayer.sendTitlePart(
						TitlePart.TITLE,
						builder.getWaitingText()
					);
				}
			}
		}.runTaskTimer(getPlugin(), 0, 20);
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
		}.runTaskTimer(getPlugin(), 0, 20L);
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

	public static final class Builder {
		private final Game game;

		public Builder(Game game) {
			this.game = game;
		}

		private Location location;
		public Builder at(Location location) {
			this.location = location;
			return this;
		}
		public Builder in(World world) {
			this.location = world.getSpawnLocation();
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

		private Collection<OfflinePlayer> players;
		private Group playersGroup;
		private final Set<OfflinePlayer> getPlayers() {
			if (players != null) return new HashSet<>(players);
			if (playersGroup != null) {
				LobbyWaitPlayersOption option = GroupConfigMap.WAIT_PLAYERS.getGroupConfig(playersGroup);
				return option.getPlayers(playersGroup);
			}
			return null;
		}
		public Builder waitFor(Collection<OfflinePlayer> players) {
			this.players = players;
			return this;
		}
		public Builder waitFor(Group group) {
			this.playersGroup = group;
			this.players = null;
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

		public Lobby build() {
			return new Lobby(this);
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
	}
}
