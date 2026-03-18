package fr.ludos.item.sheep;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.DyeColor;

/**
 * Medium Explosion Sheep - Creates a 5.0f explosion on impact.
 */
public class MediumExplosionSheep extends AbstractSheep {

    public MediumExplosionSheep(String name, String description, Material material) {
        super(name, description, material);
    }

    @Override
    public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.ORANGE);

        startSheepTimer(sheep, () -> {
            sheep.getWorld().createExplosion(sheep.getLocation(), 5.0f, true, true, sheep);
        }, launcher);
    }
}
