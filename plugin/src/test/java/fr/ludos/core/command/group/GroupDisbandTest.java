package fr.ludos.core.command.group;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import be.seeseemelk.mockbukkit.entity.PlayerMock;

class GroupDisbandTest extends GroupTest {

	@Test
	void testInvalidDisband() {
		PlayerMock player1 = createPlayer("Player1");

		player1.performCommand("ludos group disband");
		assertEquals("You are not in a group.", player1.nextMessage(), "Successfully disbanded, when not in group");

		PlayerMock player2 = createPlayer("Player2");
		assertCreateGroupWithInvite(player2, Set.of(player1));
		assertJoinGroup(player1, player2);

		player1.performCommand("ludos group disband");
		assertEquals("Only the group leader can disband the group.", player1.nextMessage(), "Successfully disbanded, without the right to do so");
	}

	@Test
	void testDisband() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");
		assertCreateGroupWithInvite(player1, Set.of(player2));
		assertJoinGroup(player2, player1);

		player1.performCommand("ludos group disband");
		assertEquals("Your group has been disbanded.", player1.nextMessage(), "Could not disband own group");
	}

	@ParameterizedTest
	@ValueSource(strings = {"all"})
	void testMemberCanDisbandWhenRightsAllowIt(String rights) {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		assertCreateGroupWithInvite(player1, Collections.singletonList(player2));
		assertJoinGroup(player2, player1);

		PlayerMock player3 = createPlayer("Player3");
		assertInvitePlayerToGroup(player1, Collections.singletonList(player3));

		assertJoinGroup(player3, player1);

		player1.performCommand("ludos group config group member_authorisation " + rights);
		assertEquals("Members authorisation set to " + rights, player1.nextMessage(), "Could not set group rights");

		player2.performCommand("ludos group disband");
		assertEquals("Your group has been disbanded.", player2.nextMessage(), "Member could not disband, even with " + rights + " group authorisation.");
	}
}
