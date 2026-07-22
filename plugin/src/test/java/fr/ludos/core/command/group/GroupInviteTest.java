package fr.ludos.core.command.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import be.seeseemelk.mockbukkit.entity.PlayerMock;

class GroupInviteTest extends GroupTest {
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

	@Test
	void testInvitePlayerWhoRequestedToJoin() {
		PlayerMock player1 = createPlayer("Player1");

		player1.performCommand("ludos group create");
		assertEquals("You have created a new group.", player1.nextMessage(), "Could not create group");

		PlayerMock player2 = createPlayer("Player2");
		player2.performCommand("ludos group join " + player1.getName());
		assertEquals("Requested to join " + player1.getName() + "'s group.", player2.nextMessage(), "Could not request to join Player's group");
		assertTrue(player1.nextMessage().startsWith(player2.getName() + " has requested to join your group."), "Did not receive group join request message");

		player1.performCommand("ludos group invite " + player2.getName());
		assertEquals(player2.getName() + " has joined the group.", player1.nextMessage(), "Failed to invite valid player to group");
	}

	@Test
	void testInviteWhenNotInGroup() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		player1.performCommand("ludos group invite " + player2.getName());
		assertEquals("You are not in a group.", player1.nextMessage(), "Successfully invited Player, when not in group");
	}

	@ParameterizedTest
	@ValueSource(strings = {"invite", "game", "config", "all"})
	void testMemberCanInviteWhenRightsAllowIt(String rights) {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		assertCreateGroupWithInvite(player1, Collections.singletonList(player2));
		assertJoinGroup(player2, player1);

		player1.performCommand("ludos group config group member_authorisation " + rights);
		assertEquals("Members authorisation set to " + rights, player1.nextMessage(), "Could not set group rights");

		PlayerMock player3 = createPlayer("Player3");

		player2.performCommand("ludos group invite " + player3.getName());
		String inviteMessage = player2.nextMessage();
		assertTrue(inviteMessage.startsWith("Invited " + player3.getName()), "Member should be allowed to invite when rights permit it");
	}

	@ParameterizedTest
	@ValueSource(strings = {"none"})
	void testMemberCannotInviteWhenRightsDisallowIt(String rights) {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");
		PlayerMock player3 = createPlayer("Player3");

		assertCreateGroupWithInvite(player1, Collections.singletonList(player2));
		player2.performCommand("ludos group join " + player1.getName());
		assertEquals("You have joined " + player1.getName() + "'s group.", player2.nextMessage(), "Join should be accepted after invite");
		assertEquals(player2.getName() + " has joined the group.", player1.nextMessage(), "Leader should be notified on join");

		player1.performCommand("ludos group config group member_authorisation " + rights);
		assertEquals("Members authorisation set to none", player1.nextMessage(), "Could not set group rights");

		player2.performCommand("ludos group invite " + player3.getName());
		assertEquals("Only the group leader can invite new members.", player2.nextMessage(), "Member should not be allowed to invite when rights are " + rights);
	}
}
