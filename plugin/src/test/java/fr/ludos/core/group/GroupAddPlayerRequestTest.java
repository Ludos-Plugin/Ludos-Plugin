package fr.ludos.core.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GroupAddPlayerRequestTest {
	@Test
	@DisplayName("Should correctly initialize record components")
	void testConstructorAndGetters() {
		OfflinePlayer mockPlayer = Mockito.mock(OfflinePlayer.class);
		boolean isFromLeader = true;

		GroupAddPlayerRequest request = new GroupAddPlayerRequest(mockPlayer, isFromLeader);

		assertEquals(mockPlayer, request.player());
		assertEquals(isFromLeader, request.isFromLeader());
	}

	@Test
	@DisplayName("Should handle false isFromLeader")
	void testConstructorWithFalseLeader() {
		OfflinePlayer mockPlayer = Mockito.mock(OfflinePlayer.class);
		boolean isFromLeader = false;

		GroupAddPlayerRequest request = new GroupAddPlayerRequest(mockPlayer, isFromLeader);

		assertFalse(request.isFromLeader());
	}
}