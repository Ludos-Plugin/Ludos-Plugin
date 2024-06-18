package fr.ludos.item.trapper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TrapperTrapSlowing extends TrapperTrap {

    public TrapperTrapSlowing(Player owner, Location location, World world) {
        super(owner, location, world, 7);

        location.getBlock().setType(Material.COBWEB);
    }

    @Override
    public void process(Player player) {
        if (player.getLocation().distance(this.getLocation()) <= this.getRadius()) {

            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 1));
            getOwner().sendMessage("Trap triggered !");

            if (getLocation().getBlock().getType() == Material.COBWEB) {
                getLocation().getBlock().setType(Material.AIR);
            }
        }
    }
}