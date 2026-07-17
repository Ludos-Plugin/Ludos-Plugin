package fr.ludos.core.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.Ludos;
import fr.ludos.core.group.Group;
import fr.ludos.core.role.Role;


public abstract class MockBukkitTestBase {
	protected static ServerMock server;
	protected static Plugin plugin;
	protected static World baseWorld;

	@BeforeAll
	static void setUpServer() {
		server = MockBukkit.mock();

		plugin = server.getPluginManager().loadPlugin(Ludos.class, new Object[] {});
		server.getPluginManager().enablePlugin(plugin);
		assertTrue(plugin.isEnabled(), "Plugin should be enabled");

		baseWorld = server.addSimpleWorld("default");
	}

	@AfterAll
	static void tearDownServer() {
		MockBukkit.unmock();
	}

	@AfterEach
	void clearPlayers() {
		server.setPlayers(0);
	}

	public void initPlayer(PlayerMock player) {
		player.setLocation(baseWorld.getSpawnLocation());
	}
	public final PlayerMock createPlayer(String name) {
		PlayerMock player = server.addPlayer(name);
		initPlayer(player);

		return player;
	}

	protected final static void clearMessages(PlayerMock player) {
		while (player.nextMessage() != null) {}
	}


	protected void assertResetRole(PlayerMock player) {
		clearMessages(player);

		player.performCommand("ludos role reset");
		clearMessages(player);
		player.performCommand("ludos role get");
		assertEquals(Role.NONE_LABEL, player.nextMessage(), "Role is not unset by default");
	}


	public static void assertCreateGroup(PlayerMock leader) {
		clearMessages(leader);

		leader.performCommand("ludos group create");
		assertEquals("You have created a new group.", leader.nextMessage(), "Could not create group");
	}
	public static void assertCreateGroupWithInvite(PlayerMock leader, Collection<PlayerMock> playersToInvite) {
		clearMessages(leader);
		for (PlayerMock invitee : playersToInvite) {
			clearMessages(invitee);
		}

		leader.performCommand("ludos group create " + playersToInvite.stream().map(PlayerMock::getName).collect(Collectors.joining(" ")));
		assertEquals("You have created a new group.", leader.nextMessage(), "Could not create group");
		assertPlayersInvitedToGroup(leader, playersToInvite);
	}

	public static void assertInvitePlayerToGroup(PlayerMock leader, Collection<PlayerMock> playersToInvite) {
		clearMessages(leader);
		for (PlayerMock invitee : playersToInvite) {
			clearMessages(invitee);
		}

		List<PlayerMock> ordered = playersToInvite.stream().toList();
		leader.performCommand("ludos group invite " + ordered.stream().map(PlayerMock::getName).collect(Collectors.joining(" ")));
		assertEquals("Invited " + ordered.stream().map(PlayerMock::getName).collect(Collectors.joining(", ")) + " to the group.", leader.nextMessage());
		assertPlayersInvitedToGroup(leader, playersToInvite);
	}

	public static void assertPlayersInvitedToGroup(PlayerMock leader, Collection<PlayerMock> playersToInvite) {
		for (PlayerMock player : playersToInvite) {
			String message = player.nextMessage();
			assertNotNull(message, "Player " + player.getName() + " was not invited when creating group.");
			assertTrue(message.startsWith("You have been invited to join " + leader.getName() + "'s group."), "Was not invited when creating group. Message: " + message);
		}
	}

	protected void assertJoinGroup(PlayerMock player, PlayerMock leader) {
		clearMessages(player);

		Group leaderGroup = Group.getGroupOfPlayer(leader);
		assertNotNull(leaderGroup, "Player attempted to join the group of a player not in a group");

		Set<PlayerMock> groupPlayers = leaderGroup.getMembers().stream()
			.map(OfflinePlayer::getPlayer)
			.map(p -> (PlayerMock) p)
			.collect(Collectors.toSet());

		player.performCommand("ludos group join " + leader.getName());
		assertEquals("You have joined " + leader.getName() + "'s group.", player.nextMessage(), "Failed to accept group invite");

		assertEquals(player.getName() + " has joined the group.", leader.nextMessage(), "Leader did not receive join notification");

		for (PlayerMock member : groupPlayers) {
			assertEquals(player.getName() + " has joined the group.", member.nextMessage(), "Member " + member.getName() + " did not receive join notification");
		}
	}

	protected void assertGroupInfo(PlayerMock player) {
		clearMessages(player);

		Group group = Group.getGroupOfPlayer(player);
		assertNotNull(group, "Cannot get group info of player not in a group.");
		OfflinePlayer leader = group.getLeader();
		assertGroupInfo(player, leader, group.getMembers());
	}
	protected void assertGroupInfo(PlayerMock player, OfflinePlayer leader, Collection<OfflinePlayer> members) {
		clearMessages(player);

		player.performCommand("ludos group info");
		assertEquals("Group leader: " + leader.getName(), player.nextMessage(), "Invalid Group leader in Info");
		String infoMemberNamesString = player.nextMessage().substring("Group members: ".length());

		Set<String> actualMemberNames = members.stream().map(OfflinePlayer::getName).collect(Collectors.toSet());
		Set<String> infoMemberNames = infoMemberNamesString.isBlank()
			? Collections.emptySet()
			: Arrays.stream(infoMemberNamesString.trim().split(", ")).collect(Collectors.toSet());

		assertTrue(actualMemberNames.equals(infoMemberNames), "Invalid Group members in Info\nActual Members: " + actualMemberNames.stream().collect(Collectors.joining(", ")) + ".\nInfo Members: " + infoMemberNamesString + ".");
	}
}
