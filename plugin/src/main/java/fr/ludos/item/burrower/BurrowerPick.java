package fr.ludos.item.burrower;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import fr.ludos.game.Game;
import fr.ludos.item.BranchItem;
import fr.ludos.item.ItemUtilities;
import fr.ludos.item.LevelBranchItem;
import fr.ludos.item.LevelItem;
import fr.ludos.item.SpecialItem;
import fr.ludos.item.huntsman.HuntsmanArrow;
import fr.ludos.role.BurrowerRole;
import fr.ludos.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public class BurrowerPick extends LevelBranchItem<BurrowerPickBranches, BurrowerPickLevels> {
	private static final String ID = "manhuntBurrowerPick";

	// private final static Map<UUID, BurrowerPick> cachedItems = new HashMap<>();


	public static BurrowerPick fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// BurrowerPick cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;
		Integer branchIndex = BranchItem.branchFromItemStack(stack, game);
		if (branchIndex == null) return null;
		LevelState levelState = LevelItem.levelFromItemStack(stack, game);
		if (levelState == null) return null;

		BurrowerPick burrowerPick = new BurrowerPick(stack, owner, BurrowerPickBranches.values()[branchIndex], levelState, game);
		// cachedItems.put(itemId, burrowerPick);

		return burrowerPick;
	}

	public static BurrowerPick createItem(Player owner, LevelState level, Game game) {
		BurrowerPickLevels lvl = BurrowerPickLevels.values()[level.getLevel()];
		BurrowerPick burrowerPick = new BurrowerPick(new ItemStack(lvl.getMaterial()), owner, BurrowerPickBranches.Pickaxe, level, game);
		UUID itemId = burrowerPick.initializeItem();

		// cachedItems.put(itemId, burrowerPick);

		return burrowerPick;
	}

	protected BurrowerPick(ItemStack stack, Player owner, BurrowerPickBranches branch, LevelState level, Game game) {
		super(BurrowerPickBranches.class, BurrowerPickLevels.class, stack, owner, branch, level, game);
	}


	@Override
	public String getTypeId() {
		return ID;
	}

	@Override
	public Component getName() {
		return Component.text("Burrower's Pick (")
			.append(getBranch().getName())
			.append(Component.text(")"))
			.decoration(TextDecoration.ITALIC, false); // TODO: Translate
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();

		BurrowerPickLevels level = getLvlObject();
		int size = 1 + level.getRadius() * 2;
		int depth = level.getDepth() + 1;


		lore.add(BranchItem.getCycleBranchAnnotation("key.use"));

		lore.add(
			Component.text("Size: ")
				.color(NamedTextColor.GRAY)
			.append(Component.text(size + "x" + size)
				.color(NamedTextColor.YELLOW))
			.decoration(TextDecoration.ITALIC, false)
		);

		lore.add(
			Component.text("Depth: ")
				.color(NamedTextColor.GRAY)
			.append(Component.text(depth)
				.color(NamedTextColor.YELLOW))
			.decoration(TextDecoration.ITALIC, false)
		);

		return lore;
	}

	public void breakRadius(Block block, BlockFace face) {
		TriFunction<Integer, Integer, Integer, Vector> vectorGetter =
			face == BlockFace.EAST || face == BlockFace.WEST ? (x, y, z) -> new Vector(z, x, y) :
			face == BlockFace.UP || face == BlockFace.DOWN ? (x, y, z) -> new Vector(x, z, y) :
			face == BlockFace.SOUTH || face == BlockFace.NORTH ? (x, y, z) -> new Vector(x, y, z) :
			(x, y, z) -> new Vector();

		Boolean isDepthAxisPositive = face == BlockFace.EAST || face == BlockFace.UP || face == BlockFace.SOUTH;

		BurrowerPickLevels level = getLvlObject();
		int radius = level.getRadius();
		int depth = level.getDepth();

		float blockHardness = block.getType().getHardness();

		for (int depthOffset = 0; depthOffset <= depth; depthOffset++) {
			for (int xOffset = -radius; xOffset <= radius; xOffset++) {
				for (int yOffset = -radius; yOffset <= radius; yOffset++) {
					Vector vector = vectorGetter.apply(xOffset, yOffset, isDepthAxisPositive ? -depthOffset : depthOffset);
					Block relativeBlock = block.getRelative(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());

					if (
						ItemUtilities.isBreakable(relativeBlock) &&
						relativeBlock.isPreferredTool(getStack()) &&
						relativeBlock.getType().getHardness() == blockHardness
					) {
						awardBreak(relativeBlock);
						relativeBlock.breakNaturally(getStack());
					}
				}
			}
		}
	}


	public void awardBreak(Block block) {
		double oreXp = BurrowerPick.getOreReward(block);
		if (oreXp != 0) {
			addXp(oreXp);
		}
	}


	public static double getOreReward(Block ore) {
		Material material = ore.getType();
		switch (material) {
			case ANCIENT_DEBRIS:
				return 60;
			case EMERALD_ORE:
				return 50;
			case DIAMOND_ORE:
				return 45;
			case GOLD_ORE:
				return 40;
			case REDSTONE_ORE:
				return 35;
			case LAPIS_ORE:
				return 30;
			case NETHER_QUARTZ_ORE:
				return 25;
			case IRON_ORE:
				return 20;
			case OBSIDIAN:
				return 15;
			case COAL_ORE:
				return 10;
			case COPPER_ORE:
				return 5;
			default:
				return material.getHardness();
		}
	}


	public static class Events extends LevelBranchItem.Events<BurrowerPick, BurrowerPickBranches, BurrowerPickLevels> {

		public Events(Game game) {
			super(game, 0);
		}

		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			Action action = event.getAction();
			if (! action.isRightClick()) return;

			Player player = event.getPlayer();
			BurrowerPick pickaxe = getItem(player.getInventory().getItemInMainHand(), game);
			if (pickaxe == null) return;

			if (! pickaxe.refreshUseCooldown()) return;
			event.setCancelled(true);

			pickaxe.cycleBranch();
		}

		@EventHandler
		public void onBlockBreak(BlockBreakEvent event) {
			Player player = event.getPlayer();
			ItemStack mainHandItem = player.getInventory().getItemInMainHand();

			BurrowerPick pick = getItem(mainHandItem, game);
			if (pick == null) return;

			pick.getBranch().onBreakBlock(pick, event);
		}


		@Override
		@Nullable
		protected BurrowerPick getItem(ItemStack stack, Game game) {
			return BurrowerPick.fromItemStack(stack, game);
		}
		@Override
		protected BurrowerPick createItem(Player owner, LevelState level, Game game) {
			return BurrowerPick.createItem(owner, level, game);
		}
		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, BurrowerRole.id);
		}

		@Override
		protected BurrowerPickBranches[] getBranches() {
			return BurrowerPickBranches.values();
		}
	}
}
