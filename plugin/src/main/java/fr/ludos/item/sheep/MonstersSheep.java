package fr.ludos.item.sheep;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.DyeColor;

/**
 * Monsters Sheep - Spawns hostile monsters on impact.
 */
public class MonstersSheep extends AbstractSheep {

    public MonstersSheep(String name, String description, Material material) {
        super(name, description, material);
    }

    @Override
    public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.LIGHT_GRAY);

        startSheepTimer(sheep, () -> {
            spawnMonsters(sheep.getLocation());
        }, launcher);
    }
}
