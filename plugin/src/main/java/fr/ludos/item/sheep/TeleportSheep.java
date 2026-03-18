package fr.ludos.item.sheep;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.DyeColor;

/**
 * Teleport Sheep - Teleports the launcher to the impact location.
 */
public class TeleportSheep extends AbstractSheep {

    public TeleportSheep(String name, String description, Material material) {
        super(name, description, material);
    }

    @Override
    public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.PURPLE);

        startSheepTimer(sheep, () -> {
            teleportOwnerToLocation(sheep.getLocation(), launcher);
        }, launcher);
    }
}
