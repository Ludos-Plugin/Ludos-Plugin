package fr.ludos.core.command.group;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;

class GroupPromoteTest extends GroupTest {

	@Test
	void testPromoteWhenNotInGroup() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		player1.performCommand("ludos group promote " + player2.getName());
		assertEquals("You are not in a group.", player1.nextMessage(), "Successfully promoted Player, when not in group");

		assertCreateGroup(player1);
		player1.performCommand("ludos group promote " + player2.getName());
		assertEquals(player2.getName() + " is not a member of your group.", player1.nextMessage(), "Successfully promoted Player who was not part of the group");
	}

	@Test
	void testPromoteInvalidPlayer() {
		PlayerMock player1 = createPlayer("Player1");
		assertCreateGroup(player1);

		player1.performCommand("ludos group promote " + player1.getName());
		assertEquals(player1.getName() + " is already group leader.", player1.nextMessage(), "Successfully promoted group leader to group leader");

		player1.performCommand("ludos group promote UnknownPlayer");
		assertEquals("Could not find Player.", player1.nextMessage(), "Successfully promoted Unknown player to group leader");
	}

	@Test
	void testPromote() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		assertCreateGroupWithInvite(player1, Set.of(player2));
		assertJoinGroup(player2, player1);

		player2.performCommand("ludos group promote " + player1.getName());
		assertEquals("Only the group leader can promote a member to leader.", player2.nextMessage(), "Successfully promoted group leader as member");

		player1.performCommand("ludos group promote " + player2.getName());
		assertEquals(player2.getName() + " has been promoted to group leader.", player1.nextMessage(), "Could not promote group member as leader");
		assertEquals("You have been promoted to group leader.", player2.nextMessage(), "Did not receive promotion message as promotee");
	}
}
