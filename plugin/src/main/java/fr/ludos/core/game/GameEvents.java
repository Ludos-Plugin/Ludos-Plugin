package fr.ludos.core.game;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * A {@link GameProcessBase} that is tied to a {@link Game}.
 */
public class GameEvents extends GameProcessBase {
	public final Game game;
	public final Game game() {
		return game;
	}

	@Override
	protected JavaPlugin plugin() {
		return game.plugin();
	}

	public GameEvents(Game game) {
		this.game = game;
	}

}
