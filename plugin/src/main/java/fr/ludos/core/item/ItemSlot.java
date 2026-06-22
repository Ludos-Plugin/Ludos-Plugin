package fr.ludos.core.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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
	OFFHAND;

	public static void setItemInInventory(ItemSlot slot, ItemStack item, PlayerInventory inventory) {
		int index = slot != null
			? slot.ordinal()
			: -1;

		if (index == -1 || inventory.getItem(index) != null) {
			inventory.addItem(item);
		} else {
			inventory.setItem(index, item);
		}
	}
}
