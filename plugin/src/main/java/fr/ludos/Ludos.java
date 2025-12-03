package fr.ludos;

import java.net.http.WebSocket.Listener;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.command.ludos.LudosCommand;
import fr.ludos.game.Game;
import fr.ludos.game.manhunt.ManhuntGame;
import fr.ludos.game.sheepwars.SheepwarsGame;
import fr.ludos.role.Role;
import fr.ludos.role.BurrowerRole;
import fr.ludos.role.HuntsmanRole;
import fr.ludos.role.TrapperRole;
import fr.ludos.item.texture.TextureManager;
import fr.ludos.item.texture.TextureListener;


public class Ludos extends JavaPlugin implements Listener {

	public static final String namespace = "ludos";
	private TextureManager textureManager;

	public Ludos() { }
	
	public TextureManager getTextureManager() {
		return textureManager;
	}

	@Override
	public void onEnable() {
		Role.loadConfigRoles(this);


		Game.registerGame(new ManhuntGame.Builder(this));
		Game.registerGame(new SheepwarsGame.Builder(this));

		Role.registerRole(new HuntsmanRole.Builder(this));
		Role.registerRole(new BurrowerRole.Builder(this));
		Role.registerRole(new TrapperRole.Builder(this));

		textureManager = new TextureManager(this);
		
		getServer().getPluginManager().registerEvents(new TextureListener(this), this);

		PluginCommand cmd = getCommand("ludos");
		LudosCommand ludosCommand = new LudosCommand(this);
		cmd.setExecutor(ludosCommand);
		cmd.setTabCompleter(ludosCommand);

		//pour l'instant garder c'est pour savoir l'indexation des texture à chaque items, customiser
		PluginCommand textureCmd = getCommand("texture");

		if (textureCmd != null) {
			textureCmd.setExecutor(textureManager);
		}
	}

	@Override
	public void onDisable() {
		Game.stopCurrentGame();
	}
}
