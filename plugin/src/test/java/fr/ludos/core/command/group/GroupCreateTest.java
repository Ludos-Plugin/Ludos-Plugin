package fr.ludos.core.command.group;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;

class GroupCreateTest extends GroupTest {
	@Test
	void testGroupCreate() {
		PlayerMock player1 = createPlayer("Player1");

		player1.performCommand("ludos group create");
		assertEquals("You have created a new group.", player1.nextMessage(), "Could not create group");

		assertGroupInfo(player1, player1, Collections.emptyList());
	}

	@Test
	void testGroupCreateWhenAlreadyInGroup() {
		PlayerMock player1 = createPlayer("Player1");

		player1.performCommand("ludos group create");
		assertEquals("You have created a new group.", player1.nextMessage(), "Could not create group");

		assertGroupInfo(player1, player1, Collections.emptyList());

		player1.performCommand("ludos group create");
		assertEquals("Your group has been disbanded.", player1.nextMessage(), "Group was not disbanded when leaving, as only member");
		assertEquals("You have left the group.", player1.nextMessage(), "Could not create group");
		assertEquals("You have created a new group.", player1.nextMessage(), "Could not create group");
	}

	@Test
	void testGroupCreateWithInvite() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		assertCreateGroupWithInvite(player1, Collections.singletonList(player2));
		assertJoinGroup(player2, player1);

		assertGroupInfo(player1, player1, Collections.singletonList(player2));
		assertGroupInfo(player2, player1, Collections.singletonList(player2));
	}

	@Test
	void testElectNewLeader() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		assertCreateGroupWithInvite(player1, Collections.singletonList(player2));
		assertJoinGroup(player2, player1);
		assertGroupInfo(player1, player1, Collections.singletonList(player2));
		assertGroupInfo(player2, player1, Collections.singletonList(player2));

		player1.performCommand("ludos group leave");
		assertEquals(player2.getName() + " has been promoted to group leader.", player1.nextMessage(), "Did not receive new leader promotion notification");
		assertEquals("You have left the group.", player1.nextMessage(), "Could not leave the group");
		assertEquals("You have been promoted to group leader.", player2.nextMessage(), "Member was not promoted to Leader");
		assertEquals(player1.getName() + " has left the group.", player2.nextMessage(), "Did not receive player leave notification");
	}
}
