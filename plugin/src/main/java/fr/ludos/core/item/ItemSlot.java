package fr.ludos.core.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Developper-Friendly wrapper for {@link PlayerInventory} item slots.
 */
public enum ItemSlot {
	HOTBAR_1,
	HOTBAR_2,
	HOTBAR_3,
	HOTBAR_4,
	HOTBAR_5,
	HOTBAR_6,
	HOTBAR_7,
	HOTBAR_8,
	HOTBAR_9,
	TOP_1,
	TOP_2,
	TOP_3,
	TOP_4,
	TOP_5,
	TOP_6,
	TOP_7,
	TOP_8,
	TOP_9,
	MID_1,
	MID_2,
	MID_3,
	MID_4,
	MID_5,
	MID_6,
	MID_7,
	MID_8,
	MID_9,
	BOT_1,
	BOT_2,
	BOT_3,
	BOT_4,
	BOT_5,
	BOT_6,
	BOT_7,
	BOT_8,
	BOT_9,
	BOOTS,
	LEGGINGS,
	CHESTPLATE,
	HELMET,
	OFFHAND,
	MAX_VALUE;

	public static final ItemSlot[] ARROW_ORDER = new ItemSlot[] {
		OFFHAND,
		HOTBAR_1, HOTBAR_2, HOTBAR_3, HOTBAR_4, HOTBAR_5, HOTBAR_6, HOTBAR_7, HOTBAR_8, HOTBAR_9,
		TOP_1, TOP_2, TOP_3, TOP_4, TOP_5, TOP_6, TOP_7, TOP_8, TOP_9,
		MID_1, MID_2, MID_3, MID_4, MID_5, MID_6, MID_7, MID_8, MID_9,
		BOT_1, BOT_2, BOT_3, BOT_4, BOT_5, BOT_6, BOT_7, BOT_8, BOT_9
	};

	public static void set(ItemSlot slot, ItemStack item, PlayerInventory inventory) {
		if (slot == null) {
			inventory.addItem(item);
			return;
		}
		slot.set(item, inventory);
	}

	public void set(ItemStack item, PlayerInventory inventory) {
		int index = ordinal();

		if (index < 0 || index >= MAX_VALUE.ordinal() || inventory.getItem(index) != null) {
			inventory.addItem(item);
		} else {
			inventory.setItem(index, item);
		}
	}

	public ItemStack get(PlayerInventory inventory) {
		return inventory.getItem(ordinal());
	}
}
