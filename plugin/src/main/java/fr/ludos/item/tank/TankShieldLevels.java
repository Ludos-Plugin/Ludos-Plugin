package fr.ludos.item.tank;

import fr.ludos.item.LevelItem.Level;

import org.bukkit.Material;

import fr.ludos.item.SpecialItem;

public enum TankShieldLevels implements Level<TankShieldLevels> {
	LEVEL_1(5, 15),
	LEVEL_2(10, 40),
	LEVEL_3(15, 100),
	LEVEL_4(20, 200),
	LEVEL_5(30, 400);

	private final int durability;
	private final double xpThreshold;

	TankShieldLevels(int durability, double xpThreshold) {
		this.durability = durability;
		this.xpThreshold = xpThreshold;
	}

	@Override
	public Class<TankShieldLevels> getLevelClass() {
		return TankShieldLevels.class;
	}

	public int getDurability() {
		return durability;
	}
	public double durabilityPerDamage() {
		return (double) Material.SHIELD.getMaxDurability() / (double) durability;
	}
	@Override
	public double getXpThreshold() {
		return this.xpThreshold;
	}

	@Override
	public void onEquip(SpecialItem item) { }

	@Override
	public void onUnequip(SpecialItem item) { }

	@Override
	public void onSetLevel(SpecialItem item) { }

	@Override
	public void onUnsetLevel(SpecialItem item) { }

}
