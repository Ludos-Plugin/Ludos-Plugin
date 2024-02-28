package fr.ludos.item.burrower.pick;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.ludos.item.LevelItem;


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

public class BurrowerPick extends LevelItem<BurrowerPickLevels> {

    public BurrowerPick(ItemStack stack) throws IllegalArgumentException {
        super(stack);
    }

    public BurrowerPick(Player owner) {
        this(owner, BurrowerPickLevels.WOODEN);
    }
    public BurrowerPick(Player owner, BurrowerPickLevels level) {
        super(new ItemStack(level.getMaterial()), owner, level);
    }

    public BurrowerPick(ItemStack item, Player owner) {
        this(item, owner, BurrowerPickLevels.WOODEN);
    }
    public BurrowerPick(ItemStack item, Player owner, BurrowerPickLevels level) {
        this(item, owner, level, 0);
    }
    public BurrowerPick(ItemStack item, Player owner, BurrowerPickLevels level, double xp) {
        super(item, owner, level, xp);
    }

    
    /**
     * @param inventory
     * @return true if the provided inventory contains a Burrower's pick
     */
    public static Boolean containedIn(Inventory inventory) {
        ItemStack[] items = inventory.getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null) {
                continue;
            }
            try {
                new BurrowerPick(item);
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
    public static BurrowerPick findIn(Inventory inventory) {
        ItemStack[] items = inventory.getContents();
        for (int i = 0; i < items.length; i++) {
            try {
                BurrowerPick pick = new BurrowerPick(items[i]);
                return pick;
            } catch (IllegalArgumentException e) {
                continue;
            }
        }

        return null;
    }

	@Override
	public BurrowerPickLevels convertToLevel(int level) {
        return BurrowerPickLevels.findByKey(level);
	}

	@Override
	public NamespacedKey getOwnerKey() {
        return BurrowerPickEvents.getOwnerKey();
	}

	@Override
	public NamespacedKey getLvlKey() {
        return BurrowerPickEvents.getLvlKey();
	}

	@Override
	public NamespacedKey getXpKey() {
        return BurrowerPickEvents.getXpKey();
	}

	@Override
	protected String getLore() {
        return "";
	}

	@Override
	protected String getName() {
        return "Burrower's Pick"; // TODO: Translate
	}

    public void awardBreak(Player player, Block block) {
        if (getOwner() != player) {
            return;
        }

        double oreXp = BurrowerPick.getOreReward(block);
        if (oreXp != 0) {
            addXp(oreXp);
        }
    }

    public void addLvl() {
        if (getLevel().isMax()) {
            return;
        }

        setLvl(BurrowerPickLevels.getNextLevel(getLevel()));
        if (getOwner() != null) {
            getOwner().sendMessage(ChatColor.GREEN + "Your pickaxe has leveled up!"); // TODO: Translate
        }
    }
    
    
    public static double getOreReward(Block ore){
        switch (ore.getType()) {
           case ANCIENT_DEBRIS:
                return 60;
            case EMERALD_ORE:
                return 50;
            case DIAMOND_ORE:
                return 45;
            case GOLD_ORE:
                return 40;
            case REDSTONE_ORE:
                return 35;
            case LAPIS_ORE:
                return 30;
            case NETHER_QUARTZ_ORE:
                return 25;
            case IRON_ORE:
                return 20;
            case OBSIDIAN:
                return 15;
            case COAL_ORE:
                return 10;
            case COPPER_ORE:
                return 5;
            case STONE:
                return 1;
            default:
                return 0;
        }
    }
}
