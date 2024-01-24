package fr.ludos.item.burrower.pick;

import java.util.Collections;
import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.ludos.Main;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;

import fr.ludos.role.BurrowerRole;
import fr.ludos.role.Role;

import org.bukkit.event.enchantment.EnchantItemEvent;



/**
 * BurrowerPick is a class that represents a special item, "The Burrower's Pickaxe," in Minecraft.
 * This item allows the miner player to improve their own pickaxe based on the ores they collect.
 * <br><br>
 * Features:
 * <br><br>
 * - Provides methods to create a miner pickaxe, give it to the player and level up the pickaxe based on XP.
 * <br><br>
 * - Automatically updates the pickaxe's material and enchantments as it levels up.
 * <br><br>
 * - Defines an evolution path from wood to stone, iron, gold, diamond and finally netherite pickaxe.
 * <br><br>
 * Usage:
 * Example:
 * @author feur25 & Ganon358
 * @version 1.0
 * @see org.bukkit.entity.Player
 * @see org.bukkit.inventory.ItemStack
 * @see org.bukkit.Material
 * @see org.bukkit.inventory.meta.ItemMeta
 * @see java.util.Collections
 */

public class BurrowerPickEvents implements Listener {

    private static final String OWNER_NAMESPACE_KEY = "ludos_miner_pickaxe_owner";
    private static final String XP_NAMESPACE_KEY = "ludos_miner_pickaxe_xp";
    private static final String LVL_NAMESPACE_KEY = "ludos_miner_pickaxe_lvl";

    private static NamespacedKey ownerKey = null;
    private static NamespacedKey xpKey = null;
    private static NamespacedKey lvlKey = null;

    private static Map<String, BurrowerPickLevels> deadPlayerLevels = Collections.emptyMap();

    

    static NamespacedKey getOwnerKey() {
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
        ownerKey = new NamespacedKey(plugin, OWNER_NAMESPACE_KEY);
        xpKey = new NamespacedKey(plugin, XP_NAMESPACE_KEY);
        lvlKey = new NamespacedKey(plugin, LVL_NAMESPACE_KEY);
    }
    
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        
        try {
            new BurrowerPick(item);
            event.setCancelled(true);
        } finally {

        }
    }
    @EventHandler
    public void onInventoryClickItem(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();

        try {
            new BurrowerPick(item);
            if ( event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.getInventory() != event.getClickedInventory() ) {
                event.setResult(Result.DENY);
            }
        } finally {
            
        }

    }
    // @EventHandler
    // public void onInventoryMoveItem(InventoryMoveItemEvent event) {
    //     Bukkit.broadcastMessage(event.getDestination().toString());
    //     ItemStack item = event.getItem();

    //     Bukkit.broadcastMessage(item.toString());
    //     BurrowerPick pick = BurrowerPick.getFrom(item);

    //     if (pick != null && event.getDestination().getType() != InventoryType.PLAYER) {
    //         event.setCancelled(true);
    //     }
    // }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
        
        try {
            new BurrowerPick(item);
            event.setCancelled(true);
        } finally {

        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        try {
            BurrowerPick pick = new BurrowerPick(item);
            pick.awardBreak(player, event.getBlock());
        } finally {

        }
    }


    @EventHandler
    public void playerJoinTheGame(PlayerJoinEvent event) {
        actuatePickInventory(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if ( ! Role.isPlayerRole(player, BurrowerRole.id) ) {
            return;
        }

        BurrowerPick pick = BurrowerPick.findIn(player.getInventory());
        if ( pick != null ) {
            Integer index = pick.getLevel().index() - 1;
            index = Math.max(0, index);
            deadPlayerLevels.put( player.getName(), BurrowerPickLevels.values()[index] );
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event)  {
        actuatePickInventory(event.getPlayer());
    }


    private void actuatePickInventory(Player player) {
        if ( ! Role.isPlayerRole(player, BurrowerRole.id) ) {
            return;
        }

        Inventory inventory = player.getInventory();
        if ( BurrowerPick.containedIn(inventory) ) {
            return;
        }


        BurrowerPickLevels level = BurrowerPickLevels.WOODEN;
        if (player != null && deadPlayerLevels.containsKey(player.getName())) {
            level = deadPlayerLevels.get(player.getName());
        }
        inventory.addItem(
            new BurrowerPick(player, level).getStack()
        );
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        ItemStack item = event.getItem();

        // Vérifie si l'arme est l'arme spécifique que vous voulez protéger du désenchantement
        try {
            new BurrowerPick(item);
            event.setCancelled(true);
        } finally {

        }
    }
}
