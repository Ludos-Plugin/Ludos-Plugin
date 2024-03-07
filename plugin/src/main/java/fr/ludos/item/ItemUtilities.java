package fr.ludos.item;

import org.bukkit.block.Block;

public class ItemUtilities {

	public static Boolean isBreakable(Block block) {
		return block.getType().isSolid() && ! block.getType().isAir() && block.getType().getHardness() >= 0;
	}
}