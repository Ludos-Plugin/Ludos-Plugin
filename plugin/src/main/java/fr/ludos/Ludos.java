package fr.ludos;

import java.net.http.WebSocket.Listener;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.command.GameCommand;
import fr.ludos.command.RoleCommand;
import fr.ludos.game.Game;
import fr.ludos.game.manhunt.ManhuntGame;
import fr.ludos.role.Role;
import fr.ludos.role.BurrowerRole;
import fr.ludos.role.HuntsmanRole;
import fr.ludos.role.TrapperRole;


public class Ludos extends JavaPlugin implements Listener {

	public static final String namespace = "ludos";

	public Ludos() {
	}

	@Override
	public void onEnable() {
		Role.loadConfigRoles(this);


		Game.registerGame(new ManhuntGame.Builder(this));

		Role.registerRole(new HuntsmanRole.Builder(this));
		Role.registerRole(new BurrowerRole.Builder(this));
		Role.registerRole(new TrapperRole.Builder(this));


		PluginCommand cmd = getCommand("game");
		GameCommand gameCommand = new GameCommand();
		cmd.setExecutor(gameCommand);
		cmd.setTabCompleter(gameCommand);
		cmd.setUsage(gameCommand.getUsage());

		cmd = getCommand("role");
		RoleCommand roleCommand = new RoleCommand(this);
		cmd.setExecutor(roleCommand);
		cmd.setTabCompleter(roleCommand);
		cmd.setUsage(roleCommand.getUsage());
	}

	@Override
	public void onDisable() {
		Game.stopCurrentGame();
	}
}
