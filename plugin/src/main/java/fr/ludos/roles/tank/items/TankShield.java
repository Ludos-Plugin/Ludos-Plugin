package fr.ludos.roles.tank.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
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
import org.bukkit.util.Vector;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.core.item.level.LevelItem;
import fr.ludos.core.item.level.LevelItemInterface;
import fr.ludos.core.item.level.LevelState;
import fr.ludos.core.item.level.LevelValue;
import fr.ludos.core.role.Role;
import fr.ludos.roles.tank.TankRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public class TankShield extends LevelItem<TankShieldLevels> {
	private static final String ID = "tankShield";
	private static final int COOLDOWN_DURATION_SECONDS = 10;
	private static final Vector ALLY_PROTECTION_RANGE = new Vector(2.0, 2.0, 2.0);


	public static @Nullable TankShield fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItemInterface.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// TrapperDagger cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItemInterface.getSpecialItemOwner(stack, game);
		if (owner == null) return null;
		LevelValue levelValue = LevelItemInterface.levelFromItemStack(stack, game);
		if (levelValue == null) return null;

		TankShield shield = new TankShield(stack, owner, levelValue, game);
		// cachedItems.put(itemId, dagger);

		return shield;
	}

	public static TankShield createItem(Player owner, LevelValue level, Game game) {
		TankShield shield = new TankShield(createItemStack(), owner, level, game);
		shield.initializeItem();

		ItemMeta meta = shield.getStack().getItemMeta();
		meta.setUnbreakable(false);
		shield.getStack().setItemMeta(meta);

		// cachedItems.put(itemId, dagger);

		return shield;
	}

	protected TankShield(ItemStack stack, Player owner, LevelValue level, Game game) {
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
		ItemStack stack = getStack();
		ItemMeta meta = stack.getItemMeta();
		if (! (meta instanceof Damageable damageable)) return;

		TankShieldLevels level = levelObject(level());

		int currentDamage = damageable.getDamage();
		int maxDamage = stack.getType().getMaxDurability();

		double durabilityPerDamage = level.durabilityPerDamage();
		double convertedDamage = durabilityPerDamage * damage;
		int newDamage = (int) Math.ceil(currentDamage + convertedDamage);

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
	public void restore(double health) {
		ItemStack stack = getStack();
		ItemMeta meta = stack.getItemMeta();
		if (! (meta instanceof Damageable damageable)) return;

		TankShieldLevels level = levelObject(level());

		int currentDamage = damageable.getDamage();

		double durabilityPerDamage = level.durabilityPerDamage();
		double convertedHealth = durabilityPerDamage * health;
		int newDamage = (int) Math.floor(currentDamage - convertedHealth);

		if (newDamage <= 0) {
			damageable.setDamage(0);
		}
		else {
			damageable.setDamage(newDamage);
		}
		stack.setItemMeta(damageable);
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
		lore.add(
			Component.text("Protects nearby allies when raised.")
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
		private BukkitTask tankRoutine;
		public Events(Game game) {
			super(game, ItemSlot.OFFHAND);
		}

		@Override
		protected void onItemStart() {
			super.onItemStart();

			tankRoutine = new BukkitRunnable() {
				@Override
				public void run() {
					Player[] tankPlayers = getGame().getGroup().getOnlinePlayers().stream()
						.filter(Role.ofRole(TankRole.id))
						.toArray(Player[]::new);

					for (Player player : tankPlayers) {
						PlayerInventory inventory = player.getInventory();
						for (TankShield shield : TankShield.findAllIn(inventory, (ItemStack stack) -> TankShield.fromItemStack(stack, game))) {
							TankShieldLevels level = shield.getLvlObject();
							shield.restore(level.getRegen());

							if (player.isBlocking() && player.getCooldown(Material.SHIELD) == 0) {
								displayProtectionRadius(player);
							}
						}
					}
				}
			}.runTaskTimer(getPlugin(), 0, 20);
		}

		private void displayProtectionRadius(Player player) {
			double radius = ALLY_PROTECTION_RANGE.getX();
			int particleCount = 12;

			Location playerLoc = player.getLocation();

			// Draw particles in a circle on the ground
			for (int i = 0; i < particleCount; i++) {
				double angle = 2 * Math.PI * i / particleCount;
				double cos = Math.cos(angle);
				double sin = Math.sin(angle);
				double x = playerLoc.getX();
				double z = playerLoc.getZ();
				double y = playerLoc.getY();

				player.getWorld().spawnParticle(
					Particle.GLOW,
					x + radius * cos, y, z + radius * sin,
					1, 0, 0, 0, 0.0
				);
				player.getWorld().spawnParticle(
					Particle.GLOW,
					x + radius * 0.3 * cos, y + 0.3, z + radius * 0.3 * sin,
					1, 0, 0, 0, 0.0
				);
			}
		}

		@Override
		protected void onItemStop() {
			super.onItemStop();

			if (tankRoutine != null) {
				tankRoutine.cancel();
			}
			tankRoutine = null;
		}

		@Override
		public TankShield createItem(Player owner, LevelValue level) {
			return TankShield.createItem(owner, level, game);
		}

		@Override
		@Nullable
		public TankShield getItem(ItemStack stack) {
			return TankShield.fromItemStack(stack, game);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return Role.isPlayerRole(owner, TankRole.id);
		}

		@EventHandler
		public void onPlayerBlockWithShield(EntityDamageByEntityEvent event) {
			if (! (event.getEntity() instanceof Player defender)) return;

			if (! defender.isBlocking() || defender.getCooldown(Material.SHIELD) > 0) return;

			TankShield shield;

			ItemStack offHand = defender.getInventory().getItemInOffHand();
			shield = TankShield.fromItemStack(offHand, game);
			if (shield == null) {
				ItemStack mainHand = defender.getInventory().getItemInMainHand();
				shield = TankShield.fromItemStack(mainHand, game);
			}
			if (shield == null) return;

			event.setCancelled(true);
			defender.playEffect(EntityEffect.SHIELD_BLOCK);

			shield.hit(event.getDamage());
		}

		@EventHandler(priority = EventPriority.HIGH)
		public void onAllyDamageNearShield(EntityDamageByEntityEvent event) {
			Entity victim = event.getEntity();

			if (event.isCancelled()) return;

			Iterable<Entity> nearbyTanks = victim.getNearbyEntities(
				ALLY_PROTECTION_RANGE.getX(),
				ALLY_PROTECTION_RANGE.getY(),
				ALLY_PROTECTION_RANGE.getZ()
			);

			for (Entity tankEntity : nearbyTanks) {
				if (! (tankEntity instanceof Player tankPlayer)) continue;

				if (! game.getTeamController().areEntitiesAllies(victim, tankPlayer)) continue;
				if (! tankPlayer.isBlocking() || tankPlayer.getCooldown(Material.SHIELD) > 0) continue;

				ItemStack mainHand = tankPlayer.getInventory().getItemInMainHand();
				ItemStack offHand = tankPlayer.getInventory().getItemInOffHand();
				TankShield shield = TankShield.fromItemStack(offHand, game);
				if (shield == null) {
					shield = TankShield.fromItemStack(mainHand, game);
				}
				if (shield == null) continue;

				event.setCancelled(true);
				shield.hit(event.getDamage());
				tankEntity.playEffect(EntityEffect.SHIELD_BLOCK);
				return;
			}
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void onShieldDamage(PlayerItemDamageEvent event) {
			ItemStack item = event.getItem();

			TankShield shield = TankShield.fromItemStack(item, game);
			if (shield == null) return;

			event.setCancelled(true);
		}
	}
}