package fr.ludos.game.sheepwars;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import fr.ludos.game.GameTeamController;


public final class SheepwarsTeamController extends GameTeamController {
	private List<Team> teams;
	private Set<Player> selectedPlayers;
	private int teamCount;

	public Set<Player> getSelectedPlayers() {
		return selectedPlayers;
	}

	public SheepwarsTeamController(SheepwarsGame game, @Nullable Set<Player> players, int teamCount) {
		super(game);
		
		this.teamCount = teamCount;
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

		this.selectedPlayers = finalPlayers;
	}

	@Override
	protected void onStart() {
		Scoreboard scoreboard = getGame().getScoreboard();

		NamedTextColor[] teamColors = {
			NamedTextColor.RED,
			NamedTextColor.BLUE, 
			NamedTextColor.GREEN,
			NamedTextColor.YELLOW
		};

		String[] teamNames = {"Red", "Blue", "Green", "Yellow"};

		for (int i = 0; i < teamCount; i++) {
			Team team = scoreboard.registerNewTeam("team" + (i + 1));
			team.displayName(Component.text(teamNames[i]).color(teamColors[i]));
			team.color(teamColors[i]);
			team.setAllowFriendlyFire(false);
			teams.add(team);
		}

		List<Player> playerList = new ArrayList<>(selectedPlayers);
		Random random = new Random();
		
		for (int i = 0; i < playerList.size(); i++) {
			Player player = playerList.get(i);
			Team team = teams.get(i % teamCount);
			team.addEntry(player.getName());
			
			player.sendMessage(
				Component.text("You have been assigned to team: ")
					.append(team.displayName())
			);
		}
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
	public Collection<LivingEntity> getEntities() {
		return new ArrayList<>(selectedPlayers);
	}

	public Team getPlayerTeam(Player player) {
		return getGame().getScoreboard().getEntryTeam(player.getName());
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
	protected void joinPlayer(Player player) {
		selectedPlayers.add(player);
	}

	@Override
	protected void discardPlayer(Player player) {
		selectedPlayers.remove(player);
	}
}