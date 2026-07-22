package fr.ludos.roles.assassin.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.roles.assassin.AssassinRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of the Assassin Boots, for use by any Player with {@link AssassinRole}.
 */
public class AssassinBoots extends SpecialItem<AssassinBoots> {
	public static final String ID = "assassin_boots";

	AssassinBoots(SpecialItem.ItemData info, Events events) {
		super(info, events);
	}

	@Override
	public Component getName(){
		return Component.text("Bottes d'Assassin")
			.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	public List<Component> getLore(){
		return new ArrayList<>(Arrays.asList(
			Component.text("Vitesse augmentée"),
			Component.text("Saut amélioré"),
			Component.text("Résistance aux dégâts de chute (+2 blocs)")
		));
	}

	/**
	 * Events for the {@link AssassinBoots}.
	 */
	public static class Events extends SpecialItem.Events<AssassinBoots> {
		public Events(Game game) {
			super(game, new Events.Info(ItemSlot.BOOTS));
		}

		@Override
		public String getTypeId() {
			return ID;
		}

		@EventHandler
		public void onPlayerMove(PlayerMoveEvent event) {
			Player player = event.getPlayer();
			if (! isPlayerValid(player)) return;

			ItemStack bootsStack = player.getInventory().getBoots();
			if (getItem(bootsStack) == null) return;

			// Vitesse constante
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 2, 1, true, false));
		}

		@EventHandler
		public void onFallDamage(EntityDamageEvent event) {
			if (! (event.getEntity() instanceof Player player)) return;
			if (! isPlayerValid(player)) return;

			if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

			ItemStack bootsStack = player.getInventory().getBoots();
			if (getItem(bootsStack) == null) return;

			if (event.getDamage() <= 2) {
				event.setCancelled(true);
			}
		}

		@Override
		protected AssassinBoots getItemInternal(SpecialItem.ItemData info) {
			return new AssassinBoots(info, this);
		}

		@Override
		protected AssassinBoots createItemInternal(Player owner) {
			return new AssassinBoots(new SpecialItem.ItemData(new ItemStack(Material.IRON_BOOTS), owner), this);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.getLudos().getRoleManager().isPlayerRole(owner, AssassinRole.ID);
		}
	}
}
