package fr.ludos.item.huntsman;

import fr.ludos.role.HuntsmanRole;
import fr.ludos.item.SpecialItem;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;
import javax.annotation.Nullable;

public class HuntsmanBow extends SpecialItem {
	public HuntsmanBow(ItemStack stack){
		super(stack);
	}
	public HuntsmanBow(Player owner){
		super(new ItemStack(Material.BOW), owner);
	}

	@Override
	public String getId() {
		return "manhuntHuntsmanBow";
	}

	@Override
	protected String getName(){
		return "Stolen Bow";
	}

	@Override
	public List<String> getLore(){
		return null;
	}


	public static class Events extends SpecialItem.Events<HuntsmanBow> {

		public Events() {
			super(HuntsmanRole.id);

			updateAllInventories();
		}

		@EventHandler
		public void onShootArrow(EntityShootBowEvent event) {
			if ( ! (event.getEntity() instanceof Player) ) {
				return;
			}
			Player player = (Player) event.getEntity();

			Arrow arrowProjectile = (Arrow) event.getProjectile();
			arrowProjectile.setPickupStatus(PickupStatus.DISALLOWED);
			if (arrowProjectile.isShotFromCrossbow()) {
				arrowProjectile.setGravity(false);
				arrowProjectile.setDamage(0.5);
			}
		}


		@Override
		@Nullable
		protected HuntsmanBow getItem(ItemStack stack) {
			try {
				HuntsmanBow bow = new HuntsmanBow(stack);
				return bow;
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		@Override
		protected HuntsmanBow createItem(Player owner) {
			return new HuntsmanBow(owner);
		}
	}

}