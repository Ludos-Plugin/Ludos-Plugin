package fr.ludos.item.sheep;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.DyeColor;

/**
 * Anvils Sheep - Spawns a grid of anvils above the impact location.
 */
public class AnvilsSheep extends AbstractSheep {

    public AnvilsSheep(String name, String description, Material material) {
        super(name, description, material);
    }

    @Override
    public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.GRAY);

        startSheepTimer(sheep, () -> {
            spawnAnvils(sheep.getLocation(), 5);
        }, launcher);
    }
}
