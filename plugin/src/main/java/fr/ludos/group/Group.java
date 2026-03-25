package fr.ludos.group;

import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Group implements ConfigurationSerializable {
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

	private ConfigurationSection config;
	public ConfigurationSection getConfig() {
		return config;
	}

	private @Nullable Game game;
	public @Nullable Game getGame() {
		return game;
	}
	public void setGame(@Nullable Game game) {
		this.game = game;
	}


	private Group(@NotNull OfflinePlayer leader, @NotNull Collection<OfflinePlayer> members, @Nullable ConfigurationSection config) {
		this.leader = leader;
		this.members = new HashSet<>(members);
		this.config = config == null ? new MemoryConfiguration() : config;
	}


	public final Set<OfflinePlayer> getPlayers() {
		Set<OfflinePlayer> allPlayers = new HashSet<>(members);
		allPlayers.add(leader);
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

	public final static Group createGroup(@NotNull OfflinePlayer leader, @Nullable Set<OfflinePlayer> members) {
		if (leader == null) {
			throw new IllegalArgumentException("Leader cannot be null");
		}

		Group oldGroup = getGroupOfPlayer(leader);
		if (oldGroup != null) {
			oldGroup.removePlayer(leader);
		}


		Group group = new Group(leader, members == null ? Collections.emptySet() : members, null);
		initializeGroup(group);

		Ludos plugin = JavaPlugin.getPlugin(Ludos.class);
		Group.saveConfigGroup(plugin, group);


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

		Ludos plugin = JavaPlugin.getPlugin(Ludos.class);
		Group.removeConfigGroup(plugin, this);


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

	public final void addPlayer(OfflinePlayer player) {
		if (isLeader(player)) return;
		if (isMember(player)) return;

		Group currentGroup = getGroupOfPlayer(player);
		if (currentGroup != null) {
			currentGroup.removePlayer(player);
		}

		members.add(player);
		playerGroupMap.put(player, this);

		Ludos plugin = JavaPlugin.getPlugin(Ludos.class);
		Group.saveConfigGroup(plugin, this);


		Game game = getGame();
		if (game != null) {
			game.getTeamController().addPlayer(player);
		}


		Player onlinePlayer = player.getPlayer();
		Component targetMessage = Component.text("You have joined " + leader.getName() + "'s group.");
		if (onlinePlayer != null) {
			onlinePlayer.sendMessage(targetMessage);
		}

		Component joinMessage = Component.text(player.getName() + " has joined the group.");
		for (Player member : getOnlinePlayers()) {
			member.sendMessage(joinMessage);
		}
	}

	public final void removePlayer(OfflinePlayer player) {
		boolean wasLeader = isLeader(player);
		if (!wasLeader && !isMember(player)) return;

		Ludos plugin = JavaPlugin.getPlugin(Ludos.class);

		Component playerLeftMessage;
		if (wasLeader) {
			playerLeftMessage = Component.text("You have left your group.");

			if (members.isEmpty()) {
				disband();
			} else {
				OfflinePlayer newLeader = getMembers().iterator().next();

				members.remove(leader);
				leader = newLeader;

				Group.saveConfigGroup(plugin, this);

				Component newLeaderMessage = Component.text("The previous leader has left the group. You are now the new leader.");
				Player onlineNewLeader = newLeader.getPlayer();
				if (onlineNewLeader != null) {
					onlineNewLeader.sendMessage(newLeaderMessage);
				}
			}
		}
		else {
			members.remove(player);
			playerGroupMap.remove(player);

			Group.saveConfigGroup(plugin, this);

			playerLeftMessage = Component.text("You have left " + leader.getName() + "'s group.");
		}


		Player onlinePlayer = player.getPlayer();
		Game game = getGame();
		if (game != null && onlinePlayer != null) {
			game.getTeamController().removePlayer(onlinePlayer);
		}


		if (onlinePlayer != null) {
			onlinePlayer.sendMessage(playerLeftMessage);
		}

		Component leaveMessage = Component.text(player.getName() + " has left the group.");
		for (Player member : getOnlinePlayers()) {
			member.sendMessage(leaveMessage);
		}
	}

	public final void invitePlayer(Player player) {
		if (player == null) return;
		if (isLeader(player)) return;
		if (isMember(player)) return;

		Component invitationMessage = Component.text("You have been invited to join " + leader + "'s group. Click ")
			.append(
				Component.text("Here")
					.color(NamedTextColor.GOLD)
					.clickEvent(
						ClickEvent.runCommand(String.format("/ludos:ludos group join %s", leader))
					)
			)
			.append(Component.text(" to join."));
		player.sendMessage(invitationMessage);
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

	public static @NotNull Group deserialize(@NotNull Map<String, Object> data) {
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

		Group newGroup = new Group(leader, members, configSection);
		initializeGroup(newGroup);

		return newGroup;
	}


	public static void removeConfigGroup(Ludos plugin, Group group) {
		FileConfiguration pluginConfig = plugin.getConfig();

		pluginConfig.set(
			groupsKey + "." + group.leader.getUniqueId().toString(),
			null
		);
	}

	public static void saveConfigGroup(Ludos plugin, Group group) {
		FileConfiguration pluginConfig = plugin.getConfig();

		pluginConfig.set(
			groupsKey + "." + group.leader.getUniqueId().toString(),
			group.serialize()
		);
	}

	public static void saveConfigGroups(Ludos plugin) {
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
		ConfigurationSection groupsSection = plugin.getConfig().getConfigurationSection(groupsKey);
		if (groupsSection != null) {
			for (Map.Entry<String, Object> groupEntry : groupsSection.getValues(false).entrySet()) {
				if (groupEntry.getValue() instanceof MemorySection groupData) {
					try {
						Group group = Group.deserialize(groupData.getValues(true));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
