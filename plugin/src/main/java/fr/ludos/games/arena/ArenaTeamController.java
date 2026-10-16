package fr.ludos.games.arena;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Triple;
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
import fr.ludos.core.area.Area;
import fr.ludos.core.game.teamController.GameTeamController;
import fr.ludos.core.game.teamController.TeamControllerUtility;
import fr.ludos.core.world.WorldManager;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Controller for {@link ArenaGame} teams, handling team selection and setup.
 */
public final class ArenaTeamController extends GameTeamController {
	private final ArenaModeOption mode;

	private final Set<OfflinePlayer> selectedPrimary;
	private final Set<OfflinePlayer> selectedSecondary;

	private Team primaryTeam;
	private Team secondaryTeam;
	private Team spectatorTeam;


	@Nullable
	private static Player pickPlayer(Set<Player> candidates, @Nullable Player excluded) {
		List<Player> filtered = candidates.stream()
			.filter(p -> p != null && p.isOnline())
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


		Set<Player> online = new HashSet<>(game().group().getOnlinePlayers());
		if (online.size() < 2) {
			throw new IllegalArgumentException("At least 2 online players are required for Arena : " + online.size());
		}

		Triple<Set<Player>, Set<Player>, Set<Player>> finalPlayers = TeamControllerUtility.splitPlayersInTwoTeams(game().group(), selectedPrimary, selectedSecondary);
		Set<Player> primaryPlayers = finalPlayers.getLeft();
		Set<Player> secondaryPlayers = finalPlayers.getMiddle();
		Set<Player> spectatorPlayers = finalPlayers.getRight();

		if (mode == ArenaModeOption.duel) {
			Player p1 = pickPlayer(primaryPlayers, null);
			Player p2 = pickPlayer(secondaryPlayers, p1);
			if (p1 == null || p2 == null) throw new IllegalArgumentException("Could not resolve duel players");

			primaryPlayers = Set.of(p1);
			secondaryPlayers = Set.of(p2);

			spectatorPlayers = new HashSet<>(game().group().getOnlinePlayers());
			spectatorPlayers.remove(p1);
			spectatorPlayers.remove(p2);
		}

		for (Player primaryPlayer : primaryPlayers) {
			joinActivePlayer(primaryPlayer, primaryTeam);
		}
		for (Player secondaryPlayer : secondaryPlayers) {
			joinActivePlayer(secondaryPlayer, secondaryTeam);
		}
		for (Player spectatorPlayer : spectatorPlayers) {
			joinActivePlayer(spectatorPlayer, spectatorTeam);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (primaryTeam != null) {
			primaryTeam.unregister();
		}
		primaryTeam = null;

		if (secondaryTeam != null) {
			secondaryTeam.unregister();
		}
		secondaryTeam = null;

		if (spectatorTeam != null) {
			spectatorTeam.unregister();
		}
		spectatorTeam = null;
	}

	@Override
	public Collection<Team> getTeams() {
		return Set.of(primaryTeam, secondaryTeam, spectatorTeam);
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

	public Team getPrimaryTeam() {
		return this.primaryTeam;
	}
	public Team getSecondaryTeam() {
		return this.secondaryTeam;
	}
	public Team getSpectatorTeam() {
		return this.spectatorTeam;
	}

	@Override
	protected void joinPlayer(OfflinePlayer player) {
		if (player == null) return;

		if (getPlayers().contains(player)) return;

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

		primaryTeam.removePlayer(player);
		secondaryTeam.removePlayer(player);
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

		primaryTeam.removePlayer(player);
		secondaryTeam.removePlayer(player);
		spectatorTeam.addPlayer(player);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		onlinePlayer.setScoreboard(game().scoreboard());

		onlinePlayer.setGameMode(GameMode.SPECTATOR);

		placeSpectator(onlinePlayer);
	}

	@Override
	protected void discardPlayer(OfflinePlayer player) {
		primaryTeam.removePlayer(player);
		secondaryTeam.removePlayer(player);
		joinSpectator(player);
	}

	@Override
	public void removePlayer(OfflinePlayer player) {
		primaryTeam.removePlayer(player);
		secondaryTeam.removePlayer(player);
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
				Team enemyTeam = destination == primaryTeam ? secondaryTeam : secondaryTeam;
				Player enemyPlayer = pickPlayer(getTeamAlivePlayers(enemyTeam), null);
				if (enemyPlayer == null) return area.getCenter();

				return area.constrain(
					Utility.getLocationAround(enemyPlayer.getLocation(), 30, 100, area.getCenter())
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

		WorldManager worldManager = game().worldManager();
		Area area = worldManager.getArea();

		Location center = area != null
			? area.getCenter()
			: worldManager.getWorld().getSpawnLocation();
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
