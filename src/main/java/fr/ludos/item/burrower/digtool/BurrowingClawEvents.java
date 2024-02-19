package fr.ludos.item.burrower.digtool;

import org.bukkit.NamespacedKey;

import fr.ludos.Main;

import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;



/**
 * BurrowingClaw is a class that represents a special item, "The Burrower's Pickaxe," in Minecraft.
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

public class BurrowingClawEvents implements Listener {

    private static final String OWNER_NAMESPACE_KEY = "ludos_miner_claw_owner";
    private static final String USAGES_NAMESPACE_KEY = "ludos_miner_claw_usages";

    private static NamespacedKey ownerKey = null;
    private static NamespacedKey usagesKey = null;

    private static Map<Player, Long> messageToggle = new HashMap<>();

    private static final int TUNNEL_LENGTH = 20;

    private final List<Block> tunnelBlocks = new ArrayList<>();

    private final Map<Player, Long> cooldowns = new HashMap<>();
    private final Map<Player, Integer> usages = new HashMap<>();

    private static final int MAX_USAGES = 3;
    private static final int COOLDOWN_SECONDS = 10;

    

    static NamespacedKey getOwnerKey() {
        return ownerKey;
    }
    static NamespacedKey getUsagesKey() {
        return usagesKey;
    }


    public BurrowingClawEvents() {
        Main plugin = Main.getInstance();
        ownerKey = new NamespacedKey(plugin, OWNER_NAMESPACE_KEY);
        usagesKey = new NamespacedKey(plugin, USAGES_NAMESPACE_KEY);
    }
    

    public static int counterSpell = 3;
    private int range = 150;

    public class BlockBreak {
        Location location;
        Material material;
        
        public BlockBreak(Location location, Material material){
            this.location = location;
            this.material = material;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getInventory().addItem(new BurrowingClaw(player).getStack());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack mainItem = player.getInventory().getItemInMainHand();

        if (!isBurrowingClaw(mainItem)) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleRightClick(player);
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            retractTunnel();
        }
    }

    private void handleRightClick(Player player) {
        long currentTime = System.currentTimeMillis();
        if (!canUseClaw(player, currentTime)) {
            return;
        }

        generateTunnel(player);
        startCooldown(player, currentTime);
        reduceUsage(player);
    }

    private boolean isBurrowingClaw(ItemStack item) {
        return item != null && item.getType() == Material.RABBIT_FOOT;
    }

    private boolean canUseClaw(Player player, long currentTime) {
        long lastUse = cooldowns.getOrDefault(player, 0L);
        return currentTime - lastUse >= COOLDOWN_SECONDS * 1000;
    }

    private void startCooldown(Player player, long currentTime) {
        cooldowns.put(player, currentTime);
    }

    private void reduceUsage(Player player) {
        int remainingUsages = usages.getOrDefault(player, MAX_USAGES);
        if (remainingUsages > 0) {
            usages.put(player, remainingUsages - 1);
        }
    }

    private void generateTunnel(Player player) {
        for (int i = 1; i <= TUNNEL_LENGTH; i++) {
            generateBlock(player, i);
            generateBlock(player, -i);
        }
    }

    private void generateBlock(Player player, int yOffset) {
        Location loc = player.getLocation().clone().add(0, yOffset, 0);
        Block block = loc.getBlock();
        tunnelBlocks.add(block);
        block.setType(Material.AIR);
    }

    private void retractTunnel() {
        tunnelBlocks.forEach(block -> block.setType(block.getType()));
        tunnelBlocks.clear();
    }


    // @EventHandler
    // public void interactRightClick(PlayerInteractEvent event) {
    //     if ( event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {        
    //         return;
    //     }

    //     Player player = event.getPlayer();
    //     ItemStack mainItem = player.getInventory().getItemInMainHand();
    //     BurrowingClaw claw;
    //     try {
    //         claw = new BurrowingClaw(mainItem);
    //     } catch (IllegalArgumentException e) {
    //         return;
    //     }

    //     long cooldown = 100L;
    //     Long currentTime = System.currentTimeMillis();
    //     if ( currentTime - messageToggle.getOrDefault(player, 0L) > cooldown ) {
    //         messageToggle.remove(player);
    //     }
    //     if (messageToggle.containsKey(player)) {
    //         return;
    //     }


    //     if (claw.getUsages() < 1) {
    //         return;
    //     }

    //     messageToggle.put(player, currentTime);
    //     claw.setUsages(claw.getUsages() - 1);


    //     List<BlockBreak> blockBreakList = new ArrayList<>();
    //     Location topBlockLocation = player.getEyeLocation();

    //     for (int i = 0; i < range; i++) {
    //         topBlockLocation = topBlockLocation.add(topBlockLocation.getDirection());
    //         Block topBlock = topBlockLocation.getBlock();
    //         Location bottomBlockLocation = topBlockLocation.clone().add(0, -1, 0);
    //         Block bottomBlock = bottomBlockLocation.getBlock();

    //         blockBreakList.add(new BlockBreak(topBlockLocation, topBlock.getType()));
    //         blockBreakList.add(new BlockBreak(bottomBlockLocation, bottomBlock.getType()));

    //         topBlock.setType(Material.AIR);
    //         bottomBlock.setType(Material.AIR);
    //     }
        
    //     new BukkitRunnable() {
    //         @Override
    //         public void run(){
                
    //         }
    //     }.runTaskLater(Main.getInstance(), 5000L);
    // }

    // @EventHandler
    // public void BreakBlock(BlockBreakEvent event) {
    //     Block block = event.getBlock();
    //     if (block.getType() == Material.AIR) {
    //         block.setType(Material.STONE);
    //     }
    // }
}