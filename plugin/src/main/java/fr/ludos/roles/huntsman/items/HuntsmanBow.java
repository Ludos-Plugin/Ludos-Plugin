package fr.ludos.roles.huntsman.items;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.core.role.Role;
import fr.ludos.roles.huntsman.HuntsmanRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;


public class HuntsmanBow extends SpecialItem {
	private static final String ID = "manhuntHuntsmanBow";

	// private final static Map<UUID, HuntsmanBow> cachedItems = new HashMap<>();


	public static @Nullable HuntsmanBow fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItemInterface.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// HuntsmanBow cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItemInterface.getSpecialItemOwner(stack, game);
		if (owner == null) return null;

		HuntsmanBow bow = new HuntsmanBow(stack, owner, game);
		// cachedItems.put(itemId, bow);

		return bow;
	}
	public static HuntsmanBow createItem(Player owner, Game game) {
		HuntsmanBow bow = new HuntsmanBow(new ItemStack(Material.BOW), owner, game);
		UUID itemId = bow.initializeItem();

		// cachedItems.put(itemId, bow);

		return bow;
	}

	protected HuntsmanBow(ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);
	}


	@Override
	public String getTypeId() {
		return ID;
	}

	@Override
	public Component getName(){
		return Component.text("Stolen Bow")
			.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	public List<Component> getLore(){
		return new ArrayList<>();
	}


	public static class Events extends SpecialItem.Events<HuntsmanBow> {

		public Events(Game game) {
			super(game, new Events.Info(ItemSlot.HOTBAR_1));
		}

		@EventHandler
		public void onShootArrow(EntityShootBowEvent event) {
			if ( ! (event.getEntity() instanceof Player player) ) return;
			if (! isPlayerValid(player)) return;

			Arrow arrowProjectile = (Arrow) event.getProjectile();
			arrowProjectile.setPickupStatus(PickupStatus.DISALLOWED);
			if (arrowProjectile.isShotFromCrossbow()) {
				arrowProjectile.setGravity(false);
				arrowProjectile.setDamage(0.5);
			}
		}


		@Override
		@Nullable
		public HuntsmanBow getItem(ItemStack stack) {
			return HuntsmanBow.fromItemStack(stack, game);
		}
		@Override
		public HuntsmanBow createItem(Player owner) {
			return HuntsmanBow.createItem(owner, game);
		}
		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return Role.isPlayerRole(owner, HuntsmanRole.ID);
		}
	}
}