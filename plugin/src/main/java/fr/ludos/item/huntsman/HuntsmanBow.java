package fr.ludos.item.huntsman;

import fr.ludos.item.LevelItem;
import fr.ludos.role.HuntsmanRole;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.List;

import javax.annotation.Nullable;



/**
 * Pickaxe is a class that represents a special item, "Miner Pickaxe," in Minecraft.
 * This item allows the miner player to improve their own pickaxe based on the ores they collect.
 * <br><br>
 * Features:
 * <br><br>
 * - Provides methods to give a miner pickaxe to the player and level up the pickaxe based on XP.
 * <br><br>
 * - Automatically updates the pickaxe's material as it levels up.
 * <br><br>
 * - Defines an evolution path from wood to stone, iron, gold, and finally diamond pickaxe.
 * <br><br>
 * Usage:
 * <br><br>
 * - Call addPickaxeInventory(player) to give a miner pickaxe to the specified player.
 * <br><br>
 * - Call levelPickaxe(player, xp) with the XP gained from mining to level up the pickaxe.
 * <br><br>
 * Example:
 * <pre>{@code
 * Pickaxe pickaxe = new Pickaxe();
 * pickaxe.addPickaxeInventory(player);
 * pickaxe.levelPickaxe(player, xp);
 * }</pre>
 * <br><br>
 * @author feur25 & Ganon358
 * @version 1.0
 * @see org.bukkit.entity.Player
 * @see org.bukkit.inventory.ItemStack
 * @see org.bukkit.Material
 * @see org.bukkit.inventory.meta.ItemMeta
 * @see java.util.Collections
 */

public class HuntsmanBow extends LevelItem<HuntsmanBowLevels> {


	public HuntsmanBow(ItemStack stack) throws IllegalArgumentException {
		super(stack);
	}

	public HuntsmanBow(Player owner) {
		this(owner, HuntsmanBowLevels.BASE);
	}
	public HuntsmanBow(Player owner, HuntsmanBowLevels level) {
		this(new ItemStack(Material.BOW), owner, level);
	}

	protected HuntsmanBow(ItemStack item, Player owner) {
		this(item, owner, HuntsmanBowLevels.BASE);
	}
	protected HuntsmanBow(ItemStack item, Player owner, HuntsmanBowLevels level) {
		this(item, owner, level, 0);
	}
	protected HuntsmanBow(ItemStack item, Player owner, HuntsmanBowLevels level, double xp) {
		super(item, owner, level, xp);
	}


	@Override
	public HuntsmanBowLevels convertToLevel(int level) {
		return HuntsmanBowLevels.findByKey(level);
	}


	@Override
	protected String getName() {
		return "Stolen Bow"; // TODO: Translate
	}


	@Override
	public void setLvl(HuntsmanBowLevels lvl) {
		super.setLvl(lvl);
	}


	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile projectile = event.getEntity();

		if (!(projectile.getShooter() instanceof Player)) return;

		if (event.getHitEntity() != null) {
			Entity hitEntity = event.getEntity();
			if (hitEntity instanceof LivingEntity) {
				LivingEntity livingEntity = (LivingEntity) hitEntity;

			}

		}
		Player shooterPlayer = (Player) projectile.getShooter();
		shooterPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 2));

	}


	@Override
	public void addLvl() {
		if (getLevel().isMax()) {
			return;
		}

		ItemStack stack = new ItemStack(Material.BOOK);


		List<HuntsmanBowLevels> evolutions = getLevel().getEvolutions();

		if (evolutions.size() == 0) {
			return;
		} else if (evolutions.size() == 1) {

		} else {

		}

		if (getOwner() != null) {
			getOwner().sendMessage(ChatColor.GREEN + "Your pickaxe has leveled up!"); // TODO: Translate
		}
	}



	public static class Events extends LevelItem.Events<HuntsmanBow, HuntsmanBowLevels> {

		public Events() {
			super(HuntsmanRole.id, HuntsmanBowLevels.BASE);
		}


		public void poisonArrow1(Player player) {
			Location eyeLocation = player.getEyeLocation();
			World world = player.getWorld();

			Arrow poisonArrow = (Arrow) world.spawnEntity(eyeLocation, EntityType.ARROW);
			poisonArrow.setPickupStatus(PickupStatus.DISALLOWED);
			poisonArrow.setGravity(true);
			poisonArrow.setShooter(player);
			poisonArrow.setVelocity(eyeLocation.getDirection().multiply(2));

			PotionEffect poisoneffect = new PotionEffect(PotionEffectType.POISON, 100, 1);
			poisonArrow.addCustomEffect(poisoneffect, true);
		}


			//Arrow arrowProjectile = (Arrow) event.getProjectile();
			// arrowProjectile.setPickupStatus(PickupStatus.DISALLOWED);
			// if (arrowProjectile.isShotFromCrossbow()) {
			//     arrowProjectile.setGravity(false);
			//     arrowProjectile.setDamage(0.5);
			// }

		public void poisonArrow2(Player player) {

			Location eyeLocation = player.getEyeLocation();
			World world = player.getWorld();
			Arrow poisonArrow = (Arrow) world.spawnEntity(eyeLocation, EntityType.ARROW);
			poisonArrow.setPickupStatus(PickupStatus.DISALLOWED);
			poisonArrow.setShooter(player);
			poisonArrow.setVelocity(eyeLocation.getDirection().multiply(2));

			PotionEffect poisonEffect = new PotionEffect(PotionEffectType.POISON, 60, 2);
			poisonArrow.addCustomEffect(poisonEffect, true);

		}

		public void fireArrow(Player player, int level) {

			Location eyeLocation = player.getEyeLocation();
			World world = player.getWorld();

			Arrow fireArrow = (Arrow) world.spawnEntity(eyeLocation, EntityType.ARROW);
			fireArrow.setShooter(player);
			fireArrow.setVelocity(eyeLocation.getDirection().multiply(2));
		}

		@EventHandler
		public void onShootArrow(EntityShootBowEvent event) {
			if ( ! (event.getEntity() instanceof Player) ) {
				return;
			}
			Player player = (Player) event.getEntity();

			HuntsmanBow bow = getItem(event.getBow());
			if (bow == null) {
				return;
			}

			Arrow arrowProjectile = (Arrow)event.getProjectile();
			arrowProjectile.setPickupStatus(PickupStatus.DISALLOWED);
			updateArrowCount(player);

			if (bow.getLevel().getType() == HuntsmanBowLevels.LevelBranch.POISON) {
				if (bow.getLevel() == HuntsmanBowLevels.POISON1) {
					poisonArrow1(player);
				}
				if (bow.getLevel() == HuntsmanBowLevels.POISON2) {
					poisonArrow2(player);
				}
			}
		}

		@Override
		public void updateItemInInventory(Player player) {
			super.updateItemInInventory(player);
			updateArrowCount(player);
		}

		private void updateArrowCount(Player player) {
			Inventory inventory = player.getInventory();
			if (inventory.contains(Material.ARROW)) {
				return;
			}

			ItemStack arrowItem = new ItemStack(Material.ARROW);
			inventory.addItem(arrowItem);
		}

		@Override
		@Nullable
		protected HuntsmanBow getItem(ItemStack stack) {
			try {
				HuntsmanBow bow = new HuntsmanBow(stack);
				return bow;
			} catch (IllegalArgumentException e) {
				return null;
			}
		}

		@Override
		protected HuntsmanBow createItem(Player owner, HuntsmanBowLevels level) {
			return new HuntsmanBow(owner, level);
		}

	}
}
