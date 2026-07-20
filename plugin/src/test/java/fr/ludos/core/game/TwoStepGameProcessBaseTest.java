package fr.ludos.core.game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import fr.ludos.core.Ludos;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TwoStepGameProcessBaseTest {
	private ServerMock server;
	private Ludos ludos;
	private TwoStepGameProcessBase process;

	@BeforeAll
	void setUpAll() {
		server = MockBukkit.mock();
	}

	@AfterAll
	void tearDownAll() {
		MockBukkit.unmock();
	}

	@BeforeEach
	void setUp() {
		ludos = (Ludos) server.getPluginManager().loadPlugin(Ludos.class, new Object[] {});
		server.getPluginManager().enablePlugin(ludos);
		assertTrue(ludos.isEnabled(), "Plugin should be enabled");

		process = new TwoStepGameProcessBase() {
			@Override
			protected JavaPlugin getPlugin() {
				return ludos;
			}
		};
	}

	@Test
	@DisplayName("Should start and stop correctly")
	void testStartAndStop() {
		assertFalse(process.isStarted());
		assertFalse(process.isSetup());

		// Setup first
		process.setUp();
		assertTrue(process.isSetup());
		assertFalse(process.isStarted());

		// Then start
		process.start();
		assertTrue(process.isStarted());
		assertTrue(process.isSetup());

		// Then stop
		process.stop();
		assertFalse(process.isStarted());
		assertFalse(process.isSetup());
	}

	@Test
	@DisplayName("Should throw exception if start called without setup")
	void testStartWithoutSetup() {
		assertFalse(process.isSetup());
		try {
			process.start();
		} catch (IllegalStateException e) {
			assertTrue(e.getMessage().contains("not setup"));
		}
	}

	@Test
	@DisplayName("Should return isClear correctly")
	void testIsClear() {
		assertTrue(process.isClear());


		process.setUp();
		assertTrue(process.isClear());

		process.start();
		assertFalse(process.isClear());

		process.stop();
		assertTrue(process.isClear());
	}
}