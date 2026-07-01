package fr.ludos.roles.tank.items;

import org.bukkit.Material;

import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.core.item.level.LevelItemInterface.Level;

public enum TankShieldLevels implements Level<TankShieldLevels> {
	LEVEL_1(10, 0.25, 15),
	LEVEL_2(20, 0.5, 25),
	LEVEL_3(30, 0.75, 35),
	LEVEL_4(40, 1.0, 45),
	LEVEL_5(60, 1.5, 60),
	LEVEL_6(80, 2.0, 75),
	LEVEL_7(100, 2.5, 90),
	LEVEL_8(150, 2.5, 120),
	LEVEL_9(200, 2.5, 150),
	LEVEL_10(250, 2.5, 180);

	private final int durability;
	private final double regen;
	private final double xpThreshold;

	TankShieldLevels(int durability, double regen, double xpThreshold) {
		this.durability = durability;
		this.regen = regen;
		this.xpThreshold = xpThreshold;
	}

	public int getDurability() {
		return durability;
	}
	public double durabilityPerDamage() {
		return (double) Material.SHIELD.getMaxDurability() / (double) durability;
	}
	public double getRegen() {
		return regen;
	}
	@Override
	public double xpThreshold() {
		return this.xpThreshold;
	}

	@Override
	public void onEquip(SpecialItemInterface item) { }

	@Override
	public void onUnequip(SpecialItemInterface item) { }

	@Override
	public void onSetLevel(SpecialItemInterface item) { }

	@Override
	public void onUnsetLevel(SpecialItemInterface item) { }

}
