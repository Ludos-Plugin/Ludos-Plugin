package fr.ludos.core.command.group;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.group.GroupRightsOption;

class GroupKickTest extends GroupTest {

	@Test
	void testKickWhenNotInGroup() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		player1.performCommand("ludos group kick " + player2.getName());
		assertEquals("You are not in a group.", player1.nextMessage(), "Successfully kicked Player, when not in group");

		assertCreateGroup(player1);
		player1.performCommand("ludos group kick " + player2.getName());
		assertEquals(player2.getName() + " is not a member of your group.", player1.nextMessage(), "Successfully kicked Player who was not part of the group");
	}

	@Test
	void testKick() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		assertCreateGroupWithInvite(player1, Set.of(player2));
		assertJoinGroup(player2, player1);

		player2.performCommand("ludos group kick " + player1.getName());
		assertEquals("Only the group leader can kick members.", player2.nextMessage(), "Successfully kicked group leader as member");

		player1.performCommand("ludos group kick " + player2.getName());
		assertEquals(player2.getName() + " has been kicked from the group.", player1.nextMessage(), "Could not kick group member as leader");
	}

	@ParameterizedTest
	@ValueSource(strings = {"all"})
	void testMemberCanKickWhenRightsAllowIt(String rights) {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		assertCreateGroupWithInvite(player1, Collections.singletonList(player2));
		assertJoinGroup(player2, player1);

		PlayerMock player3 = createPlayer("Player3");
		assertInvitePlayerToGroup(player1, Collections.singletonList(player3));

		assertJoinGroup(player3, player1);

		player1.performCommand("ludos group config group member_authorisation " + rights);
		assertEquals("Members authorisation set to " + rights, player1.nextMessage(), "Could not set group rights");

		player2.performCommand("ludos group kick " + player3.getName());
		assertEquals("Player3 has been kicked from the group.", player2.nextMessage(), "Member could not kick another member, even with " + rights + " group authorisation.");
	}

	@ParameterizedTest
	@ValueSource(strings = {"none", "invite", "game", "config"})
	void testMemberCannotKickWhenRightsDisallowIt(String rights) {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		assertCreateGroupWithInvite(player1, Collections.singletonList(player2));
		assertJoinGroup(player2, player1);

		PlayerMock player3 = createPlayer("Player3");
		assertInvitePlayerToGroup(player1, Collections.singletonList(player3));

		assertJoinGroup(player3, player1);

		player1.performCommand("ludos group config group member_authorisation " + rights);
		assertEquals("Members authorisation set to " + rights, player1.nextMessage(), "Could not set group rights");

		player2.performCommand("ludos group kick " + player3.getName());
		assertEquals("Only the group leader can kick members.", player2.nextMessage(), "Member could kick another member, even with " + rights + " group authorisation.");
	}

	@Test
	void testMemberWithoutManageRightsCannotKick() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		assertCreateGroupWithInvite(player1, Collections.singletonList(player2));
		assertJoinGroup(player2, player1);

		PlayerMock player3 = createPlayer("Player3");

		assertInvitePlayerToGroup(player1, Collections.singletonList(player3));
		assertJoinGroup(player3, player1);

		player1.performCommand("ludos group config group member_authorisation " + GroupRightsOption.none.name());
		assertEquals("Members authorisation set to " + GroupRightsOption.none.name(), player1.nextMessage(), "Could not set group rights");

		player2.performCommand("ludos group kick " + player3.getName());
		assertEquals("Only the group leader can kick members.", player2.nextMessage(), "Member should not be allowed to kick when rights are none");
	}
}
