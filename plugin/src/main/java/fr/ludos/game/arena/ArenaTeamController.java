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
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (spectatorTeam != null) {
			spectatorTeam.unregister();
		}
		for (Team team : combatTeams) {
			if (team != null) {
				team.unregister();
			}
		}
		combatTeams.clear();
		spectatorTeam = null;
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

		String name = player.getName();
		for (Team team : combatTeams) {
			team.removeEntry(name);
		}
		spectatorTeam.removeEntry(name);
		destination.addEntry(name);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

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

		if (primaryTeamPlayers.size() <= secondaryTeamPlayers.size()) {
			moveToTeam(player, primaryTeam);
		} else {
			moveToTeam(player, secondaryTeam);
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
