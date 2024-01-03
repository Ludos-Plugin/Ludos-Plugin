package fr.ludos.listener.items.burrower;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.ludos.Main;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;


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

public class BurrowerPickEvents implements Listener {

    private static final String OWNER_NAMEPSACE_KEY = "ludos_miner_pickaxe_owner";
    private static final String XP_NAMEPSACE_KEY = "ludos_miner_pickaxe_xp";
    private static final String LVL_NAMEPSACE_KEY = "ludos_miner_pickaxe_lvl";

    private static NamespacedKey ownerKey = null;
    private static NamespacedKey xpKey = null;
    private static NamespacedKey lvlKey = null;

    static NamespacedKey getOwnerkey() {
        return ownerKey;
    }
    static NamespacedKey getXpKey() {
        return xpKey;
    }
    static NamespacedKey getLvlKey() {
        return lvlKey;
    }


    public BurrowerPickEvents() {
        Main plugin = Main.getInstance();
        ownerKey = new NamespacedKey(plugin, OWNER_NAMEPSACE_KEY);
        xpKey = new NamespacedKey(plugin, XP_NAMEPSACE_KEY);
        lvlKey = new NamespacedKey(plugin, LVL_NAMEPSACE_KEY);
    }
    
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        
        BurrowerPick pick = BurrowerPick.getFrom(item);
        if ( pick != null ) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
        
        BurrowerPick pick = BurrowerPick.getFrom(item);
        if ( pick != null ) {
            event.setCancelled(true);
        }
    }

    // @EventHandler
    // public void onCraftItem(CraftItemEvent event) {
    //     ItemStack item = event.getCurrentItem();
    //     if (Pickaxe.isPickaxe(item) && Pickaxe.isOwnedBy(item, player)) {
    //         event.setCancelled(true);
    //     }
    // }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        BurrowerPick pick = BurrowerPick.getFrom(item);
        if ( pick != null ) {
            pick.awardBreak(player, event.getBlock());
        }
    }

    @EventHandler
    public void playerJoinTheGame(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.getInventory().addItem(
            BurrowerPick.createNew(player).getItem()
        );
    }

}
