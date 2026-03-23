package fr.ludos.game;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;


public abstract class GameTeamController extends GameProcessBase {
	private final Game game;
	public final Game getGame() {
		return game;
	}

	@Override
	protected final JavaPlugin getPlugin() {
		return getGame().getPlugin();
	}

	private final GameJoinOption joinOption;


	public GameTeamController(Game game, GameJoinOption joinOption) {
		this.game = game;
		this.joinOption = joinOption;
	}
	public GameTeamController(Game game) {
		this(game, GameJoinOption.auto);
	}

	public abstract Collection<LivingEntity> getEntities();
	public abstract Collection<Team> getTeams();

	public Collection<Player> getPlayers() {
		return getEntities().stream()
			.filter(e -> e instanceof Player)
			.map(e -> (Player)e)
			.collect(Collectors.toSet());
	}


	public Set<Player> getArenaPlayers(GameTeamController teamController) {
		return new HashSet<>(teamController.getPlayers());
	}

	public List<Player> getAliveArenaPlayers(GameTeamController teamController) {
		return getArenaPlayers(teamController).stream()
			.filter(Player::isOnline)
			.filter(p -> !p.isDead())
			.filter(p -> p.getGameMode() == GameMode.SURVIVAL)
			.toList();
	}

	public static List<Set<Player>> createTeamBuckets(int teamCount) {
		if (teamCount < 1) {
			throw new IllegalArgumentException("teamCount must be >= 1");
		}

		List<Set<Player>> teams = new ArrayList<>(teamCount);
		for (int i = 0; i < teamCount; i++) {
			teams.add(new HashSet<>());
		}

		return teams;
	}

	public static void distributePlayersInTeams(Collection<Player> players, List<Set<Player>> teams, boolean enabled, boolean shuffle) {
		if (!enabled || players == null || players.isEmpty() || teams == null || teams.isEmpty()) return;

		List<Player> pool = new ArrayList<>(players);
		if (shuffle) {
			Collections.shuffle(pool);
		}

		for (Player player : pool) {
			int smallestTeamIndex = 0;
			for (int i = 1; i < teams.size(); i++) {
				if (teams.get(i).size() < teams.get(smallestTeamIndex).size()) {
					smallestTeamIndex = i;
				}
			}
			teams.get(smallestTeamIndex).add(player);
		}
	}

	public Set<Player> extractPlayersFromTeam(Team team) {
		Set<Player> players = new HashSet<>();
		if (team == null) return players;

		for (String name : team.getEntries()) {
			Player player = Bukkit.getPlayer(name);
			if (player != null) {
				players.add(player);
			}
		}

		return players;
	}

	public boolean areAllies(LivingEntity entity1, LivingEntity entity2) {
		if (entity1 == null || entity2 == null) return false;
		if (entity1.equals(entity2)) return true;

		Scoreboard scoreboard = game.getScoreboard();

		var entity1Team = scoreboard.getEntryTeam(entity1.getName());
		var entity2Team = scoreboard.getEntryTeam(entity2.getName());

		return entity1Team != null && entity1Team.equals(entity2Team);
	}

	public Set<LivingEntity> getEnemies(Player player) {
		return getEntities().stream()
			.filter(other -> ! areAllies(player, other))
			.collect(Collectors.toSet());
	}

	public Set<Player> getEnemyPlayers(Player player) {
		return getPlayers().stream()
			.filter(other -> ! areAllies(player, other))
			.collect(Collectors.toSet());
	}

	public void addPlayer(Player player) {
		if (joinOption == GameJoinOption.none) {
			player.sendMessage("Joining is not enabled for this game session.");
			return;
		}
		joinPlayer(player);
	}

	protected abstract void joinPlayer(Player player);
	protected abstract void discardPlayer(Player player);

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (joinOption == GameJoinOption.auto) {
			joinPlayer(event.getPlayer());
		}
		else {
			discardPlayer(event.getPlayer());
		}
	}
}
