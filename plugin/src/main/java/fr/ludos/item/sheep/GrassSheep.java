package fr.ludos.item.sheep;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.DyeColor;

/**
 * Grass Sheep - Transforms connected structure blocks to dirt using flood fill algorithm.
 */
public class GrassSheep extends AbstractSheep {

    public GrassSheep(String name, String description, Material material) {
        super(name, description, material);
    }

    @Override
    public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.GREEN);

        startSheepTimer(sheep, () -> {
            transformStructureToDirt(sheep.getLocation());
        }, launcher);
    }
}
