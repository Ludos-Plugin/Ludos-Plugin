package fr.ludos.roles.assassin.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

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
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.role.Role;
import fr.ludos.roles.assassin.AssassinRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class AssassinBoots extends SpecialItem {
	public static final String ID = "assassin_boots";

	// private final static Map<UUID, AssassinBoots> cachedItems = new HashMap<>();

	public static @Nullable AssassinBoots fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// AssassinBoots cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;

		AssassinBoots boots = new AssassinBoots(stack, owner, game);
		// cachedItems.put(itemId, boots);

		return boots;
	}

	public static AssassinBoots createItem(Player owner, Game game) {
		AssassinBoots boots = new AssassinBoots(new ItemStack(Material.IRON_BOOTS), owner, game);
		UUID itemId = boots.initializeItem();

		// cachedItems.put(itemId, boots);

		return boots;
	}

	public AssassinBoots(ItemStack stack, Player player, Game game) {
		super(stack, player, game);
	}

	@Override
	public String getTypeId() {
		return ID;
	}

	@Override
	protected Component getName(){
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


	public static class Events extends SpecialItem.Events<AssassinBoots> {
		public Events(Game game) {
			super(game);
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
		@Nullable
		public AssassinBoots getItem(ItemStack stack) {
			return AssassinBoots.fromItemStack(stack, game);
		}

		@Override
		public AssassinBoots createItem(Player owner) {
			return AssassinBoots.createItem(owner, game);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return Role.isPlayerRole(owner, AssassinRole.id);
		}
	}
}
