package fr.ludos.item;

import fr.ludos.item.LevelItem.LevelState;

public interface LevelItemInterface extends SpecialItemInterface {
	public LevelState getLevelState();

	public int getLvl();
	public double getXp();
}
