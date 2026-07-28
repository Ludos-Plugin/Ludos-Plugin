package fr.ludos.core.command.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;

class GroupConfigCallTest extends ConfigTest {

	@Test
	void testDisableLoginMessage() {
		PlayerMock player1 = createPlayer("Player1");
		assertNotNull(player1.nextMessage());
		player1.setOp(true);

		assertSetConfigValues(player1, "ludos config global group", GroupConfigMap.GAME_JOIN, "never");
		assertSetConfigValues(player1, "ludos config global group", GroupConfigMap.GROUP_JOIN, "always");
		assertSetConfigValues(player1, "ludos config global group", GroupConfigMap.MEMBERS_AUTH, "admin");
		assertSetConfigValues(player1, "ludos config global group", GroupConfigMap.START_DELAY, "three");
		assertSetConfigValues(player1, "ludos config global group", GroupConfigMap.WAIT_PLAYERS, "offline");


		assertCreateGroup(player1);

		assertSetConfigValues(player1, "ludos config group group", GroupConfigMap.GAME_JOIN, "never");
		assertSetConfigValues(player1, "ludos config group group", GroupConfigMap.GROUP_JOIN, "always");
		assertSetConfigValues(player1, "ludos config group group", GroupConfigMap.MEMBERS_AUTH, "admin");
		assertSetConfigValues(player1, "ludos config group group", GroupConfigMap.START_DELAY, "three");
		assertSetConfigValues(player1, "ludos config group group", GroupConfigMap.WAIT_PLAYERS, "offline");

		player1.performCommand("ludos group disband");
	}
}
