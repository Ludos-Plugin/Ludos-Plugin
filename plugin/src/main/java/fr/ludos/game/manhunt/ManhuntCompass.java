package fr.ludos.game.manhunt;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import fr.ludos.item.SpecialItem;
import fr.ludos.item.burrower.BurrowerPick;
import fr.ludos.game.Game;


public class ManhuntCompass extends SpecialItem {
	private static final String ID = "manhuntCompass";

	// private static final Map<UUID, ManhuntCompass> cachedItems = new HashMap<>();


	public static @Nullable ManhuntCompass fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// ManhuntCompass cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;

		ManhuntCompass compass = new ManhuntCompass(stack, owner, game);
		// cachedItems.put(itemId, compass);

		return compass;
	}
	
	public static ManhuntCompass createItem(Player owner, Game game) {
		ManhuntCompass compass = new ManhuntCompass(createItemStack(), owner, game);
		UUID itemId = compass.initializeItem();

		// cachedItems.put(itemId, compass);

		return compass;
	}

	protected ManhuntCompass(ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);
	}

	@Override
	public String getTypeId() {
		return ID;
	}

	@Override
	public Component getName() {
		return Component.text("Hunter's Compass")
			.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	public List<Component> getLore() {
		return new ArrayList<Component>(){{
			add(
				Component.text("When the timer ends,")
					.decoration(TextDecoration.ITALIC, false)
			);
			add(
				Component.text("the position of the prey is revealed through the compass.")
					.decoration(TextDecoration.ITALIC, false)
			);
		}};
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


	public static class Events extends SpecialItem.Events<ManhuntCompass> {

		public Events(Game game) {
			super(game, 8);
		}

		@Override
		@Nullable
		protected ManhuntCompass getItem(ItemStack stack, Game game) {
			return ManhuntCompass.fromItemStack(stack, game);
		}

		protected ManhuntCompass createItem(Player owner, Game game) {
			return ManhuntCompass.createItem(owner, game);
		}

		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			if (! (game instanceof ManhuntGame manhunt)) return false;
			return manhunt.getGameTeamController().hunterTeam.hasEntry(owner.getName());
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
