package fr.ludos.core.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class GroupTest {
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
		player2 = server.addPlayer("Player2");
		player1.teleport(new Location(baseWorld, 0, 64, 0));
		player2.teleport(new Location(baseWorld, 100, 64, 100));
	}

	@AfterAll
	static void tearDown() {
		MockBukkit.unmock();
	}

	@BeforeEach
	void reset() {
		player1.performCommand("ludos group leave");
		while (player1.nextMessage() != null) {}

		player2.performCommand("ludos group leave");
		while (player2.nextMessage() != null) {}
	}

	@Test
	void testGroupCreate() {
		player1.performCommand("ludos group create");
		assertEquals("You have created a new group.", player1.nextMessage(), "Could not create group");

		player1.performCommand("ludos group info");

		assertEquals("Group leader: " + player1.getName(), player1.nextMessage(), "Invalid Group leader in Info for creator");
		assertEquals("Group members: ", player1.nextMessage(), "Invalid Group member in Info for creator");
	}

	@Test
	void testGroupCreateWithInvite() {
		player1.performCommand("ludos group create " + player2.getName());
		assertEquals("You have created a new group.", player1.nextMessage(), "Could not create group");
		assertTrue(player2.nextMessage().startsWith("You have been invited to join " + player1.getName() + "'s group."), "Was not invited when creating group");

		player2.performCommand("ludos group join " + player1.getName());
		assertEquals("You have joined " + player1.getName() + "'s group.", player2.nextMessage(), "Failed to accept group invite");
		assertEquals(player2.getName() + " has joined the group.", player1.nextMessage(), "Leader did not receive join notification");

		player1.performCommand("ludos group info");
		assertEquals("Group leader: " + player1.getName(), player1.nextMessage(), "Invalid Group leader in Info for creator");
		assertEquals("Group members: " + player2.getName(), player1.nextMessage(), "Invalid Group member in Info for creator");
		assertNull(player1.nextMessage(), "Extra Group member in Info for creator");

		player2.performCommand("ludos group info");
		assertEquals("Group leader: " + player1.getName(), player2.nextMessage(), "Invalid Group leader in Info for invitee");
		assertEquals("Group members: " + player2.getName(), player2.nextMessage(), "Invalid Group member in Info for invitee");
		assertNull(player2.nextMessage(), "Extra Group member in Info for invitee");
	}

	@Test
	void testInviteInvalidPlayer() {
		player1.performCommand("ludos group create");
		assertEquals("You have created a new group.", player1.nextMessage(), "Could not create group");

		boolean isValid = player1.performCommand("ludos group invite");
		player1.nextMessage();
		assertFalse(isValid, "Successfully invited no one to group");

		player1.performCommand("ludos group invite Player1");
		assertEquals("No valid player names provided.", player1.nextMessage(), "Successfully invited self to group");

		player1.performCommand("ludos group invite Player2");
		assertEquals("Invited " + player2.getName() + " to the group.", player1.nextMessage(), "Failed to invite valid player to group");

		player1.performCommand("ludos group invite Player2 Player3");
		assertEquals("Invited " + player2.getName() + " to the group.", player1.nextMessage(), "Successfully invited invalid Player to group");
	}

	@Test
	void testElectNewLeader() {
		player1.performCommand("ludos group create " + player2.getName());
		assertEquals("You have created a new group.", player1.nextMessage(), "Could not create group");
		assertTrue(player2.nextMessage().startsWith("You have been invited to join " + player1.getName() + "'s group."), "Was not invited when creating group");

		player2.performCommand("ludos group join " + player1.getName());
		assertEquals("You have joined " + player1.getName() + "'s group.", player2.nextMessage(), "Failed to accept group invite");
		assertEquals(player2.getName() + " has joined the group.", player1.nextMessage(), "Leader did not receive join notification");

		player1.performCommand("ludos group info");
		assertEquals("Group leader: " + player1.getName(), player1.nextMessage(), "Invalid Group leader in Info for creator");
		assertEquals("Group members: " + player2.getName(), player1.nextMessage(), "Invalid Group member in Info for creator");
		assertNull(player1.nextMessage(), "Extra Group member in Info for creator");

		player2.performCommand("ludos group info");
		assertEquals("Group leader: " + player1.getName(), player2.nextMessage(), "Invalid Group leader in Info for invitee");
		assertEquals("Group members: " + player2.getName(), player2.nextMessage(), "Invalid Group member in Info for invitee");
		assertNull(player2.nextMessage(), "Extra Group member in Info for invitee");

		player1.performCommand("ludos group leave");
		assertEquals("You have left the group.", player1.nextMessage(), "Could not leave the group");
		assertEquals("You have been promoted to group leader.", player2.nextMessage(), "Member was not promoted to Leader");
		assertEquals(player1.getName() + " has left the group.", player2.nextMessage(), "Did not receive player leave notification");
	}
}