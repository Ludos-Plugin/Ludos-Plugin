package fr.ludos.role;

import fr.ludos.Main;
import fr.ludos.item.burrower.pick.BurrowerPickEvents;
import fr.ludos.item.burrower.digtool.BurrowingClawEvents;

import org.bukkit.ChatColor;
// import org.bukkit.Location;
// import org.bukkit.Material;
// import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
// import org.bukkit.event.block.BlockBreakEvent;
import java.util.ArrayList;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
// import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Bukkit;

import org.bukkit.scheduler.BukkitRunnable;
// import org.bukkit.util.StructureSearchResult;

public class BurrowerRole extends Role {

    private final BurrowerPickEvents pickEvents;
    private final BurrowingClawEvents clawEvents;

    public BurrowerRole(Builder builder) {
        super(builder);
        PluginManager manager = Bukkit.getPluginManager();

        pickEvents = new BurrowerPickEvents();
        manager.registerEvents((Listener)pickEvents, Main.getInstance());

        clawEvents = new BurrowingClawEvents();
        manager.registerEvents((Listener)clawEvents, Main.getInstance());
    }

    @Override
    public void stop() {
        super.stop();

        HandlerList.unregisterAll(pickEvents);
        HandlerList.unregisterAll(clawEvents);
    }

    public static final String id = "burrower";


    private ArrayList<Player> radarPlayers = new ArrayList<>();

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (message.startsWith("/radar")) {
            toggleRadar(player);
            onRadar(event,player);
        }
    }

    private void toggleRadar(Player player) {
        if (radarPlayers.contains(player)) {
            radarPlayers.remove(player);
            player.sendMessage(ChatColor.RED + "Mineshaft radar désactivé.");
        } else {
            radarPlayers.add(player);
            player.sendMessage(ChatColor.GREEN + "Mineshaft radar activé.");
        }
    }

    @EventHandler
    public void onRadar(AsyncPlayerChatEvent event, Player player) {
        
        var location = player.getWorld().locateNearestStructure(player.getLocation(),StructureType.MINESHAFT,100,false);

        if (location != null) {
            player.sendMessage(ChatColor.GREEN + "Mineshaft détecté à " + location);
        } else {
            player.sendMessage(ChatColor.RED + "Aucun mineshaft détecté.");
        }
    }
    
    @EventHandler
    public void createAdvancedFurnace(CraftItemEvent event) {
        Inventory inventory = event.getInventory();
        for (ItemStack item : inventory) {
            if (item != null && item.getType() == Material.FURNACE) {
                inventory.remove(item);
                inventory.addItem(new ItemStack(Material.SMOKER, 1));
                event.getWhoClicked().sendMessage(ChatColor.GREEN + "Furnace upgraded to smoker.");
                return;
            }
        }
    }


    public static class Builder extends Role.Builder {
        @Override
        public String getId() {
            return id;
        }

        @Override
        public Role build(String gameId){
            return new BurrowerRole(this);
        }
    }
    
    public void startPassiveGain() {
        new BukkitRunnable() {
            @Override
            public void run() {
                giveRandomOreToPlayers();
            }
        }.runTaskTimer(Main.getInstance(), 0, 20 * 60 * 2); // 20 ticks * 60 secondes * 2 minutes
    }

    private void giveRandomOreToPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Material randomOre = getRandomOreType();
            if (randomOre != null) {
                ItemStack ore = new ItemStack(randomOre);
                player.getInventory().addItem(ore);
            }
        }
    }

    private Material getRandomOreType() {
        Material[] oreTypes = {Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE, Material.ANCIENT_DEBRIS}; 
        return oreTypes[(int) (Math.random() * oreTypes.length)];
    }
    
}


