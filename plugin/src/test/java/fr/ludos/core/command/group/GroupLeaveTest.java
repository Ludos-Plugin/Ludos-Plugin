package fr.ludos.core.command.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Set;

import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;

class GroupLeaveTest extends GroupTest {

	@Test
	void testLeaveWhenNotInGroup() {
		PlayerMock player1 = createPlayer("Player1");

		player1.performCommand("ludos group leave");
		assertEquals("You are not in a group.", player1.nextMessage(), "Successfully left group, when not in group");
	}

	@Test
	void testLeave() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");
		assertCreateGroupWithInvite(player2, Set.of(player1));
		assertJoinGroup(player1, player2);

		player1.performCommand("ludos group leave");
		assertEquals("You have left the group.", player1.nextMessage(), "Could not leave group");
	}

	@Test
	void testLeaveAsLeader() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");
		assertCreateGroupWithInvite(player1, Set.of(player2));
		assertJoinGroup(player2, player1);

		player1.performCommand("ludos group leave");
		assertEquals(player2.getName() + " has been promoted to group leader.", player1.nextMessage(), "Did not receive member promotion message when leaving");
		assertEquals("You have left the group.", player1.nextMessage(), "Could not leave group");
		assertNull(player1.nextMessage(), "Could not leave group");
	}
}
