package fr.ludos.core.game;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
class GameEventsTest {
	private ServerMock server;
	private Ludos mockLudos;
	private Game mockGame;
	private GameEvents gameEvents;


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
		mockLudos = mock(Ludos.class);
		mockGame = mock(Game.class);

		when(mockLudos.getServer()).thenReturn(server);
		when(mockGame.getPlugin()).thenReturn(mockLudos);


		gameEvents = new GameEvents(mockGame);
	}


	@Test
	@DisplayName("Should initialize with correct game reference")
	void testInitialization() {
		assertNotNull(gameEvents.getGame());
		assertEquals(mockGame, gameEvents.getGame());
	}


	@Test
	@DisplayName("Should return correct plugin from game")
	void testGetPlugin() {
		assertEquals(mockLudos, gameEvents.getPlugin());
	}
}