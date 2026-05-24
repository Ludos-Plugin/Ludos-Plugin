package fr.ludos.game.arena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

import fr.ludos.Utility;
import fr.ludos.area.Area;
import fr.ludos.game.teamController.GameTeamController;
import fr.ludos.item.SpecialItem;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ArenaTeamController extends GameTeamController {
	private static final int PRIMARY_TEAM_INDEX = 0;
	private static final int SECONDARY_TEAM_INDEX = 1;

	private final Set<Player> selectedPrimary;
	private final Set<Player> selectedSecondary;
	private final List<Team> combatTeams = new ArrayList<>();

	private Team spectatorTeam;


	@Nullable
	private static Player pickPlayer(Set<Player> candidates, @Nullable Player excluded) {
		List<Player> filtered = candidates.stream()
			.filter(Player::isOnline)
			.filter(p -> excluded == null || !p.equals(excluded))
			.collect(Collectors.toList());
		if (filtered.isEmpty()) return null;
		return filtered.get(new java.util.Random().nextInt(filtered.size()));
	}

	public ArenaTeamController(ArenaGame game, ArenaModeOption mode, @Nullable Set<OfflinePlayer> selectedPrimaryPlayers, @Nullable Set<OfflinePlayer> selectedSecondaryPlayers) {
		super(game);

		Set<Player> online = new HashSet<>(game.getGroup().getOnlinePlayers());
		if (online.size() < 2) {
			throw new IllegalArgumentException("At least 2 online players are required for Arena : " + online.size());
		}

		Set<Player> configuredPrimary;
		Set<Player> configuredSecondary;
		if (selectedPrimaryPlayers == null && selectedSecondaryPlayers == null) {
			List<? extends Collection<Player>> split = Utility.split(online, 2);
			configuredPrimary = split.get(0).stream().collect(Collectors.toSet());
			configuredSecondary = split.get(1).stream().collect(Collectors.toSet());
		}
		else {
			configuredPrimary = Utility.getOnline(selectedPrimaryPlayers).collect(Collectors.toCollection(HashSet::new));
			configuredSecondary = Utility.getOnline(selectedSecondaryPlayers).collect(Collectors.toCollection(HashSet::new));

			if (selectedPrimaryPlayers == null) {
				configuredPrimary = new HashSet<>(online);
				configuredPrimary.removeAll(configuredSecondary);
			} else {
				configuredSecondary = new HashSet<>(online);
				configuredSecondary.removeAll(configuredPrimary);
			}

			configuredSecondary.removeAll(configuredPrimary);
		}

		if (configuredPrimary.isEmpty() || configuredSecondary.isEmpty()) {
			throw new IllegalStateException("Cannot start Arena game with only one team");
		}

		if (mode == ArenaModeOption.duel) {
			Player p1 = pickPlayer(configuredPrimary, null);
			Player p2 = pickPlayer(configuredSecondary, p1);
			if (p1 == null || p2 == null) throw new IllegalArgumentException("Could not resolve duel players");

			this.selectedPrimary = Set.of(p1);
			this.selectedSecondary = Set.of(p2);
		} else {
			this.selectedPrimary = configuredPrimary;
			this.selectedSecondary = configuredSecondary;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Scoreboard scoreboard = getGame().getScoreboard();

		Team primaryTeam = createOrGetTeam(scoreboard, "ArenaTeam1", NamedTextColor.BLUE, false);
		Team secondaryTeam = createOrGetTeam(scoreboard, "ArenaTeam2", NamedTextColor.RED, false);
		spectatorTeam = createOrGetTeam(scoreboard, "ArenaSpectators", NamedTextColor.GRAY, true);

		combatTeams.add(primaryTeam);
		combatTeams.add(secondaryTeam);

		for (Player player : getGame().getGroup().getOnlinePlayers()) {
			if (selectedPrimary.contains(player)) {
				moveToTeam(player, primaryTeam);
			} else if (selectedSecondary.contains(player)) {
				moveToTeam(player, secondaryTeam);
			} else {
				moveToTeam(player, spectatorTeam);
			}
		}

		// Area area = game.getWorldManager().getArea();
		// Location center = area != null
		// 	? area.getCenter()
		// 	: game.getWorldManager().getWorld().getSpawnLocation();

		// Location primarySpawn = area != null
		// 	? area.pickRandom(0.30, 0.35)
		// 	: game.getWorldManager().getWorld().getSpawnLocation()
		// 		.add(40, 0, 20);
		// Location secondarySpawn = center.clone().subtract(primarySpawn.clone().subtract(center));
		// Utility.snapToHighestY(primarySpawn);
		// Utility.snapToHighestY(secondarySpawn);

		// Vector primaryLookDirection = secondarySpawn.toVector().subtract(primarySpawn.toVector()).normalize();
		// primarySpawn.setDirection(primaryLookDirection);

		// Vector secondaryLookDirection = primarySpawn.toVector().subtract(secondarySpawn.toVector()).normalize();
		// secondarySpawn.setDirection(secondaryLookDirection);

		// ArenaTeamController teamController = game.getTeamController();

		// PotionEffect glowEffect = new PotionEffect(PotionEffectType.GLOWING, 10 * 20, 0, true, false);
		// for (Player player : Utility.getTeamAlivePlayers(teamController.getCombatTeam(0)).toList()) {
		// 	player.teleport(primarySpawn);
		// 	player.addPotionEffect(glowEffect);
		// }
		// for (Player player : Utility.getTeamAlivePlayers(teamController.getCombatTeam(1)).toList()) {
		// 	player.teleport(secondarySpawn);
		// 	player.addPotionEffect(glowEffect);
		// }
		// for (Player player : Utility.getTeamOnlinePlayers(teamController.getSpectatorTeam()).toList()) {
		// 	player.teleport(center);
		// }
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

	// public void moveToCombatTeam(OfflinePlayer player, int index) {
	// 	moveToTeam(player, getCombatTeam(index));
	// }

	public void joinSpectator(OfflinePlayer player) {
		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		moveToTeam(onlinePlayer, spectatorTeam);

		Area area = getGame().getWorldManager().getArea();
		Location center = area != null
			? area.getCenter()
			: getGame().getWorldManager().getWorld().getSpawnLocation();
		onlinePlayer.teleport(center);
		onlinePlayer.setGameMode(GameMode.SPECTATOR);
	}

	private void moveToTeam(OfflinePlayer player, Team destination) {
		if (player == null || destination == null) return;

		Location teammateLocation = getLocationAroundTeammate(
			destination,
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

		String name = player.getName();
		for (Team team : combatTeams) {
			team.removeEntry(name);
		}
		spectatorTeam.removeEntry(name);

		destination.addEntry(name);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		onlinePlayer.teleport(teammateLocation, true);

		onlinePlayer.setScoreboard(getGame().getScoreboard());
	}

	public void joinAnyPlayer(Player player, @Nullable Location location) {
		player.setScoreboard(getGame().getScoreboard());

		Utility.resetPlayer(player);
		player.setGameMode(GameMode.SURVIVAL);

		player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 3, 0, true, false, false));

		if (location != null) {
			player.teleport(location);
			player.setBedSpawnLocation(location, true);
		}
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
			moveToTeam(player, primaryTeam);
		} else if (primarySize > secondarySize) {
			moveToTeam(player, secondaryTeam);
		} else {
			moveToTeam(player, getGame().random.nextFloat() < 0.5 ? primaryTeam : secondaryTeam);
		}
	}

	@Override
	protected void discardPlayer(OfflinePlayer player) {
		joinSpectator(player);
	}

	@Override
	public void removePlayer(OfflinePlayer player) {
		combatTeams.forEach(team -> team.removeEntry(player.getName()));
		spectatorTeam.removeEntry(player.getName());

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer != null) {
			SpecialItem.Events.removeFromPlayerInventory(getGame(), onlinePlayer);
			onlinePlayer.teleport(getGame().getWorldManager().getReturnLocation());
		}
	}


	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (getGame().getWorldManager().isLobbyStarted()) return;

		Player player = event.getEntity();
		if (!getPlayers().contains(player)) return;

		Utility.onDeathSpectate(event, getPlugin());
	}
}
