package fr.ludos;

import java.net.http.WebSocket.Listener;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.command.PlayCommand;
import fr.ludos.command.RoleCommand;
import fr.ludos.command.MonsterCommand;
import fr.ludos.role.Role;
import fr.ludos.role.BurrowerRole;
import fr.ludos.role.HuntsmanRole;
import fr.ludos.role.NecromancerRole;
import fr.ludos.role.StalkerRole;
import fr.ludos.games.Game;
import fr.ludos.games.ManhuntGame;
import fr.ludos.listener.InteractListener;
import fr.ludos.listener.ServerListener;
import fr.ludos.item.burrower.BurrowerPickEvents;
import fr.ludos.recipe.ArrowRecipe;
import fr.ludos.recipe.BerriesRecipe;
import fr.ludos.recipe.EnchantementTableRecipe;
import fr.ludos.recipe.GoldenAppleRecipe;
import fr.ludos.recipe.Recipe;
import fr.ludos.item.SoulVial;
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

		PluginManager manager = Bukkit.getPluginManager();


        manager.registerEvents(new InteractListener(this), this);
        manager.registerEvents(new ServerListener(this), this);

        Game.registerGame(new ManhuntGame.Builder());

        Role.registerRole(new HuntsmanRole.Builder());
        Role.registerRole(new NecromancerRole.Builder());
        Role.registerRole(new StalkerRole.Builder());
        Role.registerRole(new BurrowerRole.Builder());
        

		// Recipe.RegisterRecipe(new ArrowRecipe(this));
		// Recipe.RegisterRecipe(new BerriesRecipe(this));
		// Recipe.RegisterRecipe(new EnchantementTableRecipe(this));
		// Recipe.RegisterRecipe(new GoldenAppleRecipe(this));

        manager.registerEvents(new BurrowerPickEvents(), this);
        manager.registerEvents(new HuntsmanRole(), this);


        manager.registerEvents(new VampiricLeechSkill(), this);
        manager.registerEvents(new SoulVial(), this);



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

    }

    /**
     * Called when the plugin is disabled. Currently empty.
     */

    @Override
    public void onDisable() {}

}
