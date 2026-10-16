package fr.ludos.core.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.group.Group;

class LobbyWaitPlayersOptionTest {

	private ServerMock server;
	private Group mockGroup;

	// Note: Mocking OfflinePlayer for those not online is tricky in MockBukkit without a real UUID context
	// We will test the logic based on the Group's stream.

	@DisplayName("Should return online players for ONLINE option")
	@Test
	void testOnlineOption() {
		server = MockBukkit.mock();
		PlayerMock p1 = server.addPlayer("Online1");
		PlayerMock p2 = server.addPlayer("Online2");

		mockGroup = mock(Group.class);
		when(mockGroup.getPlayers()).thenReturn(Set.of(p1, p2));
		when(mockGroup.getOnlinePlayers()).thenReturn(Set.of(p1, p2));

		Set<OfflinePlayer> result = LobbyWaitPlayersOption.online.getPlayers(mockGroup);

		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(result.contains(p1));
		assertTrue(result.contains(p2));

		MockBukkit.unmock();
	}

	@DisplayName("Should return all players for ALL option")
	@Test
	void testAllOption() {
		server = MockBukkit.mock();
		PlayerMock p1 = server.addPlayer("Online1");

		mockGroup = mock(Group.class);
		when(mockGroup.getPlayers()).thenReturn(Set.of(p1));

		Set<OfflinePlayer> result = LobbyWaitPlayersOption.all.getPlayers(mockGroup);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.contains(p1));

		MockBukkit.unmock();
	}

	@DisplayName("Should get correct options list")
	@Test
	void testGetOptions() {
		List<String> options = LobbyWaitPlayersOption.getOptions();

		assertNotNull(options);
		assertTrue(options.contains("online"));
		assertTrue(options.contains("all"));
		assertEquals(2, options.size());
	}

	@DisplayName("Should get correct usage string with all options separated by pipes")
	@Test
	void testGetUsage() {
		String usage = LobbyWaitPlayersOption.getUsage();

		assertNotNull(usage, "Usage string should not be null");

		// Verify format: <option1 | option2>
		assertTrue(usage.startsWith("<"), "Usage string should start with '<'");
		assertTrue(usage.endsWith(">"), "Usage string should end with '>'");

		// Verify content includes both options
		assertTrue(usage.contains("online"), "Usage string should contain 'online'");
		assertTrue(usage.contains("all"), "Usage string should contain 'all'");

		// Verify separator
		assertTrue(usage.contains(" | "), "Usage string should contain ' | ' separator");

		// Optional: Verify exact expected string if order is guaranteed (it is in the enum definition)
		String expected = "<online | all>";
		assertEquals(expected, usage, "Usage string should match the expected format exactly");
	}
}