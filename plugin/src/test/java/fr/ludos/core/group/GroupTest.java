// package fr.ludos.core.group;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.when;

// import java.util.Set;

// import org.bukkit.configuration.file.FileConfiguration;
// import org.junit.jupiter.api.AfterAll;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.TestInstance;

// import be.seeseemelk.mockbukkit.MockBukkit;
// import be.seeseemelk.mockbukkit.ServerMock;
// import be.seeseemelk.mockbukkit.entity.PlayerMock;
// import fr.ludos.core.Ludos;

// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
// class GroupTest {

// 	private ServerMock server;
// 	private Ludos mockLudos;
// 	private FileConfiguration mockConfig;


// 	@BeforeAll
// 	void setUpAll() {
// 		server = MockBukkit.mock();
// 	}

// 	@AfterAll
// 	void tearDownAll() {
// 		MockBukkit.unmock();
// 	}

// 	@BeforeEach
// 	void setUp() {
// 		mockLudos = mock(Ludos.class);
// 		mockConfig = mock(FileConfiguration.class);

// 		when(mockConfig.getConfigurationSection(anyString())).thenReturn(mockConfig);
// 		when(mockLudos.getConfig()).thenReturn(mockConfig);
// 	}

// 	@Test
// 	@DisplayName("Should add player and notify listeners")
// 	void testAddPlayerWithListeners() {
// 		GroupManager manager = new GroupManager(mockLudos);
// 		PlayerMock leader = server.addPlayer("Leader");
// 		Group group = manager.createGroup(leader, null);

// 		PlayerMock newMember = server.addPlayer("NewMember");
// 		boolean[] listenerCalled = {false};

// 		group.addJoinGroupListener(player -> listenerCalled[0] = true);


// 		group.addPlayer(newMember);

// 		assertTrue(group.isMember(newMember));
// 		assertTrue(listenerCalled[0]);
// 		assertEquals(2, group.getPlayers().size());
// 	}

// 	@Test
// 	@DisplayName("Should kick player and notify listeners")
// 	void testRemovePlayerKick() {
// 		GroupManager manager = new GroupManager(mockLudos);
// 		PlayerMock leader = server.addPlayer("Leader");
// 		PlayerMock member = server.addPlayer("Member");
// 		Group group = manager.createGroup(leader, Set.of(member));

// 		boolean[] leaveListenerCalled = {false};
// 		group.addLeaveGroupListener(player -> leaveListenerCalled[0] = true);


// 		boolean removed = group.removePlayer(member, true);

// 		assertTrue(removed);
// 		assertFalse(group.isMember(member));
// 		assertTrue(leaveListenerCalled[0]);
// 	}

// 	@Test
// 	@DisplayName("Should elect new leader when current leader leaves")
// 	void testRemoveLeaderElections() {
// 		GroupManager manager = new GroupManager(mockLudos);
// 		PlayerMock leader = server.addPlayer("Leader");
// 		PlayerMock member1 = server.addPlayer("Member1");
// 		PlayerMock member2 = server.addPlayer("Member2");
// 		Group group = manager.createGroup(leader, Set.of(member1, member2));


// 		group.removePlayer(leader, false);

// 		assertFalse(group.isLeader(leader));
// 		assertFalse(group.isPlayer(leader));
// 		// One of the members should be the new leader
// 		assertTrue(group.isLeader(member1) || group.isLeader(member2));
// 	}

// 	@Test
// 	@DisplayName("Should handle requestAddPlayer with manual accept")
// 	void testRequestAddPlayerManual() {
// 		GroupManager manager = new GroupManager(mockLudos);
// 		PlayerMock leader = server.addPlayer("Leader");
// 		Group group = manager.createGroup(leader, null);
// 		PlayerMock requester = server.addPlayer("Requester");


// 		Group.AddPlayerResult result = group.requestAddPlayer(requester, Group.AddPlayerMethod.Join);

// 		assertEquals(Group.AddPlayerResult.Requested, result);
// 		assertTrue(group.getJoinRequests().containsKey(requester.getUniqueId()));
// 	}

// 	@Test
// 	@DisplayName("Should return correct return location when offline")
// 	void testPickReturnLocationOffline() {
// 		GroupManager manager = new GroupManager(mockLudos);
// 		PlayerMock leader = server.addPlayer("Leader");
// 		Group group = manager.createGroup(leader, null);
// 		leader.disconnect();


// 		var location = group.pickReturnLocation();

// 		assertNotNull(location);
// 		assertEquals(server.getWorlds().get(0).getSpawnLocation(), location);
// 	}
// }