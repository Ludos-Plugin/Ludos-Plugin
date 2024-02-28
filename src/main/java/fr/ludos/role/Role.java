package fr.ludos.role;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import fr.ludos.Main;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;


/**
 * The Role class contains runtime data for the Role itself as well as the events for the Role-users
 * It contains events and Data.
 */
public abstract class Role implements Listener {

	private static final String rolesKey = "PlayerRoles";

	private static final Map<String, Builder> registered = new HashMap<String, Builder>();
	private static final Map<String, String> playerRoles = new HashMap<String, String>();


	public static Map<String, Builder> getRegistered() {
		return Role.registered;
	}
	public static Map<String, String> getPlayerRoles() {
		return Role.playerRoles;
	}

	/**
	 * The Builder class is used to configure a Role before it is initialized and serves as the data for the Role.
	 * It contains configuration for the Role itself.
	 */
	public Role(Builder builder) {
		Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
	}

	public void stop() {
		HandlerList.unregisterAll(this);
	}

	public static void loadConfigRoles() {
		Main main = Main.getInstance();

		Set<String> players = main.getConfig().getConfigurationSection(rolesKey).getKeys(false);
		for (String playerName : players) {
			if (playerName == null) {
				continue;
			}
			String roleId = main.getConfig().getString(rolesKey + '.' + playerName);
			playerRoles.put(playerName, roleId);
		}
	}

	public static void registerRole(Builder constructor) {
		Role.registered.put(constructor.getId(), constructor);
	}

	@Nullable
	public static Builder getRole(HumanEntity player) {
		return registered.getOrDefault(
			playerRoles.getOrDefault(player.getName(), ""), null);
	}

	public static boolean isPlayerRole(HumanEntity player, String role) {
		Builder currentRole = getRole(player);
		return (currentRole != null && currentRole.getId() == role);
	}

	public static void setRole(HumanEntity player, String roleId) {
		if ( playerRoles.containsKey(player.getName()) && playerRoles.get(player.getName()).equalsIgnoreCase(roleId) ) {
			return;
		}

		playerRoles.put(player.getName(), roleId);
		player.sendMessage("Your role is now " + roleId);
		
		Main main = Main.getInstance();
		main.getConfig().set(rolesKey + '.' + player.getName(), roleId);
		main.saveConfig();

	}

	public static void removeRole(Player player) {
		if ( ! playerRoles.containsKey(player.getName()) ) {
			return;
		}

		playerRoles.remove(player.getName());
		player.sendMessage("Your role is now randomly chosen");

		Main main = Main.getInstance();
		main.getConfig().set(rolesKey + '.' + player.getName(), null);
		main.saveConfig();
	}


	// public Role() {
	//     Bukkit.getPluginManager().registerEvents((Listener)this, Main.getInstance());
	// }


	/**
	 * The Builder class is used to configure a Role before it is initialized and serves as the data for the Role.
	 * It contains configuration for the Role itself.
	 */
	public static abstract class Builder {
		public abstract String getId();

		public abstract Role build(String gameId);
	}

}