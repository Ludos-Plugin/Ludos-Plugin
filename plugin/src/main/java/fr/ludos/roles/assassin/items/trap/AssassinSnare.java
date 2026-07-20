package fr.ludos.roles.assassin.items.trap;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import fr.ludos.core.item.BranchItemInterface;

/**
 * {@link BranchItemInterface.Branch} for the {@link AssassinSnareDevice}.
 */
public interface AssassinSnare extends BranchItemInterface.Branch {
	public AssassinTrap createTrap(Player owner, Block block, BlockFace face);
	public int getLimit();
}
