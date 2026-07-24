package fr.ludos.roles.huntsman.items;

import java.util.ArrayList;
import java.util.List;

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
import fr.ludos.roles.huntsman.HuntsmanRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of the Huntsman Bow, for use by any Player with {@link HuntsmanRole}.
 */
public class HuntsmanBow extends SpecialItem<HuntsmanBow> {
	public static final String ID = "huntsman_bow";


	protected HuntsmanBow(SpecialItem.ItemData info, Events events) {
		super(info, events);
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

	/**
	 * Events for the {@link HuntsmanBow}.
	 */
	public static class Events extends SpecialItem.Events<HuntsmanBow> {

		public Events(Game game) {
			super(game, new Events.Info(ItemSlot.HOTBAR_1));
		}

		@Override
		public String getTypeId() {
			return ID;
		}

		@Override
		public boolean isRanged() {
			return true;
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
		protected HuntsmanBow getItemInternal(ItemData info) {
			return new HuntsmanBow(info, this);
		}
		@Override
		protected HuntsmanBow createItemInternal(Player owner) {
			return new HuntsmanBow(new SpecialItem.ItemData(new ItemStack(Material.BOW), owner), this);
		}


		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.ludos().getRoleManager().isPlayerRole(owner, HuntsmanRole.ID);
		}
	}
}