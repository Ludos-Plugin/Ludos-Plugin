package fr.ludos.game.arena;

import java.time.Duration;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.ludos.Utility;
import fr.ludos.game.areaController.GameAreaController;
import fr.ludos.game.waves.DefaultWaveLoadout;
import fr.ludos.game.waves.WaveController;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public final class ArenaWaveController extends WaveController {
	private final ArenaGame game;

	private int primaryWins = 0;
	private int secondaryWins = 0;

	protected ArenaWaveController(ArenaGame game, int maxRounds) {
		super(game, maxRounds, new DefaultWaveLoadout(game));
		this.game = game;
	}

	@Override
	protected void onStop() {
		Component result;
		if (primaryWins > secondaryWins) {
			result = Component.text("Arena finished: Team 1 wins " + primaryWins + " - " + secondaryWins).color(NamedTextColor.BLUE);
		} else if (secondaryWins > primaryWins) {
			result = Component.text("Arena finished: Team 2 wins " + secondaryWins + " - " + primaryWins).color(NamedTextColor.RED);
		} else {
			result = Component.text("Arena finished: Draw " + primaryWins + " - " + secondaryWins).color(NamedTextColor.WHITE);
		}


		for (Player player : getGame().getTeamController().getOnlinePlayers()) {
			player.showTitle(Title.title(
				Component.text("Match Over").color(NamedTextColor.GOLD),
				result,
				Title.Times.times(Duration.ZERO, Duration.ofSeconds(5), Duration.ofSeconds(2))
			));
		}

		super.onStop();
		getGame().stop();
	}

	@Override
	protected void nextWave() {
		for (Player player : getGame().getTeamController().getOnlinePlayers()) {
			Utility.resetPlayer(player);
			player.setGameMode(GameMode.SURVIVAL);
		}

		game.getWorldController().getLobbyController().start();
	}

	@Override
	public void startWave() {
		Bukkit.broadcast(Component.text("Round " + getCurrentWaveNumber() + " starts!").color(NamedTextColor.GREEN));

		for (Player player : getGame().getTeamController().getOnlinePlayers()) {
			Utility.resetPlayer(player);
			player.setGameMode(GameMode.SURVIVAL);

			applyLoadout(player);
		}

		teleportTeamsToRoundSpawns();
	}

	@Override
	protected void evaluateWaveState() {
		ArenaTeamController teamController = game.getTeamController();

		long alivePrimary = Utility.getTeamAlivePlayers(teamController.getCombatTeam(0)).count();
		long aliveSecondary = Utility.getTeamAlivePlayers(teamController.getCombatTeam(1)).count();
		if (alivePrimary > 0 && aliveSecondary > 0) return;

		int currentRound = getCurrentWaveNumber();

		if (alivePrimary > aliveSecondary) {
			primaryWins++;
			Bukkit.broadcast(Component.text("Round " + currentRound + " won by Team 1").color(NamedTextColor.BLUE));
		} else if (aliveSecondary > alivePrimary) {
			secondaryWins++;
			Bukkit.broadcast(Component.text("Round " + currentRound + " won by Team 2").color(NamedTextColor.RED));
		} else {
			Bukkit.broadcast(Component.text("Round " + currentRound + " is a draw").color(NamedTextColor.WHITE));
		}

		scheduleNextWave();
	}

	private void teleportTeamsToRoundSpawns() {
		GameAreaController areaController = game.getWorldController().getAreaController();
		Location center = areaController.getCenter();

		Location primarySpawn = areaController.pickRandom(0.45, 0.55);
		Location secondarySpawn = center.clone().add(primarySpawn.clone().subtract(center));
		Utility.snapToHighestY(primarySpawn);
		Utility.snapToHighestY(secondarySpawn);

		primarySpawn.setDirection(secondarySpawn.toVector().subtract(primarySpawn.toVector()).normalize());
		secondarySpawn.setDirection(primarySpawn.toVector().subtract(secondarySpawn.toVector()).normalize());

		ArenaTeamController teamController = game.getTeamController();

		for (Player player : Utility.getTeamAlivePlayers(teamController.getCombatTeam(0)).toList()) {
			player.teleport(primarySpawn);
		}
		for (Player player : Utility.getTeamAlivePlayers(teamController.getCombatTeam(1)).toList()) {
			player.teleport(secondarySpawn);
		}
		for (Player player : Utility.getTeamOnlinePlayers(teamController.getSpectatorTeam()).toList()) {
			player.teleport(center);
		}
	}
}
