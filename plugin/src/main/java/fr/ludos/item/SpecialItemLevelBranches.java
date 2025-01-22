package fr.ludos.item;

public interface SpecialItemLevelBranches<T extends SpecialItemLevelBranches<T>> extends SpecialItemBranches<T> {
	public double getXpThreshold();
	public boolean isMax(int level);
}
