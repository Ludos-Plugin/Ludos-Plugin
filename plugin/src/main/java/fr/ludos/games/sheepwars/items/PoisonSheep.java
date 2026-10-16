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
 * Poison Sheep - Creates a poison cloud on impact.
 */
public class PoisonSheep extends AbstractSheep {

	public PoisonSheep(String name, String description, Material material) {
		super(name, description, material);
	}

	@Override
	public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
		org.bukkit.entity.Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.MAGENTA);

		startSheepTimer(sheep, () -> {
			createPotionEffectCloud(sheep.getLocation(), PotionEffectType.POISON, Particle.SLIME, Color.PURPLE, 1);
		}, launcher);
	}
}
