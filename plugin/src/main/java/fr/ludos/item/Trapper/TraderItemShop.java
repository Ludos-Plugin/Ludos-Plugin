package fr.ludos.item.Trapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.EventHandler;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import javax.annotation.Nullable;

import java.util.Arrays;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.TrapperRole;

public class TraderItemShop extends SpecialItem {

    TraderItemShop(ItemStack stack) throws IllegalArgumentException {
        super(stack);
    }

    public TraderItemShop(Player owner) {
		this(new ItemStack(Material.ANVIL), owner);
	}

    protected TraderItemShop(ItemStack stack, Player owner) {
		super(stack, owner);
	}

	@Override
	public String getId() {
		return "manhunt_trader_shop";
	}

    @Override
    public String getName() {
        return "§6Trader Item Shop";
    }

    public static void openShop(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 9, "§6Trader Item Shop");

        inventory.addItem(createItem(Material.ARMOR_STAND, "§6TntTrap", 10));
        inventory.addItem(createItem(Material.ARMOR_STAND, "§6CobwebTrap", 15));
        inventory.addItem(createItem(Material.ARMOR_STAND, "§6BoostTrap", 15));
        inventory.addItem(createItem(Material.ARMOR_STAND, "§6GlowingTrap", 10));

        player.openInventory(inventory);
    }

    private static ItemStack createItem(Material material, String name, int price) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList("§7Price: §6" + price + " coins"));
        item.setItemMeta(meta);
        return item;
    }

    public static class Events extends SpecialItem.Events<TraderItemShop> {

		public Events() {
			super(TrapperRole.id);
		}

		public void playerEventListener(Player player, PlayerDeathEvent event, ItemStack trident) {
			player.getInventory().removeItem(trident);
		}

        @EventHandler
        public void onInteract(PlayerInteractEvent event) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                ItemStack item = event.getItem();
                TraderItemShop stack = getItem(item);
                if (stack == null) {
                    return;
                }
                openShop(event.getPlayer());
            }
        }

		@Override
		@Nullable
		protected TraderItemShop getItem(ItemStack stack) {
			try {
				TraderItemShop bow = new TraderItemShop(stack);
				return bow;
			} catch (IllegalArgumentException e) {
				return null;
			}
		}

		@Override
		protected TraderItemShop createItem(Player owner) {
			return new TraderItemShop(owner);
		}
	}
}