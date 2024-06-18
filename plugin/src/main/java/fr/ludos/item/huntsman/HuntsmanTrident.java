package fr.ludos.item.huntsman;

import fr.ludos.role.HuntsmanRole;
import fr.ludos.item.SpecialItem;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.entity.PlayerDeathEvent;

import javax.annotation.Nullable;


public class HuntsmanTrident extends SpecialItem {
	public HuntsmanTrident(ItemStack stack){
		super(stack);
	}
	public HuntsmanTrident(Player owner){
		super(new ItemStack(Material.TRIDENT), owner);
	}
	public HuntsmanTrident(ItemStack item, Player owner) {
		super(item, owner);
	}


	@Override
	public String getId() {
		return "manhunt_huntsman_trident";
	}

	@Override
	protected String getName(){
		return "Old Trident";
	}


	public static class Events extends SpecialItem.Events<HuntsmanTrident> {

		public Events() {
			super(HuntsmanRole.id);
		}


		public void playerEventListener(Player player, PlayerDeathEvent event, ItemStack trident) {
			player.getInventory().removeItem(trident);
		}


		@Override
		@Nullable
		protected HuntsmanTrident getItem(ItemStack stack) {
			try {
				HuntsmanTrident bow = new HuntsmanTrident(stack);
				return bow;
			} catch (IllegalArgumentException e) {
				return null;
			}
		}

		@Override
		protected HuntsmanTrident createItem(Player owner) {
			return new HuntsmanTrident(owner);
		}

	}
}