package fr.ludos.controller;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

/**
 * NecromancerMonsterController class is responsible for controlling the behavior of zombies
 * summoned by a necromancer in a Bukkit/Spigot server.
 * <br><br>
 * Features:
 * - Controls the behavior of zombies based on specified parameters.
 * <br><br>
 * Usage:
 * - Use the controlZombie method to control the behavior of a zombie.
 * - Parameters include the zombie entity, the summoner player, teleport range, and attack range.
 * <br><br>
 * Example:
 * NecromancerMonsterController.controlZombie(zombie, summoner, teleportRange, attackRange);
 * <br><br>
 * @param zombie        The zombie entity to be controlled.
 * @param summoner      The player who summoned the zombie.
 * @param teleportRange The maximum distance for the zombie to teleport towards the target player.
 * @param attackRange   The range within which the zombie attacks nearby enemies.
 *
 * @author feur25
 * @version 1.0
 * @see org.bukkit.entity.Entity
 * @see org.bukkit.entity.EntityType
 * @see org.bukkit.entity.Player
 * @see org.bukkit.entity.Zombie
 */

public class NecromancerMonsterController {

    /**
     * Controls the behavior of the specified zombie based on the given parameters.
     *
     * @param zombie        The zombie entity to be controlled.
     * @param summoner      The player who summoned the zombie.
     * @param teleportRange The maximum distance for the zombie to teleport towards the target player.
     * @param attackRange   The range within which the zombie attacks nearby enemies.
     */

     public static void controlZombie(Zombie zombie, Player summoner, double teleportRange, double attackRange) {

        if (zombie.getLocation().distance(summoner.getLocation()) > teleportRange && zombie.getTarget() == null) {
            zombie.teleport(summoner);
        }

        for (Entity entity : zombie.getNearbyEntities(attackRange, attackRange, attackRange)) {
            if (entity.getType() == EntityType.PLAYER && entity != summoner) {
                zombie.setTarget((Player) entity);
            }
        }
    }
}