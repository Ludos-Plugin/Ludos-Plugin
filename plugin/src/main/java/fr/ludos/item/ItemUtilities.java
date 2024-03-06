package fr.ludos.item;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;

public class ItemUtilities {

	public static Boolean isBreakable(Block block) {
		return block.getType().isSolid() && ! block.getType().isAir() && block.getType().getHardness() >= 0;
	}
}