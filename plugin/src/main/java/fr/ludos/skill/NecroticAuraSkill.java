// NecroticAuraSkill.java

package fr.ludos.skill;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.Main;

import org.bukkit.entity.Entity;

/**
 * NecroticAuraSkill class provides the implementation for the Necrotic Aura skill in a Bukkit/Spigot server.
 * When activated by a necromancer player, it deals damage and applies a wither effect to the prey player within
 * a specified radius. Additionally, it displays particle effects around the prey player.
 * <br><br>
 * Features:
 * <br><br>
 * - Deals damage and applies wither effect to the prey player.
 * <br><br>
 * - Displays particle effects around the prey player.
 * <br><br>
 * Usage:
 * <br><br>
 * - Call activateNecroticAura method to trigger the Necrotic Aura skill.
 * <br><br>
 * Example:
 * <br><br>
 * <pre>{@code
 * NecroticAuraSkill.activateNecroticAura(plugin, necromancer, prey);
 * }</pre>
 * <br><br>
 * @param plugin      The instance of the main plugin class.
 * @param necromancer The player who activates the skill.
 * @param prey      The player being prey.
 * @author feur25
 * @version 1.0
 * @see org.bukkit.Bukkit
 * @see org.bukkit.Particle
 * @see org.bukkit.entity.LivingEntity
 * @see org.bukkit.entity.Player
 * @see org.bukkit.potion.PotionEffect
 * @see org.bukkit.potion.PotionEffectType
 * @see org.bukkit.entity.Entity
 * @see fr.ludos.Main
 */

public class NecroticAuraSkill {

	private static final int AURA_RADIUS = 8;
	private static final int AURA_DAMAGE = 2;

	/**
	 * Activate the Necrotic Aura skill around the necromancer player.
	 *
	 * @param plugin      The instance of the main plugin class.
	 * @param necromancer The player who activates the skill.
	 * @param prey      The player being prey.
	 */

	public static void activateNecroticAura(Main plugin, Player necromancer, Player prey) {

		// Apply damage and potion effect
		for (Entity entity : necromancer.getNearbyEntities(AURA_RADIUS, AURA_RADIUS, AURA_RADIUS)) {
			if (entity instanceof LivingEntity && entity.getUniqueId().equals(prey.getUniqueId())) {
				LivingEntity livingEntity = (LivingEntity) entity;

				livingEntity.damage(AURA_DAMAGE);

				livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
			}
		}

		// Display particle effects for the aura
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			for (double t = 0; t <= Math.PI * 2; t += Math.PI / 16) {
				double x = AURA_RADIUS * Math.cos(t);
				double y = 1;
				double z = AURA_RADIUS * Math.sin(t);

				prey.getWorld().spawnParticle(Particle.SPELL_WITCH, prey.getLocation().clone().add(x, y, z), 1);
			}
		});
	}
}