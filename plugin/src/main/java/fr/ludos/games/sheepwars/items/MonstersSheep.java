package fr.ludos.games.sheepwars.items;

import javax.annotation.Nullable;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

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

	private void spawnMonsters(Location center) {
		World world = center.getWorld();

		// Spawn 2 zombies with helmets
		for (int i = 0; i < 2; i++) {
			double offsetX = (Math.random() - 0.5) * 4;
			double offsetZ = (Math.random() - 0.5) * 4;
			Location spawnLoc = center.clone().add(offsetX, 1, offsetZ);
			org.bukkit.entity.Zombie zombie = (org.bukkit.entity.Zombie) world.spawnEntity(spawnLoc, EntityType.ZOMBIE);
			zombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
		}

		// Spawn 1 skeleton with helmet
		double offsetX = (Math.random() - 0.5) * 4;
		double offsetZ = (Math.random() - 0.5) * 4;
		Location spawnLoc = center.clone().add(offsetX, 1, offsetZ);
		org.bukkit.entity.Skeleton skeleton = (org.bukkit.entity.Skeleton) world.spawnEntity(spawnLoc, EntityType.SKELETON);
		skeleton.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));

		// Spawn 1 spider
		offsetX = (Math.random() - 0.5) * 4;
		offsetZ = (Math.random() - 0.5) * 4;
		spawnLoc = center.clone().add(offsetX, 1, offsetZ);
		world.spawnEntity(spawnLoc, EntityType.SPIDER);

		world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.5f);
		world.spawnParticle(Particle.SMOKE_LARGE, center, 30);
	}
}
