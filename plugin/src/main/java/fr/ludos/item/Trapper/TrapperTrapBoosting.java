package fr.ludos.item.trapper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class TrapperTrapBoosting extends TrapperTrap {

    public TrapperTrapBoosting(Player owner, Location location, World world) {
        super(owner, location, world, 5);

        location.getBlock().setType(Material.COARSE_DIRT);
    }

    @Override
    public void process(Player player) {
    }
}