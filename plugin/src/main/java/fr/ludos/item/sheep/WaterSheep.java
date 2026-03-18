package fr.ludos.item.sheep;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.DyeColor;

/**
 * Water Sheep - Creates a sphere of water on impact.
 */
public class WaterSheep extends AbstractSheep {

    public WaterSheep(String name, String description, Material material) {
        super(name, description, material);
    }

    @Override
    public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.BLUE);

        startSheepTimer(sheep, () -> {
            transformToAnotherMaterial(sheep.getLocation(), 5, Material.AIR, Material.WATER, Sound.BLOCK_WATER_AMBIENT);
        }, launcher);
    }
}
