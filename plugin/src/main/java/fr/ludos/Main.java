package fr.ludos;

import java.net.http.WebSocket.Listener;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.command.PlayCommand;
import fr.ludos.command.RoleCommand;
import fr.ludos.command.MonsterCommand;
import fr.ludos.role.Role;
import fr.ludos.role.BurrowerRole;
import fr.ludos.role.HuntsmanRole;
import fr.ludos.role.NecromancerRole;
import fr.ludos.role.StalkerRole;
import fr.ludos.game.Game;
import fr.ludos.game.manhunt.ManhuntGame;
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
		Role.loadConfigRoles();


		Game.registerGame(new ManhuntGame.Builder());

		Role.registerRole(new HuntsmanRole.Builder());
		Role.registerRole(new NecromancerRole.Builder());
		Role.registerRole(new StalkerRole.Builder());
		Role.registerRole(new BurrowerRole.Builder());


		PluginCommand cmd = getCommand("play");
		PlayCommand playCommand = new PlayCommand();
		cmd.setExecutor(playCommand);
		cmd.setTabCompleter(playCommand);
		cmd.setUsage(playCommand.getUsage());

		cmd = getCommand("role");
		RoleCommand roleCommand = new RoleCommand();
		cmd.setExecutor(roleCommand);
		cmd.setTabCompleter(roleCommand);
		cmd.setUsage(roleCommand.getUsage());



		cmd = getCommand("bomberzombie");
		MonsterCommand zombieBomberCommand = new MonsterCommand();
		cmd.setExecutor(zombieBomberCommand);
	}

	@Override
	public void onDisable() {
		Game.stopGame();
	}

}
