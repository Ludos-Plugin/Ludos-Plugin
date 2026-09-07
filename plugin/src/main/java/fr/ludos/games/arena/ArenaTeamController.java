package fr.ludos.games.arena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.ludos.core.Utility;
import fr.ludos.core.game.teamController.GameTeamController;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Controller for {@link ArenaGame} teams, handling team selection and setup.
 */
public final class ArenaTeamController extends GameTeamController {
	private static final int PRIMARY_TEAM_INDEX = 0;
	private static final int SECONDARY_TEAM_INDEX = 1;

	private final ArenaModeOption mode;

	private final Set<OfflinePlayer> selectedPrimary;
	private final Set<OfflinePlayer> selectedSecondary;
	private final List<Team> combatTeams = new ArrayList<>();

	private Team primaryTeam;
	private Team secondaryTeam;
	private Team spectatorTeam;


	@Nullable
	private static Player pickPlayer(Set<Player> candidates, @Nullable Player excluded) {
		List<Player> filtered = candidates.stream()
			.filter(Player::isOnline)
			.filter(p -> excluded == null || !p.equals(excluded))
			.collect(Collectors.toList());
		if (filtered.isEmpty()) return null;
		return filtered.get(new Random().nextInt(filtered.size()));
	}

	public ArenaTeamController(ArenaGame game, ArenaModeOption mode, @Nullable Set<OfflinePlayer> primaryPlayers, @Nullable Set<OfflinePlayer> secondaryPlayers) {
		super(game);
		this.mode = mode;
		this.selectedPrimary = primaryPlayers;
		this.selectedSecondary = secondaryPlayers;
	}

	@Override
	protected void onStart() {
		super.onStart();

		Scoreboard scoreboard = game().scoreboard();

		primaryTeam = createOrGetTeam(scoreboard, "ArenaTeam1", NamedTextColor.BLUE, false);
		secondaryTeam = createOrGetTeam(scoreboard, "ArenaTeam2", NamedTextColor.RED, false);
		spectatorTeam = createOrGetTeam(scoreboard, "ArenaSpectators", NamedTextColor.GRAY, true);

		combatTeams.add(primaryTeam);
		combatTeams.add(secondaryTeam);

		Set<Player> online = new HashSet<>(game().group().getOnlinePlayers());
		if (online.size() < 2) {
			throw new IllegalArgumentException("At least 2 online players are required for Arena : " + online.size());
		}

		Set<Player> finalPrimary;
		Set<Player> finalSecondary;
		if ((selectedPrimary == null || selectedPrimary.isEmpty()) && (selectedSecondary == null || selectedSecondary.isEmpty())) {
			List<? extends Collection<Player>> split = Utility.split(online, 2);
			finalPrimary = split.get(0).stream().collect(Collectors.toSet());
			finalSecondary = split.get(1).stream().collect(Collectors.toSet());
		}
		else {
			finalPrimary = Utility.getOnline(selectedPrimary).collect(Collectors.toCollection(HashSet::new));
			finalSecondary = Utility.getOnline(selectedSecondary).collect(Collectors.toCollection(HashSet::new));

			if (selectedPrimary == null || selectedPrimary.isEmpty()) {
				finalPrimary = new HashSet<>(online);
				finalPrimary.removeAll(finalSecondary);
			} else {
				finalSecondary = new HashSet<>(online);
				finalSecondary.removeAll(finalPrimary);
			}

			finalSecondary.removeAll(finalPrimary);
		}

		if (finalPrimary.isEmpty() || finalSecondary.isEmpty()) {
			throw new IllegalStateException("Cannot start Arena game with only one team");
		}

		if (mode == ArenaModeOption.duel) {
			Player p1 = pickPlayer(finalPrimary, null);
			Player p2 = pickPlayer(finalSecondary, p1);
			if (p1 == null || p2 == null) throw new IllegalArgumentException("Could not resolve duel players");

			finalPrimary = Set.of(p1);
			finalSecondary = Set.of(p2);
		}

		for (Player player : game().group().getOnlinePlayers()) {
			if (finalPrimary.contains(player)) {
				joinActivePlayer(player, primaryTeam);
			} else if (finalSecondary.contains(player)) {
				joinActivePlayer(player, secondaryTeam);
			} else {
				joinActivePlayer(player, spectatorTeam);
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (spectatorTeam != null) {
			spectatorTeam.unregister();
		}
		spectatorTeam = null;

		for (Team team : combatTeams) {
			if (team != null) {
				team.unregister();
			}
		}

		combatTeams.clear();
	}

	@Override
	public Collection<Team> getTeams() {
		HashSet<Team> teams = new HashSet<>(combatTeams);
		teams.add(spectatorTeam);

		return teams;
	}

	private Team createOrGetTeam(Scoreboard scoreboard, String name, NamedTextColor color, boolean friendlyFire) {
		Team team = scoreboard.getTeam(name);
		if (team == null) {
			team = scoreboard.registerNewTeam(name);
		}
		team.color(color);
		team.setAllowFriendlyFire(friendlyFire);
		return team;
	}


	public int getCombatTeamCount() {
		return combatTeams.size();
	}

	@Nullable
	public Team getCombatTeam(int index) {
		if (index < 0 || index >= combatTeams.size()) return null;
		return combatTeams.get(index);
	}

	public Team getSpectatorTeam() {
		return this.spectatorTeam;
	}

	@Override
	protected void joinPlayer(OfflinePlayer player) {
		if (player == null) return;

		if (getPlayers().contains(player)) return;

		Team primaryTeam = getCombatTeam(PRIMARY_TEAM_INDEX);
		Team secondaryTeam = getCombatTeam(SECONDARY_TEAM_INDEX);
		Set<Player> primaryTeamPlayers = getTeamOnlinePlayers(primaryTeam);
		Set<Player> secondaryTeamPlayers = getTeamOnlinePlayers(secondaryTeam);

		int primarySize = primaryTeamPlayers.size();
		int secondarySize = secondaryTeamPlayers.size();

		if (primarySize < secondarySize) {
			joinActivePlayer(player, primaryTeam);
		} else if (primarySize > secondarySize) {
			joinActivePlayer(player, secondaryTeam);
		} else {
			joinActivePlayer(player, game().random().nextFloat() < 0.5 ? primaryTeam : secondaryTeam);
		}
	}

	private void joinActivePlayer(OfflinePlayer player, Team destination) {
		if (player == null || destination == null) return;

		for (Team team : combatTeams) {
			team.removePlayer(player);
		}
		spectatorTeam.removePlayer(player);

		destination.addPlayer(player);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		onlinePlayer.setScoreboard(game().scoreboard());

		onlinePlayer.setGameMode(GameMode.SURVIVAL);

		Utility.resetPlayer(onlinePlayer);
		onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 3, 0, true, false, false));

		placeActivePlayer(onlinePlayer, destination);
	}

	public void joinSpectator(OfflinePlayer player) {
		if (player == null) return;

		for (Team team : combatTeams) {
			team.removePlayer(player);
		}
		spectatorTeam.addPlayer(player);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		onlinePlayer.setScoreboard(game().scoreboard());

		onlinePlayer.setGameMode(GameMode.SPECTATOR);

		placeSpectator(onlinePlayer);
	}

	@Override
	protected void discardPlayer(OfflinePlayer player) {
		combatTeams.forEach(team -> team.removePlayer(player));
		joinSpectator(player);
	}

	@Override
	public void removePlayer(OfflinePlayer player) {
		combatTeams.forEach(team -> team.removePlayer(player));
		spectatorTeam.removePlayer(player);
	}


	@Override
	public void placePlayer(OfflinePlayer player) {
		if (primaryTeam.hasPlayer(player)) {
			placeActivePlayer(player, primaryTeam);
		}
		else if (secondaryTeam.hasPlayer(player)) {
			placeActivePlayer(player, secondaryTeam);
		}
		else {
			placeSpectator(player);
		}
	}

	public void placeActivePlayer(OfflinePlayer player, Team destination) {
		if (player == null) return;

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		Location teammateLocation = getLocationAroundTeammate(
			onlinePlayer, destination,
			(area) -> {
				if (destination == spectatorTeam) return area.getCenter();

				int size = combatTeams.size();
				int index = combatTeams.indexOf(destination);
				if (size == 0 || index == -1) return area.getCenter();

				Team enemyTeam = getCombatTeam(index % size);
				Player enemyPlayer = pickPlayer(getTeamAlivePlayers(enemyTeam), null);
				if (enemyPlayer == null) return area.getCenter();

				return area.constrain(
					Utility.getLocationAround(enemyPlayer.getLocation(), index, index, area.getCenter())
				);
			}
		);
		teammateLocation = Utility.snapToHighestY(teammateLocation, true);
		teammateLocation.setYaw(onlinePlayer.getLocation().getYaw());
		teammateLocation.setPitch(onlinePlayer.getLocation().getPitch());

		onlinePlayer.teleport(teammateLocation, true);
		onlinePlayer.setBedSpawnLocation(teammateLocation, true);
	}

	public void placeSpectator(OfflinePlayer player) {
		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		Location center = game().worldManager().getArea() != null
			? game().worldManager().getArea().getCenter()
			: game().worldManager().getWorld().getSpawnLocation();
		center = Utility.snapToHighestY(center, true);
		onlinePlayer.teleport(center, true);
	}


	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (game().worldManager().isLobbyStarted()) return;

		Player player = event.getEntity();
		if (!getPlayers().contains(player)) return;

		Utility.onDeathSpectate(event, plugin());
	}
}
