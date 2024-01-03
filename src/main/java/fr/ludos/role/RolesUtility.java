package fr.ludos.role;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

/**
 * RolesUtility class is a Bukkit plugin that manages player name tags on the server.
 * It provides the ability to remove name tags for all online players, enhancing a "No Name Tag" experience.
 */

public class RolesUtility extends JavaPlugin implements Listener {

    /** The server instance. */
    static Server server;

    /** The console command sender instance. */
    static ConsoleCommandSender console;

    /** The scoreboard manager instance. */
    ScoreboardManager sbm;

    /** The team for players with no name tags. */
    Team NoNameTagTeam;

    /**
     * Called when the plugin is enabled. Registers events, initializes components, and removes name tags for online players.
     */

    @Override
    public void onEnable() {
        Bukkit.broadcastMessage("Started");
        server = this.getServer();
        server.getPluginManager().registerEvents(this, this);
        sbm = server.getScoreboardManager();
        console = server.getConsoleSender();
        console.sendMessage(ChatColor.GREEN + "" + ChatColor.ITALIC + "NoNameTag Loaded");

        // Remove name tags for all online players
        for (Player player : server.getOnlinePlayers()) {
            removeNameTag(player.getDisplayName());
        }
    }

    /** Called when the plugin is disabled. Displays a message and logs the plugin's shutdown. */

    @Override
    public void onDisable() {
        Bukkit.broadcastMessage("Ended");
        console.sendMessage(ChatColor.RED + "NoNameTag Disabled");
    }

    /**
     * Removes the name tag for the specified player.
     *
     * @param playerName The name of the player whose name tag will be removed.
     */

    public void removeNameTag(String playerName) {
        if (!teamExists()) {
            makeTeam();
        }
        NoNameTagTeam.addEntry(playerName);
    }

    /**
     * Checks if the NoNameTagTeam exists on the main scoreboard.
     *
     * @return true if the team exists, false otherwise.
     */

    public boolean teamExists() {
        return sbm.getMainScoreboard().getTeam("NoNameTagTeam") != null;
    }

    /** Creates the NoNameTagTeam if it does not exist and configures its options. */
    
    public void makeTeam() {
        sbm.getMainScoreboard().registerNewTeam("NoNameTagTeam");
        NoNameTagTeam = sbm.getMainScoreboard().getTeam("NoNameTagTeam");
        NoNameTagTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
    }
}
