package fr.ludos.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LudosTest {
	protected static ServerMock server;
	protected static Ludos ludos;

	@BeforeAll
	static void setUpServer() {
		server = MockBukkit.mock();

		ludos = (Ludos) server.getPluginManager().loadPlugin(Ludos.class, new Object[] {});
		server.getPluginManager().enablePlugin(ludos);
		assertTrue(ludos.isEnabled(), "Plugin should be enabled");
	}

	@AfterAll
	static void tearDownServer() {
		MockBukkit.unmock();
	}

	@AfterEach
	void clearPlayers() {
		server.setPlayers(0);
	}

	@Test
	@DisplayName("Should be given Guidebook")
	void testGetGuidebook() {
		ItemStack guidebook = ludos.createGuidebook();
		assertNotNull(guidebook);

		BookMeta itemMeta = (BookMeta) guidebook.getItemMeta();

		assertEquals("Ludos Guidebook", itemMeta.getTitle());
		assertEquals("Ludos", itemMeta.getAuthor());
		assertTrue(itemMeta.pages().size() >= ludos.getGameManager().getRegistered().keySet().size() + ludos.getRoleManager().getRegistered().keySet().size() + 3);
	}

}