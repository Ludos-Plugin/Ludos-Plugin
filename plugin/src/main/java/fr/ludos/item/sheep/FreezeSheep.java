package fr.ludos.item.sheep;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.DyeColor;

/**
 * Freeze Sheep - Creates a sphere of packed ice on impact.
 */
public class FreezeSheep extends AbstractSheep {

    public FreezeSheep(String name, String description, Material material) {
        super(name, description, material);
    }

    @Override
    public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.LIGHT_BLUE);

        startSheepTimer(sheep, () -> {
            transformToAnotherMaterial(sheep.getLocation(), 5, Material.AIR, Material.PACKED_ICE, Sound.BLOCK_GLASS_BREAK);
        }, launcher);
    }
}
