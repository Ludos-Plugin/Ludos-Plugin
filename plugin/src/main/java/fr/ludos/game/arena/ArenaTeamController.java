package fr.ludos.game.arena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.ludos.game.GameJoinOption;
import fr.ludos.game.Game;
import fr.ludos.game.GameTeamController;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ArenaTeamController extends GameTeamController {
	private static final int PRIMARY_TEAM_INDEX = 0;
	private static final int SECONDARY_TEAM_INDEX = 1;

	private final Set<Player> selectedPrimaryTeam;
	private final Set<Player> selectedSecondaryTeam;
	private final List<Team> combatTeams = new ArrayList<>();

	private Team spectatorTeam;

	public ArenaTeamController(ArenaGame game, Set<Player> selectedPrimaryPlayers, Set<Player> selectedSecondaryPlayers, GameJoinOption joinOption) {
		super(game, joinOption);

		this.selectedPrimaryTeam = new HashSet<>(selectedPrimaryPlayers);
		this.selectedSecondaryTeam = new HashSet<>(selectedSecondaryPlayers);
	}

	@Override
	protected void onStart() {
		Scoreboard scoreboard = getGame().getScoreboard();

		Team primaryTeam = createOrGetTeam(scoreboard, "ArenaTeam1", NamedTextColor.BLUE, false);
		Team secondaryTeam = createOrGetTeam(scoreboard, "ArenaTeam2", NamedTextColor.RED, false);
		spectatorTeam = createOrGetTeam(scoreboard, "ArenaSpectators", NamedTextColor.GRAY, true);

		combatTeams.add(primaryTeam);
		combatTeams.add(secondaryTeam);

		for (Player player : selectedPrimaryTeam) {
			moveToCombatTeam(player, PRIMARY_TEAM_INDEX);
		}
		for (Player player : selectedSecondaryTeam) {
			moveToCombatTeam(player, SECONDARY_TEAM_INDEX);
		}
	}

	@Override
	protected void onStop() {
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
	private Team getCombatTeam(int index) {
		if (index < 0 || index >= combatTeams.size()) return null;
		return combatTeams.get(index);
	}

	public Set<Player> getCombatPlayers(int index) {
		Team team = getCombatTeam(index);
		if (team == null) return new HashSet<>();
		return extractPlayersFromTeam(team);
	}

	public Set<Player> getAliveCombatPlayers(int index) {
		return getCombatPlayers(index).stream()
			.filter(Player::isOnline)
			.filter(p -> p.getGameMode() == GameMode.SURVIVAL)
			.filter(p -> !p.isDead())
			.collect(Collectors.toSet());
	}

	public void moveToCombatTeam(Player player, int index) {
		moveToTeam(player, getCombatTeam(index));
	}

	public void joinSpectator(Player player) {
		moveToTeam(player, spectatorTeam);
		if (player == null) return;

		Location center = getGame().getGameAreaController().getCenter();
		player.teleport(center);
		player.setGameMode(GameMode.SPECTATOR);
	}

	private void moveToTeam(Player player, Team destination) {
		if (player == null || destination == null) return;

		String name = player.getName();
		for (Team team : combatTeams) {
			team.removeEntry(name);
		}
		spectatorTeam.removeEntry(name);
		destination.addEntry(name);
		player.setScoreboard(getGame().getScoreboard());
	}

	@Override
	public Collection<Team> getTeams() {
		return List.copyOf(combatTeams);
	}

	@Override
	public Collection<LivingEntity> getEntities() {
		Set<LivingEntity> entities = new HashSet<>();
		for (Team team : combatTeams) {
			Team t = team;
			if (t != null) entities.addAll(extractPlayersFromTeam(t));
		}
		return entities;
	}

	@Override
	protected void joinPlayer(Player player) {
		if (player == null) return;

		if (getPlayers().contains(player)) return;

		if (((ArenaGame)getGame()).isWaveMode()) {
			moveToCombatTeam(player, PRIMARY_TEAM_INDEX);
			return;
		}

		if (getCombatPlayers(PRIMARY_TEAM_INDEX).size() <= getCombatPlayers(SECONDARY_TEAM_INDEX).size()) {
			moveToCombatTeam(player, PRIMARY_TEAM_INDEX);
		} else {
			moveToCombatTeam(player, SECONDARY_TEAM_INDEX);
		}
	}

	@Override
	protected void discardPlayer(Player player) {
		joinSpectator(player);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (!getPlayers().contains(player)) return;

		event.setKeepInventory(true);
		event.getDrops().clear();
		event.setDroppedExp(0);

		Bukkit.getScheduler().runTask(getPlugin(), () -> {
			player.setGameMode(GameMode.SPECTATOR);
			player.setHealth(Math.max(1.0, player.getHealth()));
		});
	}
}
