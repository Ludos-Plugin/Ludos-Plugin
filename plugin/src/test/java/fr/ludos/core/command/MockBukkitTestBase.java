package fr.ludos.core.command;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.Ludos;


public abstract class MockBukkitTestBase {
	protected static ServerMock server;
	protected static Plugin plugin;
	protected static World baseWorld;

	@BeforeAll
	static void setUpServer() {
		server = MockBukkit.mock();

		plugin = server.getPluginManager().loadPlugin(Ludos.class, new Object[] {});
		server.getPluginManager().enablePlugin(plugin);
		assertTrue(plugin.isEnabled(), "Plugin should be enabled");

		baseWorld = server.addSimpleWorld("default");
	}

	@AfterAll
	static void tearDownServer() {
		MockBukkit.unmock();
	}

	@AfterEach
	void clearPlayers() {
		server.setPlayers(0);
	}

	public void initPlayer(PlayerMock player) {
		player.setLocation(baseWorld.getSpawnLocation());
	}
	public final PlayerMock createPlayer(String name) {
		PlayerMock player = server.addPlayer(name);
		clearMessages(player);

		initPlayer(player);

		clearMessages(player);
		return player;
	}

	protected final static void clearMessages(PlayerMock player) {
		while (player.nextMessage() != null) {}
	}
}
