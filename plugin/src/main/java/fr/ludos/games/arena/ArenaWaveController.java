package fr.ludos.games.arena;

import java.time.Duration;

import org.bukkit.entity.Player;

import fr.ludos.core.Utility;
import fr.ludos.core.game.teamController.GameTeamController;
import fr.ludos.core.item.SpecialItem;
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

		game.worldManager()
			.mutateLobby(lobby -> lobby
				.wait(Duration.ofSeconds(3))
				.showOnStart(Component.text("Round starting"))
				.thenDont(game()::start)
				.then(this::startWave)
			);
	}

	@Override
	protected void onStop() {
		super.onStop();
		game().stop();
	}

	@Override
	public void onScheduleEnd() {
		Component result;
		if (primaryWins > secondaryWins) {
			result = Component.text("Arena finished: ")
				.append(Component.text("Team 1").color(NamedTextColor.BLUE))
				.append(Component.text(" wins "))

				.append(Component.text(primaryWins).color(NamedTextColor.BLUE))
				.append(Component.text(" / "))
				.append(Component.text(secondaryWins).color(NamedTextColor.RED));
		} else if (secondaryWins > primaryWins) {
			result = Component.text("Arena finished: ")
				.append(Component.text("Team 2").color(NamedTextColor.RED))
				.append(Component.text(" wins "))

				.append(Component.text(secondaryWins).color(NamedTextColor.RED))
				.append(Component.text(" / "))
				.append(Component.text(primaryWins).color(NamedTextColor.BLUE));
		} else {
			result = Component.text("Arena finished: Draw " + primaryWins + " wins each!").color(NamedTextColor.WHITE);
		}


		game().group().showTitle(Title.title(
			Component.text("Match Over").color(NamedTextColor.GOLD),
			result,
			Title.Times.times(Duration.ZERO, Duration.ofSeconds(5), Duration.ofSeconds(2))
		));
	}

	@Override
	public void startWave() {
		GameTeamController teamController = game.teamController();
		for (Player player : teamController.getOnlinePlayers()) {
			Utility.resetPlayer(player);
			SpecialItem.Events.refreshPlayerInventory(game, player);
			teamController.placePlayer(player);
		}

		game().group().sendMessage(Component.text("Round " + getCurrentWaveNumber() + " starts!").color(NamedTextColor.GREEN));
	}

	@Override
	protected void nextWave() {
		Lobby lobby = game.worldManager().getLobby();
		if (lobby == null) {
			stop();
			return;
		}

		lobby.restart();
	}

	@Override
	protected void evaluateWaveState() {
		ArenaTeamController teamController = game.teamController();

		long alivePrimary = Utility.getTeamAlivePlayers(teamController.getCombatTeam(0), game.plugin().getServer()).count();
		long aliveSecondary = Utility.getTeamAlivePlayers(teamController.getCombatTeam(1), game.plugin().getServer()).count();
		if (alivePrimary > 0 && aliveSecondary > 0) return;

		int currentRound = getCurrentWaveNumber();

		if (alivePrimary > aliveSecondary) {
			primaryWins++;
			game().group().sendMessage(Component.text("Round " + currentRound + " won by Team 1").color(NamedTextColor.BLUE));
		} else if (aliveSecondary > alivePrimary) {
			secondaryWins++;
			game().group().sendMessage(Component.text("Round " + currentRound + " won by Team 2").color(NamedTextColor.RED));
		} else {
			game().group().sendMessage(Component.text("Round " + currentRound + " is a draw").color(NamedTextColor.WHITE));
		}

		completeCurrentWave();
	}
}
