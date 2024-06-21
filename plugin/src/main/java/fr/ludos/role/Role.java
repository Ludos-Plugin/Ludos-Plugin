package fr.ludos.role;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import fr.ludos.Ludos;
import fr.ludos.game.Game;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;


/**
 * The Role class contains runtime data for the Role itself as well as the events for the Role-users
 * It contains events and Data.
 */
public abstract class Role implements Listener {

	private static final String rolesKey = "playerRoles";

	public static Map<String, Builder> getRegistered() {
		return registered;
	}
	private static Map<String, Builder> registered = new HashMap<String, Builder>();

	public static Map<String, String> getPlayerRoles() {
		return playerRoles;
	}
	private static Map<String, String> playerRoles = new HashMap<String, String>();


	/**
	 * The Builder class is used to configure a Role before it is initialized and serves as the data for the Role.
	 * It contains configuration for the Role itself.
	 */
	public Role(Builder builder, Game game) {
		Bukkit.getPluginManager().registerEvents(this, Ludos.getInstance());
	}

	public void stop() {
		HandlerList.unregisterAll(this);
	}


	public static void loadConfigRoles(Ludos plugin) {
		ConfigurationSection rolesSection = plugin.getConfig().getConfigurationSection(rolesKey);
		if (rolesSection != null) {
			playerRoles = rolesSection.getKeys(false).stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap((s) -> s, (s) -> plugin.getConfig().getString(rolesKey + '.' + s)));
		}
	}

	public static void registerRole(Builder constructor) {
		Role.registered.put(constructor.getId(), constructor);
	}

	public static List<Player> getPlayersOfRole(String roleId) {
		return Role.getPlayerRoles().entrySet().stream()
			.filter(entry -> (entry.getValue().equals(roleId)))
			.map(entry -> Bukkit.getPlayerExact(entry.getKey()))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	@Nullable
	public static Builder getRole(HumanEntity player) {
		return registered.getOrDefault(playerRoles.getOrDefault(player.getName(), ""), null);
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

		Ludos main = Ludos.getInstance();
		main.getConfig().set(rolesKey + '.' + player.getName(), roleId);
		main.saveConfig();

	}

	public static void removeRole(Player player) {
		if ( ! playerRoles.containsKey(player.getName()) ) {
			return;
		}

		playerRoles.remove(player.getName());
		player.sendMessage("Your role is now randomly chosen");

		Ludos main = Ludos.getInstance();
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

		public abstract Role build(Game.Builder builder, Game game);
	}
}