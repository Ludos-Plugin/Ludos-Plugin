package fr.ludos.core.group;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.Ludos;
import fr.ludos.core.Utility;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.command.ludos.config.player.PlayerConfigMap;
import fr.ludos.core.game.Game;
import fr.ludos.core.role.Role;
import net.kyori.adventure.text.Component;

/**
 * Manager class for {@link Group}s, used to maintain the state of current Groups, for use in {@link Ludos}.
 */
public class GroupManager implements Listener {
	public static final String LEADER_KEY = "leader";
	public static final String MEMBERS_KEY = "members";

	private final Ludos ludos;

	private final File groupsFile;
	private final FileConfiguration groupsData;

	private final Set<Group> groups = new HashSet<>();
	private final Map<UUID, Group> playerGroupMap = new HashMap<>();


	public GroupManager(Ludos ludos) {
		this.ludos = Objects.requireNonNull(ludos);

		groupsFile = new File(ludos.getDataFolder(), "groups.yml");
		groupsData = YamlConfiguration.loadConfiguration(groupsFile);
	}

	public final Ludos getLudos() {
		return ludos;
	}

	public final Set<Group> getAllGroups() {
		return Collections.unmodifiableSet(groups);
	}



	public final Group createGroup(@NotNull OfflinePlayer leader, @Nullable Set<OfflinePlayer> members) {
		if (leader == null) {
			throw new IllegalArgumentException("Leader cannot be null");
		}

		Group oldGroup = getGroupOfPlayer(leader);
		if (oldGroup != null) {
			oldGroup.removePlayer(leader, false);
		}


		Group group = new Group(leader, members);
		addGroup(group);


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

	public void addGroup(Group group) {
		for (OfflinePlayer member : group.getMembers()) {
			setPlayerGroup(member, group);
		}
		OfflinePlayer leader = group.getLeader();
		if (leader != null) {
			setPlayerGroup(leader, group);
		}
		groups.add(group);

		group.groupManager = this;

		writeAllToConfig(group);
	}
	public void removeGroup(Group group) {
		for (OfflinePlayer member : group.getMembers()) {
			unsetPlayerGroup(member);
		}
		OfflinePlayer leader = group.getLeader();
		if (leader != null) {
			unsetPlayerGroup(leader);
		}
		groups.remove(group);

		FileConfiguration pluginConfig = getConfig();

		pluginConfig.set(
			group.getId().toString(),
			null
		);

		group.groupManager = null;
	}

	public @NotNull Group deserialize(UUID id, @NotNull Map<String, Object> data) {
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

		Group newGroup = new Group(id, leader, members);
		addGroup(newGroup);

		return newGroup;
	}

	public void writeLeaderToConfig(Group group) {
		getConfigSection(group).set(LEADER_KEY, group.getLeaderId().toString());
	}
	public void writeMembersToConfig(Group group) {
		getConfigSection(group).set(MEMBERS_KEY, group.getMemberIds().stream()
			.map(UUID::toString)
			.collect(Collectors.toList())
		);
	}
	public void writeAllToConfig(Group group) {
		writeLeaderToConfig(group);
		writeMembersToConfig(group);
	}

	public Group getGroupOfPlayer(@NotNull OfflinePlayer player) {
		return playerGroupMap.get(Objects.requireNonNull(player, "Player cannot be null").getUniqueId());
	}

	public void setPlayerGroup(@NotNull OfflinePlayer player, Group group) {
		playerGroupMap.put(Objects.requireNonNull(player, "Player cannot be null").getUniqueId(), group);
	}

	public void unsetPlayerGroup(@NotNull OfflinePlayer player) {
		playerGroupMap.remove(Objects.requireNonNull(player, "Player cannot be null").getUniqueId());
	}

	public final FileConfiguration getConfig() {
		return groupsData;
	}

	public ConfigurationSection getGlobalGroupConfig() {
		return Utility.getOrCreateConfigSection(getLudos().getConfig(), GroupConfigMap.INSTANCE.getNamespace());
	}
	public ConfigurationSection getConfigSection(Group group) {
		return Utility.getOrCreateConfigSection(groupsData, group.getId().toString());
	}
	public ConfigurationSection getScopedConfig(Group group) {
		return Utility.getOrCreateConfigSection(getConfigSection(group), "config");
	}
	public ConfigurationSection getScopedConfig(Group group, String path) {
		return Utility.getOrCreateConfigSection(getConfigSection(group), "config." + path);
	}
	public ConfigurationSection getGroupConfig(Group group) {
		return getScopedConfig(group, GroupConfigMap.INSTANCE.getNamespace());
	}
	public ConfigurationSection getGameConfig(Group group, Game.Builder game) {
		return getScopedConfig(group, game.getManager().configMap.getNamespace() + "." + game.getId());
	}
	public ConfigurationSection getRoleConfig(Group group, Role.Builder role) {
		return getScopedConfig(group, role.getManager().configMap.getNamespace() + "." + role.getId());
	}
	public ConfigurationSection getPlayerConfig(Group group) {
		return getScopedConfig(group, PlayerConfigMap.INSTANCE.getNamespace());
	}

	public void loadConfigGroups() {
		ConfigurationSection configSection = getConfig();

		for (Map.Entry<String, Object> groupEntry : configSection.getValues(false).entrySet()) {
			try {
				UUID groupUuid = UUID.fromString(groupEntry.getKey());
				if (groupEntry.getValue() instanceof MemorySection groupData) {
					try {
						deserialize(groupUuid, groupData.getValues(true));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				continue;
			}
		}
	}

	public void saveConfig() {
		try {
			groupsData.save(groupsFile);
		} catch (IOException ex) {
			getLudos().getLogger().log(Level.SEVERE, "Could not save groups to " + groupsFile, ex);
		}
	}


	@EventHandler
	public void onLeaderDisconnect(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Group group = getGroupOfPlayer(player);
		if (group == null) return;

		if (! group.isLeader(player)) return;

		group.demoteLeader();

		saveConfig();
	}
}