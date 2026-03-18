package fr.ludos.item.sheep;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.DyeColor;

/**
 * Big Explosion Sheep - Creates a 10.0f explosion on impact.
 */
public class BigExplosionSheep extends AbstractSheep {

    public BigExplosionSheep(String name, String description, Material material) {
        super(name, description, material);
    }

    @Override
    public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.RED);

        startSheepTimer(sheep, () -> {
            sheep.getWorld().createExplosion(sheep.getLocation(), 10.0f, true, true, sheep);
        }, launcher);
    }
}
