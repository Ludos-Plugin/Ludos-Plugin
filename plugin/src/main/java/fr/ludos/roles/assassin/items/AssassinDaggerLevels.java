package fr.ludos.roles.assassin.items;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;

import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.core.item.level.LevelItemInterface;

/**
 * Default {@link LevelItemInterface.Level}s for the {@link AssassinDagger}.
 */
public enum AssassinDaggerLevels implements LevelItemInterface.Level<AssassinDaggerLevels> {
	BASE    (10, Collections.emptyMap(),                                  false),
	SHARP1  (20, new HashMap<>() {{ put(Enchantment.DAMAGE_ALL, 1); }},   false),
	POISONED(30, new HashMap<>() {{ put(Enchantment.DAMAGE_ALL, 1); }},   true),
	SHARP2  (0,  new HashMap<>() {{ put(Enchantment.DAMAGE_ALL, 2); }},   true);

	private final double xpThreshold;
	private final Map<Enchantment, Integer> enchantments;
	private final boolean appliesPoison;

	AssassinDaggerLevels(double xpThreshold, Map<Enchantment, Integer> enchantments, boolean appliesPoison) {
		this.xpThreshold = xpThreshold;
		this.enchantments = enchantments;
		this.appliesPoison = appliesPoison;
	}

	@Override
	public double xpThreshold() { return xpThreshold; }

	public Map<Enchantment, Integer> getEnchantments() { return enchantments; }

	public boolean appliesPoison() { return appliesPoison; }

	@Override
	public void onSwitchToLevel(SpecialItemInterface item) {
		item.getStack().removeEnchantment(Enchantment.DAMAGE_ALL);
		item.getStack().addEnchantments(enchantments);
	}

	@Override
	public void onSwitchOffLevel(SpecialItemInterface item) {
		item.getStack().removeEnchantment(Enchantment.DAMAGE_ALL);
	}

	@Override
	public void onEquip(SpecialItemInterface item) { }

	@Override
	public void onUnequip(SpecialItemInterface item) { }
}
