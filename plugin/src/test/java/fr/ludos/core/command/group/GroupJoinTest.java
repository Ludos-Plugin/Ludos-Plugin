package fr.ludos.core.command.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;

class GroupJoinTest extends GroupTest {

	@Test
	void testJoinPlayerNotInGroup() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		player1.performCommand("ludos group join " + player2.getName());
		assertEquals(player2.getName() + " is not in a group.", player1.nextMessage(), "Successfully join Player not in group");

		assertCreateGroup(player1);
		player1.performCommand("ludos group join " + player2.getName());
		assertEquals(player2.getName() + " is not in a group.", player1.nextMessage(), "Successfully join Player not in group, when already in group");
	}

	@Test
	void testJoinInvalidPlayer() {
		PlayerMock player1 = createPlayer("Player1");

		player1.performCommand("ludos group join " + player1.getName());
		assertEquals(player1.getName() + " is not in a group.", player1.nextMessage(), "Successfully requested to join own group, when not in a group");

		player1.performCommand("ludos group join UnknownPlayer");
		assertEquals("Could not find Player.", player1.nextMessage(), "Successfully requested to join non-existent Player's group");


		PlayerMock player2 = createPlayer("Player2");
		assertCreateGroupWithInvite(player1, Set.of(player2));
		assertJoinGroup(player2, player1);

		player2.performCommand("ludos group join " + player1.getName());
		assertEquals("You are already in this group.", player2.nextMessage(), "Successfully requested to join own group");
	}

	@Test
	void testJoin() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");
		assertCreateGroup(player1);

		player2.performCommand("ludos group join " + player1.getName());
		assertTrue(player1.nextMessage().startsWith(player2.getName() + " has requested to join your group."), "Successfully invited group leader to group leader");
		assertEquals("Requested to join " + player1.getName() + "'s group.", player2.nextMessage(), "Successfully invited group leader to group leader");

		player1.performCommand("ludos group join UnknownPlayer");
		assertEquals("Could not find Player.", player1.nextMessage(), "Successfully invited Unknown player to group leader");
	}
}
