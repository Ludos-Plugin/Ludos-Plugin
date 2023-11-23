// ZombieBomber.java

package fr.ludos.monster;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.Main;
import fr.ludos.controller.NecromancerMonsterController;

/**
 * ZombieBomber class represents a custom zombie entity in a Bukkit/Spigot server, known as "Bomber."
 * This zombie has unique attributes such as carrying TNT and exhibiting specific behavior.
 * <br><br>
 * Features:
 * - Spawns a ZombieBomber at the specified location controlled by a summoner player.
 * - The ZombieBomber wears TNT as a helmet and carries Flint and Steel.
 * - Periodically places primed TNT near its target (excluding the summoner) and exhibits specific behavior.
 * - Controlled by the NecromancerMonsterController for targeted actions.
 * - Removes itself if dead or after placing three bombs.
 * <br><br>
 * Usage:
 * - Construct a new ZombieBomber by providing the spawn location, summoner player, and the Main plugin instance.
 * - The ZombieBomber has unique attributes such as health, equipment, and targeting behavior.
 *  <br><br>
 * Example:
 * new ZombieBomber(location, summoner, plugin);
 * <br><br>
 * @param location The location where the ZombieBomber will be spawned.
 * @param summoner The player who summoned the ZombieBomber.
 * @param plugin   The Main plugin instance for scheduling tasks.
 *
 * @author feur25
 * @version 1.0
 * @see org.bukkit.Location
 * @see org.bukkit.entity.Player
 * @see org.bukkit.entity.Zombie
 * @see org.bukkit.Material
 * @see org.bukkit.inventory.ItemStack
 * @see org.bukkit.attribute.Attributable
 * @see org.bukkit.attribute.Attribute
 * @see org.bukkit.attribute.AttributeInstance
 * @see org.bukkit.entity.Entity
 * @see org.bukkit.entity.TNTPrimed
 * @see org.bukkit.potion.PotionEffect
 * @see org.bukkit.potion.PotionEffectType
 * @see org.bukkit.scheduler.BukkitRunnable
 * @see fr.ludos.Main
 * @see fr.ludos.controller.NecromancerMonsterController
 */

public class ZombieBomber {

    /**
     * Constructs a new ZombieBomber at the specified location, controlled by the given summoner player.
     *
     * @param location The location where the ZombieBomber will be spawned.
     * @param summoner The player who summoned the ZombieBomber.
     * @param plugin   The Main plugin instance for scheduling tasks.
     */

    public ZombieBomber(Location location, Player summoner, Main plugin) {
        int maxHealth = 7;
        Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);

        zombie.setCustomName("Bomber" + ChatColor.RED + (int) zombie.getHealth() + " / " + maxHealth + " ❤︎");
        zombie.setCustomNameVisible(true);

        zombie.setBaby();

        zombie.getEquipment().setItemInMainHand(new ItemStack(Material.FLINT_AND_STEEL, 1));
        zombie.getEquipment().setHelmet(new ItemStack(Material.TNT, 1));

        Attributable zombieAt = zombie;

        AttributeInstance attribute = zombieAt.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        attribute.setBaseValue(maxHealth);
        zombie.setHealth(maxHealth);


        new BukkitRunnable() {
            int bombsPlaced = 0;

            @Override
            public void run() {
                if (zombie.isDead() || bombsPlaced >= 3) {
                    cancel();
                    zombie.remove();
                    return;
                }

                Player target = (Player) zombie.getTarget();
                if (target != null && target != summoner && zombie.getLocation().distance(target.getLocation()) < 5) {
                    TNTPrimed tnt = (TNTPrimed) zombie.getLocation().getWorld().spawnEntity(zombie.getLocation(), EntityType.PRIMED_TNT);
                    tnt.setFuseTicks(20);
                    bombsPlaced++;
                }

                if (zombie.getTarget() == null || (Player) zombie.getTarget() == summoner) {
                    for (Entity entity : zombie.getNearbyEntities(10, 10, 10)) {
                        if (entity instanceof Player && (Player) entity != summoner) {
                            zombie.setTarget((Player) entity);
                        }
                    }
                }

                NecromancerMonsterController.controlZombie(zombie, summoner, 10, 20);

                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 2));
            }

        }.runTaskTimer(plugin, 100L, 100L);
    }
}