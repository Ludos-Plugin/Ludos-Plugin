package fr.ludos.item.burrower;

import org.bukkit.persistence.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.UUID;

import javax.annotation.Nullable;


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

public class BurrowerPick {

    private static final double LEVEL_UP_THRESHOLD = 1;
    private static final String MAX_LVL_LABEL = "MAX";

    private final ItemStack item;
    
    @Nullable
    private final Player owner;
    private BurrowerPickLevels level;
    private double xp;

    public ItemStack getItem() {
        return item;
    }

    @Nullable
    public Player getOwner() {
        return owner;
    }
    public BurrowerPickLevels getLevel() {
        return level;
    }
    public double getXp() {
        return xp;
    }


    BurrowerPick(ItemStack item, Player owner, BurrowerPickLevels level, double xp) {
        this.item = item;
        this.owner = owner;
        this.level = level;
        this.xp = xp;
    }


    /**
     * @param inventory
     * @return true if the provided inventory contains a Burrower's pick
     */
    public static Boolean containedIn(Inventory inventory) {
        ItemStack[] items = inventory.getContents();
        for (int i = 0; i < items.length; i++) {
            if ( getFrom(items[i]) != null ) {
                return true;
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
            BurrowerPick pick = getFrom(items[i]);
            if (pick != null) {
                return pick;
            }
        }

        return null;
    }

    @Nullable
    public static BurrowerPick getFrom(ItemStack item) {
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (
            ! container.has(BurrowerPickEvents.getOwnerkey(), PersistentDataType.STRING) ||
            ! container.has(BurrowerPickEvents.getLvlKey(), PersistentDataType.INTEGER) ||
            ! container.has(BurrowerPickEvents.getXpKey(), PersistentDataType.DOUBLE)
        ) {
            return null;
        }

        Player owner = getOwnerFromItem(item);
        BurrowerPickLevels level = getLvlFromItem(item);
        double xp = getXpFromItem(item);
        
        return new BurrowerPick(item, owner, level, xp);
    }

    public static BurrowerPick createFrom(ItemStack item, Player owner, BurrowerPickLevels level, double xp) {
        BurrowerPick pick = new BurrowerPick(item, owner, level, xp);

        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.RESET.toString() + ChatColor.BOLD + "Burrower's Pick"); // TODO: Translate
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(BurrowerPickEvents.getOwnerkey(), PersistentDataType.STRING, owner.getUniqueId().toString());
        item.setItemMeta(meta);

        pick.setLvl(level);
        pick.setXp(xp);

        return pick;
    }

    public static BurrowerPick createNew(Player owner) {
        BurrowerPickLevels level = BurrowerPickLevels.WOODEN;
        
        return createNew(owner, level);
    }

    public static BurrowerPick createNew(Player owner, BurrowerPickLevels level) {
        ItemStack item = new ItemStack(level.getMaterial());
        BurrowerPick pick = createFrom(item, owner, level, 0);

        return pick;
    }
    
    
    private static BurrowerPickLevels getLvlFromItem(ItemStack item) {
        return BurrowerPickLevels.findByKey(item.getItemMeta().getPersistentDataContainer().get(BurrowerPickEvents.getLvlKey(), PersistentDataType.INTEGER));
    }

    private static double getXpFromItem(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(BurrowerPickEvents.getXpKey(), PersistentDataType.DOUBLE);
    }

    private static Player getOwnerFromItem(ItemStack item) {
        return Bukkit.getPlayer(
            UUID.fromString(
                item.getItemMeta().getPersistentDataContainer().get(BurrowerPickEvents.getOwnerkey(), PersistentDataType.STRING)
            )
        );
    }


    public void awardBreak(Player player, Block block) {
        if (owner != player) {
            return;
        }

        double oreXp = BurrowerPick.getOreReward(block);
        if (oreXp != 0) {
            addXp(oreXp);
        }
    }


    public void setLvl(BurrowerPickLevels level) {
        ItemMeta meta = item.getItemMeta();

        this.level = level;
        meta.getPersistentDataContainer().set(BurrowerPickEvents.getLvlKey(), PersistentDataType.INTEGER, level.index());
        item.setItemMeta(meta);
        item.setType(level.getMaterial());
        item.addEnchantments(level.getEnchantments());
    }
    
    public void addLvl() {
        if (level.isMax()) {
            return;
        }

        setLvl(BurrowerPickLevels.getNextLevel(level));
        if (owner != null) {
            owner.sendMessage(ChatColor.GREEN + "Your pickaxe has leveled up!"); // TODO: Translate
        }
    }
    
    public void setXp(double value) {

        double scaledTreshold = LEVEL_UP_THRESHOLD * level.xpThreshold();
        if (value >= scaledTreshold) {
            addLvl();
            value -= scaledTreshold;
        }

        ItemMeta meta = item.getItemMeta();
        
        this.xp = value;
        meta.getPersistentDataContainer().set(BurrowerPickEvents.getXpKey(), PersistentDataType.DOUBLE, xp);

        String xpFormatted = ChatColor.GRAY + "XP: " + ChatColor.YELLOW;

        if ( level.isMax() ) {
            xpFormatted += MAX_LVL_LABEL;
        } else {
            String xpRounded = Double.toString(Math.round(xp * 100.0) / 100.0);
            xpFormatted += xpRounded + '/' + LEVEL_UP_THRESHOLD * level.xpThreshold();
        }

        meta.setLore(Collections.singletonList(xpFormatted));
        item.setItemMeta(meta);
    }

    public void addXp(double xp) {
        if (level.isMax()) {
            return;
        }

        double newXp = this.xp + xp;
        setXp(newXp);
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
            case TUFF:
            case CALCITE:
            case ANDESITE:
            case DIORITE:
            case GRANITE:
            case STONE:
                return 1;
            default:
                return 0;
        }
    }
}
