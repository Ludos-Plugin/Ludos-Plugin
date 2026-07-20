package fr.ludos.core.game.teamController;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameTeamControllerTest {
	private ServerMock server;
	private Ludos mockLudos;
	private Game mockGame;
	private GameTeamController controller;
	private Team mockTeam;
	private Scoreboard mockScoreboard;

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
		mockScoreboard = mock(Scoreboard.class);
		mockTeam = mock(Team.class);


		when(mockLudos.getServer()).thenReturn(server);
		when(mockGame.getRandom()).thenReturn(new Random());
		when(mockGame.getPlugin()).thenReturn(mockLudos);
		when(mockGame.getScoreboard()).thenReturn(mockScoreboard);
		when(mockScoreboard.getEntryTeam(anyString())).thenReturn(mockTeam);
		when(mockTeam.getEntries()).thenReturn(Collections.emptySet());


		// Create a concrete implementation for testing
		controller = new GameTeamController(mockGame, GameJoinOption.auto) {
			@Override
			public Collection<Team> getTeams() {
				return Collections.singletonList(mockTeam);
			}
			@Override
			protected void joinPlayer(OfflinePlayer player) { }
			@Override
			protected void discardPlayer(OfflinePlayer player) { }
			@Override
			public void removePlayer(OfflinePlayer player) { }
		};
	}


	@Test
	@DisplayName("Should reflect Teams players presence in more specific getters")
	void testEmptyTeams() {
		assertFalse(controller.getTeams().isEmpty());
		assertTrue(controller.getPlayers().isEmpty());
		assertTrue(controller.getOnlinePlayers().isEmpty());
		assertTrue(controller.getAlivePlayers().isEmpty());

		PlayerMock player = server.addPlayer("TestPlayer");

		when(mockTeam.getEntries()).thenReturn(Collections.singleton(player.getName()));

		assertFalse(controller.getPlayers().isEmpty());
		assertFalse(controller.getOnlinePlayers().isEmpty());
		assertFalse(controller.getAlivePlayers().isEmpty());
	}


	@Test
	@DisplayName("Should add player to team and retrieve it")
	void testAddPlayer() {
		PlayerMock player = server.addPlayer("TestPlayer");

		when(mockTeam.getEntries()).thenReturn(Collections.singleton(player.getUniqueId().toString()));

		// equals(Object) is not implemented for PlayerMock, so we check by name manually
		assertTrue(controller.getPlayers().stream()
			.map(OfflinePlayer::getName)
			.collect(Collectors.toSet())
			.contains(player.getName())
		);
	}


	@Test
	@DisplayName("Should return false for alliances with null names")
	void testAlliesWithNull() {
		assertFalse(controller.areAllies(null, "Player1"));
		assertFalse(controller.areAllies("Player1", null));
		assertFalse(controller.areAllies(null, null));
	}


	@Test
	@DisplayName("Should return true for allies with same name")
	void testAlliesSameName() {
		assertTrue(controller.areAllies("Player1", "Player1"));
		assertTrue(controller.arePlayersAllies(server.addPlayer("Player1"), server.addPlayer("Player1")));
	}


	@Test
	@DisplayName("Should pick a random alive player")
	void testPickRandomPlayer() {
		PlayerMock player1 = server.addPlayer("Player1");
		player1.setGameMode(org.bukkit.GameMode.SURVIVAL);
		PlayerMock player2 = server.addPlayer("Player2");
		player2.setGameMode(org.bukkit.GameMode.SURVIVAL);

		when(mockTeam.getEntries()).thenReturn(Set.of(player1.getUniqueId().toString(), player2.getUniqueId().toString()));


		PlayerMock picked = (PlayerMock) controller.pickRandomPlayer();

		assertNotNull(picked);
		assertTrue(picked.equals(player1) || picked.equals(player2));
	}


	@Test
	@DisplayName("Should return null if no alive players exist")
	void testPickRandomPlayerNoneAlive() {
		PlayerMock player = server.addPlayer("Player1");
		player.setGameMode(GameMode.SPECTATOR); // Not alive


		when(mockTeam.getEntries()).thenReturn(Collections.singleton(player.getUniqueId().toString()));

		assertNull(controller.pickRandomPlayer());
	}


	@Test
	@DisplayName("Should handle GameJoinOption none correctly")
	void testJoinOptionNone() {
		GameTeamController noJoinController = new GameTeamController(mockGame, GameJoinOption.none) {
			@Override
			public Collection<Team> getTeams() { return Collections.emptyList(); }
			@Override
			protected void joinPlayer(OfflinePlayer player) { }
			@Override
			protected void discardPlayer(OfflinePlayer player) { }
			@Override
			public void removePlayer(OfflinePlayer player) { }
		};


		PlayerMock player = server.addPlayer("TestPlayer");
		noJoinController.addPlayer(player);


		// Should not call joinPlayer
		// The message is sent in the real implementation, tested implicitly by no exception
	}
}