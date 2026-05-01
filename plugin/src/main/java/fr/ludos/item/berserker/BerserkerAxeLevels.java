package fr.ludos.item.berserker;

import java.util.Collections;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import fr.ludos.item.LevelItem;
import fr.ludos.item.SpecialItem;

public enum BerserkerAxeLevels implements LevelItem.Level<BerserkerAxeLevels> {
	IRON(Material.IRON_AXE, Material.GOLDEN_AXE, 0, Collections.emptyMap(), 20),
	IRON2(Material.IRON_AXE, Material.GOLDEN_AXE, 0.5, Collections.singletonMap(Enchantment.DAMAGE_ALL, 1), 40),
	GOLD(Material.GOLDEN_AXE, Material.DIAMOND_AXE, 1, Collections.singletonMap(Enchantment.DAMAGE_ALL, 1), 60),
	GOLD2(Material.GOLDEN_AXE, Material.DIAMOND_AXE, 1.5, Collections.singletonMap(Enchantment.DAMAGE_ALL, 2), 80),
	DIAMOND(Material.DIAMOND_AXE, Material.NETHERITE_AXE, 2, Collections.singletonMap(Enchantment.DAMAGE_ALL, 1), 100),
	DIAMOND2(Material.DIAMOND_AXE, Material.NETHERITE_AXE, 2.5, Collections.singletonMap(Enchantment.DAMAGE_ALL, 2), 120);


	private final Material material;
	public Material getMaterial() {
		return material;
	}

	private final Material offhandMaterial;
	public Material getOffhandMaterial() {
		return offhandMaterial;
	}

	public Material getMaterialForVariant(BerserkerAxe.Variant variant) {
		return switch (variant) {
			case FIRST -> material;
			case SECOND -> offhandMaterial;
		};
	}

	private final double damageBonus;
	public double getDamageBonus() {
		return damageBonus;
	}

	private final Map<Enchantment, Integer> enchantments;
	public Map<Enchantment, Integer> getEnchantments() {
		return enchantments;
	}

	private final double xpThreshold;
	public double getXpThreshold() {
		return xpThreshold;
	}

	private BerserkerAxeLevels(Material material, Material offhandMaterial, double damageBonus, Map<Enchantment, Integer> enchantments, double xpThreshold) {
		this.material = material;
		this.offhandMaterial = offhandMaterial;
		this.damageBonus = damageBonus;
		this.enchantments = enchantments;
		this.xpThreshold = xpThreshold;
	}

	@Override
	public Class<BerserkerAxeLevels> getLevelClass() {
		return BerserkerAxeLevels.class;
	}

	@Override
	public void onEquip(SpecialItem item) { }
	@Override
	public void onUnequip(SpecialItem item) { }

	@Override
	public void onSetLevel(SpecialItem item) {
		ItemStack stack = item.getStack();
		Material material = getMaterialForVariant(((BerserkerAxe) item).getVariant());
		stack.setType(material);
		stack.removeEnchantment(Enchantment.DAMAGE_ALL);
		stack.addEnchantments(enchantments);
	}
	@Override
	public void onUnsetLevel(SpecialItem item) { }
}
