package fr.ludos.core.command.group;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GroupMembershipTest extends GroupTest {
	@Test
	void testGroupCreateWithInvite() {
		createGroupWithInvite();
		joinGroup();

		assertGroupInfo(player1, player1.getName(), player2.getName());
		assertGroupInfo(player2, player1.getName(), player2.getName());
	}

	@Test
	void testElectNewLeader() {
		createGroupWithInvite();
		joinGroup();
		assertGroupInfo(player1, player1.getName(), player2.getName());
		assertGroupInfo(player2, player1.getName(), player2.getName());

		player1.performCommand("ludos group leave");
		assertEquals("You have left the group.", player1.nextMessage(), "Could not leave the group");
		assertEquals("You have been promoted to group leader.", player2.nextMessage(), "Member was not promoted to Leader");
		assertEquals(player1.getName() + " has left the group.", player2.nextMessage(), "Did not receive player leave notification");
	}
}
