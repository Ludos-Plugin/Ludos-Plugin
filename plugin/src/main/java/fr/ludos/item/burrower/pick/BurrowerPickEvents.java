package fr.ludos.item.burrower.pick;

import fr.ludos.Main;
import fr.ludos.item.LevelItemEvents;
import fr.ludos.role.BurrowerRole;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;


import java.util.List;
import javax.annotation.Nullable;


/**
 * BurrowerPick is a class that represents a special item, "The Burrower's Pickaxe," in Minecraft.
 * This item allows the miner player to improve their own pickaxe based on the ores they collect.
 * <br><br>
 * Features:
 * <br><br>
 * - Provides methods to create a miner pickaxe, give it to the player and level up the pickaxe based on XP.
 * <br><br>
 * - Automatically updates the pickaxe's material and enchantments as it levels up.
 * <br><br>
 * - Defines an evolution path from wood to stone, iron, gold, diamond and finally netherite pickaxe.
 * <br><br>
 * Usage:
 * Example:
 * @author feur25 & Ganon358
 * @version 1.0
 * @see org.bukkit.entity.Player
 * @see org.bukkit.inventory.ItemStack
 * @see org.bukkit.Material
 * @see org.bukkit.inventory.meta.ItemMeta
 * @see java.util.Collections
 */

public class BurrowerPickEvents extends LevelItemEvents<BurrowerPick, BurrowerPickLevels> {
	private static final String OWNER_NAMESPACE_KEY = "ludos_miner_pickaxe_owner";
	private static final String XP_NAMESPACE_KEY = "ludos_miner_pickaxe_xp";
	private static final String LVL_NAMESPACE_KEY = "ludos_miner_pickaxe_lvl";
	private static final String MODE_NAMESPACE_KEY = "ludos_miner_pickaxe_mode";

	private static NamespacedKey ownerKey = null;
	private static NamespacedKey xpKey = null;
	private static NamespacedKey lvlKey = null;
	private static NamespacedKey modeKey = null;


	static NamespacedKey getOwnerKey() {
		return ownerKey;
	}
	static NamespacedKey getXpKey() {
		return xpKey;
	}
	static NamespacedKey getLvlKey() {
		return lvlKey;
	}
	static NamespacedKey getModeKey() {
		return modeKey;
	}


	public BurrowerPickEvents() {
		Main plugin = Main.getInstance();
		ownerKey = new NamespacedKey(plugin, OWNER_NAMESPACE_KEY);
		xpKey = new NamespacedKey(plugin, XP_NAMESPACE_KEY);
		lvlKey = new NamespacedKey(plugin, LVL_NAMESPACE_KEY);
		modeKey = new NamespacedKey(plugin, MODE_NAMESPACE_KEY);
	}


	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
			return;
		}


		Player player = event.getPlayer();
		BurrowerPick pickaxe = getItem(player.getInventory().getItemInMainHand());
		if (pickaxe == null) {
			return;
		}

		if (player.hasCooldown(pickaxe.getStack().getType())) {
			return;
		}

		pickaxe.toggleHammerMode();

		player.setCooldown(pickaxe.getStack().getType(), 5);
	}

	@EventHandler
	public void onSwitchItem(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();

		BurrowerPick pick = BurrowerPick.findIn(player.getInventory(), this::getItem);
		if (pick == null) {
			return;
		}

		pick.updateWielding(player.getInventory().getItem(event.getNewSlot()));
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		ItemStack mainHandItem = player.getInventory().getItemInMainHand();

		BurrowerPick pick = getItem(mainHandItem);
		if (pick == null) {
			return;
		}

		List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 100);
		if (lastTwoTargetBlocks.size() != 2) return;

		Block targetBlock = lastTwoTargetBlocks.get(1);
		Block adjacentBlock = lastTwoTargetBlocks.get(0);


		if (! pick.getHammerMode()) {
			pick.awardBreak(player, targetBlock);
			return;
		}

		BlockFace face = targetBlock.getFace(adjacentBlock);

		pick.breakRadius(targetBlock.getLocation(), face);
	}

	@Override
	protected String getRoleId() {
		return BurrowerRole.id;
	}

	@Override
	protected BurrowerPickLevels getDefaultLevel() {
		return BurrowerPickLevels.WOODEN;
	}

	@Override
	@Nullable
	protected BurrowerPick getItem(ItemStack stack) {
		try {
			BurrowerPick pick = new BurrowerPick(stack);
			return pick;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	@Override
	protected BurrowerPick createItem(Player owner, BurrowerPickLevels level) {
		return new BurrowerPick(owner, level);
	}
}