package fr.ludos.core.command.config;

import java.util.List;

import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.games.arena.ArenaGameConfigMap;
import fr.ludos.games.arena.ArenaModeOption;

class ArenaConfigCallTest extends ConfigTest {

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

		assertSetConfigValues(player1, "ludos config global game arena", ArenaGameConfigMap.TEAM_1_PLAYERS, additionalPlayerArgs, "everyone");
		assertSetConfigValues(player1, "ludos config global game arena", ArenaGameConfigMap.TEAM_2_PLAYERS, additionalPlayerArgs, "everyone_else");
		assertSetConfigValues(player1, "ludos config global game arena", ArenaModeOption.CONFIG, "bossfight");
		assertSetConfigValues(player1, "ludos config global game arena", ArenaGameConfigMap.ROUNDS, "ten");
		assertSetConfigValues(player1, "ludos config global game arena", WorldBorderArea.CONFIG, "big");

		assertSetConfigValues(player1, "ludos config group game arena", ArenaGameConfigMap.TEAM_1_PLAYERS, additionalPlayerArgs, "everyone");
		assertSetConfigValues(player1, "ludos config group game arena", ArenaGameConfigMap.TEAM_2_PLAYERS, additionalPlayerArgs, "everyone_else");
		assertSetConfigValues(player1, "ludos config group game arena", ArenaModeOption.CONFIG, "bossfight");
		assertSetConfigValues(player1, "ludos config group game arena", ArenaGameConfigMap.ROUNDS, "ten");
		assertSetConfigValues(player1, "ludos config group game arena", WorldBorderArea.CONFIG, "big");

		player1.performCommand("ludos group disband");
	}
}
