package fr.ludos.core.role;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.ludos.config.role.RoleConfigMap;
import fr.ludos.core.group.Group;

/**
 * Manager class for {@link Role}s, used to maintain a registry of roles for use in {@link Ludos}.
 */
public final class RoleManager {
	private final Ludos ludos;
	private Map<String, Role.Builder> registered = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private Map<UUID, String> playerRoles = new HashMap<UUID, String>();

	public final RoleConfigMap configMap = new RoleConfigMap(this);


	public RoleManager(Ludos ludos) {
		this.ludos = ludos;
	}


	public Ludos getLudos() {
		return ludos;
	}

	public Map<String, Role.Builder> getRegistered() {
		return registered;
	}

	public void registerRole(Role.Builder role) {
		registered.put(role.getId().toLowerCase(), role);
	}

	public List<String> getRoleIds() {
		return registered.keySet().stream().collect( Collectors.toList() );
	}
	public List<Role.Builder> getBuilders() {
		return registered.values().stream().collect( Collectors.toList() );
	}

	@Nullable
	public Role.Builder getRoleById(String roleId) {
		if (roleId == null) return null;
		return registered.getOrDefault(roleId, null);
	}

	@Nullable
	public Role.Builder getPlayerRole(OfflinePlayer player) {
		String roleId = getPlayerRoleId(player);
		return getRoleById(roleId);
	}


	public void loadConfigRoles() {
		playerRoles = ludos.getPlayersConfig().getKeys(false).stream()
			.filter(Objects::nonNull)
			.map(s -> {
				OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(s));
				String roleId = ludos.getPlayerConfigSection(player).getString(Role.NAMESPACE);
				return Pair.of(player, roleId);
			})
			.filter(p -> p.getKey() != null && p.getValue() != null)
			.collect(Collectors.toMap(
				(p) -> p.getKey().getUniqueId(),
				(p) -> {
					String roleId = p.getValue();
					ludos.getLogger().info("Loaded Role of Player : " + p.getKey() + " | Role ID : " + roleId);
					return roleId;
				}
			));
	}

	public final boolean isAuthorizedToEditRole(ServerOperator operator, OfflinePlayer target) {
		if (operator.isOp() || operator == target) {
			return true;
		}

		if (operator instanceof Player player) {
			final Group group = ludos.getGroupManager().getGroupOfPlayer(player);
			if (group != null && group.isLeader(player) && group == ludos.getGroupManager().getGroupOfPlayer(target)) {
				return true;
			}
		}

		return false;
	}

	public List<Player> getPlayersOfRole(String roleId) {
		return getPlayerRoles().entrySet().stream()
			.filter(entry -> (entry.getValue().equals(roleId)))
			.map(entry -> Bukkit.getPlayer(entry.getKey()))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	@Nullable
	public String getPlayerRoleId(OfflinePlayer player) {
		return playerRoles.getOrDefault(player.getUniqueId(), null);
	}

	public boolean isPlayerRole(OfflinePlayer player, String roleId) {
		String playerRoleId = getPlayerRoleId(player);
		if (roleId == playerRoleId) return true;
		else if (roleId == null || playerRoleId == null) return false;
		return roleId.equalsIgnoreCase(playerRoleId);
	}

	public Predicate<OfflinePlayer> ofRole(String id) {
		return (OfflinePlayer p) -> isPlayerRole(p, id);
	}


	public Map<UUID, String> getPlayerRoles() {
		return playerRoles;
	}

	public void setRole(OfflinePlayer player, String roleId) {
		UUID playerUUID = player.getUniqueId();
		if ( playerRoles.containsKey(playerUUID) && playerRoles.get(playerUUID).equalsIgnoreCase(roleId) ) return;

		Role.Builder role = getRegistered().get(roleId);
		if (role == null) return;

		playerRoles.put(playerUUID, roleId);

		ludos.getPlayerConfigSection(player).set(Role.NAMESPACE, roleId);
		ludos.savePlayersConfig();

		Player online = player.getPlayer();
		if (online != null) {
			online.sendMessage("Your role is now " + roleId);
		}
	}

	public void unsetRole(OfflinePlayer player) {
		UUID playerUUID = player.getUniqueId();
		if ( ! playerRoles.containsKey(playerUUID) ) return;

		Role.Builder role = getRegistered().get(playerRoles.get(playerUUID));
		if (role == null) return;

		playerRoles.remove(playerUUID);
		ludos.getPlayerConfigSection(player).set(Role.NAMESPACE, null);
		ludos.savePlayersConfig();

		Player online = player.getPlayer();
		if (online != null) {
			online.sendMessage("You now have no role");
		}
	}
}
