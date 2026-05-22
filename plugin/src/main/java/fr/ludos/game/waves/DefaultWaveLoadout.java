package fr.ludos.game.waves;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.ludos.game.Game;
import fr.ludos.item.SpecialItem;

public class DefaultWaveLoadout extends WaveLoadoutService {
	public DefaultWaveLoadout(Game game) {
		super(game);
	}

	@Override
	protected void applyBaseKit(Player player)  {
		PlayerInventory inventory = player.getInventory();
		inventory.clear();

		ItemStack sword = enchantedItem(Material.DIAMOND_SWORD, Enchantment.DAMAGE_ALL, 3);
		inventory.setItem(0, sword);

		ItemStack helmet = enchantedItem(Material.DIAMOND_HELMET, Enchantment.PROTECTION_ENVIRONMENTAL, 3);
		ItemStack chestplate = enchantedItem(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION_ENVIRONMENTAL, 3);
		ItemStack leggings = enchantedItem(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION_ENVIRONMENTAL, 3);
		ItemStack boots = enchantedItem(Material.DIAMOND_BOOTS, Enchantment.PROTECTION_ENVIRONMENTAL, 3);

		inventory.setArmorContents(new ItemStack[] { boots, leggings, chestplate, helmet });

		SpecialItem.Events.refreshPlayerInventory(getGame(), player);
	}
}