package fr.ludos.item.burrower.pick;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import fr.ludos.item.SpecialItemLevels;

public enum BurrowerPickLevels implements SpecialItemLevels {
	WOODEN      (Material.WOODEN_PICKAXE,    25,    1, Collections.emptyMap()),
	STONE       (Material.STONE_PICKAXE,     37,    1, Collections.emptyMap()),
	STONE1      (Material.STONE_PICKAXE,     55,    1, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 1); }}),
	GOLDEN      (Material.GOLDEN_PICKAXE,    82,    1, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 1); }}),
	GOLDEN1     (Material.GOLDEN_PICKAXE,    123,   1, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 2); }}),
	IRON        (Material.IRON_PICKAXE,      184,   1, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 2); }}),
	IRON1       (Material.IRON_PICKAXE,      276,   1, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 3); }}),
	IRON2       (Material.IRON_PICKAXE,      414,   1, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 3); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}}),
	DIAMOND     (Material.DIAMOND_PICKAXE,   621,   2, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 3); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}}),
	DIAMOND1    (Material.DIAMOND_PICKAXE,   931,   2, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}}),
	DIAMOND2    (Material.DIAMOND_PICKAXE,   1396,  2, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}}),
	NETHERITE   (Material.NETHERITE_PICKAXE, 2094,  2, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}}),
	NETHERITE1  (Material.NETHERITE_PICKAXE, 6238,  2, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 5); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}}),
	NETHERITE2  (Material.NETHERITE_PICKAXE, 10000, 2, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 5); put(Enchantment.LOOT_BONUS_BLOCKS, 3);}});

	private Material material;
	private double xpThreshold;
	private Map<Enchantment, Integer> enchantments;
	private int radius;

	private final static BurrowerPickLevels[] values = BurrowerPickLevels.values();

	public int index() {
		return ArrayUtils.indexOf(values, this);
	}
	public Material getMaterial() {
		return material;
	}
	public Map<Enchantment, Integer> getEnchantments() {
		return enchantments;
	}
	public double getXpThreshold() {
		return xpThreshold;
	}
	public boolean isMax() {
		return (index() + 1) >= values.length;
	}
	public int getRadius(){
		return radius;
	}


	private BurrowerPickLevels(Material material, double xpThreshold, int radius, Map<Enchantment, Integer> enchantments) {
		this.material = material;
		this.xpThreshold = xpThreshold;
		this.radius = radius;
		this.enchantments = enchantments;
	}


	@Nullable
	public static BurrowerPickLevels findByKey(int i) {
		if ( i >= values.length ) {
			return null;
		}
		return values()[i];
	}

	@Override
	public BurrowerPickLevels getPrevious() {
		Integer index = index() - 1;
		index = Math.max(0, index);
		return values()[index];
	}

	@Override
	public BurrowerPickLevels getNext() {
		int currentIndex = index();
		if ( currentIndex + 1 >= values.length ) {
			return this;
		}
		return values()[currentIndex + 1];
	}
}