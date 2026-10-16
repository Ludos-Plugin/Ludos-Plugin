package fr.ludos.core.wave;

import org.apache.commons.lang3.tuple.Pair;
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
	public final Game game() {
		return this.game;
	}

	public WaveLoadoutService(Game game) {
		this.game = game;
	}

	protected abstract void applyBaseKit(Player player);

	@SafeVarargs
	protected final ItemStack enchantedItem(Material type, Pair<Enchantment, Integer>... enchantments) {
		ItemStack stack = new ItemStack(type);
		for (Pair<Enchantment, Integer> enchantment : enchantments) {
			if (enchantment == null || enchantment.getLeft() == null || enchantment.getRight() == null) continue;
			stack.addUnsafeEnchantment(enchantment.getLeft(), enchantment.getRight());
		}
		return stack;
	}
}
