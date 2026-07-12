package fr.ludos.core.command.group;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;

class GroupCreationTest extends GroupTest {
	@Test
	void testGroupCreate() {
		PlayerMock player1 = createPlayer("Player1");

		player1.performCommand("ludos group create");
		assertEquals("You have created a new group.", player1.nextMessage(), "Could not create group");

		assertGroupInfo(player1, player1, java.util.Collections.emptyList());
	}
}
