package fr.ludos.game.sheepwars;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkit.GameRule;

import fr.ludos.Ludos;

public final class GameManager {

	private static final List<String> MAP_TEMPLATES = List.of(
		"sheep_wars_with_water",
		"sheep_wars_without_water"
	);

	private final Ludos plugin;
	private final WorldManager worldManager;

	public GameManager(Ludos plugin) {
		this.plugin = plugin;
		this.worldManager = new WorldManager(plugin);
	}

	public void startSheepWars(List<Team> teams, List<Player> players) {
		String selectedTemplate = MAP_TEMPLATES.get(ThreadLocalRandom.current().nextInt(MAP_TEMPLATES.size()));
		worldManager.loadGameWorld(selectedTemplate, () -> {
			World arenaWorld = worldManager.getActiveWorld();
			if (arenaWorld == null) {
				plugin.getLogger().warning("SheepWars world finished loading but could not be resolved.");
				return;
			}

			arenaWorld.setGameRule(GameRule.DO_TILE_DROPS, false);

			Location teamOneSpawn = new Location(arenaWorld, 227, 46, 79);
			Location teamTwoSpawn = new Location(arenaWorld, 216, 48, 120);

			List<Location> teamSpawns = Arrays.asList(teamOneSpawn, teamTwoSpawn);
			List<Team> orderedTeams = new ArrayList<>(teams);
			for (int i = 0; i < orderedTeams.size() && i < teamSpawns.size(); i++) {
				Team team = orderedTeams.get(i);
				Location spawn = teamSpawns.get(i);
				for (Player player : players) {
					if (team.hasEntry(player.getName())) {
						player.teleport(spawn);
					}
				}
			}
		});
	}

	public void stopSheepWars() {
		Location lobbySpawn = Bukkit.getWorlds().stream()
			.map(World::getSpawnLocation)
			.findFirst()
			.orElse(null);

		worldManager.destroyWorld(lobbySpawn);
	}
}