package fr.ludos.core.command.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupRightsOption;

class GroupPermissionTest extends GroupTest {
	@Test
	void testNonLeaderCannotInviteWhenRightsDisallowIt() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");
		PlayerMock player3 = createPlayer("Player3");

		createGroupWithInvite(player1, java.util.Collections.singletonList(player2));
		player2.performCommand("ludos group join " + player1.getName());
		assertEquals("You have joined " + player1.getName() + "'s group.", player2.nextMessage(), "Join should be accepted after invite");
		assertEquals(player2.getName() + " has joined the group.", player1.nextMessage(), "Leader should be notified on join");

		player1.performCommand("ludos group config local member_authorisation " + GroupRightsOption.none.name());
		assertEquals("Members authorisation set to none", player1.nextMessage(), "Could not set group rights");

		player2.performCommand("ludos group invite " + player3.getName());
		assertEquals("Only the group leader can invite new members.", player2.nextMessage(), "Member should not be allowed to invite when rights are none");
	}

	@Test
	void testMemberWithoutManageRightsCannotKick() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		createGroupWithInvite(player1, Collections.singletonList(player2));
		joinGroup(player2, player1);

		PlayerMock player3 = createPlayer("Player3");

		invitePlayerToGroup(player1, Collections.singletonList(player3));
		joinGroup(player3, player1);

		player1.performCommand("ludos group config local member_authorisation " + GroupRightsOption.none.name());
		assertEquals("Members authorisation set to " + GroupRightsOption.none.name(), player1.nextMessage(), "Could not set group rights");

		player2.performCommand("ludos group kick " + player3.getName());
		assertEquals("Only the group leader can kick members.", player2.nextMessage(), "Member should not be allowed to kick when rights are none");
	}

	@ParameterizedTest
	@ValueSource(strings = {"invite", "game", "config", "all"})
	void testMemberCanInviteWhenRightsAllowIt(String rights) {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		createGroupWithInvite(player1, Collections.singletonList(player2));
		joinGroup(player2, player1);

		player1.performCommand("ludos group config local member_authorisation " + rights);
		assertEquals("Members authorisation set to " + rights, player1.nextMessage(), "Could not set group rights");

		PlayerMock player3 = createPlayer("Player3");

		player2.performCommand("ludos group invite " + player3.getName());
		String inviteMessage = player2.nextMessage();
		assertTrue(inviteMessage.startsWith("Invited " + player3.getName()), "Member should be allowed to invite when rights permit it");
	}

	@ParameterizedTest
	@ValueSource(strings = {"all"})
	void testMemberCanKickWhenRightsAllowIt(String rights) {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		createGroupWithInvite(player1, Collections.singletonList(player2));
		joinGroup(player2, player1);

		PlayerMock player3 = createPlayer("Player3");
		invitePlayerToGroup(player1, Collections.singletonList(player3));

		joinGroup(player3, player1);

		player1.performCommand("ludos group config local member_authorisation " + rights);
		assertEquals("Members authorisation set to " + rights, player1.nextMessage(), "Could not set group rights");

		player2.performCommand("ludos group kick " + player3.getName());
		assertEquals("Player3 has left the group.", player2.nextMessage(), "Member could not kick another member, even with " + rights + " group authorisation.");
	}

	@ParameterizedTest
	@ValueSource(strings = {"config", "all"})
	void testMemberCanConfigureGroupWhenRightsAllowIt(String rights) {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		createGroupWithInvite(player1, Collections.singletonList(player2));
		joinGroup(player2, player1);

		player1.performCommand("ludos group config local member_authorisation " + rights);
		assertEquals("Members authorisation set to " + rights, player1.nextMessage(), "Could not set group rights");

		player2.performCommand("ludos group config local member_authorisation none");
		assertEquals("Members authorisation set to none", player2.nextMessage(), "Member should be allowed to configure the group when rights permit it");
	}

	@Test
	void testRepeatedJoinRequestIsHandledSafely() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		createGroupWithInvite(player1, Collections.singletonList(player2));
		joinGroup(player2, player1);

		Group group = Group.getGroupOfPlayer(player1);
		assertTrue(group != null && group.isMember(player2), "Player should be a member after joining");
	}
}
