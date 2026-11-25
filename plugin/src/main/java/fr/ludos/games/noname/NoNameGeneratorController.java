package fr.ludos.games.noname;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameProcessBase;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.lobby.Lobby.ClearMode;
import fr.ludos.core.wave.WaveLoadoutService;
import net.kyori.adventure.text.Component;

public class NoNameGeneratorController extends GameProcessBase {
    private final NoNameGame game;
    @Nullable
	//private final WaveLoadoutService loadout;

	public NoNameGeneratorController(NoNameGame game) {
		this.game = game;
	}

    private final Game game() {
        return this.game;
    }

    // public void applyLoadout(Player player) {
	// 	if (loadout != null) {
	// 		loadout.applyBaseKit(player);
	// 	}
	// 	SpecialItem.Events.refreshPlayerInventory(game, player);
	// }

	// @Override
	// protected final void onStart() {
	// 	super.onStart();
	// 	resume();

	// 	for (Player player : game.group().getOnlinePlayers()) {
	// 		bossbar.addPlayer(player);
	// 	}
	// 	bossbar.setVisible(true);
	// }

    @Override
	protected JavaPlugin plugin() {
		return game.plugin();
	}

	@Override
	protected void onStop() {
		super.onStop();
		game().stop();
	}
}
