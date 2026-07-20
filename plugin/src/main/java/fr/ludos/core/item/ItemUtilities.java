package fr.ludos.core.item;

import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;

/**
 * Utilites for {@link ItemStack}s and related.
 */
public class ItemUtilities {

	public static Boolean isBreakable(Block block) {
		return block.getType().isSolid() && ! block.getType().isAir() && block.getType().getHardness() >= 0;
	}

	public static void doSweepAttack(HumanEntity attacker, LivingEntity primaryTarget, double damage, int enchantmentLevel, double horizontalRange) {
		if (attacker.getAttackCooldown() >= 0.848) {
			attacker.getWorld().spawnParticle(Particle.SWEEP_ATTACK, attacker.getLocation().add(0, 1, 0).add(attacker.getEyeLocation().getDirection()), 1);

			double doubleEnchantmentLevel = (double)enchantmentLevel;
			double sweepDamage = 1.0 + damage * (doubleEnchantmentLevel / (doubleEnchantmentLevel + 1.0));

			attacker.getNearbyEntities(horizontalRange, 0.25, horizontalRange).forEach(nearby -> {
				if (nearby != primaryTarget && nearby instanceof LivingEntity nearbyEntity) {
					nearbyEntity.damage(sweepDamage);
				}
			});
		}
	}
	public static void doSweepAttack(HumanEntity attacker, LivingEntity primaryTarget, double damage, int enchantmentLevel) {
		doSweepAttack(attacker, primaryTarget, damage, enchantmentLevel, 1.5);
	}
	public static void doSweepAttack(HumanEntity attacker, LivingEntity primaryTarget, double damage) {
		doSweepAttack(attacker, primaryTarget, damage, 0);
	}
}