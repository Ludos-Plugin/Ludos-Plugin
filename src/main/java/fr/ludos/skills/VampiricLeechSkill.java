// VampiricLeechSkill.java

package fr.ludos.skills;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.Main;

/**
 * VampiricLeechSkill class provides the implementation for the Vampiric Leech skill in a Bukkit/Spigot server.
 * When activated by a necromancer player, this skill allows the player to leech health from a damaged enemy entity.
 * Additionally, it registers the necessary event listener to trigger the leech effect.
 * <br><br>
 * Features:
 * - Leeches health from a damaged enemy entity.
 * - Registers an event listener for the leech effect.
 * <br><br>
 * Usage:
 * - Call activateVampiricLeech method to register the event listener.
 * - The leech effect is automatically triggered when the necromancer damages an enemy entity.
 * <br><br>
 * Example:
 * VampiricLeechSkill.activateVampiricLeech(plugin, necromancer);
 * <br><br>
 * @param plugin      The instance of the main plugin class.
 * @param necromancer The player who activates the skill.
 * @author feur25
 * @version 1.0
 * @see org.bukkit.Bukkit
 * @see org.bukkit.entity.LivingEntity
 * @see org.bukkit.entity.Player
 * @see org.bukkit.event.EventHandler
 * @see org.bukkit.event.Listener
 * @see org.bukkit.event.entity.EntityDamageByEntityEvent
 * @see org.bukkit.potion.PotionEffect
 * @see org.bukkit.potion.PotionEffectType
 * @see fr.ludos.Main
 */

public class VampiricLeechSkill implements Listener {

    private static final double LEECH_PERCENTAGE = 0.2;

    /**
     * Activate the Vampiric Leech skill when the necromancer damages an entity.
     *
     * @param event The event triggered when an entity is damaged by the necromancer.
     */

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player necromancer = (Player) event.getDamager();
            LivingEntity target = (LivingEntity) event.getEntity();

            if (isEnemy(target)) {
                double leechAmount = event.getDamage() * LEECH_PERCENTAGE;

                leechHealth(necromancer, target, leechAmount);
            }
        }
    }

    /** Register VampiricLeechSkill as an event listener
     * 
     * @param plugin      The instance of the main plugin class.
     * @param necromancer The player who activates the skill.
     */ 

    public static void activateVampiricLeech(Main plugin, Player necromancer) {
        Bukkit.getPluginManager().registerEvents(new VampiricLeechSkill(), plugin);
    }

    /**
     * Check if the given entity is considered an enemy. WARN : Not fixing yet
     *
     * @param entity The entity to check.
     * @return True if the entity is an enemy, false otherwise.
     */

    private boolean isEnemy(LivingEntity entity) {
        // Actually, all living entities are enemies, wait team
        return entity instanceof LivingEntity;
    }

    /**
     * Leech health from the target to the necromancer.
     *
     * @param necromancer The necromancer player.
     * @param target      The target from which health is leached.
     * @param amount      The amount of health to leach.
     */

    private void leechHealth(Player necromancer, LivingEntity target, double amount) {

        double currentHealth = necromancer.getHealth();
        double newHealth = Math.min(necromancer.getMaxHealth(), currentHealth + amount);

        necromancer.setHealth(newHealth);
        necromancer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1));

        target.damage(amount);
    }
}
