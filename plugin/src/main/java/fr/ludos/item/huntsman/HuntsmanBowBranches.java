package fr.ludos.item.huntsman;

import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.item.SpecialItemBranches;

import java.util.List;

public enum HuntsmanBowBranches implements SpecialItemBranches<HuntsmanBowBranches> {
	FLAME    ("Igniting", 200, "Igniting Description"),
	WITHER   ("Rotting",  200, "Rotting Description"),
	SLOWNESS ("Impeding", 200, "Impeding Description");

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

	public void processShotArrow(Arrow arrow, Player player, int level) {
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
	public void processLandedArrow(Arrow arrow, Player player, int level, @Nullable Entity hitEntity) {
		boolean isAbilityEnabled = isMax(level) && ! player.hasCooldown(Material.ARROW);

		switch (this) {
			case FLAME:
				if (level == 0) {
					break;
				}
				arrow.getLocation().getBlock().setType(Material.FIRE);

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
					arrow.getLocation().getBlock().setType(Material.COBWEB);
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