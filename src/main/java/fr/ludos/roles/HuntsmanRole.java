package fr.ludos.roles;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;


public class HuntsmanRole extends PlayerRole implements Listener {
    
    @Override
    public void processCrafting(Player player) {
        
    }

    @Override
    public void processAbilities(Player player) {
        
    } 
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        ItemStack crossBowHunter = new ItemStack(Material.CROSSBOW, 1);
        ItemMeta crossBowMeta = crossBowHunter.getItemMeta();
        crossBowMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        ItemStack arrow = new ItemStack(Material.ARROW, 1);
    }
    
    public void playerEventListener(Player player, PlayerDeathEvent event, ItemStack CrossBow) {
        player.getInventory().removeItem(CrossBow);
    }

    public int getPlayerXP(Player player) {
        return player.getTotalExperience();
    }

    @EventHandler
    public void upgradeStuff(Player player) {
        if (getPlayerXP(player) >= 100) {
            ItemStack hunterBow = new ItemStack(Material.BOW, 1);
            ItemMeta hunterBowMeta = hunterBow.getItemMeta();
            hunterBowMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
            ItemStack arrow = new ItemStack(Material.ARROW, 1);
        }
        
        if (getPlayerXP(player) >= 300) {
            ItemStack trident = new ItemStack(Material.TRIDENT, 1);
            ItemMeta tritentMeta = trident.getItemMeta();
            tritentMeta.addEnchant(Enchantment.RIPTIDE, 1, true);
            
        }
    }
}