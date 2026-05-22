package fr.ludos.game;

import org.bukkit.plugin.java.JavaPlugin;

public class GameEvents extends GameProcessBase {
	public final Game game;
	public final Game getGame() {
		return game;
	}

	@Override
	protected JavaPlugin getPlugin() {
		return game.getPlugin();
	}

	public GameEvents(Game game) {
		this.game = game;
	}

}
