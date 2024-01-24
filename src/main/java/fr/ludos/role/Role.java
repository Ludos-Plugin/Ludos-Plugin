package fr.ludos.role;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

public abstract class Role implements Listener {

    public static final Map<String, Builder> registered = new HashMap<String, Builder>();
    public static final Map<String, String> playerRoles = new HashMap<String, String>();


    public static void registerRole(Builder constructor) {
        Role.registered.put(constructor.getId(), constructor);
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


    public static abstract class Builder {
        public abstract String getId();
    }

}
