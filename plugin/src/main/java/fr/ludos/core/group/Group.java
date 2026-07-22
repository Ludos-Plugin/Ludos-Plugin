package fr.ludos.core.group;

import java.util.ArrayList;
import java.util.Collection;
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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.game.Game;
import fr.ludos.core.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * A structure to encapsulate a Group of Players. Meant for {@link Game} instances.
 */
public final class Group {
	GroupManager groupManager;
	private @Nullable Game game;

	private UUID id;

	private UUID leaderId;
	private final Set<UUID> memberIds;

	private Map<UUID, AddPlayerMethod> joinRequests = new HashMap<>();



	public GroupManager getManager() {
		return groupManager;
	}
	public final UUID getId() {
		return id;
	}
	public @Nullable Game getGame() {
		return game;
	}

	public final UUID getLeaderId() {
		return leaderId;
	}
	public final OfflinePlayer getLeader() {
		if (leaderId == null) return null;
		return Bukkit.getOfflinePlayer(leaderId);
	}
	public final Set<UUID> getMemberIds() {
		return memberIds.stream()
			.collect(Collectors.toSet());
	}
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

	public Map<UUID, AddPlayerMethod> getJoinRequests() {
		return this.joinRequests;
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


	public Group(UUID id, @NotNull OfflinePlayer leader, @Nullable Collection<OfflinePlayer> members) {
		this.id = id;
		this.leaderId = leader.getUniqueId();
		this.memberIds = members != null
			? members.stream()
				.map(OfflinePlayer::getUniqueId)
				.collect(Collectors.toCollection(HashSet::new))
			: new HashSet<>();
	}
	public Group(@NotNull OfflinePlayer leader, @Nullable Collection<OfflinePlayer> members) {
		this(UUID.randomUUID(), leader, members);
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

	public final void disband() {
		Game game = getGame();
		if (game != null) {
			game.stop();
		}

		getManager().removeGroup(this);

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
	}


	private boolean promoteToLeader(UUID newLeaderId) {
		UUID currentLeader = getLeaderId();

		this.leaderId = newLeaderId;
		memberIds.remove(newLeaderId);
		memberIds.add(currentLeader);
		getManager().writeAllToConfig(this);

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
	public boolean promoteToLeader(OfflinePlayer newLeader) {
		return promoteToLeader(newLeader.getUniqueId());
	}

	private final boolean electNewLeader() {
		Set<UUID> currentMemberIds = memberIds;
		if (currentMemberIds.isEmpty()) return false;

		UUID newLeaderId = currentMemberIds.iterator().next();

		return promoteToLeader(newLeaderId);
	}

	public final boolean addPlayer(OfflinePlayer player) {
		Player onlinePlayer = player.getPlayer();
		if (isPlayer(player)) return false;

		Group currentGroup = getManager().getGroupOfPlayer(player);
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
		getManager().setPlayerGroup(player, this);
	}
	private final void addMemberInternalPersistent(OfflinePlayer player) {
		addMemberInternal(player);
		getManager().writeMembersToConfig(this);
	}
	private final void removeMemberInternal(OfflinePlayer player) {
		memberIds.remove(player.getUniqueId());
		getManager().unsetPlayerGroup(player);
	}
	private final void removeMemberInternalPersistent(OfflinePlayer player) {
		removeMemberInternal(player);
		getManager().writeMembersToConfig(this);
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

		Component leaveMessage = kick
			? Component.text(player.getName() + " has been kicked from the group.")
			: Component.text(player.getName() + " has left the group.");
		for (Player member : getOnlinePlayers()) {
			member.sendMessage(leaveMessage);
		}

		return true;
	}

	public final AddPlayerResult requestAddPlayer(Player player, AddPlayerMethod method) {
		if (player == null) return AddPlayerResult.Failed;
		if (isLeader(player)) return AddPlayerResult.Failed;
		if (isMember(player)) return AddPlayerResult.Failed;
		if (method == null) return AddPlayerResult.Failed;

		GroupJoinOption joinBehaviour = GroupConfigMap.GROUP_JOIN.getGroupConfig(this);

		AddPlayerMethod currentMethod = joinRequests.get(player.getUniqueId());
		boolean consentIsTwoSided = currentMethod != null && currentMethod != method;
		if (joinBehaviour == GroupJoinOption.auto_accept || consentIsTwoSided) {
			joinRequests.remove(player.getUniqueId());
			addPlayer(player);
			return AddPlayerResult.Succeeded;
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

				joinRequests.put(player.getUniqueId(), AddPlayerMethod.Join);
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

				joinRequests.put(player.getUniqueId(), AddPlayerMethod.Invite);
				break;
		}
		return AddPlayerResult.Requested;
	}

	public ConfigurationSection getConfig() {
		return getManager().getConfigSection(this);
	}
	public ConfigurationSection getScopedConfig() {
		return getManager().getScopedConfig(this);
	}
	public ConfigurationSection getGroupConfig() {
		return getManager().getGroupConfig(this);
	}
	public ConfigurationSection getGameConfig(Game.Builder game) {
		return getManager().getGameConfig(this, game);
	}
	public ConfigurationSection getRoleConfig(Role.Builder role) {
		return getManager().getRoleConfig(this, role);
	}
	public ConfigurationSection getPlayerConfig() {
		return getManager().getPlayerConfig(this);
	}

	public Location pickReturnLocation() {
		Optional<Player> any = getOnlinePlayers().stream().filter(Player::isOnline).findFirst();
		if (any.isPresent()) return any.get().getLocation();
		return Bukkit.getWorlds().get(0).getSpawnLocation();
	}


	/**
	 * The method by which the request for a Player to be added to a Group was made.
	 */
	public enum AddPlayerMethod {
		Join,
		Invite
	}

	/**
	 * The result of a {@link Group#requestAddPlayer} operation.
	 */
	public enum AddPlayerResult {
		Succeeded,
		Requested,
		Failed
	}
}
