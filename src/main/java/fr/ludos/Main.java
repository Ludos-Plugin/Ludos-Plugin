/**
 * Main is the main class of the Bukkit plugin, responsible for handling plugin initialization and events.
 * It registers commands, listeners, and initializes game-related components.
 */

package fr.ludos;

import java.net.http.WebSocket.Listener;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.command.MainCommand;
import fr.ludos.command.MonsterCommand;
import fr.ludos.games.Game;
import fr.ludos.games.ManhuntGame;
import fr.ludos.listener.InteractListener;
import fr.ludos.listener.MonsterTargetListener;
import fr.ludos.listener.items.SoulVial;
// import fr.ludos.roles.NecromancerRole;
import fr.ludos.skills.VampiricLeechSkill;

public class Main extends JavaPlugin implements Listener {

    /**
     * Called when the plugin is enabled. Registers commands, listeners, and initializes game-related components.
     */

    @Override
    public void onEnable() {

        // Get the player to ignore from the server
        Player playerToIgnore = Bukkit.getPlayerExact("serdeau");

        if (playerToIgnore != null) {
            getServer().getPluginManager().registerEvents(new MonsterTargetListener(playerToIgnore), this);
        } else {
            getLogger().warning("Unable to find the player to ignore.");
        }

        getCommand("bomberzombie").setExecutor(new MonsterCommand());

        Bukkit.getPluginManager().registerEvents(new InteractListener(this), this);

        Game.RegisterGame(new ManhuntGame.Builder());

        // Register the playludos command
        PluginCommand playCmd = getCommand("playludos");
        MainCommand command = new MainCommand();
        playCmd.setExecutor(command);
        playCmd.setTabCompleter(command);
        playCmd.setUsage(command.getUsage());


        // Main pluginInstance = this;
        // NecromancerRole necromancerRole = new NecromancerRole(pluginInstance);
        Bukkit.getPluginManager().registerEvents(new VampiricLeechSkill(), this);
        Bukkit.getPluginManager().registerEvents(new SoulVial(), this);

    }

    /**
     * Called when the plugin is disabled. Currently empty.
     */

    @Override
    public void onDisable() {}

}
