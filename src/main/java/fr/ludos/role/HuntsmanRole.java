package fr.ludos.role;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.AbstractArrow.PickupStatus;


public class HuntsmanRole extends Role {

    // @EventHandler
    // public void onPlayerRespawn(PlayerRespawnEvent event) {
    //     ItemStack crossBowHunter = new ItemStack(Material.CROSSBOW, 1);
    //     ItemMeta crossBowMeta = crossBowHunter.getItemMeta();
    //     crossBowMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
    //     ItemStack arrow = new ItemStack(Material.ARROW, 1);
    // }
    
    public void playerEventListener(Player player, PlayerDeathEvent event, ItemStack CrossBow) {
        player.getInventory().removeItem(CrossBow);
    }

    public int getPlayerXP(Player player) {
        return player.getTotalExperience();
    }



    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (item.getType() == Material.ARROW) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onShootArrow(EntityShootBowEvent event) {
        if ( ! (event.getEntity() instanceof Player) ) {
            return;
        }
        Player player = (Player) event.getEntity();
        
        Arrow arrowProjectile = (Arrow) event.getProjectile();
        arrowProjectile.setPickupStatus(PickupStatus.DISALLOWED);
        if (arrowProjectile.isShotFromCrossbow()) {
            arrowProjectile.setGravity(false);
            arrowProjectile.setDamage(0.5);
        }

        updateArrowCount(player);
    }

    private void updateArrowCount(Player player) {
        Inventory inventory = player.getInventory();

        ItemStack arrowItem = new ItemStack(Material.ARROW);
        inventory.remove(Material.ARROW);
        inventory.addItem(arrowItem);
    }






    // @EventHandler
    // public void upgradeStuff(Player player) {        
    //     if (getPlayerXP(player) >= 100/*  && ! HuntsmanCrossbow.playerOwns(player) */) {
    //         // HuntsmanCrossbow.createNew(player);
    //     }
        
    //     if (getPlayerXP(player) >= 300/*  && ! HuntsmanSpear.playerOwns(player) */) {
    //         // HuntsmanSpear.createNew(player);
    //     }
    // }


    public static class Builder extends Role.Builder {

        @Override
        public String getId() {
            return "huntsman";
        }
    }
}