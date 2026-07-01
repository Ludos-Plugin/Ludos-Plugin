package fr.ludos.roles.tank.items;

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
import fr.ludos.core.role.Role;
import fr.ludos.roles.tank.TankRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class TankHelmet extends SpecialItem {
	private static final String ID = "tank_helmet";

	protected TankHelmet(ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);
	}

	public static @Nullable TankHelmet fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItemInterface.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// TrapperDagger cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItemInterface.getSpecialItemOwner(stack, game);
		if (owner == null) return null;

		TankHelmet helmet = new TankHelmet(stack, owner, game);
		// cachedItems.put(itemId, dagger);

		return helmet;
	}

	public static TankHelmet createItem(Player owner, Game game) {
		TankHelmet helmet = new TankHelmet(createItemStack(), owner, game);
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
		return Component.text("Tank Helmet")
				.decoration(TextDecoration.ITALIC, false);
	}

	private static ItemStack createItemStack() {
		ItemStack stack = new ItemStack(Material.IRON_HELMET);
		stack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
		stack.addUnsafeEnchantment(Enchantment.THORNS, 1);
		stack.addUnsafeEnchantment(Enchantment.DURABILITY, 3);


		return stack;
	}

	public static class Events extends SpecialItem.Events<TankHelmet> {

		public Events(Game game) {
			super(game, ItemSlot.HELMET , false);
		}

		@Override
		@Nullable
		public TankHelmet getItem(ItemStack stack) {
			return TankHelmet.fromItemStack(stack, game);
		}

		@Override
		public TankHelmet createItem(Player owner) {
			return TankHelmet.createItem(owner, game);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return Role.isPlayerRole(owner, TankRole.id);
		}
	}
}