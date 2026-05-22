package fr.ludos.game.worldController;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import fr.ludos.game.Game;
import fr.ludos.game.areaController.GameAreaController;
import fr.ludos.game.lobbyController.GameLobbyController;

public final class MultiWorldController extends GameWorldController {
	private World flushedWorld;

	public MultiWorldController(Game game, GameLobbyController lobbyController, GameAreaController areaController, Location returnLocation) {
		super(game, lobbyController, areaController, returnLocation);
	}

	public World transferToNewWorld(WorldCreator creator) {
		World oldWorld = super.transferToNewWorld(creator);
		// If we have a previous temporary world that is different from the one
		// we just returned, schedule it for flushing. Then remember the last
		// returned world so we don't attempt to flush the same world twice.
		if (oldWorld != null && oldWorld != flushedWorld) {
			retryFlushWorld(false, oldWorld);
		}
		flushedWorld = oldWorld;
		return oldWorld;
	}

	@Override
	protected void onSetdown() {
		retryFlushWorld(true, getWorld());
	}
}
