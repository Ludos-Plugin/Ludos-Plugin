package fr.ludos.core.command.role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import fr.ludos.core.role.Role;

class RoleAssignmentTest extends RoleTest {
	private final Role.Builder getValidRole() {
		Role.Builder role = Role.getRoleBuilders().get(0);
		assertNotNull(role, "Could not find valid role");
		return role;
	}

	@Test
	void testSetUnsetRole() {
		player1.performCommand("ludos role get");
		assertEquals(Role.noneLabel, player1.nextMessage(), "Role is not unset by default");

		Role.Builder role = getValidRole();
		player1.performCommand("ludos role set " + role.getId());
		assertEquals("Your role is now " + role.getId(), player1.nextMessage(), "Role was not set");

		player1.performCommand("ludos role get");
		assertEquals(role.getId(), player1.nextMessage(), "Role was not persisted after being set");

		assertEquals(role, Role.getPlayerRole(player1), "Role appears set but isn't");

		player1.performCommand("ludos role reset");
		assertEquals("Your role was reset", player1.nextMessage(), "Role reset message was not sent");
		assertEquals("You now have no role", player1.nextMessage(), "None role message was not sent");

		player1.performCommand("ludos role get");
		assertEquals(Role.noneLabel, player1.nextMessage(), "Role was not unset");

		assertNull(Role.getPlayerRole(player1), "Role appears unset but isn't");
	}

	@Test
	void testSetInvalidRole() {
		String invalidRoleId = "invalidRole";

		player1.performCommand("ludos role set " + invalidRoleId);
		assertEquals("Role not found: " + invalidRoleId.toLowerCase(), player1.nextMessage(), "Role was not set");

		player1.performCommand("ludos role get");
		assertEquals(Role.noneLabel, player1.nextMessage(), "Role was set despite being invalid");

		player1.performCommand("ludos role reset");
		assertEquals(null, player1.nextMessage(), "Role was reset, despite being already unset");

		player1.performCommand("ludos role get");
		assertEquals(Role.noneLabel, player1.nextMessage(), "Role was not reset");

		assertNull(Role.getPlayerRole(player1), "Role appears unset but isn't");
	}
}
