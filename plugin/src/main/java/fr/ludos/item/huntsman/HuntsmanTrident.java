package fr.ludos.item.huntsman;

import fr.ludos.role.HuntsmanRole;
import fr.ludos.role.Role;
import fr.ludos.game.Game;
import fr.ludos.item.SpecialItem;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.entity.PlayerDeathEvent;

import javax.annotation.Nullable;


public class HuntsmanTrident extends SpecialItem {
	public HuntsmanTrident(ItemStack stack, Game game) {
		super(stack, game);
	}
	public HuntsmanTrident(Player owner, Game game) {
		super(new ItemStack(Material.TRIDENT), owner, game);
	}
	public HuntsmanTrident(ItemStack item, Player owner, Game game) {
		super(item, owner, game);
	}


	@Override
	public String getId() {
		return "manhuntHuntsmanTrident";
	}

	@Override
	protected String getName(){
		return "Old Trident";
	}


	public static class Events extends SpecialItem.Events<HuntsmanTrident> {

		public Events(Game game) {
			super(game);
		}


		public void playerEventListener(Player player, PlayerDeathEvent event, ItemStack trident) {
			player.getInventory().removeItem(trident);
		}


		@Override
		@Nullable
		protected HuntsmanTrident getItem(ItemStack stack, Game game) {
			try {
				HuntsmanTrident bow = new HuntsmanTrident(stack, game);
				return bow;
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		@Override
		protected HuntsmanTrident createItem(Player owner, Game game) {
			return new HuntsmanTrident(owner, game);
		}
		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, HuntsmanRole.id);
		}
	}
}