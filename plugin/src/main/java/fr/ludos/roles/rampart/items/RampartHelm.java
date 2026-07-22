package fr.ludos.roles.rampart.items;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.roles.rampart.RampartRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of the Rampart Helm, for use by any Player with {@link RampartRole}.
 */
public class RampartHelm extends SpecialItem<RampartHelm> {

	public static final String ID = "rampart_helm";

	RampartHelm(SpecialItem.ItemData info, Events events) {
		super(info, events);
	}


	@Override
	public Component getName() {
		return Component.text("Rampart Helm")
				.decoration(TextDecoration.ITALIC, false);
	}

	/**
	 * Events for the {@link RampartHelm}.
	 */
	public static class Events extends SpecialItem.Events<RampartHelm> {

		public Events(Game game) {
			super(game, new Events.Info(ItemSlot.HELMET));
		}

		@Override
		public String getTypeId() {
			return ID;
		}

		@Override
		protected RampartHelm getItemInternal(SpecialItem.ItemData info) {
			return new RampartHelm(info, this);
		}

		@Override
		protected RampartHelm createItemInternal(Player owner) {
			return new RampartHelm(new SpecialItem.ItemData(createItemStack(), owner), this);
		}

		private static ItemStack createItemStack() {
			ItemStack stack = new ItemStack(Material.IRON_HELMET);
			stack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
			stack.addUnsafeEnchantment(Enchantment.THORNS, 1);
			stack.addUnsafeEnchantment(Enchantment.DURABILITY, 3);

			return stack;
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.getLudos().getRoleManager().isPlayerRole(owner, RampartRole.ID);
		}
	}
}