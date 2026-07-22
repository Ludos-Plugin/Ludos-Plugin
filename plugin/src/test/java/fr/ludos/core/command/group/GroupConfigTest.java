package fr.ludos.core.command.group;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import be.seeseemelk.mockbukkit.entity.PlayerMock;

class GroupConfigTest extends GroupTest {

	@ParameterizedTest
	@ValueSource(strings = {"config", "all"})
	void testMemberCanConfigureGroupWhenRightsAllowIt(String rights) {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		assertCreateGroupWithInvite(player1, Collections.singletonList(player2));
		assertJoinGroup(player2, player1);

		player1.performCommand("ludos group config group member_authorisation " + rights);
		assertEquals("Members authorisation set to " + rights, player1.nextMessage(), "Could not set group rights");

		player2.performCommand("ludos group config group member_authorisation none");
		assertEquals("Members authorisation set to none", player2.nextMessage(), "Member should be allowed to configure the group when rights permit it");
	}
}
