package fr.ludos.item;

public interface SpecialItemLevels {
	public int index();
	public double getXpThreshold();
	public boolean isMax();
	public <T extends SpecialItemLevels> T getPrevious();
	public <T extends SpecialItemLevels> T getNext();
}