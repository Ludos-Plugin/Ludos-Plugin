package fr.ludos.item;

import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;


public class ItemUtilities {

	public static Boolean isBreakable(Block block) {
		return block.getType().isSolid() && ! block.getType().isAir() && block.getType().getHardness() >= 0;
	}

	public static void doSweepAttack(Player attacker, LivingEntity primaryTarget, double damage, int enchantmentLevel) {
		if (attacker.getAttackCooldown() >= 0.848) {
			attacker.getWorld().spawnParticle(Particle.SWEEP_ATTACK, attacker.getLocation().add(0, 1, 0).add(attacker.getEyeLocation().getDirection()), 1);

			attacker.getNearbyEntities(1.5, 0.25, 1.5).forEach(nearby -> {
				if (nearby != primaryTarget && nearby instanceof LivingEntity nearbyEntity) {
					nearbyEntity.damage(1 + damage * (enchantmentLevel / (enchantmentLevel + 1)));
				}
			});
		}
	}
	public static void doSweepAttack(Player attacker, LivingEntity primaryTarget, double damage) {
		doSweepAttack(attacker, primaryTarget, damage, 0);
	}
}