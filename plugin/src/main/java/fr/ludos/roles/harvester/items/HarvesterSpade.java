package fr.ludos.roles.harvester.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.core.item.level.LevelItem;
import fr.ludos.roles.harvester.HarvesterRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of the Huntsman Spade, for use by any Player with {@link HarvesterRole}.
 */
public class HarvesterSpade extends LevelItem<HarvesterSpade, HarvesterSpadeLevels> {
	public static final String ID = "harvester_spade";

	private static final int COOLDOWN_SECONDS = 20;
	private static final int TUNNEL_LENGTH = 10;

	private final static Map<Player, List<List<BlockState>>> TUNNEL_BLOCKS = new HashMap<>();

	HarvesterSpade(LevelItem.ItemData<HarvesterSpadeLevels> info, Events events) {
		super(info, events);
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
		lore.add(SpecialItemInterface.getActionAnnotation("key.use", Component.text("Tunnel")));

		return lore;
	}


	public void useAbility() {
		if (! refreshUseCooldown()) return;
		boolean tunnelActive = TUNNEL_BLOCKS.containsKey(getOwner());


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
		if (TUNNEL_BLOCKS.containsKey(getOwner())) return false;

		List<Block> lastTwoTargetBlocks = getOwner().getLastTwoTargetBlocks(null, 12);
		if (lastTwoTargetBlocks.size() != 2) return false;
		TUNNEL_BLOCKS.put(getOwner(), null);


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
					TUNNEL_BLOCKS.put(getOwner(), digBlocksState);
					cancel();
				}
			}
		}.runTaskTimer(getGame().getPlugin(), 0, 1);

		return true;
	}


	private boolean revertTunnel() {
		List<List<BlockState>> blocks = TUNNEL_BLOCKS.get(getOwner());
		if (blocks == null) return false;
		TUNNEL_BLOCKS.put(getOwner(), null);


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
					TUNNEL_BLOCKS.remove(getOwner());
					cancel();
				}
			}

		}.runTaskTimer(getGame().getPlugin(), 0, 1);


		getOwner().setCooldown(getStack().getType(), COOLDOWN_SECONDS * 20);
		return true;
	}

	/**
	 * Events for the {@link HarvesterSpade}.
	 */
	public static class Events extends LevelItem.Events<HarvesterSpade, HarvesterSpadeLevels> {
		private static final List<HarvesterSpadeLevels> LEVELS = List.of(HarvesterSpadeLevels.values());
		public final HarvesterRole role;

		public Events(HarvesterRole role, Game game) {
			super(game, new Events.Info(ItemSlot.HOTBAR_3));
			this.role = role;
		}

		@Override
		public String getTypeId() {
			return ID;
		}

		@Override
		public List<HarvesterSpadeLevels> getLevels() {
			return LEVELS;
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

			role.awardBreak(event.getPlayer(), event.getBlock(), spade.getGame());
		}

		@Override
		protected HarvesterSpade getItemInternal(ItemData<HarvesterSpadeLevels> info) {
			return new HarvesterSpade(info, this);
		}

		@Override
		protected HarvesterSpade createItemInternal(LevelData<HarvesterSpadeLevels> data, Player owner) {
			HarvesterSpadeLevels currentLevel = data.getCurrentLevelOr(HarvesterSpadeLevels.WOODEN);
			return new HarvesterSpade(new ItemData<>(data, new SpecialItem.ItemData(new ItemStack(currentLevel.getMaterial()), owner)), this);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.getLudos().getRoleManager().isPlayerRole(owner, HarvesterRole.ID);
		}
	}
}