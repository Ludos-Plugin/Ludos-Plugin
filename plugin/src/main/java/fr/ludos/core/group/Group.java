package fr.ludos.core.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.game.Game;
import fr.ludos.core.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Group {
	public static final String LEADER_KEY = "leader";
	public static final String MEMBERS_KEY = "members";

	public enum JoinMethod {
		Join,
		Invite
	}

	public enum JoinResult {
		Succeeded,
		Requested,
		Failed
	}

	private static final Set<Group> GROUPS = new HashSet<>();
	public static final Set<Group> getGroups() {
		return Collections.unmodifiableSet(GROUPS);
	}

	private static final Map<UUID, Group> PLAYER_GROUP_MAP = new HashMap<>();
	public static Group getGroupOfPlayer(@NotNull UUID playerId) {
		return PLAYER_GROUP_MAP.get(Objects.requireNonNull(playerId, "Player Id cannot be null"));
	}
	public static Group getGroupOfPlayer(@NotNull OfflinePlayer player) {
		return PLAYER_GROUP_MAP.get(Objects.requireNonNull(player, "Player cannot be null").getUniqueId());
	}

	private final Ludos ludos;
	public Ludos getLudos() {
		return ludos;
	}

	private UUID id;
	public final UUID getId() {
		return id;
	}

	private UUID leaderId;
	public final OfflinePlayer getLeader() {
		return Bukkit.getOfflinePlayer(leaderId);
	}

	private final Set<UUID> memberIds;
	public final Set<OfflinePlayer> getMembers() {
		return memberIds.stream()
			.map(Bukkit::getOfflinePlayer)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}
	public final Set<Player> getOnlineMembers() {
		return memberIds.stream()
			.map(Bukkit::getPlayer)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	private Map<OfflinePlayer, JoinMethod> joinRequests = new HashMap<>();
	public Map<OfflinePlayer, JoinMethod> getJoinRequests() {
		return this.joinRequests;
	}

	private @Nullable Game game;
	public @Nullable Game getGame() {
		return game;
	}
	public void setGame(@Nullable Game game) {
		this.game = game;
	}

	private final List<Consumer<OfflinePlayer>> leaveGroupListeners = new ArrayList<>();
	public void addLeaveGroupListener(Consumer<OfflinePlayer> listener) {
		leaveGroupListeners.add(listener);
	}
	public void removeLeaveGroupListener(Consumer<OfflinePlayer> listener) {
		leaveGroupListeners.remove(listener);
	}
	private void notifyLeaveGroup(OfflinePlayer player) {
		for (Consumer<OfflinePlayer> listener : leaveGroupListeners) {
			listener.accept(player);
		}
	}

	private final List<Consumer<OfflinePlayer>> joinGroupListeners = new ArrayList<>();
	public void addJoinGroupListener(Consumer<OfflinePlayer> listener) {
		joinGroupListeners.add(listener);
	}
	public void removeJoinGroupListener(Consumer<OfflinePlayer> listener) {
		joinGroupListeners.remove(listener);
	}
	private void notifyJoinGroup(OfflinePlayer player) {
		for (Consumer<OfflinePlayer> listener : joinGroupListeners) {
			listener.accept(player);
		}
	}


	private Group(UUID id, @NotNull OfflinePlayer leader, @NotNull Collection<OfflinePlayer> members, Ludos ludos) {
		this.id = id;
		this.ludos = ludos;
		this.leaderId = leader.getUniqueId();
		this.memberIds = members.stream()
			.map(OfflinePlayer::getUniqueId)
			.collect(Collectors.toCollection(HashSet::new));
	}
	private Group(@NotNull OfflinePlayer leader, @NotNull Collection<OfflinePlayer> members, Ludos ludos) {
		this(UUID.randomUUID(), leader, members, ludos);
		writeAllToConfig();
	}


	public final Set<OfflinePlayer> getPlayers() {
		Set<UUID> allPlayers = new HashSet<>(memberIds);
		if (leaderId != null) {
			allPlayers.add(leaderId);
		}
		return allPlayers.stream()
			.map(Bukkit::getOfflinePlayer)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}
	public final Set<Player> getOnlinePlayers() {
		Set<UUID> allPlayers = new HashSet<>(memberIds);
		if (leaderId != null) {
			allPlayers.add(leaderId);
		}

		return allPlayers.stream()
			.map(Bukkit::getPlayer)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	public final boolean isLeader(OfflinePlayer player) {
		if (player == null) return false;
		return player.getUniqueId().equals(leaderId);
	}
	public final boolean isMember(OfflinePlayer player) {
		if (player == null) return false;
		return memberIds.contains(player.getUniqueId());
	}

	public final boolean isPlayer(OfflinePlayer player) {
		if (player == null) return false;
		return player.getUniqueId().equals(leaderId) || memberIds.contains(player.getUniqueId());
	}


	private static void initializeGroup(Group group) {
		for (OfflinePlayer member : group.getMembers()) {
			PLAYER_GROUP_MAP.put(member.getUniqueId(), group);
		}
		OfflinePlayer leader = group.getLeader();
		if (leader != null) {
			PLAYER_GROUP_MAP.put(leader.getUniqueId(), group);
		}
		GROUPS.add(group);
	}
	private static void deinitializeGroup(Group group) {
		for (OfflinePlayer member : group.getMembers()) {
			PLAYER_GROUP_MAP.remove(member.getUniqueId());
		}
		OfflinePlayer leader = group.getLeader();
		if (leader != null) {
			PLAYER_GROUP_MAP.remove(leader.getUniqueId());
		}
		GROUPS.remove(group);
	}

	public final static Group createGroup(@NotNull OfflinePlayer leader, @Nullable Set<OfflinePlayer> members, Ludos ludos) {
		if (leader == null) {
			throw new IllegalArgumentException("Leader cannot be null");
		}

		Group oldGroup = getGroupOfPlayer(leader);
		if (oldGroup != null) {
			oldGroup.removePlayer(leader, false);
		}


		Group group = new Group(leader, members == null ? Collections.emptySet() : members, ludos);
		initializeGroup(group);


		Component creationMessage = Component.text("You have created a new group.");
		Player onlineLeader = leader.getPlayer();
		if (onlineLeader != null) {
			onlineLeader.sendMessage(creationMessage);
		}

		Component joinMessage = Component.text("You have joined " + leader.getName() + "'s group.");
		for (OfflinePlayer member : group.getMembers()) {
			Player onlineMember = member.getPlayer();
			if (onlineMember != null) {
				onlineMember.sendMessage(joinMessage);
			}
		}

		return group;
	}

	public final void disband() {
		Game game = getGame();
		if (game != null) {
			game.stop();
		}

		deinitializeGroup(this);

		Component disbandMessage = Component.text("Your group has been disbanded.");

		OfflinePlayer leader = getLeader();
		Player onlineLeader = leader.getPlayer();
		if (onlineLeader != null) {
			onlineLeader.sendMessage(disbandMessage);
		}
		this.leaderId = null;

		for (OfflinePlayer member : getMembers()) {
			Player onlineMember = member.getPlayer();
			if (onlineMember != null) {
				onlineMember.sendMessage(disbandMessage);
			}
		}
		this.memberIds.clear();

		removeConfigGroup();
	}
	private final boolean electNewLeader() {
		Set<UUID> currentMemberIds = memberIds;
		if (currentMemberIds.isEmpty()) return false;

		UUID newLeaderId = currentMemberIds.iterator().next();

		this.leaderId = newLeaderId;
		memberIds.remove(newLeaderId);
		writeAllToConfig();

		Component promotionMessage = Component.text("You have been promoted to group leader.");
		Player onlineLeader = Bukkit.getPlayer(newLeaderId);
		if (onlineLeader != null) {
			onlineLeader.sendMessage(promotionMessage);
		}

		Component joinMessage = Component.text(getLeader().getName() + " has been promoted to group leader.");
		for (Player member : getOnlineMembers()) {
			member.sendMessage(joinMessage);
		}

		return true;
	}

	public final boolean demoteLeader() {
		OfflinePlayer oldLeader = getLeader();

		if (electNewLeader()) {
			Player onlineLeader = oldLeader.getPlayer();
			memberIds.add(oldLeader.getUniqueId());
			writeMembersToConfig();

			Component demoteMessage = Component.text(getLeader().getName() + " has been promoted to leader.");
			onlineLeader.sendMessage(demoteMessage);

			return true;
		}

		return false;
	}

	public final boolean addPlayer(OfflinePlayer player) {
		Player onlinePlayer = player.getPlayer();
		if (isPlayer(player)) {

			if (onlinePlayer != null) {
				Component alreadyJoinedMessage = Component.text("You are already in this group.");
				onlinePlayer.sendMessage(alreadyJoinedMessage);
			}

			return false;
		}

		Group currentGroup = getGroupOfPlayer(player);
		if (currentGroup != null) {
			currentGroup.removePlayer(player, false);
		}

		Component joinMessage = Component.text(player.getName() + " has joined the group.");
		for (Player member : getOnlinePlayers()) {
			member.sendMessage(joinMessage);
		}

		addMemberInternalPersistent(player);

		notifyJoinGroup(player);

		Component targetMessage = Component.text("You have joined " + getLeader().getName() + "'s group.");
		if (onlinePlayer != null) {
			onlinePlayer.sendMessage(targetMessage);
		}

		return true;
	}

	private final void addMemberInternal(OfflinePlayer player) {
		memberIds.add(player.getUniqueId());
		PLAYER_GROUP_MAP.put(player.getUniqueId(), this);
	}
	private final void addMemberInternalPersistent(OfflinePlayer player) {
		addMemberInternal(player);
		writeMembersToConfig();
	}
	private final void removeMemberInternal(OfflinePlayer player) {
		memberIds.remove(player.getUniqueId());
		PLAYER_GROUP_MAP.remove(player.getUniqueId());
	}
	private final void removeMemberInternalPersistent(OfflinePlayer player) {
		removeMemberInternal(player);
		writeMembersToConfig();
	}
	public final boolean removePlayer(OfflinePlayer player, boolean kick) {
		boolean wasLeader = isLeader(player);
		if (!wasLeader && !isMember(player)) return false;
		if (kick && wasLeader) return false;

		Component playerLeftMessage = null;

		if (wasLeader) {
			if (electNewLeader()) {
				removeMemberInternalPersistent(player);
			}
			else {
				disband();
				playerLeftMessage = Component.text("You have left the group. Since you were the leader and there are no more members, the group has been disbanded.");
			}
		} else {
			removeMemberInternalPersistent(player);

			if (kick) {
				playerLeftMessage = Component.text("You have been kicked from " + getLeader().getName() + "'s group.");
			}
		}

		notifyLeaveGroup(player);


		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer != null) {
			onlinePlayer.sendMessage(
				playerLeftMessage != null
					? playerLeftMessage
					: Component.text("You have left the group.")
			);
		}

		Component leaveMessage = Component.text(player.getName() + " has left the group.");
		for (Player member : getOnlinePlayers()) {
			member.sendMessage(leaveMessage);
		}

		return true;
	}

	public final JoinResult requestPlayerJoin(Player player, JoinMethod method) {
		if (player == null) return JoinResult.Failed;
		if (isLeader(player)) return JoinResult.Failed;
		if (isMember(player)) return JoinResult.Failed;
		if (method == null) return JoinResult.Failed;

		GroupJoinOption joinBehaviour = GroupConfigMap.GROUP_JOIN.getGroupConfig(this);

		JoinMethod currentMethod = joinRequests.get(player);
		boolean consentIsTwoSided = currentMethod != null && currentMethod != method;
		if (joinBehaviour == GroupJoinOption.auto_accept || consentIsTwoSided) {
			joinRequests.remove(player);
			addPlayer(player);
			return JoinResult.Succeeded;
		}

		OfflinePlayer leader = getLeader();
		switch (method) {
			case Join:
				Player onlineLeader = leader.getPlayer();
				if (onlineLeader == null) {
					Component offlineMessage = Component.text(leader.getName() + " is currently offline and cannot accept your request.");
					player.sendMessage(offlineMessage);
				}
				else {
					Component joinRequestMessage = Component.text(player.getName() + " has requested to join your group. Click ")
						.append(
							Component.text("Here")
								.color(NamedTextColor.GOLD)
								.clickEvent(
									ClickEvent.runCommand(String.format("/ludos:ludos group invite %s", player.getName()))
								)
						)
						.append(Component.text(" to accept."));
					onlineLeader.sendMessage(joinRequestMessage);
				}

				joinRequests.put(player, JoinMethod.Join);
				break;
			case Invite:
				Component invitationMessage = Component.text("You have been invited to join " + leader.getName() + "'s group. Click ")
					.append(
						Component.text("Here")
							.color(NamedTextColor.GOLD)
							.clickEvent(
								ClickEvent.runCommand(String.format("/ludos:ludos group join %s", leader.getName()))
							)
					)
					.append(Component.text(" to join."));
				player.sendMessage(invitationMessage);

				joinRequests.put(player, JoinMethod.Invite);
				break;
		}
		return JoinResult.Requested;
	}

	public static @NotNull Group deserialize(UUID id, @NotNull Map<String, Object> data, Ludos ludos) {
		Object leaderRaw = data.get("leader");
		if (!(leaderRaw instanceof String leaderName)) {
			throw new IllegalArgumentException("Invalid leader UUID in group data");
		}
		OfflinePlayer leader = Bukkit.getOfflinePlayer(UUID.fromString(leaderName));

		Object membersRaw = data.get("members");
		List<OfflinePlayer> members;
		if (membersRaw instanceof List<?> membersList) {
			members = membersList.stream()
				.filter(String.class::isInstance)
				.map(item -> Bukkit.getOfflinePlayer(UUID.fromString((String)item)))
				.collect(Collectors.toList());
		} else {
			members = new ArrayList<>();
		}

		Group newGroup = new Group(id, leader, members, ludos);
		initializeGroup(newGroup);

		return newGroup;
	}

	private void writeLeaderToConfig() {
		getConfigSection().set(LEADER_KEY, leaderId.toString());
	}
	private void writeMembersToConfig() {
		getConfigSection().set(MEMBERS_KEY, memberIds.stream()
			.map(UUID::toString)
			.collect(Collectors.toList())
		);
	}
	private void writeAllToConfig() {
		writeLeaderToConfig();
		writeMembersToConfig();
	}

	public ConfigurationSection getConfigSection() {
		return ludos.getGroupConfigSection(this);
	}
	public ConfigurationSection getConfig() {
		return ludos.getGroupScopedConfig(this);
	}
	public ConfigurationSection getGroupConfig() {
		return ludos.getGroupConfig(this);
	}
	public ConfigurationSection getGameConfig(Game.Builder game) {
		return ludos.getGroupGameConfig(this, game);
	}
	public ConfigurationSection getRoleConfig(Role.Builder role) {
		return ludos.getGroupRoleConfig(this, role);
	}
	public ConfigurationSection getPlayerConfig() {
		return ludos.getGroupPlayerConfig(this);
	}

	public final void removeConfigGroup() {
		FileConfiguration pluginConfig = ludos.getGroupsConfig();

		pluginConfig.set(
			id.toString(),
			null
		);
	}

	public static void loadConfigGroups(Ludos ludos) {
		ConfigurationSection configSection = ludos.getGroupsConfig();

		for (Map.Entry<String, Object> groupEntry : configSection.getValues(false).entrySet()) {
			try {
				UUID groupUuid = UUID.fromString(groupEntry.getKey());
				if (groupEntry.getValue() instanceof MemorySection groupData) {
					try {
						Group.deserialize(groupUuid, groupData.getValues(true), ludos);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				continue;
			}
		}
	}

	public Location pickReturnLocation() {
		Optional<Player> any = getOnlinePlayers().stream().filter(Player::isOnline).findFirst();
		if (any.isPresent()) return any.get().getLocation();
		return Bukkit.getWorlds().get(0).getSpawnLocation();
	}
}
