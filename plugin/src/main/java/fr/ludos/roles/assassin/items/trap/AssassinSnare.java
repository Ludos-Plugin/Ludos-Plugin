package fr.ludos.roles.assassin.items.trap;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.core.item.BranchItemInterface;

/**
 * {@link BranchItemInterface.Branch} for the {@link AssassinSnareDevice}.
 */
public interface AssassinSnare extends BranchItemInterface.Branch {
	public AssassinTrap createTrap(JavaPlugin plugin, Player owner, Block block, BlockFace face);
}
