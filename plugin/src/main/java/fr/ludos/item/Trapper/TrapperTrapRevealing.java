package fr.ludos.item.trapper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TrapperTrapRevealing extends TrapperTrap {

    public TrapperTrapRevealing(Player owner, Location location, World world) {
        super(owner, location, world, 3);

        location.getBlock().setType(Material.SUNFLOWER);
    }

    @Override
    public void process(Player target) {
        if (target.getLocation().distance(this.getLocation()) <= this.getRadius()) {

            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 400, 1));
            getOwner().sendMessage("Trap triggered !");

            if (getLocation().getBlock().getType() == Material.SUNFLOWER) {
                getLocation().getBlock().setType(Material.AIR);
            }
            // TODO Evoyer un event au plugin pour notifier que le joueur a été révélé et declencher le reveal de la position du joueur
            //Bukkit.getServer().getPluginManager().callEvent()
        }
    }
}