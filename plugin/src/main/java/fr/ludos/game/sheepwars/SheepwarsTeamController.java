package fr.ludos.game.sheepwars;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Team;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import fr.ludos.game.Game;
import fr.ludos.game.TeamController;


public final class SheepwarsTeamController extends TeamController {
	private List<Team> teams;
	private Set<Player> selectedPlayers;

	public Set<Player> getSelectedPlayers() {
		return selectedPlayers;
	}

	public SheepwarsTeamController(SheepwarsGame game, @Nullable Set<Player> players, int teamCount) {
		super(game, game.getScoreboard());
		
		this.teams = new ArrayList<>();
		
		Set<Player> finalPlayers = new HashSet<>();
		if (players == null) {
			finalPlayers.addAll(Bukkit.getOnlinePlayers());
		} else {
			finalPlayers.addAll(players);
		}

		if (finalPlayers.isEmpty()) {
			throw new IllegalArgumentException("No players available for Sheepwars game");
		}

		if (finalPlayers.size() > 8) {
			throw new IllegalArgumentException("Sheepwars supports a maximum of 8 players (2 teams of 4)");
		}

		this.selectedPlayers = finalPlayers;
	}

	@Override
	protected void onStart() {
		Team redTeam = scoreboard.registerNewTeam("team1");
		redTeam.displayName(Component.text("Red").color(NamedTextColor.RED));
		redTeam.color(NamedTextColor.RED);
		redTeam.setAllowFriendlyFire(false);
		teams.add(redTeam);

		Team blueTeam = scoreboard.registerNewTeam("team2");
		blueTeam.displayName(Component.text("Blue").color(NamedTextColor.BLUE));
		blueTeam.color(NamedTextColor.BLUE);
		blueTeam.setAllowFriendlyFire(false);
		teams.add(blueTeam);

		List<Player> playerList = new ArrayList<>(selectedPlayers);
		
		for (int i = 0; i < playerList.size(); i++) {
			Player player = playerList.get(i);
			Team team = teams.get(i % 2);
			team.addEntry(player.getName());
			
			player.sendMessage(
				Component.text("You have been assigned to team: ")
					.append(team.displayName())
			);
		}

		// Only two teams are used in SheepWars: Red and Blue.
		// The previous Green/Yellow team slots are intentionally not created.
	}

	@Override
	protected void onStop() {
		for (Team team : teams) {
			team.unregister();
		}
		teams.clear();
	}

	@Override
	public Collection<Team> getTeams() {
		return teams;
	}

	@Override
	public Collection<Player> getPlayers() {
		return selectedPlayers;
	}

	public Team getPlayerTeam(Player player) {
		return scoreboard.getEntryTeam(player.getName());
	}

	public List<Player> getTeamPlayers(Team team) {
		return team.getEntries().stream()
			.map(Bukkit::getPlayerExact)
			.filter(player -> player != null)
			.toList();
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		Team team = getPlayerTeam(player);
		
		if (team != null) {
			Bukkit.broadcast(
				Component.text(player.getName() + " from team ")
					.append(team.displayName())
					.append(Component.text(" has been eliminated!"))
					.color(NamedTextColor.GOLD)
			);
		}
	}

	@Override
	public Collection<LivingEntity> getEntities() {
		return new ArrayList<>(selectedPlayers);
	}
}