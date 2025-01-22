package fr.ludos.item;

public interface SpecialItemBranches<T extends SpecialItemBranches<T>> {
	public String getName();
	public String getDescription();

	public int index();
}