package fr.ludos.item.huntsman;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;

import fr.ludos.game.Game;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.HuntsmanRole;
import fr.ludos.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;


public class HuntsmanBow extends SpecialItem {
	private static final String ID = "manhuntHuntsmanBow";

	// private final static Map<UUID, HuntsmanBow> cachedItems = new HashMap<>();


	public static @Nullable HuntsmanBow fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// HuntsmanBow cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
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
			super(game, 0);
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
		protected HuntsmanBow getItem(ItemStack stack, Game game) {
			return HuntsmanBow.fromItemStack(stack, game);
		}
		@Override
		protected HuntsmanBow createItem(Player owner, Game game) {
			return HuntsmanBow.createItem(owner, game);
		}
		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return Role.isPlayerRole(owner, HuntsmanRole.id);
		}
	}
}