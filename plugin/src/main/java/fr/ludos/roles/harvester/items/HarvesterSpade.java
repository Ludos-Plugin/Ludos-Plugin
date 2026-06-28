package fr.ludos.roles.harvester.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.core.Utility;
import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.ItemUtilities;
import fr.ludos.core.item.LevelItem;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.role.Role;
import fr.ludos.roles.harvester.HarvesterRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public class HarvesterSpade extends LevelItem<HarvesterSpadeLevels> {
	private static final String ID = "manhuntHarvesterSpade";

	// private final static Map<UUID, HarvesterSpade> cachedItems = new HashMap<>();

	private static final int COOLDOWN_SECONDS = 20;
	private static final int TUNNEL_LENGTH = 10;

	private final static Map<Player, List<List<BlockState>>> tunnelBlocks = new HashMap<>();


	public static HarvesterSpade fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// HarvesterSpade cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;
		LevelState levelState = LevelItem.levelFromItemStack(stack, game);
		if (levelState == null) return null;

		HarvesterSpade harvesterSpade = new HarvesterSpade(stack, owner, levelState, game);
		// cachedItems.put(itemId, harvesterSpade);

		return harvesterSpade;
	}

	public static HarvesterSpade createItem(Player owner, LevelState level, Game game) {
		HarvesterSpadeLevels lvl = HarvesterSpadeLevels.values()[level.getLevel()];
		HarvesterSpade harvesterSpade = new HarvesterSpade(new ItemStack(lvl.getMaterial()), owner, level, game);
		UUID itemId = harvesterSpade.initializeItem();

		// cachedItems.put(itemId, harvesterSpade);

		return harvesterSpade;
	}

	protected HarvesterSpade(ItemStack stack, Player owner, LevelState level, Game game) {
		super(HarvesterSpadeLevels.class, stack, owner, level, game);
	}


	@Override
	public String getTypeId() {
		return ID;
	}

	@Override
	public Component getName() {
		return
			Component.text("Harvester's Spade")
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
		if (! refreshUseCooldown()) return;
		boolean tunnelActive = tunnelBlocks.containsKey(getOwner());


		if (tunnelActive) {
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


	private boolean digTunnel() {
		if (tunnelBlocks.containsKey(getOwner())) return false;

		List<Block> lastTwoTargetBlocks = getOwner().getLastTwoTargetBlocks(null, 12);
		if (lastTwoTargetBlocks.size() != 2) return false;
		tunnelBlocks.put(getOwner(), null);


		Block targetBlock = lastTwoTargetBlocks.get(1);
		Block adjacentBlock = lastTwoTargetBlocks.get(0);
		BlockFace face = targetBlock.getFace(adjacentBlock);
		World world = targetBlock.getWorld();

		List<List<Block>> digBlocks;
		if (face == BlockFace.UP || face == BlockFace.DOWN) {
			digBlocks = Utility.getAllBlockRows(
				targetBlock, face,
				Pair.of(0, 0),
				Pair.of(0, 0),
				Pair.of(0, TUNNEL_LENGTH)
			);
		} else {
			digBlocks = Utility.getAllBlockColumns(
				targetBlock, face,
				Pair.of(0, 0),
				Pair.of(-1, 0),
				Pair.of(0, TUNNEL_LENGTH)
			);
		}

		if (digBlocks.size() == 0) return false;
		List<List<BlockState>> digBlocksState = digBlocks.stream()
			.map(blockColumn -> blockColumn.stream()
				.map(Block::getState)
				.toList()
			).toList();


		new BukkitRunnable() {
			int current = 0;

			@Override
			public void run() {
				List<Block> blockColumn = digBlocks.get(current);
				for (Block block : blockColumn) {
					if (! ItemUtilities.isBreakable(block)) continue;
					world.playEffect(block.getLocation(), org.bukkit.Effect.STEP_SOUND, block.getType());
					block.setType(Material.AIR, false);
				}

				current++;
				if (current >= digBlocks.size()) {
					tunnelBlocks.put(getOwner(), digBlocksState);
					cancel();
				}
			}
		}.runTaskTimer(getGame().getPlugin(), 0, 1);

		return true;
	}


	private boolean revertTunnel() {
		List<List<BlockState>> blocks = tunnelBlocks.get(getOwner());
		if (blocks == null) return false;
		tunnelBlocks.put(getOwner(), null);


		new BukkitRunnable() {
			int current = 0;

			@Override
			public void run() {
				List<BlockState> blockColumn = blocks.get(current);
				for (BlockState state : blockColumn) {
					Location location = state.getLocation();
					Block block = location.getBlock();
					World world = location.getWorld();

					world.playEffect(location, org.bukkit.Effect.STEP_SOUND, state.getType());
					block.setBlockData(state.getBlockData(), false);
				}

				current++;
				if (current >= blocks.size()) {
					tunnelBlocks.remove(getOwner());
					cancel();
				}
			}

		}.runTaskTimer(getGame().getPlugin(), 0, 1);


		getOwner().setCooldown(getStack().getType(), COOLDOWN_SECONDS * 20);
		return true;
	}


	public static class Events extends LevelItem.Events<HarvesterSpade, HarvesterSpadeLevels> {

		public Events(Game game) {
			super(game, ItemSlot.HOTBAR_3);
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

			HarvesterSpade spade = getItem(mainItem);
			if (spade == null) {
				return;
			}

			spade.useAbility();
		}

		@EventHandler
		public void onBlockBreak(BlockBreakEvent event) {
			Player player = event.getPlayer();
			ItemStack mainHandItem = player.getInventory().getItemInMainHand();

			HarvesterSpade spade = getItem(mainHandItem);
			if (spade == null) return;

			HarvesterRole.awardBreak(event.getPlayer(), event.getBlock(), spade.getGame());
		}

		@Override
		@Nullable
		public HarvesterSpade getItem(ItemStack stack) {
			return HarvesterSpade.fromItemStack(stack, game);
		}
		@Override
		public HarvesterSpade createItem(Player owner, LevelState level) {
			return HarvesterSpade.createItem(owner, level, game);
		}
		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return Role.isPlayerRole(owner, HarvesterRole.id);
		}
	}
}