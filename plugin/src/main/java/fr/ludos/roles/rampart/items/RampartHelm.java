package fr.ludos.roles.rampart.items;

import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.roles.rampart.RampartRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of the Rampart Helm, for use by any Player with {@link RampartRole}.
 */
public class RampartHelm extends SpecialItem {
	public static final String ID = "rampart_helm";

	protected RampartHelm(ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);
	}

	public static @Nullable RampartHelm fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItemInterface.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// TrapperDagger cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItemInterface.getSpecialItemOwner(stack, game);
		if (owner == null) return null;

		RampartHelm helmet = new RampartHelm(stack, owner, game);
		// cachedItems.put(itemId, dagger);

		return helmet;
	}

	public static RampartHelm createItem(Player owner, Game game) {
		RampartHelm helmet = new RampartHelm(createItemStack(), owner, game);
		helmet.initializeItem();

		// cachedItems.put(itemId, dagger);

		return helmet;
	}


	@Override
	public String getTypeId() {
		return ID;
	}

	@Override
	public Component getName() {
		return Component.text("Rampart Helm")
				.decoration(TextDecoration.ITALIC, false);
	}

	private static ItemStack createItemStack() {
		ItemStack stack = new ItemStack(Material.IRON_HELMET);
		stack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
		stack.addUnsafeEnchantment(Enchantment.THORNS, 1);
		stack.addUnsafeEnchantment(Enchantment.DURABILITY, 3);


		return stack;
	}

	/**
	 * Events for the {@link RampartHelm}.
	 */
	public static class Events extends SpecialItem.Events<RampartHelm> {

		public Events(Game game) {
			super(game, new Events.Info(ItemSlot.HELMET));
		}

		@Override
		@Nullable
		public RampartHelm getItem(ItemStack stack) {
			return RampartHelm.fromItemStack(stack, game);
		}

		@Override
		public RampartHelm createItem(Player owner) {
			return RampartHelm.createItem(owner, game);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.getLudos().getRoleManager().isPlayerRole(owner, RampartRole.ID);
		}
	}
}