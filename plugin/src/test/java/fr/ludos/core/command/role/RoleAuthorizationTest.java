package fr.ludos.core.command.role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.role.Role;

class RoleAuthorizationTest extends RoleTest {
	private final Role.Builder getValidRole() {
		Role.Builder role = Role.getRoleBuilders().get(0);
		assertNotNull(role, "Could not find valid role");
		return role;
	}

	@Test
	void testUnsetOtherRole() {
		PlayerMock player1 = createPlayer("Player1");
		PlayerMock player2 = createPlayer("Player2");

		Role.Builder role = getValidRole();
		player1.setOp(true);

		player1.performCommand("ludos role set " + role.getId() + " " + player2.getName());
		assertEquals("The role of Player " + player2.getName() + " is now " + role.getId(), player1.nextMessage(), "Role was not set");
		assertEquals("Your role is now " + role.getId(), player2.nextMessage(), "Role was not set");

		player1.performCommand("ludos role get " + player2.getName());
		assertEquals(role.getId(), player1.nextMessage(), "Role was not persisted after being set");

		assertEquals(Role.getPlayerRole(player2), role, "Role appears set but isn't");

		player1.performCommand("ludos role reset " + player2.getName());
		assertEquals("The role of player " + player2.getName() + " was reset", player1.nextMessage(), "Role was not reset");

		player1.performCommand("ludos role get");
		assertEquals(Role.NONE_LABEL, player1.nextMessage(), "Role was not reset");

		assertNull(Role.getPlayerRole(player1), "Role appears unset but isn't");
	}

	// @Test
	// void testSetRoleAuthz() {
	// 	PlayerMock player1 = createPlayer("Player1");
	// 	PlayerMock player2 = createPlayer("Player2");

	// 	Role.Builder role = getValidRole();

	// 	player1.performCommand("ludos role set " + role.getId() + " " + player2.getName());
	// 	assertEquals("You are not authorized to reset this player's role", player1.nextMessage(), "Role was set despite missing any authorization");

	// 	player1.performCommand("ludos group create " + player2.getName());
	// 	assertEquals("You have created a new group.", player1.nextMessage(), "Could not create group");
	// 	assertTrue(player2.nextMessage().startsWith("You have been invited to join " + player1.getName() + "'s group."), "Was not invited when creating group");

	// 	player2.performCommand("ludos group join " + player1.getName());
	// 	assertEquals("You have joined " + player1.getName() + "'s group.", player2.nextMessage(), "Failed to accept group invite");
	// 	assertEquals(player2.getName() + " has joined the group.", player1.nextMessage(), "Leader did not receive join notification");

	// 	player1.performCommand("ludos role set " + role.getId() + " " + player2.getName());
	// 	assertEquals("The role of Player " + player2.getName() + " is now " + role.getId(), player1.nextMessage(), "Role was not set despite having group leadership authorization");
	// 	assertEquals("Your role is now " + role.getId(), player2.nextMessage(), "Role set message was not received by other player");

	// 	player2.performCommand("ludos role set " + role.getId() + " " + player1.getName());
	// 	assertEquals("You are not authorized to reset this player's role", player2.nextMessage(), "Role was set despite not having group leadership authorization");
	// 	assertEquals(null, player2.nextMessage(), "Role set message was received by other player");
	// }
}
