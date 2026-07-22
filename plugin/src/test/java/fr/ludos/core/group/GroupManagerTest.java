package fr.ludos.core.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.OfflinePlayer;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GroupManagerTest {

	private ServerMock server;
	private Ludos mockLudos;

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
		when(mockLudos.getLogger()).thenReturn(Logger.getLogger("Test"));
	}

	@Test
	@DisplayName("Should create a group and register it correctly")
	void testCreateGroup() {
		GroupManager manager = spy(new GroupManager(mockLudos));
		PlayerMock leader = server.addPlayer("LeaderName");
		PlayerMock member1 = server.addPlayer("Member1");

		doNothing().when(manager).saveConfig();


		Group group = manager.createGroup(leader, Set.of(member1));

		assertNotNull(group);
		assertEquals(leader.getUniqueId(), group.getLeaderId());
		assertTrue(group.isMember(member1));

		assertEquals(2, group.getPlayers().size());

		assertTrue(manager.getAllGroups().contains(group));
		assertEquals(group, manager.getGroupOfPlayer(leader));
		assertEquals(group, manager.getGroupOfPlayer(member1));
	}

	@Test
	@DisplayName("Should disband a group and remove all references")
	void testDisbandGroup() {
		GroupManager manager = spy(new GroupManager(mockLudos));
		PlayerMock leaderPlayer = server.addPlayer("LeaderName");

		doNothing().when(manager).saveConfig();


		Group group = manager.createGroup(leaderPlayer, null);
		int beforeDisbandCount = manager.getAllGroups().size();

		group.disband();

		assertFalse(manager.getAllGroups().contains(group));
		assertNull(manager.getGroupOfPlayer(leaderPlayer));
		assertEquals(beforeDisbandCount - 1, manager.getAllGroups().size());
	}

	@Test
	@DisplayName("Should handle deserialization correctly")
	void testDeserialize() {
		GroupManager manager = spy(new GroupManager(mockLudos));
		UUID testUuid = UUID.randomUUID();
		OfflinePlayer leader = server.addPlayer("DeserializedLeader");
		UUID incorrectUuid = UUID.randomUUID();

		doNothing().when(manager).saveConfig();

		Map<String, Object> data = new HashMap<>();
		data.put("leader", leader.getUniqueId().toString());
		data.put("members", Collections.singletonList(incorrectUuid.toString()));


		Group group = manager.deserialize(testUuid, data);

		assertNotNull(group);
		assertEquals(testUuid, group.getId());
		assertEquals(leader.getUniqueId(), group.getLeaderId());
		assertEquals(1, group.getMemberIds().size());
		assertEquals(incorrectUuid, group.getMemberIds().iterator().next());
		assertTrue(manager.getAllGroups().contains(group));
	}

	@Test
	@DisplayName("Should remove player from old group when joining new one")
	void testCreateGroupRemovesFromOld() {
		GroupManager manager = spy(new GroupManager(mockLudos));
		PlayerMock player = server.addPlayer("PlayerName");

		doNothing().when(manager).saveConfig();


		Group oldGroup = manager.createGroup(player, null);

		Group newGroup = manager.createGroup(player, null);

		assertFalse(oldGroup.isPlayer(player));
		assertTrue(newGroup.isPlayer(player));
		assertEquals(newGroup, manager.getGroupOfPlayer(player));
	}
}