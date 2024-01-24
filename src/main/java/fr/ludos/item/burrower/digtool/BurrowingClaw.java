package fr.ludos.item.burrower.digtool;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;

import fr.ludos.item.SpecialItem;


/**
 * Pickaxe is a class that represents a special item, "Miner Pickaxe," in Minecraft.
 * This item allows the miner player to improve their own pickaxe based on the ores they collect.
 * <br><br>
 * Features:
 * <br><br>
 * - Provides methods to give a miner pickaxe to the player and level up the pickaxe based on XP.
 * <br><br>
 * - Automatically updates the pickaxe's material as it levels up.
 * <br><br>
 * - Defines an evolution path from wood to stone, iron, gold, and finally diamond pickaxe.
 * <br><br>
 * Usage:
 * <br><br>
 * - Call addPickaxeInventory(player) to give a miner pickaxe to the specified player.
 * <br><br>
 * - Call levelPickaxe(player, xp) with the XP gained from mining to level up the pickaxe.
 * <br><br>
 * Example:
 * <pre>{@code
 * Pickaxe pickaxe = new Pickaxe();
 * pickaxe.addPickaxeInventory(player);
 * pickaxe.levelPickaxe(player, xp);
 * }</pre>
 * <br><br>
 * @author feur25 & Ganon358
 * @version 1.0
 * @see org.bukkit.entity.Player
 * @see org.bukkit.inventory.ItemStack
 * @see org.bukkit.Material
 * @see org.bukkit.inventory.meta.ItemMeta
 * @see java.util.Collections
 */

public class BurrowingClaw extends SpecialItem {

    private int usages = 0;

    public int getUsages() {
        return usages;
    }

    public BurrowingClaw(ItemStack stack) throws IllegalArgumentException {
        super(stack);

        PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
        if ( ! container.has(BurrowingClawEvents.getUsagesKey(), PersistentDataType.INTEGER) ) {
            throw new IllegalArgumentException();
        }

        this.usages = getPersistentData(stack, BurrowingClawEvents.getUsagesKey(), PersistentDataType.INTEGER);
    }

    public BurrowingClaw(Player owner) {
        this(new ItemStack(Material.RABBIT_FOOT), owner);
    }
    public BurrowingClaw(ItemStack stack, Player owner) {
        super(stack, owner);

        setUsages(300);
    }

    public void setUsages(int usages) {
        ItemMeta meta = getStack().getItemMeta();

        this.usages = usages;
        meta.getPersistentDataContainer().set(BurrowingClawEvents.getUsagesKey(), PersistentDataType.INTEGER, usages);
        getStack().setItemMeta(meta);
    }

    
    /**
     * @param inventory
     * @return true if the provided inventory contains a Burrowering Claw
     */
    public static Boolean containedIn(Inventory inventory) {
        ItemStack[] items = inventory.getContents();
        for (int i = 0; i < items.length; i++) {
            try {
                new BurrowingClaw(items[i]);
                return true;
            } catch (IllegalArgumentException e) {
                continue;
            }
        }

        return false;
    }

    /**
     * @param inventory
     * @return true if the provided inventory contains a Burrower's pick
     */
    public static BurrowingClaw findIn(Inventory inventory) {
        ItemStack[] items = inventory.getContents();
        for (int i = 0; i < items.length; i++) {
            try {
                BurrowingClaw pick = new BurrowingClaw(items[i]);
                return pick;
            } catch (IllegalArgumentException e) {
                continue;
            }
        }

        return null;
    }

    public void SetUsages(int usages) {

    }

	@Override
	public NamespacedKey getOwnerKey() {
        return BurrowingClawEvents.getOwnerKey();
	}


	@Override
	protected String getLore() {
        return "";
	}

	@Override
	protected String getName() {
        return ChatColor.LIGHT_PURPLE + "Stick of Burrowing"; // TODO: Translate
	}
}