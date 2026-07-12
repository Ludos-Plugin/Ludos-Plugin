package fr.ludos.core.command.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;

class GroupInvitationTest extends GroupTest {
	@Test
	void testInviteInvalidPlayer() {
		PlayerMock player1 = createPlayer("Player1");

		player1.performCommand("ludos group create");
		assertEquals("You have created a new group.", player1.nextMessage(), "Could not create group");

		boolean isValid = player1.performCommand("ludos group invite");
		player1.nextMessage();
		assertFalse(isValid, "Successfully invited no one to group");

		player1.performCommand("ludos group invite Player1");
		assertEquals("No valid player names provided.", player1.nextMessage(), "Successfully invited self to group");

		PlayerMock player2 = server.addPlayer("Player2");

		player1.performCommand("ludos group invite Player2");
		assertEquals("Invited " + player2.getName() + " to the group.", player1.nextMessage(), "Failed to invite valid player to group");

		player1.performCommand("ludos group invite Player2 Player3");
		assertEquals("Invited " + player2.getName() + " to the group.", player1.nextMessage(), "Successfully invited invalid Player to group");
	}
}
