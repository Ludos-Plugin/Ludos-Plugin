package fr.ludos;

import java.net.http.WebSocket.Listener;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.command.GameCommand;
import fr.ludos.command.RoleCommand;
import fr.ludos.role.Role;
import fr.ludos.role.BurrowerRole;
import fr.ludos.role.HuntsmanRole;
import fr.ludos.role.TrapperRole;
import fr.ludos.game.Game;
import fr.ludos.game.manhunt.ManhuntGame;

public class Ludos extends JavaPlugin implements Listener {

	public static final String namespace = "ludos";

	private static Ludos instance;
	public static Ludos getInstance() {
		return instance;
	}


	@Override
	public void onEnable() {

		instance = this;
		Role.loadConfigRoles(this);


		Game.registerGame(new ManhuntGame.Builder(this));

		Role.registerRole(new HuntsmanRole.Builder());
		Role.registerRole(new BurrowerRole.Builder());
		Role.registerRole(new TrapperRole.Builder());


		PluginCommand cmd = getCommand("game");
		GameCommand gameCommand = new GameCommand();
		cmd.setExecutor(gameCommand);
		cmd.setTabCompleter(gameCommand);
		cmd.setUsage(gameCommand.getUsage());

		cmd = getCommand("role");
		RoleCommand roleCommand = new RoleCommand();
		cmd.setExecutor(roleCommand);
		cmd.setTabCompleter(roleCommand);
		cmd.setUsage(roleCommand.getUsage());
	}

	@Override
	public void onDisable() {
		Game.stopCurrentGame();
	}
}
