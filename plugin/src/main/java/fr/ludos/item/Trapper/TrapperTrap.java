package fr.ludos.item.Trapper;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

class InnerTrapperTrap {
    public String name;
    public Location location;
    public World world;
    public int radius;
}

public class TrapperTrap {
    private ArrayList<InnerTrapperTrap> trap = new ArrayList<>();

    public void addTrap(String name, Location loc, World world, int radius) {
        InnerTrapperTrap innerTrap = new InnerTrapperTrap();
        innerTrap.name = name;
        innerTrap.location = loc;
        innerTrap.world = world;
        innerTrap.radius = radius;
        this.trap.add(innerTrap);
    }

    public static void trapTnt(World world, Location loc) {
        world.spawnEntity(loc, EntityType.PRIMED_TNT);
    }

    public InnerTrapperTrap getTrap(String name) {
        for (InnerTrapperTrap innerTrap : this.trap) {
            if (innerTrap.name.equals(name)) {
                return innerTrap;
            }
        }
        return null;
    }

    public void trapCobweb(String name) {
        InnerTrapperTrap innerTrap = getTrap(name);
        loop(innerTrap.radius, innerTrap, Material.COBWEB);
    }

    public void trapBoost(String name, int speed, int strength, int saturation) {
        InnerTrapperTrap innerTrap = getTrap(name);
        loop(innerTrap.radius, innerTrap, Material.BEACON);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getMaterial() == Material.STRING) {
                trapThrowing(event.getPlayer().getName());
            }
        }
    }

    public void trapThrowing(String name){
        // throwObject();
    } 

    public void throwObject(Player player, Material material) {
        Item item = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(material));
        item.setVelocity(player.getLocation().getDirection().multiply(2));
    }

    public void trapGlowing(String name, int duration) {
        final Player targetPlayer = Bukkit.getPlayer(name);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            return;
        }

        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration * 20, 1));
    }

    public void loop(int radius , InnerTrapperTrap innerTrap, Material material){
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (innerTrap.location.getBlock().getRelative(x, y, z).getType().isAir()) {
                        innerTrap.location.getBlock().getRelative(x, y, z).setType(material);
                    }
                }
            }
        }
    }
}