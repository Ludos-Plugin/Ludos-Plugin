package fr.ludos.item.huntsman;

import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.item.SpecialItemLevelBranches;
import net.md_5.bungee.api.ChatColor;

import java.util.List;

public enum HuntsmanBowBranches implements SpecialItemLevelBranches<HuntsmanBowBranches> {
	FLAME (ChatColor.RED.toString() + ChatColor.ITALIC + "Igniting", 200, "Igniting Description") {
		@Override
		public void processShotArrow(Arrow arrow, Player player, int level, EntityShootBowEvent event) {
			arrow.setFireTicks(10000);
		}

		@Override
		public void processLandedArrow(Arrow arrow, Player player, int level, ProjectileHitEvent event) {
			if (level == 0) {
				return;
			}

			Block firedBlock;
			if (event.getHitBlockFace() == null) {
				firedBlock = event.getHitBlock();
			} else {
				firedBlock = event.getHitBlock().getRelative(event.getHitBlockFace());
			}
			firedBlock.setType(Material.FIRE);

			if (level > 1 && isMax(level)) {
				arrow.getWorld().createExplosion(arrow.getLocation(), 2, true, false, player);
			}
		}
	},


	WITHER (ChatColor.GRAY.toString() + ChatColor.ITALIC + "Rotting",  200, "Rotting Description") {
		@Override
		public void processShotArrow(Arrow arrow, Player player, int level, EntityShootBowEvent event) {
			int poisonDuration = 20 * 3;
			int poisonAmplifier = level > 0 ? 1 : 2;
			PotionEffect poisonEffect = new PotionEffect(PotionEffectType.POISON, poisonDuration, poisonAmplifier);
			arrow.addCustomEffect(poisonEffect, true);
		}

		@Override
		public void processLandedArrow(Arrow arrow, Player player, int level, ProjectileHitEvent event) {
			if (level > 1 && isMax(level)) {
				AreaEffectCloud effect = (AreaEffectCloud) arrow.getWorld().spawnEntity(arrow.getLocation(), EntityType.AREA_EFFECT_CLOUD);
				effect.addCustomEffect(PotionEffectType.WITHER.createEffect(60, 1), false);
				effect.setDuration(40);
			}
		}
	},


	SLOWNESS (ChatColor.BLUE.toString() + ChatColor.ITALIC + "Impeding", 200, "Impeding Description") {
		@Override
		public void processShotArrow(Arrow arrow, Player player, int level, EntityShootBowEvent event) {
			int slowAmplifier = level > 0 ? 0 : 1;

			PotionEffect slowEffect = new PotionEffect(PotionEffectType.SLOW, 20 * 3, slowAmplifier);
			arrow.addCustomEffect(slowEffect, true);
		}

		@Override
		public void processLandedArrow(Arrow arrow, Player player, int level, ProjectileHitEvent event) {
			if (level > 1 && isMax(level)) {
				Block webbedBlock;
				if (event.getHitBlockFace() == null) {
					webbedBlock = event.getHitBlock();
				}
				else {
					webbedBlock = event.getHitBlock().getRelative(event.getHitBlockFace());
				}
				webbedBlock.setType(Material.COBWEB);
			}
		}
	};

	public final static HuntsmanBowBranches[] values = HuntsmanBowBranches.values();

	private String name;
	public String getName() {
		return name;
	}

	private double xpThreshold;
	public double getXpThreshold() {
		return xpThreshold;
	}

	private List<HuntsmanBowBranches> evolutions;
	public List<HuntsmanBowBranches> getEvolutions() {
		return evolutions;
	}

	private String description;
	public String getDescription() {
		return description;
	}


	private HuntsmanBowBranches(String name, double xpThreshold, String description) {
		this.name = name;
		this.xpThreshold = xpThreshold;
		this.description = description;
	}


	@Nullable
	public static HuntsmanBowBranches findByKey(int i) {
		if ( i >= values.length ) {
			return null;
		}
		return values()[i];
	}


	public int index() {
		return ArrayUtils.indexOf(values(), this);
	}

	public boolean isMax(int level) {
		return level == 2;
	}

	public abstract void processShotArrow(Arrow arrow, Player player, int level, EntityShootBowEvent event);
	public abstract void processLandedArrow(Arrow arrow, Player player, int level, ProjectileHitEvent event);
}