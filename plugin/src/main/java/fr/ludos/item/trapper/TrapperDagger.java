package fr.ludos.item.trapper;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.game.Game;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.Role;
import fr.ludos.role.TrapperRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class TrapperDagger extends SpecialItem {
	private final static String ID = "trapperDagger";
	private final static Map<ItemStack, TrapperDagger> cachedItems = new HashMap<>();


	public static @Nullable TrapperDagger fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		TrapperDagger cached = cachedItems.get(stack);
		if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, ID, game);
		if (owner == null) return null;

		return new TrapperDagger(stack, owner, game);
	}
	public static TrapperDagger createItem(Player owner, Game game) {
		TrapperDagger dagger = new TrapperDagger(new ItemStack(Material.STONE_SWORD), owner, game);
		dagger.initializeItem();

		return dagger;
	}

	protected TrapperDagger(ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);
	}


	@Override
	protected String getId() {
		return ID;
	}
	@Override
	public Component getName() {
		return Component.text("Trapper Dagger")
			.decoration(TextDecoration.ITALIC, false);
	}


	public static class Events extends SpecialItem.Events<TrapperDagger> {
		public Events(Game game) {
			super(game, 0);
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
			return TrapperDagger.fromItemStack(stack, game);
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