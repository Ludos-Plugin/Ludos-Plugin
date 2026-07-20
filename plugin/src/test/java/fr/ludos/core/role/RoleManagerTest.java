// package fr.ludos.core.role;


// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.util.List;

// import org.bukkit.OfflinePlayer;
// import org.bukkit.configuration.ConfigurationSection;
// import org.bukkit.configuration.file.FileConfiguration;
// import org.bukkit.entity.Player;
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
// import fr.ludos.core.group.Group;
// import fr.ludos.core.group.GroupManager;


// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
// class RoleManagerTest {
// 	private ServerMock server;
// 	private Ludos mockLudos;
// 	private RoleManager manager;
// 	private Role.Builder mockRoleBuilder;
// 	private FileConfiguration mockPlayersConfig;
// 	private ConfigurationSection mockPlayerConfigSection;
// 	private GroupManager mockGroupManager;
// 	private Group mockGroup;

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
// 		mockRoleBuilder = mock(Role.Builder.class);
// 		mockPlayersConfig = mock(FileConfiguration.class);
// 		mockPlayerConfigSection = mock(ConfigurationSection.class);
// 		mockGroupManager = mock(GroupManager.class);
// 		mockGroup = mock(Group.class);

// 		when(mockLudos.getLogger()).thenReturn(java.util.logging.Logger.getLogger("Test"));
// 		when(mockLudos.getServer()).thenReturn(server);
// 		when(mockLudos.getPlayersConfig()).thenReturn(mockPlayersConfig);
// 		when(mockLudos.getGroupManager()).thenReturn(mockGroupManager);
// 		when(mockLudos.getPlayerConfigSection(any())).thenReturn(mockPlayerConfigSection);


// 		when(mockRoleBuilder.getId()).thenReturn("warrior");
// 		when(mockRoleBuilder.getLudos()).thenReturn(mockLudos);
// 		when(mockRoleBuilder.getPlugin()).thenReturn(mockLudos);

// 		manager = new RoleManager(mockLudos);
// 		manager.registerRole(mockRoleBuilder);

// 		// Mock Group behavior for authorization tests
// 		when(mockGroupManager.getGroupOfPlayer(any())).thenReturn(mockGroup);
// 		when(mockGroup.isLeader(any())).thenReturn(true);
// 	}


// 	@Test
// 	@DisplayName("Should register a role and retrieve it by ID")
// 	void testRegisterAndGetRole() {
// 		assertNotNull(manager.getRoleById("warrior"));
// 		assertEquals(mockRoleBuilder, manager.getRoleById("warrior"));
// 		assertTrue(manager.getRoleIds().contains("warrior"));
// 		assertTrue(manager.getBuilders().contains(mockRoleBuilder));
// 	}

// 	@Test
// 	@DisplayName("Should handle case-insensitive role ID registration")
// 	void testRegisterRoleCaseInsensitive() {
// 		Role.Builder mockBuilder2 = mock(Role.Builder.class);
// 		when(mockBuilder2.getId()).thenReturn("WIZARD");
// 		manager.registerRole(mockBuilder2);


// 		assertNotNull(manager.getRoleById("wizard"));
// 		assertNotNull(manager.getRoleById("WIZARD"));
// 		assertTrue(manager.getRoleIds().contains("wizard"));
// 	}

// 	@Test
// 	@DisplayName("Should set role for a player and persist to config")
// 	void testSetRole() {
// 		PlayerMock player = server.addPlayer("TestPlayer");
// 		OfflinePlayer offlinePlayer = player;


// 		manager.setRole(offlinePlayer, "warrior");


// 		assertTrue(manager.isPlayerRole(offlinePlayer, "warrior"));
// 		assertEquals("warrior", manager.getPlayerRoleId(offlinePlayer));
// 		verify(mockPlayerConfigSection, times(1)).set(Role.NAMESPACE, "warrior");
// 		verify(mockLudos, times(1)).savePlayersConfig();
// 	}

// 	@Test
// 	@DisplayName("Should unset role for a player and persist to config")
// 	void testUnsetRole() {
// 		PlayerMock player = server.addPlayer("TestPlayer");
// 		manager.setRole(player, "warrior");


// 		manager.unsetRole(player);


// 		assertFalse(manager.isPlayerRole(player, "warrior"));
// 		assertNull(manager.getPlayerRoleId(player));
// 		verify(mockPlayerConfigSection, times(1)).set(Role.NAMESPACE, null);
// 	}

// 	@Test
// 	@DisplayName("Should return players of a specific role")
// 	void testGetPlayersOfRole() {
// 		PlayerMock player1 = server.addPlayer("Player1");
// 		PlayerMock player2 = server.addPlayer("Player2");


// 		manager.setRole(player1, "warrior");
// 		manager.setRole(player2, "warrior");


// 		List<Player> players = manager.getPlayersOfRole("warrior");


// 		assertEquals(2, players.size());
// 		assertTrue(players.contains(player1));
// 		assertTrue(players.contains(player2));
// 	}

// 	@Test
// 	@DisplayName("Should authorize operator to edit role if OP or same player")
// 	void testIsAuthorizedToEditRoleOperator() {
// 		PlayerMock operator = server.addPlayer("OpPlayer");
// 		operator.setOp(true);
// 		PlayerMock target = server.addPlayer("TargetPlayer");


// 		assertTrue(manager.isAuthorizedToEditRole(operator, target));
// 		assertTrue(manager.isAuthorizedToEditRole(target, target));
// 	}

// 	@Test
// 	@DisplayName("Should authorize group leader to edit role for group member")
// 	void testIsAuthorizedToEditRoleGroupLeader() {
// 		PlayerMock leader = server.addPlayer("Leader");
// 		PlayerMock member = server.addPlayer("Member");


// 		when(mockGroupManager.getGroupOfPlayer(leader)).thenReturn(mockGroup);
// 		when(mockGroupManager.getGroupOfPlayer(member)).thenReturn(mockGroup);
// 		when(mockGroup.isLeader(leader)).thenReturn(true);


// 		assertTrue(manager.isAuthorizedToEditRole(leader, member));
// 	}

// 	@Test
// 	@DisplayName("Should deny access if not OP and not group leader of target")
// 	void testIsAuthorizedToEditRoleDenied() {
// 		PlayerMock other = server.addPlayer("Other");
// 		PlayerMock target = server.addPlayer("Target");

// 		// Setup different groups or non-leader status
// 		Group otherGroup = mock(Group.class);
// 		when(mockGroupManager.getGroupOfPlayer(other)).thenReturn(otherGroup);
// 		when(mockGroupManager.getGroupOfPlayer(target)).thenReturn(mockGroup);
// 		when(otherGroup.isLeader(other)).thenReturn(false);


// 		assertFalse(manager.isAuthorizedToEditRole(other, target));
// 	}

// 	@Test
// 	@DisplayName("Should return null for non-existent role ID")
// 	void testGetNonExistentRole() {
// 		assertNull(manager.getRoleById("nonExistent"));
// 		assertNull(manager.getPlayerRole(server.addPlayer("Test")));
// 	}

// 	@Test
// 	@DisplayName("Should create a predicate to filter players by role")
// 	void testOfRolePredicate() {
// 		PlayerMock player1 = server.addPlayer("Player1");
// 		PlayerMock player2 = server.addPlayer("Player2");


// 		manager.setRole(player1, "warrior");
// 		manager.setRole(player2, "mage");


// 		assertTrue(manager.ofRole("warrior").test(player1));
// 		assertFalse(manager.ofRole("warrior").test(player2));
// 		assertFalse(manager.ofRole("warrior").test(server.addPlayer("Player3")));
// 	}
// }