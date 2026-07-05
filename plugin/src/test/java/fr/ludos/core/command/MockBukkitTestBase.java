package fr.ludos.core.command;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.Ludos;


public abstract class MockBukkitTestBase {
	protected static ServerMock server;
	protected static Plugin plugin;
	protected static World baseWorld;

	protected static PlayerMock player1;
	protected static PlayerMock player2;

	@BeforeAll
	static void setUpServer() {
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
	static void tearDownServer() {
		MockBukkit.unmock();
	}

	@BeforeEach
	protected void resetPlayers() {
		player1.setOp(false);
		player2.setOp(false);
		player1.performCommand("ludos group leave");
		clearMessages(player1);
		player2.performCommand("ludos group leave");
		clearMessages(player2);
	}

	protected static void clearMessages(PlayerMock player) {
		while (player.nextMessage() != null) {
		}
	}
}
