package fr.ludos.core.wave;

import org.apache.commons.lang3.tuple.Pair;
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

		ItemStack sword = enchantedItem(
			Material.DIAMOND_SWORD,
				Pair.of(Enchantment.DAMAGE_ALL, 3),
				Pair.of(Enchantment.MENDING, 1)
		);
		ItemSlot.HOTBAR_1.set(sword, inventory);

		ItemStack helmet = enchantedItem(
			Material.DIAMOND_HELMET,
				Pair.of(Enchantment.PROTECTION_ENVIRONMENTAL, 3),
				Pair.of(Enchantment.MENDING, 1)
		);
		ItemSlot.HELMET.set(helmet, inventory);

		ItemStack chestplate = enchantedItem(
			Material.DIAMOND_CHESTPLATE,
				Pair.of(Enchantment.PROTECTION_ENVIRONMENTAL, 3),
				Pair.of(Enchantment.MENDING, 1)
		);
		ItemSlot.CHESTPLATE.set(chestplate, inventory);

		ItemStack leggings = enchantedItem(
			Material.DIAMOND_LEGGINGS,
				Pair.of(Enchantment.PROTECTION_ENVIRONMENTAL, 3),
				Pair.of(Enchantment.MENDING, 1)
		);
		ItemSlot.LEGGINGS.set(leggings, inventory);

		ItemStack boots = enchantedItem(
			Material.DIAMOND_BOOTS,
				Pair.of(Enchantment.PROTECTION_ENVIRONMENTAL, 3),
				Pair.of(Enchantment.MENDING, 1)
		);
		ItemSlot.BOOTS.set(boots, inventory);

		SpecialItem.Events.refreshPlayerInventory(game(), player);
	}
}