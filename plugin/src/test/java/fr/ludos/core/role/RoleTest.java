package fr.ludos.core.role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.Ludos;

public class RoleTest {
	private static ServerMock server;
	private static Plugin plugin;
	private static World baseWorld;

	private static PlayerMock player1;
	private static PlayerMock player2;


	@BeforeAll
	static void setup() {
		server = MockBukkit.mock();

		plugin = server.getPluginManager().loadPlugin(Ludos.class, new Object[] {});
		server.getPluginManager().enablePlugin(plugin);
		assertTrue(plugin.isEnabled(), "Plugin should be enabled");

		baseWorld = server.addSimpleWorld("default");

		player1 = server.addPlayer("Player1");
		player1.teleport(new Location(baseWorld, 0, 64, 0));
		player2 = server.addPlayer("Player2");
		player2.teleport(new Location(baseWorld, 100, 64, 100));
	}

	@AfterAll
	static void tearDown() {
		MockBukkit.unmock();
	}

	@BeforeEach
	void reset() {
		player1.setOp(false);
		player1.performCommand("ludos role reset");
		while (player1.nextMessage() != null) {}
		player1.performCommand("ludos role get");
		assertEquals(Role.noneLabel, player1.nextMessage(), "Role is not unset by default");

		player2.setOp(false);
		player2.performCommand("ludos role reset");
		while (player2.nextMessage() != null) {}
		player2.performCommand("ludos role get");
		assertEquals(Role.noneLabel, player2.nextMessage(), "Role is not unset by default");
	}

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

	@Test
	void testUnsetOtherRole() {
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
		assertEquals(Role.noneLabel, player1.nextMessage(), "Role was not reset");

		assertNull(Role.getPlayerRole(player1), "Role appears unset but isn't");
	}

	@Test
	void testSetRoleAuthz() {
		Role.Builder role = getValidRole();

		player1.performCommand("ludos role set " + role.getId() + " " + player2.getName());
		assertEquals("You are not authorized to reset this player's role", player1.nextMessage(), "Role was set despite missing any authorization");

		player1.performCommand("ludos group create " + player2.getName());
		assertEquals("You have created a new group.", player1.nextMessage(), "Could not create group");
		assertTrue(player2.nextMessage().startsWith("You have been invited to join " + player1.getName() + "'s group."), "Was not invited when creating group");

		player2.performCommand("ludos group join " + player1.getName());
		assertEquals("You have joined " + player1.getName() + "'s group.", player2.nextMessage(), "Failed to accept group invite");
		assertEquals(player2.getName() + " has joined the group.", player1.nextMessage(), "Leader did not receive join notification");


		player1.performCommand("ludos role set " + role.getId() + " " + player2.getName());
		assertEquals("The role of Player " + player2.getName() + " is now " + role.getId(), player1.nextMessage(), "Role was not set despite having group leadership authorization");
		assertEquals("Your role is now " + role.getId(), player2.nextMessage(), "Role set message was not received by other player");

		player2.performCommand("ludos role set " + role.getId() + " " + player1.getName());
		assertEquals("You are not authorized to reset this player's role", player2.nextMessage(), "Role was set despite not having group leadership authorization");
		assertEquals(null, player2.nextMessage(), "Role set message was received by other player");
	}
}
