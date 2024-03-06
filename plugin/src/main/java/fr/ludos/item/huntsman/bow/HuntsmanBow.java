package fr.ludos.item.huntsman.bow;

import fr.ludos.item.LevelItem;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import org.bukkit.ChatColor;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;


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

	public HuntsmanBow(ItemStack item, Player owner) {
		this(item, owner, HuntsmanBowLevels.BASE);
	}
	public HuntsmanBow(ItemStack item, Player owner, HuntsmanBowLevels level) {
		this(item, owner, level, 0);
	}
	public HuntsmanBow(ItemStack item, Player owner, HuntsmanBowLevels level, double xp) {
		super(item, owner, level, xp);
	}


	@Override
	public HuntsmanBowLevels convertToLevel(int level) {
		return HuntsmanBowLevels.findByKey(level);
	}

	@Override
	public NamespacedKey getOwnerKey() {
		return HuntsmanBowEvents.getOwnerkey();
	}

	@Override
	public NamespacedKey getLvlKey() {
		return HuntsmanBowEvents.getLvlKey();
	}

	@Override
	public NamespacedKey getXpKey() {
		return HuntsmanBowEvents.getXpKey();
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


		HuntsmanBowLevels[] evolutions = getLevel().getEvolutions();

		if (evolutions.length == 0) {
			return;
		} else if (evolutions.length == 1) {

		} else {

		}

		if (getOwner() != null) {
			getOwner().sendMessage(ChatColor.GREEN + "Your pickaxe has leveled up!"); // TODO: Translate
		}
	}
}
