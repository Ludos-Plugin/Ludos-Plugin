package fr.ludos.item.burrower;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import fr.ludos.item.LevelItem;


public enum BurrowerPickLevels implements LevelItem.Level<BurrowerPickLevels> {
	WOODEN      (Material.WOODEN_PICKAXE,    25,    1, 0, Collections.emptyMap()),
	STONE       (Material.STONE_PICKAXE,     55,    1, 0, Collections.emptyMap()),
	STONE1      (Material.STONE_PICKAXE,     115,   1, 0, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 1); }}),
	IRON        (Material.IRON_PICKAXE,      175,   1, 1, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 2); }}),
	IRON1       (Material.IRON_PICKAXE,      269,   1, 1, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 3); }}),
	IRON2       (Material.IRON_PICKAXE,      387,   1, 1, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 3); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}}),
	DIAMOND     (Material.DIAMOND_PICKAXE,   499,   1, 2, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 3); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}}),
	DIAMOND1    (Material.DIAMOND_PICKAXE,   1396,  1, 2, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}}),
	DIAMOND2    (Material.DIAMOND_PICKAXE,   2094,  1, 2, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}}),
	NETHERITE   (Material.NETHERITE_PICKAXE, 6238,  1, 2, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}}),
	NETHERITE1  (Material.NETHERITE_PICKAXE, 13275, 1, 2, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 5); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}}),
	NETHERITE2  (Material.NETHERITE_PICKAXE, 27450, 1, 2, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 5); put(Enchantment.LOOT_BONUS_BLOCKS, 3);}});


	private final static BurrowerPickLevels[] values = BurrowerPickLevels.values();

	private Material material;
	public Material getMaterial() {
		return material;
	}

	private double xpThreshold;
	public double getXpThreshold() {
		return xpThreshold;
	}

	private Map<Enchantment, Integer> enchantments;
	public Map<Enchantment, Integer> getEnchantments() {
		return enchantments;
	}

	private int radius;
	public int getRadius(){
		return radius;
	}

	private int depth;
	public int getDepth(){
		return depth;
	}


	private BurrowerPickLevels(Material material, double xpThreshold, int radius, int depth, Map<Enchantment, Integer> enchantments) {
		this.material = material;
		this.xpThreshold = xpThreshold;
		this.enchantments = enchantments;
		this.radius = radius;
		this.depth = depth;
	}


	public int index() {
		return ArrayUtils.indexOf(values, this);
	}
	public boolean isMax() {
		return (index() + 1) >= values.length;
	}

	@Override
	public BurrowerPickLevels getPrevious() {
		int index = index() - 1;
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