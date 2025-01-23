package fr.ludos.item.trapper;
import fr.ludos.game.Game;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.Role;
import fr.ludos.role.TrapperRole;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.EventHandler;

import org.bukkit.potion.PotionEffectType;


public class TrapperDagger extends SpecialItem {

	public TrapperDagger(ItemStack stack) {
		super(stack);

	}
	public TrapperDagger(Player owner) {
		super(new ItemStack(Material.STONE_SWORD), owner);
	}

	@Override
	protected String getId() {
		return "trapperDagger";
	}
	@Override
	protected String getName() {
		return "Trapper Dagger";

	}


	@Nullable
	public static TrapperDagger getItem(ItemStack stack) {
		try {
			TrapperDagger dagger = new TrapperDagger(stack);
			return dagger;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	public static TrapperDagger createItem(Player owner) {
		return new TrapperDagger(owner);
	}


	public static class Events extends SpecialItem.Events<TrapperDagger> {
		public Events(Game game) {
			super(game);
		}


		@EventHandler
		public void OnPlayerOnImpactDagger(EntityDamageByEntityEvent event) {
			if (! (event.getDamager() instanceof Player attacker)) return;
			if (! (event.getEntity() instanceof Player victim)) return;

			TrapperDagger dagger = getItem(attacker.getInventory().getItemInMainHand());
			if (dagger == null) {
				return;
			}

			victim.addPotionEffect(PotionEffectType.POISON.createEffect(60, 1));
		}

		// @EventHandler
		// public void EmissiveParticules(){

		// }

		@Override
		@Nullable
		protected TrapperDagger getItem(ItemStack stack) {
			return TrapperDagger.getItem(stack);
		}

		@Override
		protected TrapperDagger createItem(Player owner) {
			return TrapperDagger.createItem(owner);
		}

		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, TrapperRole.id);
		}
	}
}


