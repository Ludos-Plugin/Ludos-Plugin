package fr.ludos.group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
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

	private static final Map<String, Group> playerGroupMap = new HashMap<>();
	public static Group getGroupOfPlayer(@NotNull Player player) {
		return playerGroupMap.get(player.getName());
	}

	private @Nullable String leaderName;
	public @Nullable String getLeaderName() {
		return leaderName;
	}
	public final @Nullable Player getLeader() {
		if (leaderName == null) return null;
		return Bukkit.getPlayer(leaderName);
	}

	private final Set<String> memberNames;
	public final Set<String> getMemberNames() {
		return Collections.unmodifiableSet(memberNames);
	}
	public final Set<Player> getMembers() {
		return memberNames.stream()
			.map((name) -> name != null ? Bukkit.getPlayer(name) : null)
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


	private Group(@NotNull String leaderName, @NotNull Set<String> memberNames, @Nullable ConfigurationSection config) {
		this.leaderName = leaderName;
		this.memberNames = new HashSet<>(memberNames);

		Bukkit.getLogger().info("Creating group with leader " + this.leaderName + " and members " + memberNames.stream()
			.collect(Collectors.joining(", ")));

		this.config = config == null ? new MemoryConfiguration() : config;
	}
	private Group(@NotNull Player leader, @NotNull Set<Player> members, @Nullable ConfigurationSection config) {
		this(leader.getName(), members.stream().map(Player::getName).collect(Collectors.toSet()), config);
	}


	public final Set<Player> getAllPlayers() {
		Set<String> allPlayers = new HashSet<>(memberNames);
		allPlayers.add(leaderName);
		return allPlayers.stream()
			.map(Bukkit::getPlayer)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	public final boolean isLeader(Player player) {
		if (player == null) return false;
		return player.getName().equals(leaderName);
	}
	public final boolean isMember(Player player) {
		if (player == null) return false;
		return memberNames.contains(player.getName());
	}


	private static void initializeGroup(Group group) {
		for (String playerName : group.getMemberNames()) {
			playerGroupMap.put(playerName, group);
		}
		String leaderName = group.getLeaderName();
		if (leaderName != null) {
			playerGroupMap.put(leaderName, group);
		}
		groups.add(group);
	}
	private static void deinitializeGroup(Group group) {
		Game game = group.getGame();
		if (game != null) {
			game.stop();
		}
		for (String playerName : group.getMemberNames()) {
			playerGroupMap.remove(playerName);
		}
		String leaderName = group.getLeaderName();
		if (leaderName != null) {
			playerGroupMap.remove(leaderName);
		}
		groups.remove(group);
	}

	public final static Group createGroup(@NotNull Player leader, @Nullable Set<Player> members) {
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
		leader.sendMessage(creationMessage);

		Component joinMessage = Component.text("You have joined " + leader.getName() + "'s group.");
		for (Player member : group.getMembers()) {
			member.sendMessage(joinMessage);
		}

		return group;
	}

	public final void disband() {
		deinitializeGroup(this);

		Ludos plugin = JavaPlugin.getPlugin(Ludos.class);
		Group.removeConfigGroup(plugin, this);


		Component disbandMessage = Component.text("Your group has been disbanded.");

		Player leader = getLeader();
		if (leader != null) {
			leader.sendMessage(disbandMessage);
		}
		this.leaderName = null;

		for (Player member : getMembers()) {
			member.sendMessage(disbandMessage);
		}
		this.memberNames.clear();
	}

	public final void addPlayer(Player player) {
		if (isLeader(player)) return;
		if (isMember(player)) return;

		Group currentGroup = getGroupOfPlayer(player);
		if (currentGroup != null) {
			currentGroup.removePlayer(player);
		}

		String playerName = player.getName();

		memberNames.add(playerName);
		playerGroupMap.put(playerName, this);

		Ludos plugin = JavaPlugin.getPlugin(Ludos.class);
		Group.saveConfigGroup(plugin, this);


		Game game = getGame();
		if (game != null) {
			game.getTeamController().addPlayer(player);
		}


		Component joinMessage = Component.text(playerName + " has joined the group.");
		for (Player member : getAllPlayers()) {
			member.sendMessage(joinMessage);
		}

		Component targetMessage = Component.text("You have joined " + leaderName + "'s group.");
		if (leaderName != null) {
			player.sendMessage(targetMessage);
		}
	}

	public final void removePlayer(Player player) {
		boolean wasLeader = isLeader(player);
		if (!wasLeader && !isMember(player)) return;

		String playerName = player.getName();

		Ludos plugin = JavaPlugin.getPlugin(Ludos.class);

		if (wasLeader) {
			Component leftOwnGroupMessage = Component.text("You have left your group.");
			player.sendMessage(leftOwnGroupMessage);

			if (memberNames.isEmpty()) {
				disband();
			} else {
				Player newLeader = getMembers().iterator().next();

				memberNames.remove(leaderName);
				leaderName = newLeader.getName();

				Group.saveConfigGroup(plugin, this);

				Component newLeaderMessage = Component.text("The previous leader has left the group. You are now the new leader.");
				newLeader.sendMessage(newLeaderMessage);
			}
		}
		else {
			memberNames.remove(playerName);
			playerGroupMap.remove(playerName);

			Group.saveConfigGroup(plugin, this);

			Component leftGroupMessage = Component.text("You have left " + leaderName + "'s group.");
			player.sendMessage(leftGroupMessage);
		}


		Game game = getGame();
		if (game != null) {
			game.getTeamController().removePlayer(player);
		}

		Component leaveMessage = Component.text(playerName + " has left the group.");
		for (Player member : getMembers()) {
			member.sendMessage(leaveMessage);
		}
	}

	public final void invitePlayer(Player player) {
		if (player == null) return;
		if (isLeader(player)) return;
		if (isMember(player)) return;

		Component invitationMessage = Component.text("You have been invited to join " + leaderName + "'s group. Click ")
			.append(
				Component.text("Here")
					.color(NamedTextColor.GOLD)
					.clickEvent(
						ClickEvent.runCommand(String.format("/ludos:ludos group join %s", leaderName))
					)
			)
			.append(Component.text(" to join."));
		player.sendMessage(invitationMessage);
	}


	@Override
	public @NotNull Map<String, Object> serialize() {
		Map<String, Object> serialized = new HashMap<>();
		serialized.put("leader", leaderName);

		serialized.put("members", new ArrayList<>(memberNames));

		serialized.put("config", config);

		return serialized;
	}

	public static @NotNull Group deserialize(@NotNull Map<String, Object> data) {
		Object leaderRaw = data.get("leader");
		if (!(leaderRaw instanceof String leaderName)) {
			throw new IllegalArgumentException("Missing or invalid 'leader' in serialized Group");
		}

		Set<String> memberNames = new HashSet<>();
		Object membersRaw = data.get("members");
		if (membersRaw instanceof Iterable<?> iterable) {
			memberNames.addAll(
				StreamSupport.stream(iterable.spliterator(), false)
					.filter(String.class::isInstance)
					.map(String.class::cast)
					.collect(Collectors.toSet())
			);
		}

		Object configRaw = data.get("config");
		MemorySection configSection = configRaw instanceof MemorySection section ? section : null;

		Group newGroup = new Group(leaderName, memberNames, configSection);
		initializeGroup(newGroup);

		return newGroup;
	}


	public static void removeConfigGroup(Ludos plugin, Group group) {
		FileConfiguration pluginConfig = plugin.getConfig();

		pluginConfig.set(
			groupsKey + "." + group.leaderName,
			null
		);
	}

	public static void saveConfigGroup(Ludos plugin, Group group) {
		FileConfiguration pluginConfig = plugin.getConfig();

		pluginConfig.set(
			groupsKey + "." + group.leaderName,
			group.serialize()
		);
	}

	public static void saveConfigGroups(Ludos plugin) {
		FileConfiguration pluginConfig = plugin.getConfig();
		ConfigurationSection groupsSection = pluginConfig.createSection(groupsKey);
		for (Group group : groups) {
			groupsSection.set(
				group.leaderName,
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
