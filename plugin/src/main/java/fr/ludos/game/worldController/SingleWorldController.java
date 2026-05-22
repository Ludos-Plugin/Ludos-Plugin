package fr.ludos.game.worldController;

import org.bukkit.Location;

import fr.ludos.game.Game;
import fr.ludos.game.areaController.GameAreaController;
import fr.ludos.game.lobbyController.GameLobbyController;

public final class SingleWorldController extends GameWorldController {

	public SingleWorldController(Game game, GameLobbyController lobbyController, GameAreaController areaController, Location returnLocation) {
		super(game, lobbyController, areaController, returnLocation);
	}

	@Override
	protected void onWorldStop() {
		super.onWorldStop();
		if (getWorld() == null) return;

		scheduleFlushWorld(getWorld(), true);
	}
}
