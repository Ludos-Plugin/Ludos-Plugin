package fr.ludos.core.command.config;

import java.util.List;

import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.games.manhunt.ManhuntGameConfigMap;

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

		assertSetConfigValues(player1, "ludos config global game manhunt", ManhuntGameConfigMap.PLAYERS, additionalPlayerArgs, "everyone");
		assertSetConfigValues(player1, "ludos config global game manhunt", ManhuntGameConfigMap.PREY, "me");
		assertSetConfigValues(player1, "ludos config global game manhunt", WorldBorderArea.CONFIG, "big");
		assertSetConfigValues(player1, "ludos config global game manhunt", ManhuntGameConfigMap.REVEAL_PERIOD, "one_minute");

		assertSetConfigValues(player1, "ludos config group game manhunt", ManhuntGameConfigMap.PLAYERS, additionalPlayerArgs, "everyone");
		assertSetConfigValues(player1, "ludos config group game manhunt", ManhuntGameConfigMap.PREY, "me");
		assertSetConfigValues(player1, "ludos config group game manhunt", WorldBorderArea.CONFIG, "big");
		assertSetConfigValues(player1, "ludos config group game manhunt", ManhuntGameConfigMap.REVEAL_PERIOD, "one_minute");

		player1.performCommand("ludos group disband");
	}
}
