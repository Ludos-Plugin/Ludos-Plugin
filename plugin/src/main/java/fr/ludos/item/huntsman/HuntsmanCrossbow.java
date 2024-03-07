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

public class HuntsmanCrossbow extends SpecialItem {
	public HuntsmanCrossbow(ItemStack stack){
		super(stack);
	}
	public HuntsmanCrossbow(Player owner){
		super(new ItemStack(Material.CROSSBOW), owner);
	}
	public HuntsmanCrossbow(ItemStack item, Player owner) {
		super(item, owner);
	}

	@Override
	protected String getName(){
		return "Old Crossbow";
	}

	@Override
	public List<String> getLore(){
		return null;
	}


	public static class Events extends SpecialItem.Events<HuntsmanCrossbow> {

		public Events() {
			super(HuntsmanRole.id);
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

			updateArrowCount(player);
		}

		private void updateArrowCount(Player player) {
			Inventory inventory = player.getInventory();

			ItemStack arrowItem = new ItemStack(Material.ARROW);
			inventory.remove(Material.ARROW);
			inventory.addItem(arrowItem);
		}


		@Override
		@Nullable
		protected HuntsmanCrossbow getItem(ItemStack stack) {
			try {
				HuntsmanCrossbow bow = new HuntsmanCrossbow(stack);
				return bow;
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		@Override
		protected HuntsmanCrossbow createItem(Player owner) {
			return new HuntsmanCrossbow(owner);
		}
	}

}