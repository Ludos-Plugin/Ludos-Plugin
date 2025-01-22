package fr.ludos.item;

public interface SpecialItemLevels<T extends SpecialItemLevels<T>> {
	public int index();
	public double getXpThreshold();
	public boolean isMax();
	public T getPrevious();
	public T getNext();
}