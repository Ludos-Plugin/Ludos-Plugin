package fr.ludos.item.burrower.digtool;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.inventory.meta.ItemMeta;

import fr.ludos.item.SpecialItem;

import fr.ludos.item.ItemUtilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

import java.util.HashMap;
import java.util.Map;


/**
 * Pickaxe is a class that represents a special item, "Miner Pickaxe," in Minecraft.
 * This item allows the miner player to improve their own pickaxe based on the ores they collect.
 * <br><br>
 * Features:
 * <br><br>
 * - Provides methods to give a miner pickaxe to the player and level up the pickaxe based on XP.
 * <br><br>
 * - Automatically updates the pickaxe's material as it levels up.
 * <br><br>
 * - Defines an evolution path from wood to stone, iron, gold, and finally diamond pickaxe.
 * <br><br>
 * Usage:
 * <br><br>
 * - Call addPickaxeInventory(player) to give a miner pickaxe to the specified player.
 * <br><br>
 * - Call levelPickaxe(player, xp) with the XP gained from mining to level up the pickaxe.
 * <br><br>
 * Example:
 * <pre>{@code
 * Pickaxe pickaxe = new Pickaxe();
 * pickaxe.addPickaxeInventory(player);
 * pickaxe.levelPickaxe(player, xp);
 * }</pre>
 * <br><br>
 * @author feur25 & Ganon358
 * @version 1.0
 * @see org.bukkit.entity.Player
 * @see org.bukkit.inventory.ItemStack
 * @see org.bukkit.Material
 * @see org.bukkit.inventory.meta.ItemMeta
 * @see java.util.Collections
 */

public class BurrowingClaw extends SpecialItem {

	public final static Map<Player, List<BlockState>> tunnelBlocks = new HashMap<>();

	private int usages = 0;

	private static final int TUNNEL_LENGTH = 20;
	private static final int MAX_USAGES = 3;

	public int getUsages() {
		return usages;
	}

	public BurrowingClaw(ItemStack stack) throws IllegalArgumentException {
		super(stack);

		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
		if ( ! container.has(BurrowingClawEvents.getUsagesKey(), PersistentDataType.INTEGER) ) {
			throw new IllegalArgumentException();
		}

		this.usages = getPersistentData(stack, BurrowingClawEvents.getUsagesKey(), PersistentDataType.INTEGER);
	}

	public BurrowingClaw(Player owner) {
		this(new ItemStack(Material.RABBIT_FOOT), owner);
	}
	public BurrowingClaw(ItemStack stack, Player owner) {
		super(stack, owner);

		setUsages(300);
	}

	public void digTunnel(Player player) {
		List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 20);
		if (lastTwoTargetBlocks.size() != 2) return;
		Block targetBlock = lastTwoTargetBlocks.get(1);
		Block adjacentBlock = lastTwoTargetBlocks.get(0);

		BlockFace face = targetBlock.getFace(adjacentBlock);
		Location currentLocation = targetBlock.getLocation();
		List<BlockState> playerBlocks = new ArrayList<>();

		for (int i = 1; i <= TUNNEL_LENGTH; i++) {
			tunnelBlock(player, currentLocation, playerBlocks);
			tunnelBlock(player, currentLocation.clone().add(0, -1, 0), playerBlocks);
			currentLocation.add(face.getDirection().multiply(-1));
		}

		tunnelBlocks.put(player, playerBlocks);
		player.setCooldown(getStack().getType(), 5);

		// reduceUsage(player);
	}

    private void tunnelBlock(Player player, Location location, List<BlockState> blockBuffer) {
        Block eyeBlock = location.getBlock();

		if (! ItemUtilities.isBreakable(eyeBlock)) {
			return;
		}
		if (blockBuffer.stream().anyMatch( state -> state.getLocation().equals(location) )) {
			return;
		}

        blockBuffer.add(eyeBlock.getState());
        eyeBlock.setType(Material.AIR, false);
    }


	public void revertTunnel(Player player) {
		List<BlockState> playerBlocks = tunnelBlocks.get(player);
		if (playerBlocks == null) {
			return;
		}

        playerBlocks.forEach(blockState -> {
			Location currentBlockLocation = blockState.getLocation();
			Block currentBlock = currentBlockLocation.getBlock();

			currentBlock.breakNaturally();
			currentBlock.setType(blockState.getType(), true);
        });

		tunnelBlocks.remove(player);

		player.setCooldown(getStack().getType(), 600);
	}

	public void setUsages(int usages) {
		ItemMeta meta = getStack().getItemMeta();

		this.usages = usages;
		meta.getPersistentDataContainer().set(BurrowingClawEvents.getUsagesKey(), PersistentDataType.INTEGER, usages);
		getStack().setItemMeta(meta);
	}


	public void SetUsages(int usages) {

	}

	@Override
	public NamespacedKey getOwnerKey() {
		return BurrowingClawEvents.getOwnerKey();
	}

	@Override
	protected String getName() {
		return "Claw of Burrowing"; // TODO: Translate
	}
}