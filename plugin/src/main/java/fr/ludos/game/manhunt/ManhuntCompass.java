package fr.ludos.game.manhunt;

import java.util.List;
import java.util.ArrayList;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import fr.ludos.item.SpecialItem;
import fr.ludos.game.Game;


public class ManhuntCompass extends SpecialItem {

	public ManhuntCompass(ItemStack item, Game game) {
		super(item, game);
	}

	public ManhuntCompass(Player owner, Game game) {
		super(createItemStack(), owner, game);
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
		return "manhuntCompass";
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
	public static ManhuntCompass getItem(ItemStack stack, Game game) {
		try {
			ManhuntCompass compass = new ManhuntCompass(stack, game);
			return compass;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static ManhuntCompass createItem(Player owner, Game game) {
		return new ManhuntCompass(owner, game);
	}

	public static class Events extends SpecialItem.Events<ManhuntCompass> {

		public Events(Game game) {
			super(game);
		}

		@Override
		@Nullable
		protected ManhuntCompass getItem(ItemStack stack, Game game) {
			return ManhuntCompass.getItem(stack, game);
		}

		protected ManhuntCompass createItem(Player owner, Game game) {
			return ManhuntCompass.createItem(owner, game);
		}

		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			if (! (game instanceof ManhuntGame manhunt)) return false;
			return manhunt.getTeamController().hunterTeam.hasEntry(owner.getName());
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
