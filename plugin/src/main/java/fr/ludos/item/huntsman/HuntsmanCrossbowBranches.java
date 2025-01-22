package fr.ludos.item.huntsman;

import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.item.SpecialItemLevelBranches;
import net.md_5.bungee.api.ChatColor;

import java.util.List;
import java.util.Random;

public enum HuntsmanCrossbowBranches implements SpecialItemLevelBranches<HuntsmanCrossbowBranches> {
	FLAME (ChatColor.RED.toString() + ChatColor.ITALIC + "Igniting", 200, "Igniting Description") {
		@Override
		public void processShotArrow(Arrow arrow, HumanEntity player, int level, EntityShootBowEvent event) {
			arrow.setFireTicks(10000);
		}

		@Override
		public void processLandedArrow(Arrow arrow, HumanEntity player, int level, ProjectileHitEvent event) {
			if (level >= 1) {
				Block firedBlock = getBlock(event);
				firedBlock.setType(Material.FIRE);
			}

			if (level >= 2) {
				arrow.getWorld().createExplosion(arrow.getLocation(), 2, true, false, null);
			}
		}
	},


	WITHER (ChatColor.GRAY.toString() + ChatColor.ITALIC + "Rotting",  200, "Rotting Description") {
		@Override
		public void processShotArrow(Arrow arrow, HumanEntity player, int level, EntityShootBowEvent event) {
			int poisonDuration = 20 * 3;
			int poisonAmplifier = level > 0 ? 1 : 2;
			PotionEffect poisonEffect = new PotionEffect(PotionEffectType.POISON, poisonDuration, poisonAmplifier);
			arrow.addCustomEffect(poisonEffect, true);
		}

		@Override
		public void processLandedArrow(Arrow arrow, HumanEntity player, int level, ProjectileHitEvent event) {
			if (level > 1 && isMax(level)) {
				AreaEffectCloud effect = (AreaEffectCloud) arrow.getWorld().spawnEntity(arrow.getLocation(), EntityType.AREA_EFFECT_CLOUD);
				effect.addCustomEffect(PotionEffectType.WITHER.createEffect(60, 1), false);
				effect.setDuration(40);
			}
		}
	},


	SLOWNESS (ChatColor.BLUE.toString() + ChatColor.ITALIC + "Impeding", 200, "Impeding Description") {
		@Override
		public void processShotArrow(Arrow arrow, HumanEntity player, int level, EntityShootBowEvent event) {
			PotionEffect slowEffect = new PotionEffect(PotionEffectType.SLOW, 20 * 7, 2);
			arrow.addCustomEffect(slowEffect, true);

			if (level >= 1) {
				PotionEffect coldEffect = new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 7, 2);
				arrow.addCustomEffect(coldEffect, true);
			}
		}

		@Override
		public void processLandedArrow(Arrow arrow, HumanEntity player, int level, ProjectileHitEvent event) {
			if (level >= 2) {
				Random random = new Random();

				Block webCenter = event.getEntity().getLocation().getBlock();
				int thickness = 2;

				for (int i = -thickness; i <= thickness; i++) {
				for (int j = -thickness; j <= thickness; j++) {
				for (int k = -thickness; k <= thickness; k++) {
					Block block = webCenter.getRelative(i, j, k);
					float distanceFromCenter = Math.max(Math.max(Math.abs(i), Math.abs(j)), Math.abs(k));
					float distanceSpawnFactor = distanceFromCenter / (thickness + 1);

					if (block.getType().isAir() && ! block.getType().isSolid() && random.nextFloat() > distanceSpawnFactor) {
						block.setType(Material.COBWEB);
					}
				}
				}
				}
			}
		}
	};



	public final static HuntsmanCrossbowBranches[] values = HuntsmanCrossbowBranches.values();

	private String name;
	public String getName() {
		return name;
	}

	private double xpThreshold;
	public double getXpThreshold() {
		return xpThreshold;
	}

	private List<HuntsmanCrossbowBranches> evolutions;
	public List<HuntsmanCrossbowBranches> getEvolutions() {
		return evolutions;
	}

	private String description;
	public String getDescription() {
		return description;
	}


	private HuntsmanCrossbowBranches(String name, double xpThreshold, String description) {
		this.name = name;
		this.xpThreshold = xpThreshold;
		this.description = description;
	}


	@Nullable
	public static HuntsmanCrossbowBranches findByKey(int i) {
		if ( i >= values.length ) return null;

		return values[i];
	}


	private static Block getBlock(ProjectileHitEvent event) {
		Block block;
		if (event.getHitBlock() != null) {
			block = event.getHitBlock().getRelative(event.getHitBlockFace());
		}
		else if (event.getHitEntity() != null ) {
			block = event.getHitEntity().getLocation().getBlock();
		}
		else {
			block = event.getEntity().getLocation().getBlock();
		}

		return block;
	}

	public int index() {
		return ArrayUtils.indexOf(values, this);
	}

	public boolean isMax(int level) {
		return level == 2;
	}

	public abstract void processShotArrow(Arrow arrow, HumanEntity player, int level, EntityShootBowEvent event);
	public abstract void processLandedArrow(Arrow arrow, HumanEntity player, int level, ProjectileHitEvent event);
}