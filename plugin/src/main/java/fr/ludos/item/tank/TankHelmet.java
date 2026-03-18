package fr.ludos.item.tank;

import java.util.List;
import java.util.ArrayList;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;

import fr.ludos.item.SpecialItem;
import fr.ludos.game.Game;
import fr.ludos.game.manhunt.ManhuntGame;
import fr.ludos.role.Role;
import fr.ludos.role.TankRole;

public class TankHelmet extends SpecialItem {
	private static final String ID = "tank_helmet";
	
	protected TankHelmet(ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);
	}

	public static @Nullable TankHelmet fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		Player owner = SpecialItem.getSpecialItemOwner(stack, ID, game);
		if (owner == null)
			return null;

		return new TankHelmet(stack, owner, game);
	}

	public static TankHelmet createItem(Player owner, Game game) {
		TankHelmet helmet = new TankHelmet(createItemStack(), owner, game);
		helmet.initializeItem();
		
		return helmet;
	}


	@Override
	public String getId() {
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
            super(game, 39 , false);
        }
        
        @Override
        @Nullable
        protected TankHelmet getItem(ItemStack stack, Game game) {
            return TankHelmet.fromItemStack(stack, game);
        }
        
        @Override
        protected TankHelmet createItem(Player owner, Game game) {
            return TankHelmet.createItem(owner, game);
        }
        
        @Override
        protected Boolean canPlayerHaveItem(HumanEntity owner) {
            return Role.isPlayerRole(owner, TankRole.id);
        }
    }
	
		
		

	
}