package fr.ludos.core.item;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public class SoulVial implements Listener {

	private static final String SOUL_VIAL_NAME = "Soul Vial";
	private static final int MAX_SOULS = 100;

	private static final Map<Player, Integer> soulCounts = new HashMap<>();

	/**
	 * Gets the Soul Vial item for the specified player.
	 *
	 * @param player The player for whom the Soul Vial is obtained.
	 * @return The Soul Vial ItemStack.
	 */

	public static ItemStack getSoulVial(Player player) {
		ItemStack soulVial = new ItemStack(Material.GLASS_BOTTLE);

		soulCounts.putIfAbsent(player, 0);

		int soulCount = soulCounts.get(player);

		ItemMeta meta = soulVial.getItemMeta();
		meta.displayName(
			Component.text(SOUL_VIAL_NAME)
				.color(NamedTextColor.RED)
			.decoration(TextDecoration.ITALIC, false)
		);

		if (soulCount > 0) {
			int red = (int) ((double) soulCount / MAX_SOULS * 255);
			int green = 255 - red;
			int blue = 0;

			((LeatherArmorMeta) meta).setColor(Color.fromRGB(red, green, blue));
		}

		meta.lore(java.util.Collections.singletonList(
			Component.text("Souls: ")
			.append(Component.text(soulCount)
				.color(NamedTextColor.DARK_RED))
			.decoration(TextDecoration.ITALIC, false)
		));

		soulVial.setItemMeta(meta);

		return soulVial;
	}

	/**
	 * Adds a specified number of souls to the player's soul count.
	 *
	 * @param player The player for whom souls are added.
	 * @param souls  The number of souls to add.
	 */

	public static void addSoul(Player player, int souls) {
		soulCounts.put(player, soulCounts.getOrDefault(player, 0) + souls);
	}

	/**
	 * Handles the onEntityDeath event, adding souls to the player's count based on the type of monster killed.
	 * Additionally, allows players to choose effects based on their soul count.
	 *
	 * @param event The EntityDeathEvent triggered when a living entity dies.
	 */

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity().getKiller() instanceof Player) {
			Player player = event.getEntity().getKiller();
			int soulsToAdd = determineSoulsToAdd(event.getEntityType());
			addSoul(player, soulsToAdd);
			player.updateInventory();

			if (soulCounts.get(player) % 50 == 0) {
				player.sendMessage("You have reached a milestone of " + soulCounts.get(player) + " souls!");
				openEffectSelectionMenu(player);
			}
		}
	}

	/**
	 * Opens a menu for the player to choose effects based on their soul count.
	 *
	 * @param player The player who will choose the effect.
	 */

	private void openEffectSelectionMenu(Player player) {
		Inventory menu = Bukkit.createInventory(player, 9, Component.text("Soul Vial Effects"));

		addPotionItem(menu, Material.POTION, "Speed", PotionEffectType.SPEED);
		addPotionItem(menu, Material.POTION, "Strength", PotionEffectType.INCREASE_DAMAGE);

		player.openInventory(menu);
	}

	/**
	 * Adds a potion item to the menu.
	 *
	 * @param inventory   The inventory to add the item to.
	 * @param potionType  The material representing the potion item.
	 * @param displayName The display name of the potion item.
	 * @param effectType  The PotionEffectType associated with the potion item.
	 */

	private void addPotionItem(Inventory inventory, Material potionType, String displayName, PotionEffectType effectType) {
		ItemStack potionItem = new ItemStack(potionType);
		ItemMeta meta = potionItem.getItemMeta();
		meta.displayName(
			Component.text(displayName)
			.decoration(TextDecoration.ITALIC, false)
		);
		potionItem.setItemMeta(meta);

		inventory.addItem(potionItem);

		inventory.setItem(inventory.getSize() - 1, potionItem);
	}

	/**
	 * Handles inventory click events.
	 *
	 * @param event The InventoryClickEvent triggered when a player clicks in the inventory.
	 */

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		ItemStack clickedItem = event.getCurrentItem();

		if (clickedItem != null && clickedItem.getType() == Material.POTION) {
			PotionMeta potionMeta = (PotionMeta) clickedItem.getItemMeta();
			PotionData potionData = potionMeta.getBasePotionData();
			PotionEffectType effectType = potionData.getType().getEffectType();


			int souls = soulCounts.getOrDefault(player, 0);
			int level = souls / 50 + 1;

			player.addPotionEffect(effectType.createEffect(20 * 60 * level, level - 1));

			player.sendMessage("You have chosen the effect: " + effectType.getName() + " Level " + level);
			player.closeInventory();
		}
	}

	/**
	 * Determines the number of souls to add based on the type of monster killed.
	 *
	 * @param entityType The EntityType of the killed monster.
	 * @return The number of souls to add.
	 */

	private int determineSoulsToAdd(EntityType entityType) {
		switch (entityType) {
			case ZOMBIE:
				return 1;
			case SKELETON:
				return 2;
			case SPIDER:
				return 3;
			case ENDERMAN:
				return 4;
			default:
				return 1;
		}
	}
}
