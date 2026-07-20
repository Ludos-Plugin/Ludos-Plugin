package fr.ludos.games.manhunt.items;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.games.manhunt.ManhuntGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of ManhuntCompass, used for and managed by {@link ManhuntGame}.
 */
public class ManhuntCompass extends SpecialItem {
	private static final String ID = "manhunt_compass";

	// private static final Map<UUID, ManhuntCompass> cachedItems = new HashMap<>();


	public static @Nullable ManhuntCompass fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItemInterface.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// ManhuntCompass cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItemInterface.getSpecialItemOwner(stack, game);
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

	/**
	 * Events for {@link ManhuntCompass}.
	 */
	public static class Events extends SpecialItem.Events<ManhuntCompass> {

		public Events(Game game) {
			super(game, new Events.Info(ItemSlot.HOTBAR_9));
		}

		@Override
		@Nullable
		public ManhuntCompass getItem(ItemStack stack) {
			return ManhuntCompass.fromItemStack(stack, game);
		}

		@Override
		public ManhuntCompass createItem(Player owner) {
			return ManhuntCompass.createItem(owner, game);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			if (! (game instanceof ManhuntGame manhunt)) return false;
			return manhunt.getTeamController().hunterTeam.hasEntry(owner.getName());
		}
	}
}
