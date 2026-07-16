package fr.ludos.roles.harvester.items;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.core.item.level.LevelItem;


public enum HarvesterPickLevels implements LevelItem.Level<HarvesterPickLevels> {
	WOODEN      (Material.WOODEN_PICKAXE,    25,   1, 0, Collections.emptyMap()),
	STONE       (Material.STONE_PICKAXE,     50,   1, 0, Collections.emptyMap()),
	STONE1      (Material.STONE_PICKAXE,     100,  1, 0, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 1); }}),
	IRON        (Material.IRON_PICKAXE,      200,  1, 1, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 2); }}),
	IRON1       (Material.IRON_PICKAXE,      500,  1, 1, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 3); }}),
	IRON2       (Material.IRON_PICKAXE,      1000, 1, 1, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 3); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}}),
	DIAMOND     (Material.DIAMOND_PICKAXE,   1500, 1, 2, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 3); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}}),
	DIAMOND1    (Material.DIAMOND_PICKAXE,   2000, 1, 2, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}}),
	DIAMOND2    (Material.DIAMOND_PICKAXE,   2500, 1, 2, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}}),
	NETHERITE   (Material.NETHERITE_PICKAXE, 3000, 1, 2, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}}),
	NETHERITE1  (Material.NETHERITE_PICKAXE, 4500, 1, 2, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 5); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}}),
	NETHERITE2  (Material.NETHERITE_PICKAXE, 6000, 1, 2, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 5); put(Enchantment.LOOT_BONUS_BLOCKS, 3);}});


	private final static HarvesterPickLevels[] VALUES = HarvesterPickLevels.values();

	private final Material material;
	public Material getMaterial() {
		return material;
	}

	private double xpThreshold;
	public double xpThreshold() {
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


	private HarvesterPickLevels(Material material, double xpThreshold, int radius, int depth, Map<Enchantment, Integer> enchantments) {
		this.material = material;
		this.xpThreshold = xpThreshold;
		this.enchantments = enchantments;
		this.radius = radius;
		this.depth = depth;
	}


	public boolean isMax() {
		return (ordinal() + 1) >= VALUES.length;
	}


	@Override
	public void onUnequip(SpecialItemInterface item) { }

	@Override
	public void onEquip(SpecialItemInterface item) { }

	@Override
	public void onSwitchToLevel(SpecialItemInterface item) {
		ItemStack stack = item.getStack();
		stack.setType(material);
		stack.removeEnchantment(Enchantment.DIG_SPEED);
		stack.removeEnchantment(Enchantment.LOOT_BONUS_BLOCKS);
		stack.addEnchantments(enchantments);
	}
	@Override
	public void onSwitchOffLevel(SpecialItemInterface item) { }
}