package fr.ludos.games.sheepwars.items;

import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import fr.ludos.core.Ludos;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Abstract base class for all sheep types in the Sheepwars game.
 * Provides common functionality and utility methods for sheep projectiles.
 */
public abstract class AbstractSheep implements Listener {

	private final String name;
	private final String description;
	private final Material material;

	private static final NamespacedKey SHEEP_OWNER =
		new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), "sheep_owner");

	/**
	 * Constructs an AbstractSheep with the specified name, description, and material.
	 * @param name The name of the sheep item.
	 * @param description The description of the sheep item.
	 * @param material The material representing the sheep item.
	 */
	public AbstractSheep(String name, String description, Material material) {
		this.name = name;
		this.description = description;
		this.material = material;
	}

	/**
	 * Abstract method that each concrete sheep must implement.
	 * Defines the behavior when the sheep is launched by a player.
	 *
	 * @param launcher The player launching the sheep
	 * @param event The player interact event (nullable)
	 * @param item The wool item stack being used (nullable)
	 */
	public abstract void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item);

	/**
	 * Gets the name of this sheep.
	 * @return The sheep name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the description of this sheep.
	 * @return The sheep description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the material representing this sheep.
	 * @return The wool material.
	 */
	public Material getMaterial() {
		return material;
	}

	/**
	 * Tags a sheep entity with its owner.
	 * @param sheep The sheep entity to tag
	 * @param owner The player who owns the sheep
	 */
	protected void tagOwner(Sheep sheep, Player owner) {
		sheep.getPersistentDataContainer().set(
			SHEEP_OWNER,
			PersistentDataType.STRING,
			owner.getUniqueId().toString()
		);
	}

	/**
	 * Removes the owner tag from a sheep entity.
	 * @param sheep The sheep entity to untag
	 */
	protected void untagOwner(Sheep sheep) {
		sheep.getPersistentDataContainer().remove(SHEEP_OWNER);
	}

	/**
	 * Consumes the wool item and cancels the event.
	 * Call this when a sheep is successfully launched.
	 *
	 * @param event The player interact event
	 * @param item The wool item stack
	 * @return Always returns true
	 */
	protected Boolean eventLaunchSheepWool(PlayerInteractEvent event, ItemStack item) {
		event.setCancelled(true);
		item.setAmount(item.getAmount() - 1);
		return true;
	}

	/**
	 * Spawns and launches a sheep with the specified color.
	 * Simple version that uses default values (no custom name, velocity multiplier of 3).
	 *
	 * @param launcher The player launching the sheep
	 * @param event The player interact event (nullable)
	 * @param item The wool item stack (nullable)
	 * @param color The dye color for the sheep
	 * @return The spawned sheep entity
	 */
	protected Sheep spawnAndLaunchSheep(
			Player launcher,
			@Nullable PlayerInteractEvent event,
			@Nullable ItemStack item,
			DyeColor color) {
		return spawnAndLaunchSheep(launcher, event, item, color, null, 3);
	}

	/**
	 * Template method that handles the common sheep spawning and launching logic.
	 * This is the complete version with full customization options for all sheep types.
	 *
	 * @param launcher The player launching the sheep
	 * @param event The player interact event (nullable)
	 * @param item The wool item stack (nullable)
	 * @param color The dye color for the sheep (nullable - if null, no color is set)
	 * @param customName The custom name for the sheep (nullable - e.g., "jeb_" for rainbow effect)
	 * @param velocityMultiplier The velocity multiplier for launching (typically 2 or 3)
	 * @return The spawned sheep entity
	 */
	protected Sheep spawnAndLaunchSheep(
		Player launcher,
		@Nullable PlayerInteractEvent event,
		@Nullable ItemStack item,
		@Nullable DyeColor color,
		@Nullable Component customName,
		int velocityMultiplier
	) {
		World world = launcher.getWorld();
		Location location = launcher.getLocation();

		// Consume the wool item
		if (event != null && item != null) {
			eventLaunchSheepWool(event, item);
		}

		// Spawn the sheep entity
		Sheep sheep = (Sheep)
			world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

		// Configure sheep appearance
		sheep.setCustomNameVisible(true);
		sheep.setAdult();

		if (customName != null) {
			sheep.customName(customName);
		}

		if (color != null) {
			sheep.setColor(color);
		}

		// Launch the sheep with configurable velocity
		Vector direction = launcher.getLocation().getDirection().multiply(velocityMultiplier);
		sheep.setVelocity(direction);

		return sheep;
	}

	/**
	 * Starts a timer for a sheep projectile that triggers an effect on landing.
	 *
	 * @param sheep The sheep entity
	 * @param effectAction The effect to trigger when the sheep lands (Runnable)
	 * @param owner The player who launched the sheep
	 */
	protected void startSheepTimer(Sheep sheep, Runnable effectAction, Player owner) {
		tagOwner(sheep, owner);

		new BukkitRunnable() {
			int flightTime = 0;

			@Override
			public void run() {
				if (sheep == null || sheep.isDead()) {
					this.cancel();
					return;
				}

				if (sheep.isOnGround() || sheep.getVelocity().length() < 0.1 || flightTime >= 100) {
					effectAction.run();  // Execute the custom effect

					untagOwner(sheep);
					sheep.remove();

					this.cancel();
				}

				sheep.getLocation().getWorld().playEffect(sheep.getLocation(), Effect.SMOKE, 0);
				flightTime++;
			}
		}.runTaskTimer(JavaPlugin.getPlugin(Ludos.class), 0L, 1L);
	}

	/**
	 * Creates an ItemStack representing the sheep item.
	 * @param amount The quantity of the item stack.
	 * @return The created ItemStack.
	 */
	public ItemStack createSheepItem(int amount) {
		ItemStack item = new ItemStack(material, amount);
		ItemMeta meta = item.getItemMeta();

		meta.displayName(Component.text(name, NamedTextColor.BLUE)
			.decoration(TextDecoration.BOLD, true));
		meta.lore(List.of(Component.text(description, NamedTextColor.AQUA)));

		item.setItemMeta(meta);
		return item;
	}

	/**
	 * Transforms blocks in a sphere from one material to another.
	 *
	 * @param center The center location of the sphere
	 * @param radius The radius of the sphere
	 * @param materialToReplace The material that should be replaced (e.g., AIR)
	 * @param materialToSet The material to set blocks to
	 * @param sound The sound to play after transformation
	 */
	protected void transformToAnotherMaterial(Location center, int radius, Material materialToReplace,
		Material materialToSet, Sound sound) {

		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {

					if (Math.sqrt(x*x + y*y + z*z) <= radius) {
						Block block = center.clone().add(x, y, z).getBlock();

						if (block.getType() != materialToReplace)
							block.setType(materialToSet);
					}
				}
			}
		}
		center.getWorld().playSound(center, sound, 1.0f, 1.0f);
	}

	/**
	 * Creates a potion effect cloud at the specified location.
	 *
	 * @param loc The location to spawn the cloud
	 * @param effectType The potion effect type
	 * @param particle The particle effect
	 * @param color The color of the cloud
	 * @param amplifier The strength of the given effectType
	 */
	protected void createPotionEffectCloud(Location loc, PotionEffectType effectType, Particle particle, Color color, int amplifier) {
		AreaEffectCloud cloud = (AreaEffectCloud) loc.getWorld().spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD);

		cloud.setRadius(4.0f);
		cloud.setDuration(160); // Duration of presence (in ticks, 160 = 8 seconds)
		cloud.setWaitTime(0); // Apply effect immediately

		PotionEffect effect = new PotionEffect(effectType, 60, amplifier);
		cloud.addCustomEffect(effect, true);

		cloud.setParticle(particle);
		cloud.setColor(color);

		loc.getWorld().playSound(loc, Sound.ENTITY_SPLASH_POTION_BREAK, 1.0f, 0.5f);
	}

	// ==================== Event Handlers ====================

	/**
	 * Prevents sheep entities from dropping items when they die.
	 * @param event
	 */
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		event.getDrops().clear();
	}

	/**
	 * Prevents the owner from taking damage from their own sheep.
	 * @param event
	 */
	@EventHandler
	public void onGetEntityDamage(EntityDamageEvent event) {
		Player player = event.getEntity() instanceof Player p ? p : null;
		if(player == null) return;

		String uuid = player.getUniqueId().toString();

		EntityDamageByEntityEvent damager = event instanceof EntityDamageByEntityEvent e ? e : null;
		if(damager == null) return;

		if(damager.getDamager().getType() != EntityType.SHEEP) return;

		Sheep sheep = damager.getDamager().getType() == EntityType.SHEEP ?
			(Sheep) damager.getDamager() : null;

		String owner = sheep.getPersistentDataContainer().get(SHEEP_OWNER, PersistentDataType.STRING);

		Boolean isOwner = owner != null && owner.equals(uuid);

		if (isOwner) {
			player.setNoDamageTicks(0);
			event.setDamage(0);
			event.setCancelled(true);
			return;
		}

		// Reduce all other sheep damage to keep the game balanced.
		event.setDamage(event.getDamage() * 0.25);
	}
}
