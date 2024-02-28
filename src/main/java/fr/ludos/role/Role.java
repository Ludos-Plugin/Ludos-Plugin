package fr.ludos.role;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import fr.ludos.Main;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


/**
 * The Role class contains runtime data for the Role itself as well as the events for the Role-users
 * It contains events and Data.
 */
public abstract class Role implements Listener {

    public static final Map<String, Builder> registered = new HashMap<String, Builder>();
    public static final Map<String, String> playerRoles = new HashMap<String, String>();


    public static void registerRole(Builder constructor) {
        Role.registered.put(constructor.getId(), constructor);
    }

    /**
     * The Builder class is used to configure a Role before it is initialized and serves as the data for the Role.
     * It contains configuration for the Role itself.
     */
    public Role(Builder builder){
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    }

    public void stop() {
        HandlerList.unregisterAll(this);
    }

    @Nullable
    public static Builder getRole(Player player) {
        return registered.getOrDefault(
            playerRoles.getOrDefault(player.getName(), ""), null);
    }

    public static boolean isPlayerRole(Player player, String role) {
        Builder currentRole = getRole(player);
        return (currentRole != null && currentRole.getId() == role);
    }

    public static void setRole(Player player, String roleId) {
        if ( playerRoles.containsKey(player.getName()) && playerRoles.get(player.getName()).equalsIgnoreCase(roleId) ) {
            return;
        }

        playerRoles.put(player.getName(), roleId);
        player.sendMessage("Your role is now " + roleId);
    }

    public static void removeRole(Player player) {
        if ( ! playerRoles.containsKey(player.getName()) ) {
            return;
        }

        playerRoles.remove(player.getName());
        player.sendMessage("Your role is now randomly chosen");
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
