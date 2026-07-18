package fr.ludos.roles.rampart.items;

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
import fr.ludos.core.item.level.LevelValue;
import fr.ludos.core.role.Role;
import fr.ludos.roles.rampart.RampartRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public class RampartShield extends LevelItem<RampartShieldLevels> {
	public static final String ID = "rampart_shield";

	private static final int COOLDOWN_DURATION_SECONDS = 10;
	private static final Vector ALLY_PROTECTION_RANGE = new Vector(2.0, 2.0, 2.0);


	public static @Nullable RampartShield fromItemStack(List<RampartShieldLevels> levels, ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItemInterface.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// TrapperDagger cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItemInterface.getSpecialItemOwner(stack, game);
		if (owner == null) return null;
		LevelValue levelValue = LevelItemInterface.levelFromItemStack(stack, game);
		if (levelValue == null) return null;

		RampartShield shield = new RampartShield(levels, levelValue, stack, owner, game);
		// cachedItems.put(itemId, dagger);

		return shield;
	}

	public static RampartShield createItem(List<RampartShieldLevels> levels, LevelValue level, Player owner, Game game) {
		RampartShield shield = new RampartShield(levels, level, createItemStack(), owner, game);
		shield.initializeItem();

		ItemMeta meta = shield.getStack().getItemMeta();
		meta.setUnbreakable(false);
		shield.getStack().setItemMeta(meta);

		// cachedItems.put(itemId, dagger);

		return shield;
	}

	protected RampartShield(List<RampartShieldLevels> levels, LevelValue level, ItemStack stack, Player owner, Game game) {
		super(levels, level, stack, owner, game);
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

		RampartShieldLevels level = lvlObject(level());

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
	}
	public void restore(double health) {
		ItemStack stack = getStack();
		ItemMeta meta = stack.getItemMeta();
		if (! (meta instanceof Damageable damageable)) return;

		RampartShieldLevels level = lvlObject(level());

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
		return Component.text("Rampart Shield")
				.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();

		RampartShieldLevels level = lvlObject();
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

	public static class Events extends LevelItem.Events<RampartShield, RampartShieldLevels> {
		private static final List<RampartShieldLevels> LEVELS = List.of(RampartShieldLevels.values());
		private BukkitTask rampartRoutine;
		public Events(Game game) {
			super(game, new Events.Info(ItemSlot.OFFHAND));
		}

		@Override
		public List<RampartShieldLevels> getLevels() {
			return LEVELS;
		}

		@Override
		protected void onItemStart() {
			super.onItemStart();

			rampartRoutine = new BukkitRunnable() {
				@Override
				public void run() {
					Player[] rampartPlayers = getGame().getGroup().getOnlinePlayers().stream()
						.filter(Role.ofRole(RampartRole.ID))
						.toArray(Player[]::new);

					for (Player player : rampartPlayers) {
						PlayerInventory inventory = player.getInventory();
						for (RampartShield shield : RampartShield.findAllIn(inventory, (ItemStack stack) -> getItem(stack))) {
							RampartShieldLevels level = shield.lvlObject();
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

			if (rampartRoutine != null) {
				rampartRoutine.cancel();
			}
			rampartRoutine = null;
		}

		@Override
		public RampartShield createItem(LevelValue level, Player owner) {
			return RampartShield.createItem(LEVELS, level, owner, game);
		}

		@Override
		@Nullable
		public RampartShield getItem(ItemStack stack) {
			return RampartShield.fromItemStack(LEVELS, stack, game);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return Role.isPlayerRole(owner, RampartRole.ID);
		}

		@EventHandler
		public void onPlayerBlockWithShield(EntityDamageByEntityEvent event) {
			if (! (event.getEntity() instanceof Player defender)) return;

			if (! defender.isBlocking() || defender.getCooldown(Material.SHIELD) > 0) return;

			RampartShield shield;

			ItemStack offHand = defender.getInventory().getItemInOffHand();
			shield = getItem(offHand);
			if (shield == null) {
				ItemStack mainHand = defender.getInventory().getItemInMainHand();
				shield = getItem(mainHand);
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

			Iterable<Entity> nearbyRamparts = victim.getNearbyEntities(
				ALLY_PROTECTION_RANGE.getX(),
				ALLY_PROTECTION_RANGE.getY(),
				ALLY_PROTECTION_RANGE.getZ()
			);

			for (Entity rampartEntity : nearbyRamparts) {
				if (! (rampartEntity instanceof Player rampartPlayer)) continue;

				if (! game.getTeamController().areEntitiesAllies(victim, rampartPlayer)) continue;
				if (! rampartPlayer.isBlocking() || rampartPlayer.getCooldown(Material.SHIELD) > 0) continue;

				ItemStack mainHand = rampartPlayer.getInventory().getItemInMainHand();
				ItemStack offHand = rampartPlayer.getInventory().getItemInOffHand();
				RampartShield shield = getItem(offHand);
				if (shield == null) {
					shield = getItem(mainHand);
				}
				if (shield == null) continue;

				event.setCancelled(true);
				shield.hit(event.getDamage());
				rampartEntity.playEffect(EntityEffect.SHIELD_BLOCK);
				return;
			}
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void onShieldDamage(PlayerItemDamageEvent event) {
			ItemStack item = event.getItem();

			RampartShield shield = getItem(item);
			if (shield == null) return;

			event.setCancelled(true);
		}
	}
}