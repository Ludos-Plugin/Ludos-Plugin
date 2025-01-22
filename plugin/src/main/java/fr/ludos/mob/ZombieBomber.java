// // ZombieBomber.java

// package fr.ludos.mob;

// import org.bukkit.ChatColor;
// import org.bukkit.Location;
// import org.bukkit.Material;
// import org.bukkit.attribute.Attributable;
// import org.bukkit.attribute.Attribute;
// import org.bukkit.attribute.AttributeInstance;
// import org.bukkit.entity.Entity;
// import org.bukkit.entity.EntityType;
// import org.bukkit.entity.Player;
// import org.bukkit.entity.TNTPrimed;
// import org.bukkit.entity.Zombie;
// import org.bukkit.inventory.ItemStack;
// import org.bukkit.potion.PotionEffect;
// import org.bukkit.potion.PotionEffectType;
// import org.bukkit.scheduler.BukkitRunnable;

// import fr.ludos.Ludos;
// import fr.ludos.controller.NecromancerMonsterController;

// /**
//  * ZombieBomber class represents a custom zombie entity in a Bukkit/Spigot server, known as "Bomber."
//  * This zombie has unique attributes such as carrying TNT and exhibiting specific behavior.
//  * <br><br>
//  * Features:
//  * <br><br>
//  * - Spawns a ZombieBomber at the specified location controlled by a summoner player.
//  * <br><br>
//  * - The ZombieBomber wears TNT as a helmet and carries Flint and Steel.
//  * <br><br>
//  * - Periodically places primed TNT near its target (excluding the summoner) and exhibits specific behavior.
//  * <br><br>
//  * - Controlled by the NecromancerMonsterController for targeted actions.
//  * <br><br>
//  * - Removes itself if dead or after placing three bombs.
//  * <br><br>
//  * Usage:
//  * <br><br>
//  * - Construct a new ZombieBomber by providing the spawn location, summoner player, and the Main plugin instance.
//  * <br><br>
//  * - The ZombieBomber has unique attributes such as health, equipment, and targeting behavior.
//  *  <br><br>
//  * Example:
//  * <br><br>
//  * <pre>{@code
//  * new ZombieBomber(location, summoner, plugin);
//  * }</pre>
//  * <br><br>
//  * @param location The location where the ZombieBomber will be spawned.
//  * @param summoner The player who summoned the ZombieBomber.
//  * @param plugin   The Main plugin instance for scheduling tasks.
//  *
//  * @author feur25
//  * @version 1.0
//  * @see org.bukkit.Location
//  * @see org.bukkit.entity.Player
//  * @see org.bukkit.entity.Zombie
//  * @see org.bukkit.Material
//  * @see org.bukkit.inventory.ItemStack
//  * @see org.bukkit.attribute.Attributable
//  * @see org.bukkit.attribute.Attribute
//  * @see org.bukkit.attribute.AttributeInstance
//  * @see org.bukkit.entity.Entity
//  * @see org.bukkit.entity.TNTPrimed
//  * @see org.bukkit.potion.PotionEffect
//  * @see org.bukkit.potion.PotionEffectType
//  * @see org.bukkit.scheduler.BukkitRunnable
//  * @see fr.ludos.Ludos
//  * @see fr.ludos.controller.NecromancerMonsterController
//  */

// public class ZombieBomber {

// 	private final int MAX_HEALTH = 7;

// 	/**
// 	 * Constructs a new ZombieBomber at the specified location, controlled by the given summoner player.
// 	 *
// 	 * @param location The location where the ZombieBomber will be spawned.
// 	 * @param summoner The player who summoned the ZombieBomber.
// 	 * @param plugin   The Main plugin instance for scheduling tasks.
// 	 */

// 	public ZombieBomber(Location location, Player summoner, Ludos plugin) {
// 		Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);

// 		zombie.setBaby();

// 		Attributable zombieAt = zombie;
// 		AttributeInstance attribute = zombieAt.getAttribute(Attribute.GENERIC_MAX_HEALTH);

// 		attribute.setBaseValue(MAX_HEALTH);
// 		zombie.setHealth(MAX_HEALTH);


// 		zombie.setCustomName("Bomber " + ChatColor.RED + (int) zombie.getHealth() + "/" + MAX_HEALTH + " ❤︎");
// 		zombie.setCustomNameVisible(true);

// 		zombie.getEquipment().setItemInMainHand(new ItemStack(Material.FLINT_AND_STEEL, 1));
// 		zombie.getEquipment().setHelmet(new ItemStack(Material.TNT, 1));


// 		new BukkitRunnable() {
// 			int bombsPlaced = 0;

// 			@Override
// 			public void run() {
// 				if (zombie.isDead() || bombsPlaced >= 3) {
// 					cancel();
// 					zombie.remove();
// 					return;
// 				}

// 				if ( ! (zombie.getTarget() instanceof Player playerTarget) || playerTarget != summoner ) {
// 					Location location = zombie.getLocation();
// 					Entity finalTarget = zombie.getNearbyEntities(10, 10, 10).stream()
// 						.filter(tar -> (tar instanceof Player playerTar) && playerTar != summoner) // get player candidates
// 						.sorted((tar1, tar2) -> (int)tar1.getLocation().distance(location) - (int)tar2.getLocation().distance(location)) // sort by distance
// 						.findFirst()
// 						.get();

// 					zombie.setTarget( (Player) finalTarget );
// 					return;
// 				}

// 				if ( zombie.getLocation().distance(playerTarget.getLocation()) < 5 ) {
// 					TNTPrimed tnt = (TNTPrimed) zombie.getLocation().getWorld()
// 						.spawnEntity(zombie.getLocation(), EntityType.PRIMED_TNT);

// 					tnt.setFuseTicks(20);
// 					bombsPlaced++;
// 				}

// 				NecromancerMonsterController.controlZombie(zombie, summoner, 10, 20);

// 				zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 6, 2));
// 			}

// 		}.runTaskTimer(plugin, 100L, 100L);
// 	}
// }