package fr.ludos.game.sheepwars;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import fr.ludos.command.ludos.GameSubcommand;
import fr.ludos.game.Game;
import fr.ludos.game.GameJoinOption;
import fr.ludos.game.GameTeamController;


public final class SheepwarsTeamController extends GameTeamController {
	private List<Team> teams;
	public Team spectatorTeam;
	private Set<Player> selectedPlayers;

	public Set<Player> getSelectedPlayers() {
		return selectedPlayers;
	}

	public SheepwarsTeamController(SheepwarsGame game, @Nullable Set<Player> players, int teamCount) {
		super(game, GameJoinOption.none);

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
		Scoreboard scoreboard = getGame().getScoreboard();
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

		spectatorTeam = scoreboard.getTeam("Spectators");
		if (spectatorTeam == null) {
			spectatorTeam = scoreboard.registerNewTeam("Spectators");
			spectatorTeam.color(NamedTextColor.GRAY);
			spectatorTeam.setAllowFriendlyFire(false);
		}

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
	public Collection<LivingEntity> getEntities() {
		return new ArrayList<>(selectedPlayers);
	}

	@Override
	protected void joinPlayer(Player player) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'joinPlayer'");
	}

	public void joinSpectator(Player player) {
		// if (hunterTeam.hasPlayer(player) || preyTeam.hasPlayer(player)) return;
		for (Team team : teams) {
			if (team.hasPlayer(player)) return;
		}

		Location gameLocation = getGame().getGameAreaController().pickRandom(0.0, 1.0);

		player.teleport(gameLocation);
		player.setBedSpawnLocation(gameLocation, true);
		player.setGameMode(GameMode.SPECTATOR);

		spectatorTeam.addEntry(player.getName());
		player.setScoreboard(getGame().getScoreboard());

		// Player prey = getTeamPrey();
		// if (prey != null) {
		// 	player.teleport(prey.getLocation());
		// }
		// TODO: Teleport to random player

		player.showTitle(Title.title(
			Component.text("You are a ")
			.append(Component.text("Spectator")
				.color(NamedTextColor.GRAY)),
			// Component.text("Run '" + GameSubcommand.join.getUsage(player, null, "ludos") + "' to join"),
			Component.text().build(),
			Title.Times.times(
				Duration.ofMillis(500),
				Duration.ofMillis(3500),
				Duration.ofMillis(1000)
			)
		));
	}

	@Override
	protected void discardPlayer(Player player) {
		joinSpectator(player);
	}
}