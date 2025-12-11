package fr.ludos.item.trapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import fr.ludos.item.LevelItem;


public enum TrapperDaggerLevels implements LevelItem.Level<TrapperDaggerLevels> {
	SHARTNESS   (Enchantment.DAMAGE_ALL, 3),
	LOOTING     (Enchantment.LOOT_BONUS_MOBS, 1),
	FIREASPECT  (Enchantment.FIRE_ASPECT, 1),

	STONE      (Material.STONE_SWORD,     25, 0, Collections.emptyMap()),
	IRON       (Material.STONE_SWORD,     55, 0, Collections.emptyMap()),
	DIMAND     (Material.DIAMOND_SWORD,   175, 0, Collections.emptyMap()),
	NETHERITE  (Material.NETHERITE_SWORD, 500, 0, Collections.emptyMap());

	private final static TrapperDaggerLevels[] values = TrapperDaggerLevels.values();

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

	private int depth;
	public int getDepth(){
		return depth;
	}

	public int getLevelByTimer() {
		// int minutes = (int) (ManhuntTimer.totalSeconds / 60);
		// return minutes / 10 < 3 ? minutes / 10 : 2;
		return 0;
	}

	private TrapperDaggerLevels(Enchantment enchantment, int level) {
		this.enchantments = new HashMap<>();
		this.enchantments.put(enchantment, getLevelByTimer() + level);
	}

	private TrapperDaggerLevels(Material material, double xpThreshold, int depth, Map<Enchantment, Integer> enchantments) {
		this.material = material;
		this.xpThreshold = xpThreshold;
		this.enchantments = enchantments;
		this.depth = depth;
	}

	public int index() {
		return ArrayUtils.indexOf(values, this);
	}

	public boolean isMax() {
		return (index() + 1) >= values.length;
	}

	@Nullable
	public static TrapperDaggerLevels findByKey(int i) {
		if ( i >= values.length ) {
			return null;
		}
		return values()[i];
	}

	@Override
	public Class<TrapperDaggerLevels> getLevelClass() {
		return TrapperDaggerLevels.class;
	}
}