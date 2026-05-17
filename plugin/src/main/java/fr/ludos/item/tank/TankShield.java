package fr.ludos.item.tank;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.game.Game;
import fr.ludos.item.SpecialItem;
import fr.ludos.item.LevelItem;
import fr.ludos.role.Role;
import fr.ludos.role.TankRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public class TankShield extends LevelItem<TankShieldLevels> {
	private static final String ID = "tankShield";
	private static final int COOLDOWN_DURATION_SECONDS = 10;


	public static @Nullable TankShield fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// TrapperDagger cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;
		LevelState levelState = LevelItem.levelFromItemStack(stack, game);
		if (levelState == null) return null;

		TankShield shield = new TankShield(stack, owner, levelState, game);
		// cachedItems.put(itemId, dagger);

		return shield;
	}

	public static TankShield createItem(Player owner, LevelState level, Game game) {
		TankShield shield = new TankShield(createItemStack(), owner, level, game);
		shield.initializeItem();

		ItemMeta meta = shield.getStack().getItemMeta();
		meta.setUnbreakable(false);
		shield.getStack().setItemMeta(meta);

		// cachedItems.put(itemId, dagger);

		return shield;
	}

	protected TankShield(ItemStack stack, Player owner, LevelState level, Game game) {
		super(TankShieldLevels.class, stack, owner, level, game);
	}

	@Override
	public String getTypeId() {
		return ID;
	}

	public void hit() {
		hit(1);
	}
	public void hit(double damage) {
		// UUID playerId = getOwner().getUniqueId();

		ItemStack stack = getStack();
		ItemMeta meta = stack.getItemMeta();
		if (! (meta instanceof Damageable damageable)) { return; }

		TankShieldLevels level = getLvlObject(getLvl());

		int currentDamage = damageable.getDamage();
		int maxDamage = stack.getType().getMaxDurability();

		double durabilityPerDamage = level.durabilityPerDamage();
		int newDamage = (int) Math.ceil(currentDamage + durabilityPerDamage * damage);

		damageable.setDamage(newDamage);
		stack.setItemMeta(damageable);

		if (newDamage >= maxDamage) {
			damageable.setDamage(maxDamage);
			doCooldown();
		}
		else {
			damageable.setDamage(newDamage);
		}
		stack.setItemMeta(damageable);

		addXp(damage);
		updateLore();
	}

	public void doCooldown() {
		Player player = getOwner();
		ItemStack stack = getStack();

		player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
		// player.setShieldBlockingDelay(COOLDOWN_DURATION);
		player.setCooldown(Material.SHIELD, COOLDOWN_DURATION_SECONDS * 20);

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
		}.runTaskLater(getGame().getPlugin(), 2);
	}

	@Override
	public Component getName() {
		return Component.text("Tank Shield")
				.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();

		TankShieldLevels level = getLvlObject();
		int durability = level.getDurability();

		lore.add(
			SpecialItem.buildDataLore("Durability", durability + " damage")
		);
		lore.add(
			SpecialItem.buildDataLore("Cooldown", COOLDOWN_DURATION_SECONDS + " seconds")
		);
		lore.add(
			Component.text("Slowly recharges durability.")
				.color(NamedTextColor.DARK_PURPLE)
				.decoration(TextDecoration.ITALIC, false)
		);

		return lore;
	}

	private static ItemStack createItemStack() {
		ItemStack stack = new ItemStack(Material.SHIELD);
		return stack;
	}

	public static class Events extends LevelItem.Events<TankShield, TankShieldLevels> {
		private BukkitTask rechargeTask;
		public Events(Game game) {
			super(game, 40);
		}

		@Override
		protected void onItemStart() {
			rechargeTask = new BukkitRunnable() {
				@Override
				public void run() {
					Player[] tankPlayers = getGame().getGroup().getOnlinePlayers().stream()
						.filter(p -> Role.isPlayerRole(p, TankRole.id))
						.toArray(Player[]::new);
					for (Player player : tankPlayers) {
						PlayerInventory inventory = player.getInventory();
						for (TankShield shield : TankShield.findAllIn(inventory, (ItemStack stack) -> TankShield.fromItemStack(stack, game))) {
							ItemStack stack = shield.getStack();
							ItemMeta meta = stack.getItemMeta();
							if (! (meta instanceof Damageable damageable)) continue;

							TankShieldLevels level = shield.getLvlObject();
							int regen = (int) Math.ceil(level.durabilityPerDamage() * 0.5);

							int currentDamage = damageable.getDamage();
							damageable.setDamage(Math.max(currentDamage - regen, 0));
							stack.setItemMeta(damageable);
						}
					}
				}

			}.runTaskTimer(getPlugin(), 0, 20);
		}

		@Override
		protected void onItemStop() {
			if (rechargeTask != null) {
				rechargeTask.cancel();
			}
			rechargeTask = null;
		}

		@Override
		protected TankShield createItem(Player owner, LevelState level, Game game) {
			return TankShield.createItem(owner, level, game);
		}

		@Override
		@Nullable
		protected TankShield getItem(ItemStack stack, Game game) {
			return TankShield.fromItemStack(stack, game);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return Role.isPlayerRole(owner, TankRole.id);
		}

		@EventHandler
		public void onPlayerBlockWithShield(EntityDamageByEntityEvent event) {
			if (!(event.getEntity() instanceof Player defender)) return;

			if (!defender.isBlocking() || defender.getCooldown(Material.SHIELD) > 0) return;


			TankShield shield;

			ItemStack mainHand = defender.getInventory().getItemInMainHand();
			ItemStack offHand = defender.getInventory().getItemInOffHand();
			shield = TankShield.fromItemStack(offHand, game);
			if (shield == null) {
				shield = TankShield.fromItemStack(mainHand, game);
			}
			if (shield == null) return;

			shield.hit(event.getDamage());
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void onShieldDamage(PlayerItemDamageEvent event) {
			Player player = event.getPlayer();
			ItemStack item = event.getItem();

			TankShield shield = TankShield.fromItemStack(item, game);
			if (shield == null) return;

			event.setCancelled(true);
		}
	}
}