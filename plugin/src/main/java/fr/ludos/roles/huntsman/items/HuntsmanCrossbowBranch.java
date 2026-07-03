package fr.ludos.roles.huntsman.items;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import fr.ludos.core.item.MultiLevelBranchItem;

public interface HuntsmanCrossbowBranch extends MultiLevelBranchItem.Branch {
	public abstract void processShotArrow(Arrow arrow, HumanEntity player, int level, EntityShootBowEvent event);
	public abstract void processLandedArrow(Arrow arrow, HumanEntity player, int level, ProjectileHitEvent event);
}
