package fr.ludos.games.noname;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameProcessBase;

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

	public void spawnGenerator() {
		Location loc = new Location(Bukkit.getWorlds().get(0), 0, 80, 0);
		Block block = loc.getBlock();
		block.setType(Material.COMMAND_BLOCK);
	}

	@EventHandler
    public void onBlockClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() == Material.COMMAND_BLOCK) {
            event.setCancelled(true); 
            openGeneratorGui(event.getPlayer());
        }
    }

    private void openGeneratorGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "Generator Menu");
        gui.setItem(13, new ItemStack(Material.DIAMOND));
        player.openInventory(gui);
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
