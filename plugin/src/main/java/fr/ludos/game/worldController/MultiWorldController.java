package fr.ludos.game.worldController;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import fr.ludos.game.Game;
import fr.ludos.game.areaController.GameAreaController;
import fr.ludos.game.lobbyController.GameLobbyController;

public final class MultiWorldController extends GameWorldController {
	public MultiWorldController(Game game, GameLobbyController lobbyController, GameAreaController areaController, Location returnLocation) {
		super(game, lobbyController, areaController, returnLocation);
	}

	public World transferToNewWorld(WorldCreator creator) {
		World oldWorld = super.transferToNewWorld(creator);
		scheduleFlushWorld(oldWorld, false);
		return oldWorld;
	}

	@Override
	protected void onWorldStop() {
		super.onStop();

		scheduleFlushWorld(getWorld(), true);
	}
}
