package fr.ludos.item;


public interface BranchItemInterface<TBranch extends Enum<TBranch> & BranchItem.Branch<TBranch>> extends SpecialItemInterface {
	public TBranch getBranch();
	public void setBranch(TBranch branch);
}