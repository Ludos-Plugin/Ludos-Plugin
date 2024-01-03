package fr.ludos;

import java.net.http.WebSocket.Listener;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.command.PlayCommand;
import fr.ludos.command.RoleCommand;
import fr.ludos.command.MonsterCommand;
import fr.ludos.role.Role;
import fr.ludos.role.HuntsmanRole;
import fr.ludos.role.NecromancerRole;
import fr.ludos.role.StalkerRole;
import fr.ludos.games.Game;
import fr.ludos.games.ManhuntGame;
import fr.ludos.listener.InteractListener;
import fr.ludos.listener.MonsterListener;
import fr.ludos.listener.ServerListener;
import fr.ludos.listener.items.burrower.BurrowerPickEvents;
import fr.ludos.listener.items.SoulVial;
import fr.ludos.skill.VampiricLeechSkill;

/**
 * Main is the main class of the Bukkit plugin, responsible for handling plugin initialization and events.
 * It registers commands, listeners, and initializes game-related components.
 */

public class Main extends JavaPlugin implements Listener {

    public static final String namespace = "ludos";

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    /**
     * Called when the plugin is enabled. Registers commands, listeners, and initializes game-related components.
     */

    @Override
    public void onEnable() {

        instance = this;

        // Get the player to ignore from the server
        Player playerToIgnore = Bukkit.getPlayerExact("serdeau");

        if (playerToIgnore != null) {
            getServer().getPluginManager().registerEvents(new MonsterListener(playerToIgnore), this);
        } else {
            getLogger().warning("Unable to find the player to ignore.");
        }

        Bukkit.getPluginManager().registerEvents(new InteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ServerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BurrowerPickEvents(), this);

        Game.registerGame(new ManhuntGame.Builder());

        Role.registerRole(new HuntsmanRole.Builder());
        Role.registerRole(new NecromancerRole.Builder());
        Role.registerRole(new StalkerRole.Builder());



        PluginCommand cmd = getCommand("ludosplay");
        PlayCommand playCommand = new PlayCommand();
        cmd.setExecutor(playCommand);
        cmd.setTabCompleter(playCommand);
        cmd.setUsage(playCommand.getUsage());

        cmd = getCommand("ludosrole");
        RoleCommand roleCommand = new RoleCommand();
        cmd.setExecutor(roleCommand);
        cmd.setTabCompleter(roleCommand);
        cmd.setUsage(roleCommand.getUsage());



        cmd = getCommand("bomberzombie");
        MonsterCommand zombieBomberCommand = new MonsterCommand();
        cmd.setExecutor(zombieBomberCommand);


        Bukkit.getPluginManager().registerEvents(new VampiricLeechSkill(), this);
        Bukkit.getPluginManager().registerEvents(new SoulVial(), this);

    }

    /**
     * Called when the plugin is disabled. Currently empty.
     */

    @Override
    public void onDisable() {}

}
