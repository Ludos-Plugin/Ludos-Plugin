package fr.ludos.core.command.config;

import java.util.List;

import org.junit.jupiter.api.AssertionFailureBuilder;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.game.Game;
import fr.ludos.games.raid.RaidGame;

class RaidConfigCallTest extends ConfigTest {

	@Test
	void testDisableLoginMessage() {
		PlayerMock player1 = createPlayer("Player1");
		player1.setOp(true);
		PlayerMock player2 = createPlayer("Player2");
		PlayerMock player3 = createPlayer("Player3");

		assertCreateGroupWithInvite(player1, List.of(player2, player3));
		assertJoinGroup(player2, player1);
		assertJoinGroup(player3, player1);

		final List<String> additionalPlayerArgs = List.of(
			player1.getName() + " " + player2.getName(),
			player2.getName() + " " + player3.getName(),
			player1.getName() + " " + player3.getName(),
			player1.getName() + " " + player2.getName() + " " + player3.getName()
		);

		Game.Builder gameBuilder = ludos.getGameManager().getGameById(RaidGame.ID);
		if (! (gameBuilder instanceof RaidGame.Builder raid)) {
			AssertionFailureBuilder.assertionFailure()
				.message("Could not get RaidGame.Builder instance from Game registry")
				.expected(RaidGame.Builder.class)
				.actual(gameBuilder.getClass())
				.buildAndThrow();
			return;
		}

		assertSetConfigValues(player1, "ludos config global game raid", raid.players, additionalPlayerArgs, "everyone");
		assertSetConfigValues(player1, "ludos config global game raid", raid.waves, "five");
		assertSetConfigValues(player1, "ludos config global game raid", WorldBorderArea.CONFIG, "big");

		assertSetConfigValues(player1, "ludos config group game raid", raid.players, additionalPlayerArgs, "everyone");
		assertSetConfigValues(player1, "ludos config group game raid", raid.waves, "five");
		assertSetConfigValues(player1, "ludos config group game raid", WorldBorderArea.CONFIG, "big");

		player1.performCommand("ludos group disband");
	}
}
