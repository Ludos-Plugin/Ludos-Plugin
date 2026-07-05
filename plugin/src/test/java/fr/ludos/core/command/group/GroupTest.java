package fr.ludos.core.command.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.command.MockBukkitTestBase;

abstract class GroupTest extends MockBukkitTestBase {
	void createGroupWithInvite() {
		player1.performCommand("ludos group create " + player2.getName());
		assertEquals("You have created a new group.", player1.nextMessage(), "Could not create group");
		assertTrue(player2.nextMessage().startsWith("You have been invited to join " + player1.getName() + "'s group."), "Was not invited when creating group");
	}

	void joinGroup() {
		player2.performCommand("ludos group join " + player1.getName());
		assertEquals("You have joined " + player1.getName() + "'s group.", player2.nextMessage(), "Failed to accept group invite");
		assertEquals(player2.getName() + " has joined the group.", player1.nextMessage(), "Leader did not receive join notification");
	}

	void assertGroupInfo(PlayerMock player, String leader, String members) {
		player.performCommand("ludos group info");
		assertEquals("Group leader: " + leader, player.nextMessage(), "Invalid Group leader in Info");
		assertEquals("Group members: " + members, player.nextMessage(), "Invalid Group member in Info");
		assertNull(player.nextMessage(), "Extra Group member in Info");
	}
}