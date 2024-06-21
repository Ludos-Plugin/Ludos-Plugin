package fr.ludos.item.burrower;

import fr.ludos.role.BurrowerRole;
import fr.ludos.role.Role;
import fr.ludos.item.SpecialItem;
import fr.ludos.game.Game;
import fr.ludos.item.ItemUtilities;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class BurrowerShovel extends SpecialItem {

	private static final int COOLDOWN_SECONDS = 20;
	private static final int TUNNEL_LENGTH = 10;

	private final static Map<Player, List<BlockState>> tunnelBlocks = new HashMap<>();


	public BurrowerShovel(ItemStack stack) throws IllegalArgumentException {
		super(stack);
	}

	public BurrowerShovel(Player owner) {
		this(new ItemStack(Material.IRON_SHOVEL), owner);
	}

	protected BurrowerShovel(ItemStack stack, Player owner) {
		super(stack, owner);
	}

	public void useAbility() {
		if (getOwner().hasCooldown(getStack().getType())) {
			return;
		}
		List<BlockState> playerBlocks = tunnelBlocks.get(getOwner());


		if (playerBlocks != null) {
			// sendHUDIcon(false);
			revertTunnel();
		} else {
			// sendHUDIcon(true);
			digTunnel();
		}
	}

	// private void sendHUDIcon(Boolean hudActive) {
	// 	if (! hudActive){
	// 		getOwner().removePotionEffect(PotionEffectType.LUCK);
	// 	} else {
	// 		getOwner().addPotionEffect(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 1, false, true));
	// 	}
    // }

	private void digTunnel() {
		List<Block> lastTwoTargetBlocks = getOwner().getLastTwoTargetBlocks(null, 20);
		if (lastTwoTargetBlocks.size() != 2) return;
		Block targetBlock = lastTwoTargetBlocks.get(1);
		Block adjacentBlock = lastTwoTargetBlocks.get(0);

		BlockFace face = targetBlock.getFace(adjacentBlock);
		Location currentLocation = targetBlock.getLocation();
		List<BlockState> playerBlocks = new ArrayList<>();

		for (int i = 1; i <= TUNNEL_LENGTH; i++) {
			tunnelBlock(getOwner(), currentLocation, playerBlocks);
			tunnelBlock(getOwner(), currentLocation.clone().add(0, -1, 0), playerBlocks);
			currentLocation.add(face.getDirection().multiply(-1));
		}

		if (playerBlocks.size() == 0) {
			return;
		}

		tunnelBlocks.put(getOwner(), playerBlocks);
		getOwner().setCooldown(getStack().getType(), 5);
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


	private void revertTunnel() {
		List<BlockState> playerBlocks = tunnelBlocks.get(getOwner());
		if (playerBlocks == null) {
			return;
		}

        playerBlocks.forEach(blockState -> {
			Location currentBlockLocation = blockState.getLocation();
			Block currentBlock = currentBlockLocation.getBlock();

			currentBlock.breakNaturally();
			currentBlock.setType(blockState.getType(), true);
        });

		tunnelBlocks.remove(getOwner());

		getOwner().setCooldown(getStack().getType(), COOLDOWN_SECONDS * 20);
	}

	@Override
	public String getId() {
		return "manhuntBurrowerShovel";
	}

	@Override
	protected String getName() {
		return "Burrower's Shovel"; // TODO: Translate
	}



	public static class Events extends SpecialItem.Events<BurrowerShovel> {

		public Events(Game game) {
			super(game);
		}


		public class BlockBreak {
			Location location;
			Material material;

			public BlockBreak(Location location, Material material){
				this.location = location;
				this.material = material;
			}
		}

		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
				return;
			}

			ItemStack mainItem = event.getItem();

			BurrowerShovel shovel = getItem(mainItem);
			if (shovel == null) {
				return;
			}

			shovel.useAbility();
		}

		// private void reduceUsage(Player player) {
		// 	int remainingUsages = usages.getOrDefault(player, MAX_USAGES);
		// 	if (remainingUsages > 0) {
		// 		usages.put(player, remainingUsages - 1);
		// 	}
		// }

		@Override
		@Nullable
		protected BurrowerShovel getItem(ItemStack stack) {
			try {
				BurrowerShovel shovel = new BurrowerShovel(stack);
				return shovel;
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		@Override
		protected BurrowerShovel createItem(Player owner) {
			return new BurrowerShovel(owner);
		}
		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, BurrowerRole.id);
		}
	}
}