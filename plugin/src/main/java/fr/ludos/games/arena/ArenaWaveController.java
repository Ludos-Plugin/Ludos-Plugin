package fr.ludos.games.arena;

import java.time.Duration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.ludos.core.Utility;
import fr.ludos.core.game.teamController.GameTeamController;
import fr.ludos.core.lobby.Lobby;
import fr.ludos.core.wave.DefaultWaveLoadout;
import fr.ludos.core.wave.WaveController;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

/**
 * Controller for managing waves in the {@link ArenaGame}.
 */
public final class ArenaWaveController extends WaveController {
	private final ArenaGame game;

	private int primaryWins = 0;
	private int secondaryWins = 0;

	protected ArenaWaveController(ArenaGame game, int maxRounds) {
		super(game, maxRounds, new DefaultWaveLoadout(game));
		this.game = game;
	}

	@Override
	protected void onStart() {
		super.onStart();

		game.getWorldManager()
			.mutateLobby(lobby -> lobby
				.wait(Duration.ofSeconds(3))
				.showOnStart(Component.text("Round starting"))
				.thenDont(getGame()::start)
				.then(this::startWave)
			);
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
		Lobby lobby = game.getWorldManager().getLobby();
		if (lobby == null) {
			stop();
			return;
		}

		GameTeamController teamController = getGame().getTeamController();
		teamController.stop();
		lobby.restart();
		teamController.start();
	}

	@Override
	public void startWave() {
		getGame().getTeamController().start();

		Bukkit.broadcast(Component.text("Round " + getCurrentWaveNumber() + " starts!").color(NamedTextColor.GREEN));
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
}
