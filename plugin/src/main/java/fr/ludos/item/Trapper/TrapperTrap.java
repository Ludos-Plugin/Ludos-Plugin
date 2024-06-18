package fr.ludos.item.trapper;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class TrapperTrap {
    public static final ArrayList<TrapperTrap> traps = new ArrayList<>();


    private TrapperSnareDeviceBranches type;
    public TrapperSnareDeviceBranches getType() {
        return type;
    }

    private Player owner;
    public Player getOwner() {
        return owner;
    }

    private Location location;
    public Location getLocation() {
        return location;
    }

    private World world;
    public World getWorld() {
        return world;
    }

    private int radius;
    public int getRadius() {
        return radius;
    }


    public TrapperTrap(Player owner, Location location, World world, int radius) {
        this.owner = owner;
        this.location = location;
        this.world = world;
        this.radius = radius;
    }


    public void process(Player target) {
        if (target.getLocation().distance(location) <= radius) {
            type.executeEffect(target, this);
        }
    }
}