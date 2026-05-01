package fr.ludos.item.harvester;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import fr.ludos.item.LevelItem.Level;
import fr.ludos.item.SpecialItem;

public enum HarvesterScytheLevels implements Level<HarvesterScytheLevels> {
	WOODEN      (Material.WOODEN_HOE,    25,   new HashMap<>(){{ put(Enchantment.SWEEPING_EDGE, 2); }}),
	STONE       (Material.STONE_HOE,     50,   new HashMap<>(){{ put(Enchantment.SWEEPING_EDGE, 2); }}),
	STONE1      (Material.STONE_HOE,     100,  new HashMap<>(){{ put(Enchantment.SWEEPING_EDGE, 2); put(Enchantment.DAMAGE_ALL, 1); }}),
	IRON        (Material.IRON_HOE,      200,  new HashMap<>(){{ put(Enchantment.SWEEPING_EDGE, 2); put(Enchantment.DAMAGE_ALL, 2); }}),
	IRON1       (Material.IRON_HOE,      500,  new HashMap<>(){{ put(Enchantment.SWEEPING_EDGE, 2); put(Enchantment.DAMAGE_ALL, 3); }}),
	IRON2       (Material.IRON_HOE,      1000, new HashMap<>(){{ put(Enchantment.SWEEPING_EDGE, 2); put(Enchantment.DAMAGE_ALL, 3); put(Enchantment.LOOT_BONUS_MOBS, 1);}}),
	DIAMOND     (Material.DIAMOND_HOE,   1500, new HashMap<>(){{ put(Enchantment.SWEEPING_EDGE, 2); put(Enchantment.DAMAGE_ALL, 3); put(Enchantment.LOOT_BONUS_MOBS, 1);}}),
	DIAMOND1    (Material.DIAMOND_HOE,   2000, new HashMap<>(){{ put(Enchantment.SWEEPING_EDGE, 3); put(Enchantment.DAMAGE_ALL, 4); put(Enchantment.LOOT_BONUS_MOBS, 1);}}),
	DIAMOND2    (Material.DIAMOND_HOE,   2500, new HashMap<>(){{ put(Enchantment.SWEEPING_EDGE, 3); put(Enchantment.DAMAGE_ALL, 4); put(Enchantment.LOOT_BONUS_MOBS, 2);}}),
	NETHERITE   (Material.NETHERITE_HOE, 3000, new HashMap<>(){{ put(Enchantment.SWEEPING_EDGE, 3); put(Enchantment.DAMAGE_ALL, 4); put(Enchantment.LOOT_BONUS_MOBS, 2);}}),
	NETHERITE1  (Material.NETHERITE_HOE, 4500, new HashMap<>(){{ put(Enchantment.SWEEPING_EDGE, 3); put(Enchantment.DAMAGE_ALL, 5); put(Enchantment.LOOT_BONUS_MOBS, 2);}}),
	NETHERITE2  (Material.NETHERITE_HOE, 6000, new HashMap<>(){{ put(Enchantment.SWEEPING_EDGE, 3); put(Enchantment.DAMAGE_ALL, 5); put(Enchantment.LOOT_BONUS_MOBS, 3);}});

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

	private HarvesterScytheLevels(Material material, double xpThreshold, Map<Enchantment, Integer> enchantments) {
		this.material = material;
		this.xpThreshold = xpThreshold;
		this.enchantments = enchantments;
	}

	@Override
	public Class<HarvesterScytheLevels> getLevelClass() {
		return HarvesterScytheLevels.class;
	}

	@Override
	public void onEquip(SpecialItem item) { }

	@Override
	public void onUnequip(SpecialItem item) { }

	@Override
	public void onSetLevel(SpecialItem item) {
		ItemStack stack = item.getStack();
		stack.setType(material);
		stack.removeEnchantment(Enchantment.DAMAGE_ALL);
		stack.removeEnchantment(Enchantment.SWEEPING_EDGE);
		stack.removeEnchantment(Enchantment.LOOT_BONUS_MOBS);
		stack.addUnsafeEnchantments(enchantments);
	}

	@Override
	public void onUnsetLevel(SpecialItem item) { }

}
