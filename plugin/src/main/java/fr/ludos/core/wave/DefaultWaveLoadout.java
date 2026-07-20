package fr.ludos.core.wave;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;

/**
 * Default implementation of a wave loadout providing the base kit
 * applied to players when a wave starts.
 */
public class DefaultWaveLoadout extends WaveLoadoutService {
	/**
	 * Constructs the default wave loadout for the given game.
	 *
	 * @param game the game instance
	 */
	public DefaultWaveLoadout(Game game) {
		super(game);
	}

	@Override
	protected void applyBaseKit(Player player)  {
		PlayerInventory inventory = player.getInventory();

		ItemStack sword = enchantedItem(Material.DIAMOND_SWORD, Enchantment.DAMAGE_ALL, 3);
		ItemSlot.setItemInInventory(ItemSlot.HOTBAR_1, sword, inventory);

		ItemStack helmet = enchantedItem(Material.DIAMOND_HELMET, Enchantment.PROTECTION_ENVIRONMENTAL, 3);
		ItemSlot.setItemInInventory(ItemSlot.HELMET, helmet, inventory);

		ItemStack chestplate = enchantedItem(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION_ENVIRONMENTAL, 3);
		ItemSlot.setItemInInventory(ItemSlot.CHESTPLATE, chestplate, inventory);

		ItemStack leggings = enchantedItem(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION_ENVIRONMENTAL, 3);
		ItemSlot.setItemInInventory(ItemSlot.LEGGINGS, leggings, inventory);

		ItemStack boots = enchantedItem(Material.DIAMOND_BOOTS, Enchantment.PROTECTION_ENVIRONMENTAL, 3);
		ItemSlot.setItemInInventory(ItemSlot.BOOTS, boots, inventory);

		SpecialItem.Events.refreshPlayerInventory(getGame(), player);
	}
}