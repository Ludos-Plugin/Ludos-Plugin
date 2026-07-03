package fr.ludos.roles.harvester.items;

import org.bukkit.event.block.BlockBreakEvent;

import fr.ludos.core.item.BranchItemInterface;

public interface HarvesterPickBranch extends BranchItemInterface.Branch {
	public abstract void onBreakBlock(HarvesterPick pick, BlockBreakEvent event);
}
