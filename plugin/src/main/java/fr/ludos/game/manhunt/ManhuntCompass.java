package fr.ludos.game.manhunt;

import fr.ludos.item.SpecialItem;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import java.util.List;
import java.util.ArrayList;
import javax.annotation.Nullable;



public class ManhuntCompass extends SpecialItem {

	public ManhuntCompass(ItemStack item) {
		super(item);
	}

	public ManhuntCompass(Player owner) {
		super(createItemStack(), owner);
	}

	private static ItemStack createItemStack() {
		ItemStack stack = new ItemStack(Material.COMPASS);
		CompassMeta meta = (CompassMeta) stack.getItemMeta();

		meta.setLodestoneTracked(false);
		meta.setLodestone(null);

		stack.setItemMeta(meta);
		return stack;
	}

	public void setLocation(Player prey) {
		ItemStack stack = getStack();
		CompassMeta meta = (CompassMeta) stack.getItemMeta();

		meta.setLodestoneTracked(false);
		meta.setLodestone(prey.getLocation());

		stack.setItemMeta(meta);
	}

	public Location getLocation() {
		ItemStack stack = getStack();
		CompassMeta meta = (CompassMeta) stack.getItemMeta();

		return meta.getLodestone();
	}

	@Override
	public String getId() {
		return "manhunt_compass";
	}

	@Override
	public List<String> getLore() {
		return new ArrayList<String>(){{ add("Every three minutes, the position of prey is revealed through the compass."); }};
	}

	@Override
	public String getName() {
		return "Hunter's Compass";
	}

	@Nullable
	public static ManhuntCompass getItem(ItemStack stack) {
		try {
			ManhuntCompass compass = new ManhuntCompass(stack);
			return compass;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static ManhuntCompass createItem(Player owner) {
		return new ManhuntCompass(owner);
	}

	public static class Events extends SpecialItem.Events<ManhuntCompass> {

		public Events() {
			super(null);

			updateAllInventories();
		}

		@Override
		@Nullable
		protected ManhuntCompass getItem(ItemStack stack) {
			return ManhuntCompass.getItem(stack);
		}

		protected ManhuntCompass createItem(Player owner) {
			return ManhuntCompass.createItem(owner);
		}

		// @EventHandler
		// public void handlePlayerDeath(PlayerDeathEvent event) {
		//     Player player = event.getEntity();
		//     ItemStack compass = ManhuntCompass.getPersistentCompass(player);

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
