package fr.ludos.item.sheep;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.DyeColor;

/**
 * All Aboard Sheep - The launcher rides the sheep and can control its direction.
 * Stacks nearby entities on landing.
 */
public class AllAboardSheep extends AbstractSheep {

    public AllAboardSheep(String name, String description, Material material) {
        super(name, description, material);
    }

    @Override
    public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(
            launcher, event, item, DyeColor.WHITE, null, 2);

        // Special configuration for riding sheep
        sheep.setInvulnerable(true);
        sheep.addPassenger(launcher);

        // Use riding timer instead of standard timer
        startRidingSheepTimer(sheep, launcher);
    }
}
