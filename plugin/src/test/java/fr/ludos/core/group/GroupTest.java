package fr.ludos.core.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.Ludos;

class GroupTest {
	private ServerMock server;
	private Plugin plugin;
	private World baseWorld;

	private PlayerMock player1;
	private PlayerMock player2;

	@BeforeEach
	void setupEach() {
		server = MockBukkit.mock();

		Object[] params = {};
		plugin = server.getPluginManager().loadPlugin(Ludos.class, params);
		server.getPluginManager().enablePlugin(plugin);
		assertTrue(plugin.isEnabled(), "Le plugin devrait être activé");

		baseWorld = server.addSimpleWorld("default");

		player1 = server.addPlayer("Player1");
		player2 = server.addPlayer("Player2");
		player1.teleport(new Location(baseWorld, 0, 64, 0));
		player2.teleport(new Location(baseWorld, 100, 64, 100));

		// "Click here to get a guidebook!"
		player1.nextMessage();
		player2.nextMessage();
	}

	@AfterEach
	void tearDownEach() {
		MockBukkit.unmock();
	}

	@Test
	void testGroupCreateSuccessfully() {
		player1.performCommand("ludos group create " + player2.getName());

		assertEquals("You have created a new group.", player1.nextMessage(), "Could not create group");
		assertTrue(player2.nextMessage().startsWith("You have been invited to join " + player1.getName() + "'s group."), "Was not invited when creating group");

		player2.performCommand("ludos group join " + player1.getName());

		assertEquals("You have joined " + player1.getName() + "'s group.", player2.nextMessage(), "Could not accept group invite");
		assertEquals(player2.getName() + " has joined the group.", player1.nextMessage(), "Did not join the group");

		player1.performCommand("ludos group info");
		assertEquals("Group leader: " + player1.getName(), player1.nextMessage(), "Invalid Group leader in Info for creator");
		assertEquals("Group members: " + player2.getName(), player1.nextMessage(), "Invalid Group member in Info for creator");
		assertNull(player1.nextMessage(), "Extra Group member in Info for creator");

		player2.performCommand("ludos group info");
		assertEquals("Group leader: " + player1.getName(), player2.nextMessage(), "Invalid Group leader in Info for invitee");
		assertEquals("Group members: " + player2.getName(), player2.nextMessage(), "Invalid Group member in Info for invitee");
		assertNull(player2.nextMessage(), "Extra Group member in Info for invitee");
	}
}