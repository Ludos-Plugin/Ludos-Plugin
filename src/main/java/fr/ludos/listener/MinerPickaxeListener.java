package fr.ludos.listener;

import org.bukkit.event.Listener;

/**
 * MiningListener is a Bukkit event listener that handles mining-related events, specifically BlockBreakEvent.
 * It checks for the player's use of a pickaxe and if the broken block is a mineable ore. If conditions are met,
 * it calculates the XP gain based on the ore type and updates the player's pickaxe level using the Pickaxe class.
 * <br><br>
 * Features:
 * <br><br>
 * - Listens for BlockBreakEvent to detect mining actions.
 * <br><br>
 * - Checks for the use of a pickaxe and whether the broken block is a mineable ore.
 * <br><br>
 * - Calculates XP gain based on the type of ore and updates the player's pickaxe level.
 * <br><br>
 * Usage:
 * <br><br>
 * - Register an instance of MiningListener in your plugin to listen for mining events.
 * <br><br>
 * - Ensure that the Pickaxe class is initialized and accessible to handle pickaxe leveling.
 * <br><br>
 * Example:
 * <pre>{@code
 * 
 * MiningListener miningListener = new MiningListener(pickaxeManager);
 * Bukkit.getPluginManager().registerEvents(miningListener, yourPluginInstance);
 * }</pre>
 * 
 * <br><br>
 * @author feur25 & Ganon358
 * @version 1.0
 * @see org.bukkit.event.Listener
 * @see org.bukkit.event.block.BlockBreakEvent
 * @see org.bukkit.Material
 * @see org.bukkit.entity.Player
 * @see org.bukkit.inventory.ItemStack
 * @see fr.ludos.listener.items.Pickaxe
 * @see org.bukkit.event.EventHandler
 */

public class MinerPickaxeListener implements Listener {
    
}
