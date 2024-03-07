package fr.ludos.game.manhunt;

import fr.ludos.item.SpecialItem;


import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.ArrayList;
import javax.annotation.Nullable;



public class ManhuntCompass extends SpecialItem {

	public ManhuntCompass(ItemStack item) {
		super(item);
	}

	public ManhuntCompass(Player owner) {
		super(new ItemStack(Material.COMPASS), owner);
	}

	@Override
	public List<String> getLore() {
		return new ArrayList<String>(){{ add("Every three minutes, the position of prey is revealed through the compass."); }};
	}

	@Override
	public String getName() {
		return "Hunter's Compass";
	}

	// @Nullable
	// public static ManhuntCompass getHunterCompass(ItemStack item) {
	// 	if ()

	// }

	public static class Events extends SpecialItem.Events<ManhuntCompass> {

		public Events() {
			super(null);
		}

		@Override
		@Nullable
		protected ManhuntCompass getItem(ItemStack stack) {
			try {
				ManhuntCompass compass = new ManhuntCompass(stack);
				return compass;
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		@Override
		protected ManhuntCompass createItem(Player owner) {
			return new ManhuntCompass(owner);
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
}
