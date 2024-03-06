package fr.ludos.game.manhunt;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.ludos.item.SpecialItemEvents;

public class ManhuntCompassEvents extends SpecialItemEvents<ManhuntCompass> {

	@Override
	@Nullable
	protected ManhuntCompass getItem(ItemStack stack) {
		try {
			// ManhuntCompass bow = new ManhuntCompass(stack);
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	@Override
	protected ManhuntCompass createItem(Player owner) {
		return new ManhuntCompass(owner);
	}

	@Override
	protected String getRoleId() {
		return null;
	}

	// @EventHandler
	// public void handlePlayerDeath(PlayerDeathEvent event) {
	//     Player player = event.getEntity();
	//     ItemStack compass = ManhuntCompass.getPersistentCompass(player);
	//     //pense pas forcément pertinant de préciser qe tu la perd si tu la regagne instannte quand tu respawn
	//     if (compass != null) {
	//         player.sendMessage(ChatColor.RED + "Votre Boussole Persistante a été détruite car vous êtes mort.");
	//         ManhuntCompass.removePersistentCompass(player);
	//     }
	// }

	// @EventHandler
	// public void handlePlayerRespawn(PlayerRespawnEvent event) {
	//     Player player = event.getPlayer();

	//     if (ManhuntCompass.hasPersistentCompass(player)) {
	//         player.sendMessage(ChatColor.GREEN + "Vous avez récupéré votre Boussole Persistante après la résurrection!");
	//     }
	// }
}