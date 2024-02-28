package fr.ludos.item;

import org.bukkit.persistence.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;


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

public abstract class LevelItem<TLevel extends SpecialItemLevels> extends SpecialItem {

	private static final String MAX_LVL_LABEL = "MAX";

	private TLevel level;
	private double xp;


	public abstract NamespacedKey getLvlKey();
	public abstract NamespacedKey getXpKey();


	public TLevel getLevel() {
		return level;
	}
	public double getXp() {
		return xp;
	}

	public abstract TLevel convertToLevel(int level);

	public LevelItem(ItemStack stack) throws IllegalArgumentException {
		super(stack);

		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
		if (
			! container.has(getLvlKey(), PersistentDataType.INTEGER) ||
			! container.has(getXpKey(), PersistentDataType.DOUBLE)
		) {
			throw new IllegalArgumentException();
		}

		this.level = convertToLevel(getLvlFromItem(stack, getLvlKey()));
		this.xp = getXpFromItem(stack, getXpKey());
	}

	public LevelItem(ItemStack stack, Player owner, TLevel level) {
		this(stack, owner, level, 0);
	}

	public LevelItem(ItemStack stack, Player owner, TLevel level, double xp) {
		super(stack, owner);

		setLvl(level);
		setXp(xp);
	}


	protected static int getLvlFromItem(ItemStack item, NamespacedKey key) {
		return getPersistentData(item, key, PersistentDataType.INTEGER);
	}

	protected static double getXpFromItem(ItemStack item, NamespacedKey key) {
		return getPersistentData(item, key, PersistentDataType.DOUBLE);
	}

	protected static Player getOwnerFromItem(ItemStack item, NamespacedKey key) {
		return Bukkit.getPlayer(
			UUID.fromString(
				getPersistentData(item, key, PersistentDataType.STRING)
			)
		);
	}

	public void setLvl(TLevel level) {
		ItemMeta meta = getStack().getItemMeta();

		this.level = level;
		meta.getPersistentDataContainer().set(getLvlKey(), PersistentDataType.INTEGER, level.index());
		getStack().setItemMeta(meta);
	}

	public abstract void addLvl();

	public void setXp(double value) {

		double scaledTreshold = level.getXpThreshold();
		if (value >= scaledTreshold) {
			addLvl();
			value -= scaledTreshold;
		}

		ItemMeta meta = getStack().getItemMeta();

		this.xp = value;
		meta.getPersistentDataContainer().set(getXpKey(), PersistentDataType.DOUBLE, xp);

		String xpFormatted = ChatColor.GRAY + "XP: " + ChatColor.YELLOW;

		if ( level.isMax() ) {
			xpFormatted += MAX_LVL_LABEL;
		} else {
			String xpRounded = Double.toString(Math.round(xp * 100.0) / 100.0);
			xpFormatted += xpRounded + '/' + level.getXpThreshold();
		}

		meta.setLore(Arrays.asList(getLore(), xpFormatted));
		getStack().setItemMeta(meta);
	}

	public void addXp(double xp) {
		if (level.isMax()) {
			return;
		}

		double newXp = this.xp + xp;
		setXp(newXp);
	}
}
