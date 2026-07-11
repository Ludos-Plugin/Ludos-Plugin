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
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Group implements ConfigurationSerializable {
	public enum JoinMethod {
		Join,
		Invite
	}

	public enum JoinResult {
		Succeeded,
		Requested,
		Failed
	}

	private static final String groupsKey = "groups";
	static {
		ConfigurationSerialization.registerClass(Group.class);
	}

	private static final Set<Group> groups = new HashSet<>();
	public static final Set<Group> getGroups() {
		return Collections.unmodifiableSet(groups);
	}

	private static final Map<OfflinePlayer, Group> playerGroupMap = new HashMap<>();
	public static Group getGroupOfPlayer(@NotNull OfflinePlayer player) {
		return playerGroupMap.get(Objects.requireNonNull(player, "Player cannot be null"));
	}

	private final Ludos plugin;
	public Ludos getPlugin() {
		return plugin;
	}

	private ConfigurationSection config;
	public ConfigurationSection getConfig() {
		return config;
	}

	private OfflinePlayer leader;
	public final OfflinePlayer getLeader() {
		return leader;
	}

	private final Set<OfflinePlayer> members;
	public final Set<OfflinePlayer> getMembers() {
		return Collections.unmodifiableSet(members);
	}
	public final Set<Player> getOnlineMembers() {
		return members.stream()
			.map(OfflinePlayer::getPlayer)
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


	private Group(@NotNull OfflinePlayer leader, @NotNull Collection<OfflinePlayer> members, Ludos plugin, @Nullable ConfigurationSection config) {
		this.leader = leader;
		this.members = new HashSet<>(members);
		this.plugin = plugin;
		this.config = config == null ? new MemoryConfiguration() : config;
	}


	public final Set<OfflinePlayer> getPlayers() {
		Set<OfflinePlayer> allPlayers = new HashSet<>(members);
		if (leader != null) {
			allPlayers.add(leader);
		}
		return Collections.unmodifiableSet(allPlayers);
	}
	public final Set<Player> getOnlinePlayers() {
		Set<OfflinePlayer> allPlayers = new HashSet<>(members);
		if (leader != null) {
			allPlayers.add(leader);
		}

		return allPlayers.stream()
			.map(OfflinePlayer::getPlayer)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	public final boolean isLeader(OfflinePlayer player) {
		if (player == null) return false;
		return player.equals(leader);
	}
	public final boolean isMember(OfflinePlayer player) {
		if (player == null) return false;
		return members.contains(player);
	}

	public final boolean isPlayer(OfflinePlayer player) {
		if (player == null) return false;
		return player.equals(leader) || members.contains(player);
	}


	private static void initializeGroup(Group group) {
		for (OfflinePlayer member : group.getMembers()) {
			playerGroupMap.put(member, group);
		}
		OfflinePlayer leader = group.getLeader();
		if (leader != null) {
			playerGroupMap.put(leader, group);
		}
		groups.add(group);
	}
	private static void deinitializeGroup(Group group) {
		Game game = group.getGame();
		if (game != null) {
			game.stop();
		}
		for (OfflinePlayer member : group.getMembers()) {
			playerGroupMap.remove(member);
		}
		OfflinePlayer leader = group.getLeader();
		if (leader != null) {
			playerGroupMap.remove(leader);
		}
		groups.remove(group);
	}

	public final static Group createGroup(@NotNull OfflinePlayer leader, @Nullable Set<OfflinePlayer> members, Ludos plugin) {
		if (leader == null) {
			throw new IllegalArgumentException("Leader cannot be null");
		}

		Group oldGroup = getGroupOfPlayer(leader);
		if (oldGroup != null) {
			oldGroup.removePlayer(leader, false);
		}


		Group group = new Group(leader, members == null ? Collections.emptySet() : members, plugin, null);
		initializeGroup(group);

		group.saveConfigGroup();


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
		deinitializeGroup(this);

		removeConfigGroup();


		Component disbandMessage = Component.text("Your group has been disbanded.");

		OfflinePlayer leader = getLeader();
		Player onlineLeader = leader.getPlayer();
		if (onlineLeader != null) {
			onlineLeader.sendMessage(disbandMessage);
		}
		this.leader = null;

		for (OfflinePlayer member : getMembers()) {
			Player onlineMember = member.getPlayer();
			if (onlineMember != null) {
				onlineMember.sendMessage(disbandMessage);
			}
		}
		this.members.clear();
	}
	private final boolean electNewLeader() {
		Set<Player> onlineMembers = getOnlineMembers();
		if (onlineMembers.isEmpty()) return false;

		removeConfigGroup();

		Player newLeader = onlineMembers.iterator().next();
		this.leader = newLeader;
		members.remove(newLeader);
		saveConfigGroup();

		Component promotionMessage = Component.text("You have been promoted to group leader.");
		newLeader.sendMessage(promotionMessage);

		Component joinMessage = Component.text(newLeader.getName() + " has been promoted to group leader.");
		for (Player member : getOnlineMembers()) {
			member.sendMessage(joinMessage);
		}

		return true;
	}

	public final boolean demoteLeader() {
		OfflinePlayer oldLeader = getLeader();

		Component demoteMessage = null;
		if (electNewLeader()) {
			members.add(oldLeader);
			demoteMessage = Component.text(leader.getName() + " has been promoted to leader.");

			saveConfigGroup();
		}

		plugin.saveConfig();

		Player onlineLeader = oldLeader.getPlayer();
		if (onlineLeader != null && demoteMessage != null) {
			onlineLeader.sendMessage(demoteMessage);
		}

		return true;
	}

	public final void addPlayer(OfflinePlayer player) {
		Player onlinePlayer = player.getPlayer();
		if (isPlayer(player)) {

			if (onlinePlayer != null) {
				Component alreadyJoinedMessage = Component.text("You are already in this group.");
				onlinePlayer.sendMessage(alreadyJoinedMessage);
			}
			return;
		}

		Group currentGroup = getGroupOfPlayer(player);
		if (currentGroup != null) {
			currentGroup.removePlayer(player, false);
		}

		Component joinMessage = Component.text(player.getName() + " has joined the group.");
		for (Player member : getOnlinePlayers()) {
			member.sendMessage(joinMessage);
		}

		members.add(player);
		playerGroupMap.put(player, this);

		Component targetMessage = Component.text("You have joined " + leader.getName() + "'s group.");
		if (onlinePlayer != null) {
			onlinePlayer.sendMessage(targetMessage);
		}

		saveConfigGroup();

		notifyJoinGroup(player);
	}

	public final void removePlayer(OfflinePlayer player, boolean kick) {
		boolean wasLeader = isLeader(player);
		if (!wasLeader && !isMember(player)) return;
		if (kick && wasLeader) return;

		Component playerLeftMessage = null;

		if (wasLeader) {
			if (electNewLeader()) {
				members.remove(player);
				playerGroupMap.remove(player);
			}
			else {
				disband();
				playerLeftMessage = Component.text("You have left the group. Since you were the leader and there are no more members, the group has been disbanded.");
			}
		} else {
			members.remove(player);
			playerGroupMap.remove(player);

			saveConfigGroup();

			if (kick) {
				playerLeftMessage = Component.text("You have been kicked from " + leader.getName() + "'s group.");
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
	}

	public final JoinResult requestPlayerJoin(Player player, JoinMethod method) {
		if (player == null) return JoinResult.Failed;
		if (isLeader(player)) return JoinResult.Failed;
		if (isMember(player)) return JoinResult.Failed;
		if (method == null) return JoinResult.Failed;

		JoinMethod currentMethod = joinRequests.get(player);
		if (currentMethod != null && currentMethod != method) {
			joinRequests.remove(player);
			addPlayer(player);
			return JoinResult.Succeeded;
		}

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


	@Override
	public @NotNull Map<String, Object> serialize() {
		Map<String, Object> serialized = new HashMap<>();
		serialized.put("leader", leader.getUniqueId().toString());

		serialized.put("members", members.stream()
			.map(OfflinePlayer::getUniqueId)
			.map(UUID::toString)
			.collect(Collectors.toList())
		);

		serialized.put("config", config);

		return serialized;
	}

	public static @NotNull Group deserialize(@NotNull Map<String, Object> data, Ludos plugin) {
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

		Object configRaw = data.get("config");
		MemorySection configSection = configRaw instanceof MemorySection section ? section : null;

		Group newGroup = new Group(leader, members, plugin, configSection);
		initializeGroup(newGroup);

		return newGroup;
	}


	public final void removeConfigGroup() {
		FileConfiguration pluginConfig = plugin.getConfig();

		pluginConfig.set(
			groupsKey + "." + leader.getUniqueId().toString(),
			null
		);
	}

	public final void saveConfigGroup() {
		FileConfiguration pluginConfig = plugin.getConfig();

		pluginConfig.set(
			groupsKey + "." + leader.getUniqueId().toString(),
			serialize()
		);
	}

	public static void saveConfigGroups(JavaPlugin plugin) {
		FileConfiguration pluginConfig = plugin.getConfig();
		ConfigurationSection groupsSection = pluginConfig.createSection(groupsKey);
		for (Group group : groups) {
			groupsSection.set(
				group.leader.getUniqueId().toString(),
				group.serialize()
			);
		}
	}

	public static void loadConfigGroups(Ludos plugin) {
		ConfigurationSection configSection = plugin.getConfig();
		if (! configSection.isConfigurationSection(groupsKey)) {
			configSection.createSection(groupsKey);
		}
		ConfigurationSection groupsSection = configSection.getConfigurationSection(groupsKey);
		if (groupsSection == null) {
			return;
		}

		for (Map.Entry<String, Object> groupEntry : groupsSection.getValues(false).entrySet()) {
			if (groupEntry.getValue() instanceof MemorySection groupData) {
				try {
					Group.deserialize(groupData.getValues(true), plugin);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Location pickReturnLocation() {
		Optional<Player> any = getOnlinePlayers().stream().filter(Player::isOnline).findFirst();
		if (any.isPresent()) return any.get().getLocation();
		return Bukkit.getWorlds().get(0).getSpawnLocation();
	}
}
