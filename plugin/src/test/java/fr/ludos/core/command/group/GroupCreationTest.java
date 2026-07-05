package fr.ludos.core.command.group;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GroupCreationTest extends GroupTest {
	@Test
	void testGroupCreate() {
		player1.performCommand("ludos group create");
		assertEquals("You have created a new group.", player1.nextMessage(), "Could not create group");

		assertGroupInfo(player1, player1.getName(), "");
	}
}
