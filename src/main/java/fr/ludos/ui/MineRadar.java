package fr.ludos.ui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class MineRadar implements Listener {

    public static void main(String[] args) {
        // Nécessite d'être utilisé comme plugin Bukkit sur un serveur Minecraft
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Vérifie si le joueur utilise une boussole de mine
        if (item != null && item.getType() == Material.COMPASS && item.hasItemMeta() && item.getItemMeta().getDisplayName().equals("Boussole de Mine")) {
            // Obtient la localisation de la mine la plus proche
            String mineLocation = getNearestMineLocation(player);

            if (mineLocation != null) {
                player.sendMessage("La mine la plus proche se trouve à : " + mineLocation);
            } else {
                player.sendMessage("Aucune mine n'a été trouvée à proximité.");
            }

            // Empêche l'interaction normale de la boussole
            event.setCancelled(true);
        }
    }

    private String getNearestMineLocation(Player player) {
        // Implémentez votre propre logique pour trouver la mine la plus proche
        // Cela pourrait impliquer une recherche dans une base de données, une liste pré-enregistrée, etc.
        // Dans cet exemple, nous retournons une chaîne fictive pour représenter la localisation de la mine.
        return "X: 100, Y: 64, Z: -50";
    }
}