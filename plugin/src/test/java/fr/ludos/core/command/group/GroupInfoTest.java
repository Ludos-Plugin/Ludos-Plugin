package fr.ludos.core.command.group;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;

class GroupInfoTest extends GroupTest {

	@Test
	void testInfoWhenNotInGroup() {
		PlayerMock player1 = createPlayer("Player1");

		player1.performCommand("ludos group info");
		assertEquals("You are not in a group.", player1.nextMessage(), "Successfully got group info, when not in a group");
	}

	@Test
	void testKick() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		assertCreateGroupWithInvite(player1, Set.of(player2));
		assertGroupInfo(player1);

		assertJoinGroup(player2, player1);
		assertGroupInfo(player1, player1, Set.of(player2));
		assertGroupInfo(player2, player1, Set.of(player2));
	}
}