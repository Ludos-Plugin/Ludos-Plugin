package fr.ludos.roles.harvester.items;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.item.LevelItem.Level;
import fr.ludos.core.item.SpecialItem;

public enum HarvesterSpadeLevels implements Level<HarvesterSpadeLevels> {
	WOODEN      (Material.WOODEN_SHOVEL,    25,   Collections.emptyMap()),
	STONE       (Material.STONE_SHOVEL,     50,   Collections.emptyMap()),
	STONE1      (Material.STONE_SHOVEL,     100,  new HashMap<>(){{ put(Enchantment.DIG_SPEED, 1); }}),
	IRON        (Material.IRON_SHOVEL,      200,  new HashMap<>(){{ put(Enchantment.DIG_SPEED, 2); }}),
	IRON1       (Material.IRON_SHOVEL,      500,  new HashMap<>(){{ put(Enchantment.DIG_SPEED, 3); }}),
	IRON2       (Material.IRON_SHOVEL,      1000, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 3); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}}),
	DIAMOND     (Material.DIAMOND_SHOVEL,   1500, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 3); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}}),
	DIAMOND1    (Material.DIAMOND_SHOVEL,   2000, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}}),
	DIAMOND2    (Material.DIAMOND_SHOVEL,   2500, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}}),
	NETHERITE   (Material.NETHERITE_SHOVEL, 3000, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}}),
	NETHERITE1  (Material.NETHERITE_SHOVEL, 4500, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 5); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}}),
	NETHERITE2  (Material.NETHERITE_SHOVEL, 6000, new HashMap<>(){{ put(Enchantment.DIG_SPEED, 5); put(Enchantment.LOOT_BONUS_BLOCKS, 3);}});

	private final Material material;
	public Material getMaterial() {
		return material;
	}

	private final double xpThreshold;
	@Override
	public double getXpThreshold() {
		return xpThreshold;
	}

	private final Map<Enchantment, Integer> enchantments;
	public Map<Enchantment, Integer> getEnchantments() {
		return enchantments;
	}

	private HarvesterSpadeLevels(Material material, double xpThreshold, Map<Enchantment, Integer> enchantments) {
		this.material = material;
		this.xpThreshold = xpThreshold;
		this.enchantments = enchantments;
	}

	@Override
	public Class<HarvesterSpadeLevels> getLevelClass() {
		return HarvesterSpadeLevels.class;
	}

	@Override
	public void onEquip(SpecialItem item) { }

	@Override
	public void onUnequip(SpecialItem item) { }

	@Override
	public void onSetLevel(SpecialItem item) {
		ItemStack stack = item.getStack();
		stack.setType(material);
		stack.removeEnchantment(Enchantment.DIG_SPEED);
		stack.removeEnchantment(Enchantment.LOOT_BONUS_BLOCKS);
		stack.addUnsafeEnchantments(enchantments);
	}

	@Override
	public void onUnsetLevel(SpecialItem item) { }

}
