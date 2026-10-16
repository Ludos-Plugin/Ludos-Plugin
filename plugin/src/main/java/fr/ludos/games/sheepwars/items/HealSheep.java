package fr.ludos.games.sheepwars.items;

import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * Heal Sheep - Creates a regeneration cloud on impact.
 */
public class HealSheep extends AbstractSheep {

    public HealSheep(String name, String description, Material material) {
        super(name, description, material);
    }

    @Override
    public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.PINK);

        startSheepTimer(sheep, () -> {
            createPotionEffectCloud(sheep.getLocation(), PotionEffectType.REGENERATION, Particle.HEART, Color.FUCHSIA, 3);
        }, launcher);
    }
}
