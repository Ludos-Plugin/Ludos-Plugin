package fr.ludos.item;

import org.bukkit.persistence.*;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;


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

public abstract class SpecialItem {

	private final ItemStack stack;

	private final Player owner;

	public ItemStack getStack() {
		return stack;
	}

	public Player getOwner() {
		return owner;
	}

	protected abstract String getLore();
	protected abstract String getName();


	public abstract NamespacedKey getOwnerKey();


	public SpecialItem(ItemStack stack) throws IllegalArgumentException {
		if (stack == null) {
			throw new IllegalArgumentException();
		}
		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
		if ( ! container.has(getOwnerKey(), PersistentDataType.STRING) ) {
			throw new IllegalArgumentException();
		}

		this.stack = stack;
		this.owner = LevelItem.getOwnerFromItem(stack, getOwnerKey());
	}

	public SpecialItem(ItemStack stack, Player owner) {
		this.stack = stack;
		this.owner = owner;

		ItemMeta meta = stack.getItemMeta();

		meta.setDisplayName(ChatColor.RESET.toString() + ChatColor.BOLD + getName());
		meta.setUnbreakable(true);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

		PersistentDataContainer container = meta.getPersistentDataContainer();
		container.set(getOwnerKey(), PersistentDataType.STRING, owner.getUniqueId().toString());

		stack.setItemMeta(meta);
	}

	/**
	 * @param inventory
	 * @return true if the provided inventory contains a Burrowering Claw
	 */
	public static <T extends SpecialItem> Boolean containedIn(Inventory inventory, Function<ItemStack, T> constructor) {
		ItemStack[] items = inventory.getContents();
		for (int i = 0; i < items.length; i++) {
			ItemStack item = items[i];
			if (item == null) {
				continue;
			}
			try {
				constructor.apply(item);
				return true;
			} catch (IllegalArgumentException e) {
				continue;
			}
		}

		return false;
	}


	/**
	 * @param inventory
	 * @return true if the provided inventory contains a Burrower's pick
	 */
	public static <T extends SpecialItem>T findIn(Inventory inventory, Function<ItemStack, T> constructor) {
		ItemStack[] items = inventory.getContents();
		for (int i = 0; i < items.length; i++) {
			ItemStack item = items[i];
			if (item == null) {
				continue;
			}
			try {
				T specialItem = constructor.apply(item);
				return specialItem;
			} catch (IllegalArgumentException e) {
				continue;
			}
		}

		return null;
	}

	protected static <T, Z> Z getPersistentData(ItemStack item, NamespacedKey key, PersistentDataType<T, Z> type) {
		return item.getItemMeta().getPersistentDataContainer().get(key, type);
	}
}
