package fr.ludos.game;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import java.util.Random;

public class Border extends JavaPlugin {

	@Override
    public void onEnable() {
        World world = Bukkit.getWorld("world");

        if (world != null) {
            setWorldBorder(world, 200);  
        } else {
            getLogger().warning("World 'world' not found!");
        }
    }

    private void setWorldBorder(World world, double size) {
        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setSize(size);

        Random random = new Random();
        double randomX = random.nextDouble() * 2 - 1 * 100000;
        double randomZ = random.nextDouble() * 2 - 1 * 100000;
        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            Location location = new Location(world, randomX, player.getLocation().getY(), randomZ);
            player.teleport(location);
        }
        worldBorder.setCenter(randomX, randomZ);
    }
    
        
    
}

