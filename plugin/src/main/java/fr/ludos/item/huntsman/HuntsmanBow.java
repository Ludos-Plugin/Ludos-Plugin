package fr.ludos.item.huntsman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Material;
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
	private final static Map<ItemStack, HuntsmanBow> cachedItems = new HashMap<>();


	public static @Nullable HuntsmanBow fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		HuntsmanBow cached = cachedItems.get(stack);
		if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, ID, game);
		if (owner == null) return null;

		return new HuntsmanBow(stack, owner, game);
	}
	public static HuntsmanBow createItem(Player owner, Game game) {
		HuntsmanBow bow = new HuntsmanBow(new ItemStack(Material.BOW), owner, game);
		bow.initializeItem();

		return bow;
	}

	protected HuntsmanBow(ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);
	}


	@Override
	public String getId() {
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
			if ( ! (event.getEntity() instanceof Player) ) {
				return;
			}

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
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, HuntsmanRole.id);
		}
	}
}