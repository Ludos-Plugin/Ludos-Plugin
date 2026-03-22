package fr.ludos.item.burrower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.ludos.game.Game;
import fr.ludos.item.ItemUtilities;
import fr.ludos.item.LevelItem;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.BurrowerRole;
import fr.ludos.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public class BurrowerShovel extends LevelItem<BurrowerShovelLevels> {
	private static final String ID = "manhuntBurrowerShovel";

	// private final static Map<UUID, BurrowerShovel> cachedItems = new HashMap<>();

	private static final int COOLDOWN_SECONDS = 20;
	private static final int TUNNEL_LENGTH = 10;

	private final static Map<Player, List<BlockState>> tunnelBlocks = new HashMap<>();


	public static BurrowerShovel fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// BurrowerShovel cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;
		LevelState levelState = LevelItem.levelFromItemStack(stack, game);
		if (levelState == null) return null;

		BurrowerShovel burrowerShovel = new BurrowerShovel(stack, owner, levelState, game);
		// cachedItems.put(itemId, burrowerShovel);

		return burrowerShovel;
	}

	public static BurrowerShovel createItem(Player owner, LevelState level, Game game) {
		BurrowerShovelLevels lvl = BurrowerShovelLevels.values()[level.getLevel()];
		BurrowerShovel burrowerShovel = new BurrowerShovel(new ItemStack(lvl.getMaterial()), owner, level, game);
		UUID itemId = burrowerShovel.initializeItem();

		// cachedItems.put(itemId, burrowerShovel);

		return burrowerShovel;
	}

	protected BurrowerShovel(ItemStack stack, Player owner, LevelState level, Game game) {
		super(BurrowerShovelLevels.class, stack, owner, level, game);
	}


	@Override
	public String getTypeId() {
		return ID;
	}

	@Override
	public Component getName() {
		return
			Component.text("Burrower's Shovel")
			.decoration(TextDecoration.ITALIC, false); // TODO: Translate
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();

		lore.add(Component.text("Ability: Dig a tunnel, use again to revert it")
			.decoration(TextDecoration.ITALIC, false)
			.color(NamedTextColor.GRAY));
		lore.add(getActionAnnotation("key.use", Component.text("Tunnel")));

		return lore;
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



	public static class Events extends LevelItem.Events<BurrowerShovel, BurrowerShovelLevels> {

		public Events(Game game) {
			super(game, 2);
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

			BurrowerShovel shovel = getItem(mainItem, game);
			if (shovel == null) {
				return;
			}

			shovel.useAbility();
		}

		@EventHandler
		public void onBlockBreak(BlockBreakEvent event) {
			Player player = event.getPlayer();
			ItemStack mainHandItem = player.getInventory().getItemInMainHand();

			BurrowerShovel shovel = getItem(mainHandItem, game);
			if (shovel == null) return;

			BurrowerRole.awardBreak(event.getPlayer(), event.getBlock(), shovel.getGame());
		}

		@Override
		@Nullable
		protected BurrowerShovel getItem(ItemStack stack, Game game) {
			return BurrowerShovel.fromItemStack(stack, game);
		}
		@Override
		protected BurrowerShovel createItem(Player owner, LevelState level, Game game) {
			return BurrowerShovel.createItem(owner, level, game);
		}
		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, BurrowerRole.id);
		}
	}
}