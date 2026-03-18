package fr.ludos.item.sheep;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.DyeColor;

/**
 * Lava Sheep - Creates a sphere of lava on impact.
 */
public class LavaSheep extends AbstractSheep {

    public LavaSheep(String name, String description, Material material) {
        super(name, description, material);
    }

    @Override
    public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.YELLOW);

        startSheepTimer(sheep, () -> {
            transformToAnotherMaterial(sheep.getLocation(), 5, Material.AIR, Material.LAVA, Sound.BLOCK_LAVA_EXTINGUISH);
        }, launcher);
    }
}
