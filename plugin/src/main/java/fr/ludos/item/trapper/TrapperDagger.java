package fr.ludos.item.trapper;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.item.SpecialItem;
import fr.ludos.game.Game;
import fr.ludos.role.Role;
import fr.ludos.role.TrapperRole;

public class TrapperDagger extends SpecialItem {

	public TrapperDagger(ItemStack stack, Game game) {
		super(stack, game);
	}
	public TrapperDagger(Player owner, Game game) {
		super(new ItemStack(Material.STONE_SWORD), owner, game);
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
	public static TrapperDagger getItem(ItemStack stack, Game game) {
		try {
			TrapperDagger dagger = new TrapperDagger(stack, game);
			return dagger;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static TrapperDagger createItem(Player owner, Game game) {
		return new TrapperDagger(owner, game);
	}


	public static class Events extends SpecialItem.Events<TrapperDagger> {
		public Events(Game game) {
			super(game);
		}


		@EventHandler
		public void onPlayerImpactDagger(EntityDamageByEntityEvent event) {
			if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
				Player attacker = (Player) event.getDamager();
				Player victim = (Player) event.getEntity();

				TrapperDagger dagger = getItem(attacker.getInventory().getItemInMainHand(), game);
				if (dagger == null) {
					return;
				}
				victim.addPotionEffect(PotionEffectType.POISON.createEffect(60, 1));
			}
		}


		@Override
		@Nullable
		protected TrapperDagger getItem(ItemStack stack, Game game) {
			return TrapperDagger.getItem(stack, game);
		}

		@Override
		protected TrapperDagger createItem(Player owner, Game game) {
			return TrapperDagger.createItem(owner, game);
		}

		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, TrapperRole.id);
		}
	}
}