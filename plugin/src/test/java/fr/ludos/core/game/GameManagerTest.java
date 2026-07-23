package fr.ludos.core.game;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import fr.ludos.core.group.Group;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameManagerTest {
	private ServerMock server;
	private Ludos mockLudos;
	private Game mockGame;
	private Game.Builder mockBuilder;
	private String mockBuilderId = "testGame";
	private Group mockGroup;


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
		mockBuilder = mock(Game.Builder.class);
		mockGroup = mock(Group.class);

		when(mockLudos.getServer()).thenReturn(server);

		when(mockBuilder.getLudos()).thenReturn(mockLudos);
		when(mockBuilder.getId()).thenReturn(mockBuilderId);
		when(mockBuilder.build(any(Group.class))).thenReturn(mockGame);
		when(mockGame.ludos()).thenReturn(mockLudos);
		when(mockGame.getPlugin()).thenReturn(mockLudos);
		when(mockGame.getGroup()).thenReturn(mockGroup);
	}


	@Test
	@DisplayName("Should register a game and retrieve it by ID")
	void testRegisterAndGetGame() {
		GameManager manager = new GameManager(mockLudos);


		manager.registerGame(mockBuilder);

		assertNotNull(manager.getGameById(mockBuilderId));
		assertEquals(mockBuilder, manager.getGameById(mockBuilderId));
		assertTrue(manager.getGameIds().contains(mockBuilderId));
		assertTrue(manager.getGameBuilders().contains(mockBuilder));
	}


	@Test
	@DisplayName("Should return null for non-existent game ID")
	void testGetNonExistentGame() {
		GameManager manager = new GameManager(mockLudos);

		assertNull(manager.getGameById("nonExistent"));
		assertFalse(manager.getGameIds().contains("nonExistent"));
	}


	@Test
	@DisplayName("Should start a game and add it to active games")
	void testStartGame() {
		GameManager manager = new GameManager(mockLudos);
		manager.registerGame(mockBuilder);


		boolean started = manager.startGame(mockBuilderId, mockGroup);
		assertTrue(started);

		verify(mockGame).setUp();
	}


	@Test
	@DisplayName("Should stop an old game before starting a new one")
	void testStartGameWithExistingGame() {
		Game oldGame = mock(Game.class);
		when(oldGame.isClear()).thenReturn(true);
		when(oldGame.getPlugin()).thenReturn(mockLudos);
		when(mockGroup.getGame()).thenReturn(oldGame);


		GameManager manager = new GameManager(mockLudos);
		manager.registerGame(mockBuilder);

		boolean started = manager.startGame(mockBuilderId, mockGroup);
		assertTrue(started);

		verify(oldGame).stop();
		verify(mockGame).setUp();
	}

	@Test
	@DisplayName("Should return false when starting non-registered game")
	void testStartNonRegisteredGame() {
		GameManager manager = new GameManager(mockLudos);


		assertFalse(manager.startGame("unknownGame", mockGroup));
		assertTrue(manager.getActiveGames().isEmpty());
	}
}