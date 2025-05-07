package fr.ludos.item.huntsman;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;

import fr.ludos.item.SpecialItem;
import fr.ludos.game.Game;
import fr.ludos.role.Role;
import fr.ludos.role.HuntsmanRole;


public class HuntsmanBow extends SpecialItem {
	public HuntsmanBow(ItemStack stack, Game game) {
		super(stack, game);
	}
	public HuntsmanBow(Player owner, Game game) {
		super(new ItemStack(Material.BOW), owner, game);
	}

	@Override
	public String getId() {
		return "manhuntHuntsmanBow";
	}

	@Override
	protected Component getName(){
		return Component.text("Stolen Bow")
			.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	public List<Component> getLore(){
		return new ArrayList<>();
	}


	public static class Events extends SpecialItem.Events<HuntsmanBow> {

		public Events(Game game) {
			super(game);
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
			try {
				HuntsmanBow bow = new HuntsmanBow(stack, game);
				return bow;
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		@Override
		protected HuntsmanBow createItem(Player owner, Game game) {
			return new HuntsmanBow(owner, game);
		}
		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, HuntsmanRole.id);
		}
	}
}