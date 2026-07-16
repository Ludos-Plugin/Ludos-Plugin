package fr.ludos.core.command.group;

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

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.command.MockBukkitTestBase;
import fr.ludos.core.group.Group;

abstract class GroupTest extends MockBukkitTestBase {
	public static void createGroupWithInvite(PlayerMock leader, Collection<PlayerMock> playersToInvite) {
		leader.performCommand("ludos group create " + playersToInvite.stream().map(PlayerMock::getName).collect(Collectors.joining(" ")));
		assertEquals("You have created a new group.", leader.nextMessage(), "Could not create group");
		assertPlayersInviteToGroup(leader, playersToInvite);
	}

	public static void invitePlayerToGroup(PlayerMock leader, Collection<PlayerMock> playersToInvite) {
		List<PlayerMock> ordered = playersToInvite.stream().toList();
		leader.performCommand("ludos group invite " + ordered.stream().map(PlayerMock::getName).collect(Collectors.joining(" ")));
		for (PlayerMock invitee : ordered) {
			assertEquals("Invited " + invitee.getName() + " to the group.", leader.nextMessage());
		}
		assertPlayersInviteToGroup(leader, playersToInvite);
	}

	public static void assertPlayersInviteToGroup(PlayerMock leader, Collection<PlayerMock> playersToInvite) {
		for (PlayerMock player : playersToInvite) {
			String message = player.nextMessage();
			assertTrue(message.startsWith("You have been invited to join " + leader.getName() + "'s group."), "Was not invited when creating group. Message: " + message);
		}
	}

	void joinGroup(PlayerMock player, PlayerMock leader) {
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

	void assertGroupInfo(PlayerMock player) {
		Group group = Group.getGroupOfPlayer(player);
		assertNotNull(group, "Cannot get group info of player not in a group.");
		OfflinePlayer leader = group.getLeader();
		assertGroupInfo(player, leader, group.getMembers());
	}
	void assertGroupInfo(PlayerMock player, OfflinePlayer leader, Collection<OfflinePlayer> members) {
		player.performCommand("ludos group info");
		assertEquals("Group leader: " + leader.getName(), player.nextMessage(), "Invalid Group leader in Info");
		String infoMemberNamesString = player.nextMessage().substring("Group members: ".length());

		Set<String> actualMemberNames = members.stream().map(OfflinePlayer::getName).collect(Collectors.toSet());
		Set<String> infoMemberNames = infoMemberNamesString.isBlank()
			? Collections.emptySet()
			: Arrays.stream(infoMemberNamesString.trim().split(", ")).collect(Collectors.toSet());

		assertTrue(actualMemberNames.equals(infoMemberNames), "Invalid Group members in Info\nActual Members: " + actualMemberNames.stream().collect(Collectors.joining(", ")) + ".\nInfo Members: " + infoMemberNamesString + ".");
	}

	@Override
	public void initPlayer(PlayerMock player) {
		super.initPlayer(player);
		clearMessages(player);
	}
}