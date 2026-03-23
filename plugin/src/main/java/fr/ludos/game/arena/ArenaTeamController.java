package fr.ludos.game.arena;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
import fr.ludos.game.GameTeamController;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ArenaTeamController extends GameTeamController {
	private final Set<Player> selectedTeamA;
	private final Set<Player> selectedTeamB;

	private Team teamA;
	private Team teamB;
	private Team spectators;

	public ArenaTeamController(ArenaGame game, Set<Player> selectedTeamA, Set<Player> selectedTeamB, GameJoinOption joinOption) {
		super(game, joinOption);

		this.selectedTeamA = new HashSet<>(selectedTeamA);
		this.selectedTeamB = new HashSet<>(selectedTeamB);
	}

	@Override
	protected void onStart() {
		Scoreboard scoreboard = getGame().getScoreboard();

		teamA = scoreboard.getTeam("ArenaA");
		if (teamA == null) {
			teamA = scoreboard.registerNewTeam("ArenaA");
		}
		teamA.color(NamedTextColor.BLUE);
		teamA.setAllowFriendlyFire(false);

		teamB = scoreboard.getTeam("ArenaB");
		if (teamB == null) {
			teamB = scoreboard.registerNewTeam("ArenaB");
		}
		teamB.color(NamedTextColor.RED);
		teamB.setAllowFriendlyFire(false);

		spectators = scoreboard.getTeam("ArenaSpectators");
		if (spectators == null) {
			spectators = scoreboard.registerNewTeam("ArenaSpectators");
		}
		spectators.color(NamedTextColor.GRAY);
		spectators.setAllowFriendlyFire(true);

		for (Player player : selectedTeamA) {
			joinTeamA(player);
		}
		for (Player player : selectedTeamB) {
			joinTeamB(player);
		}
	}

	@Override
	protected void onStop() {
		if (spectators != null) {
			spectators.unregister();
			spectators = null;
		}

		if (teamA != null) {
			teamA.unregister();
			teamA = null;
		}

		if (teamB != null) {
			teamB.unregister();
			teamB = null;
		}
	}

	public Set<Player> getTeamAPlayers() {
		if (teamA == null) return Set.of();
		return teamA.getEntries().stream()
			.map(Bukkit::getPlayerExact)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	public Set<Player> getTeamBPlayers() {
		if (teamB == null) return Set.of();
		return teamB.getEntries().stream()
			.map(Bukkit::getPlayerExact)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	public Set<Player> getAliveTeamAPlayers() {
		return getTeamAPlayers().stream()
			.filter(Player::isOnline)
			.filter(p -> p.getGameMode() == GameMode.SURVIVAL)
			.filter(p -> !p.isDead())
			.collect(Collectors.toSet());
	}

	public Set<Player> getAliveTeamBPlayers() {
		return getTeamBPlayers().stream()
			.filter(Player::isOnline)
			.filter(p -> p.getGameMode() == GameMode.SURVIVAL)
			.filter(p -> !p.isDead())
			.collect(Collectors.toSet());
	}

	public void joinTeamA(Player player) {
		if (player == null) return;
		if (teamA == null || teamB == null || spectators == null) return;

		teamB.removeEntry(player.getName());
		spectators.removeEntry(player.getName());
		teamA.addEntry(player.getName());
		player.setScoreboard(getGame().getScoreboard());
	}

	public void joinTeamB(Player player) {
		if (player == null) return;
		if (teamA == null || teamB == null || spectators == null) return;

		teamA.removeEntry(player.getName());
		spectators.removeEntry(player.getName());
		teamB.addEntry(player.getName());
		player.setScoreboard(getGame().getScoreboard());
	}

	public void joinSpectator(Player player) {
		if (player == null) return;
		if (teamA == null || teamB == null || spectators == null) return;

		teamA.removeEntry(player.getName());
		teamB.removeEntry(player.getName());
		spectators.addEntry(player.getName());

		Location center = getGame().getGameAreaController().getCenter();
		player.teleport(center);
		player.setGameMode(GameMode.SPECTATOR);
		player.setScoreboard(getGame().getScoreboard());
	}

	public void resetRoundPlayers() {
		for (Player player : getTeamAPlayers()) {
			prepareCombatState(player);
		}
		for (Player player : getTeamBPlayers()) {
			prepareCombatState(player);
		}
	}

	private void prepareCombatState(Player player) {
		player.setGameMode(GameMode.SURVIVAL);
		player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		player.setFoodLevel(20);
		player.setSaturation(20f);
		player.setFireTicks(0);
		player.setFallDistance(0f);
		player.setVelocity(player.getVelocity().zero());
		player.setAllowFlight(false);
		player.setFlying(false);
	}

	@Override
	public Collection<Team> getTeams() {
		return List.of(teamA, teamB);
	}

	@Override
	public Collection<LivingEntity> getEntities() {
		Set<LivingEntity> entities = new HashSet<>();
		entities.addAll(getTeamAPlayers());
		entities.addAll(getTeamBPlayers());
		return entities;
	}

	@Override
	protected void joinPlayer(Player player) {
		if (teamA == null || teamB == null) return;

		if (teamA.hasEntry(player.getName()) || teamB.hasEntry(player.getName())) return;

		if (((ArenaGame)getGame()).isWaveMode()) {
			joinTeamA(player);
			return;
		}

		if (teamA.getEntries().size() <= teamB.getEntries().size()) {
			joinTeamA(player);
		}
		else {
			joinTeamB(player);
		}
	}

	@Override
	protected void discardPlayer(Player player) {
		joinSpectator(player);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (!teamA.hasEntry(player.getName()) && !teamB.hasEntry(player.getName())) return;

		event.setKeepInventory(true);
		event.getDrops().clear();
		event.setDroppedExp(0);

		Bukkit.getScheduler().runTask(getPlugin(), () -> {
			player.setGameMode(GameMode.SPECTATOR);
			player.setHealth(Math.max(1.0, player.getHealth()));
		});
	}
}
