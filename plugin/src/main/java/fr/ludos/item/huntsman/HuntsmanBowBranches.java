package fr.ludos.item.huntsman;

import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.item.SpecialItemBranches;
import net.md_5.bungee.api.ChatColor;

import java.util.List;

public enum HuntsmanBowBranches implements SpecialItemBranches<HuntsmanBowBranches> {
	FLAME    (ChatColor.RED.toString() + ChatColor.ITALIC + "Igniting", 200, "Igniting Description"),
	WITHER   (ChatColor.GRAY.toString() + ChatColor.ITALIC + "Rotting",  200, "Rotting Description"),
	SLOWNESS (ChatColor.BLUE.toString() + ChatColor.ITALIC + "Impeding", 200, "Impeding Description");

	private String name;
	private double xpThreshold;
	private List<HuntsmanBowBranches> evolutions;
	private String description;

	public final static HuntsmanBowBranches[] values = HuntsmanBowBranches.values();

	public int index() {
		return ArrayUtils.indexOf(values(), this);
	}


	public String getName() {
		return name;
	}
	public double getXpThreshold() {
		return xpThreshold;
	}
	public List<HuntsmanBowBranches> getEvolutions() {
		return evolutions;
	}
	public String getDescription() {
		return description;
	}
	public boolean isMax(int level) {
		return level == 2;
	}

	public void processShotArrow(Arrow arrow, Player player, int level, EntityShootBowEvent event) {
		switch (this) {
			case FLAME:
				arrow.setFireTicks(10000);
				break;
			case WITHER:
				int poisonDuration = 60;
				int poisonAmplifier = level > 0 ? 1 : 2;
				PotionEffect poisonEffect = new PotionEffect(PotionEffectType.POISON, poisonDuration, poisonAmplifier);
				arrow.addCustomEffect(poisonEffect, true);
				break;
			case SLOWNESS:
				int slowDuration = 60;
				int slowAmplifier = level > 0 ? 0 : 1;

				PotionEffect slowEffect = new PotionEffect(PotionEffectType.SLOW, slowDuration, slowAmplifier);
				arrow.addCustomEffect(slowEffect, true);
				break;
			default:
				break;
		}
	}
	public void processLandedArrow(Arrow arrow, Player player, int level, ProjectileHitEvent event) {
		boolean isAbilityEnabled = isMax(level) && ! player.hasCooldown(Material.ARROW);

		switch (this) {
			case FLAME:
				if (level == 0) {
					break;
				}

				Block firedBlock;
				if (event.getHitBlockFace() == null) {
					firedBlock = event.getHitBlock();
				} else {
					firedBlock = event.getHitBlock().getRelative(event.getHitBlockFace());
				}
				firedBlock.setType(Material.FIRE);

				if (level > 1 && isAbilityEnabled) {
					arrow.getWorld().createExplosion(arrow.getLocation(), 2, true, false, player);
				}
				break;
			case WITHER:
				if (level > 1 && isAbilityEnabled) {
					AreaEffectCloud effect = (AreaEffectCloud) arrow.getWorld().spawnEntity(arrow.getLocation(), EntityType.AREA_EFFECT_CLOUD);
					effect.addCustomEffect(PotionEffectType.WITHER.createEffect(60, 1), false);
					effect.setDuration(40);
				}
				break;
			case SLOWNESS:
				if (level > 1 && isAbilityEnabled) {
					Block webbedBlock;
					if (event.getHitBlockFace() == null) {
						webbedBlock = event.getHitBlock();
					} else {
						webbedBlock = event.getHitBlock().getRelative(event.getHitBlockFace());
					}
					webbedBlock.setType(Material.COBWEB);
				}
				break;
			default:
				break;
		}

		if (isAbilityEnabled) {
			player.setCooldown(Material.ARROW, 200);
		}
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
}