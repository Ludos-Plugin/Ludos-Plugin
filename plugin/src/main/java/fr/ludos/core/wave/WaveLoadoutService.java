package fr.ludos.core.wave;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.game.Game;

/**
 * Service used to apply wave-specific loadouts to players.
 */
public abstract class WaveLoadoutService {
	private final Game game;
	public final Game getGame() {
		return this.game;
	}

	public WaveLoadoutService(Game game) {
		this.game = game;
	}

	protected abstract void applyBaseKit(Player player);

	protected ItemStack enchantedItem(Material type, Enchantment enchantment, int level) {
		ItemStack stack = new ItemStack(type);
		stack.addUnsafeEnchantment(enchantment, level);
		return stack;
	}
}
