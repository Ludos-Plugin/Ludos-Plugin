package fr.ludos.core.command.config;

import java.util.List;

import org.junit.jupiter.api.AssertionFailureBuilder;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.game.Game;
import fr.ludos.games.manhunt.ManhuntGame;

class ManhuntConfigCallTest extends ConfigTest {

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

		Game.Builder gameBuilder = ludos.getGameManager().getGameById(ManhuntGame.ID);
		if (! (gameBuilder instanceof ManhuntGame.Builder manhunt)) {
			AssertionFailureBuilder.assertionFailure()
				.message("Could not get ManhuntGame.Builder instance from Game registry")
				.expected(ManhuntGame.Builder.class)
				.actual(gameBuilder.getClass())
				.buildAndThrow();
			return;
		}

		assertSetConfigValues(player1, "ludos config global game manhunt", manhunt.players, additionalPlayerArgs, "everyone");
		assertSetConfigValues(player1, "ludos config global game manhunt", manhunt.prey, "me");
		assertSetConfigValues(player1, "ludos config global game manhunt", WorldBorderArea.CONFIG, "big");
		assertSetConfigValues(player1, "ludos config global game manhunt", manhunt.revealPeriod, "one_minute");

		assertSetConfigValues(player1, "ludos config group game manhunt", manhunt.players, additionalPlayerArgs, "everyone");
		assertSetConfigValues(player1, "ludos config group game manhunt", manhunt.prey, "me");
		assertSetConfigValues(player1, "ludos config group game manhunt", WorldBorderArea.CONFIG, "big");
		assertSetConfigValues(player1, "ludos config group game manhunt", manhunt.revealPeriod, "one_minute");

		player1.performCommand("ludos group disband");
	}
}
