package fr.ludos.game.manhunt;

import org.bukkit.event.Listener;

public class ManhuntCompassEvents implements Listener {

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