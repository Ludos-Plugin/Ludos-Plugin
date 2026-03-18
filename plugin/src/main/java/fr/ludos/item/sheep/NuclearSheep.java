package fr.ludos.item.sheep;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

/**
 * Nuclear Sheep - Creates a massive 70.0f explosion on impact.
 * The sheep has a rainbow "jeb_" name.
 */
public class NuclearSheep extends AbstractSheep {

    public NuclearSheep(String name, String description, Material material) {
        super(name, description, material);
    }

    @Override
    public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(
            launcher, event, item, null, "jeb_", 3);

        startSheepTimer(sheep, () -> {
            sheep.getWorld().createExplosion(sheep.getLocation(), 70.0f, true, true, sheep);
        }, launcher);
    }
}
