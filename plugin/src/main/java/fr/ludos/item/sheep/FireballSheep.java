package fr.ludos.item.sheep;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.DyeColor;

/**
 * Fireball Sheep - Spawns a fireball from the sky on impact.
 */
public class FireballSheep extends AbstractSheep {

    public FireballSheep(String name, String description, Material material) {
        super(name, description, material);
    }

    @Override
    public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.CYAN);

        startSheepTimer(sheep, () -> {
            fireballFromSky(sheep.getLocation());
        }, launcher);
    }
}
