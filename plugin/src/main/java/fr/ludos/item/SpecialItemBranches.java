package fr.ludos.item;

import net.kyori.adventure.text.Component;

public interface SpecialItemBranches<T extends SpecialItemBranches<T>> {
	public Component getName();
	public Component getDescription();

	public int index();

	public void onSwitchBranch(BranchItem<T> item);
}