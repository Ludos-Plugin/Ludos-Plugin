package fr.ludos.game.sheepwars;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Sheep;
import org.bukkit.scoreboard.Team;

import fr.ludos.game.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class SheepwarsEliminationListener implements Listener {

	private final SheepwarsGame game;
	private boolean ending = false;

	public SheepwarsEliminationListener(SheepwarsGame game) {
		this.game = game;
	}

	@EventHandler
	public void onFatalDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Sheep && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
			event.setCancelled(true);
			return;
		}

		if (!(entity instanceof Player player)) {
			return;
		}

		if (!WorldManager.ACTIVE_WORLD_NAME.equals(player.getWorld().getName())) {
			return;
		}

		SheepwarsTeamController teamController = (SheepwarsTeamController) game.getGameTeamController();
		if (teamController == null || !teamController.getSelectedPlayers().contains(player)) {
			return;
		}

		double remainingHealth = player.getHealth() - event.getFinalDamage();
		if (remainingHealth > 0.0D || player.getGameMode() == GameMode.SPECTATOR) {
			return;
		}

		event.setCancelled(true);

		double maxHealth = 20.0D;
		if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
			maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		}
		player.setHealth(maxHealth);
		player.setFireTicks(0);
		player.setGameMode(GameMode.SPECTATOR);

		if (player.getLocation().getY() < 0.0D) {
			Location rescue = player.getWorld().getSpawnLocation().clone().add(0, 5, 0);
			player.teleport(rescue);
		}

		Team eliminatedTeam = teamController.getPlayerTeam(player);
		if (eliminatedTeam == null) {
			return;
		}

		Player survivor = findAliveTeammate(teamController, eliminatedTeam, player);
		if (survivor != null) {
			player.setSpectatorTarget(survivor);
		}

		if (!isTeamEliminated(teamController, eliminatedTeam)) {
			return;
		}

		Team winner = findWinnerTeam(teamController, eliminatedTeam);
		if (winner != null) {
			Bukkit.broadcast(
				Component.text("Team ")
					.append(winner.displayName())
					.append(Component.text(" wins SheepWars!"))
					.color(NamedTextColor.GOLD)
			);
		} else {
			Bukkit.broadcast(
				Component.text("All teams have been eliminated. No winner this round.")
					.color(NamedTextColor.RED)
			);
		}

		if (!ending) {
			ending = true;
			Game.stopCurrentGame();
		}
	}

	private Player findAliveTeammate(SheepwarsTeamController teamController, Team team, Player eliminatedPlayer) {
		for (Player teammate : teamController.getTeamPlayers(team)) {
			if (teammate.equals(eliminatedPlayer)) {
				continue;
			}

			if (isAlive(teammate)) {
				return teammate;
			}
		}

		return null;
	}

	private boolean isTeamEliminated(SheepwarsTeamController teamController, Team team) {
		for (String entry : team.getEntries()) {
			Player player = Bukkit.getPlayerExact(entry);
			if (player != null && teamController.getSelectedPlayers().contains(player) && isAlive(player)) {
				return false;
			}
		}

		return true;
	}

	private Team findWinnerTeam(SheepwarsTeamController teamController, Team eliminatedTeam) {
		List<Team> teams = new ArrayList<>(teamController.getTeams());
		for (Team team : teams) {
			if (team.equals(eliminatedTeam)) {
				continue;
			}

			if (!isTeamEliminated(teamController, team)) {
				return team;
			}
		}

		return null;
	}

	private boolean isAlive(Player player) {
		return player.isOnline()
			&& WorldManager.ACTIVE_WORLD_NAME.equals(player.getWorld().getName())
			&& player.getGameMode() != GameMode.SPECTATOR;
	}
}