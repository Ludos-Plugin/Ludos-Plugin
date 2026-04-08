package fr.ludos.item.tank;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.item.SpecialItem;
import fr.ludos.game.Game;
import fr.ludos.role.Role;
import fr.ludos.role.TankRole;

public class TankShield extends SpecialItem {
	private static final String ID = "tankShield";
	private static final int MAX_HITS_BEFORE_COOLDOWN = 5;
	private static final int COOLDOWN_DURATION = 5 * 20;

	public static final Map<UUID, Integer> hitCounts = new HashMap<>();
	// private final Map<UUID, Boolean> isInCooldown = new HashMap<>();

	public static @Nullable TankShield fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// TrapperDagger cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;

		TankShield shield = new TankShield(stack, owner, game);
		// cachedItems.put(itemId, dagger);

		return shield;
	}

	public static TankShield createItem(Player owner, Game game) {
		TankShield shield = new TankShield(createItemStack(), owner, game);
		shield.initializeItem();

		ItemMeta meta = shield.getStack().getItemMeta();
		meta.setUnbreakable(false);
		shield.getStack().setItemMeta(meta);

		// cachedItems.put(itemId, dagger);

		return shield;
	}

	protected TankShield(ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);
	}

	@Override
	public String getTypeId() {
		return ID;
	}

	public void hit() {
		UUID playerId = getOwner().getUniqueId();

		int currentHits = hitCounts.getOrDefault(playerId, 0) + 1;
		hitCounts.put(playerId, currentHits);

		// defender.sendMessage(Component.text("Blocked shots: " + currentHits + "/" + MAX_HITS_BEFORE_COOLDOWN).color(NamedTextColor.YELLOW));
		ItemStack stack = getStack();
		ItemMeta meta = stack.getItemMeta();

		boolean makeCooldown = currentHits >= MAX_HITS_BEFORE_COOLDOWN;

		int newDamage;
		if (makeCooldown) {
			newDamage = 0;
		}
		else {
			float durabilityRatio = (float)(MAX_HITS_BEFORE_COOLDOWN - (MAX_HITS_BEFORE_COOLDOWN - currentHits)) / MAX_HITS_BEFORE_COOLDOWN;
			newDamage = Math.max((int)(durabilityRatio * (float)(stack.getType().getMaxDurability())), 1);
		}
		if (meta instanceof Damageable damageable) {
			damageable.setDamage(newDamage);
			stack.setItemMeta(damageable);
		}


		if (makeCooldown) {
			startCooldown();

			PlayerInventory defenderInventory = getOwner().getInventory();
			boolean isOffHand = defenderInventory.getItemInOffHand().equals(stack);
			
			if (isOffHand) {
				defenderInventory.setItemInOffHand(null);
			} else {
				defenderInventory.setItemInMainHand(null);
			}
			new BukkitRunnable() {
				public void run() {
					if (isOffHand) {
						defenderInventory.setItemInOffHand(stack);
					} else {
						defenderInventory.setItemInMainHand(stack);
					}
				}
			}.runTaskLater(getGame().getPlugin(), 5);
		}
	}

	public void startCooldown() {
		Player player = getOwner();
		UUID playerId = getOwner().getUniqueId();

		hitCounts.put(playerId, 0);

		player.setCooldown(Material.SHIELD, COOLDOWN_DURATION);
		player.setShieldBlockingDelay(COOLDOWN_DURATION);
		player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
	}

	public void resetPlayer() {
		Player player = getOwner();
		UUID playerId = player.getUniqueId();

		hitCounts.remove(playerId);
		player.setCooldown(Material.SHIELD, 0);
		player.setShieldBlockingDelay(0);
	}

	@Override
	public Component getName() {
		return Component.text("Tank Shield")
				.decoration(TextDecoration.ITALIC, false);
	}

	private static ItemStack createItemStack() {
		ItemStack stack = new ItemStack(Material.SHIELD);
		return stack;
	}

	public static class Events extends SpecialItem.Events<TankShield> {
		public Events(Game game) {
			super(game, 40);
		}

		@Override
		@Nullable
		protected TankShield getItem(ItemStack stack, Game game) {
			return TankShield.fromItemStack(stack, game);
		}

		protected TankShield createItem(Player owner, Game game) {
			return TankShield.createItem(owner, game);
		}

		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, TankRole.id);
		}

		@EventHandler
		public void onPlayerBlockWithShield(EntityDamageByEntityEvent event) {
			if (!(event.getEntity() instanceof Player defender))
				return;

			if (!defender.isBlocking() || defender.getCooldown(Material.SHIELD) > 0) {
				// event.setCancelled(false);
				return;
			}


			TankShield shield;

			ItemStack mainHand = defender.getInventory().getItemInMainHand();
			ItemStack offHand = defender.getInventory().getItemInOffHand();
			shield = TankShield.fromItemStack(offHand, game);
			if (shield == null) {
				shield = TankShield.fromItemStack(mainHand, game);
			}
			if (shield == null) return;

			shield.getOwner().sendMessage("You blocked an attack with your shield!");
			shield.hit();
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void onShieldDamage(PlayerItemDamageEvent event) {
			Player player = event.getPlayer();
			ItemStack item = event.getItem();

			TankShield shield = TankShield.fromItemStack(item, game);
			if (shield == null)
				return;

			// player.sendMessage("Your shield blocked an attack!");

			event.setCancelled(true);

			// shield.hit();
		}
	}
}