package fr.ludos.item.trapper;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class TrapperTrap {

    private final TrapperSnareDeviceBranches type;
    public TrapperSnareDeviceBranches getType() {
        return type;
    }

    private final Player owner;
    public Player getOwner() {
        return owner;
    }

    private final Location location;
    public Location getLocation() {
        return location;
    }

    private final World world;
    public World getWorld() {
        return world;
    }


    public TrapperTrap(Player owner, Location location, World world, TrapperSnareDeviceBranches type) {
        this.owner = owner;
        this.location = location;
        this.world = world;
        this.type = type;
    }


    public void process(Player target) {
        type.executeEffect(target, this);
    }
}