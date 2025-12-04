package fr.ludos.item.burrower;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Pair;
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
import fr.ludos.role.BurrowerRole;
import fr.ludos.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public class BurrowerPick extends LevelBranchItem<BurrowerPickLevels, BurrowerPickBranches> {
	private static final String ID = "manhuntBurrowerPick";


	public static BurrowerPick fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		Player owner = SpecialItem.getSpecialItemOwner(stack, ID, game);
		if (owner == null) return null;
		Integer branchIndex = BranchItem.branchFromItemStack(stack, game);
		if (branchIndex == null) return null;
		Pair<Integer, Double> levelAndXp = LevelItem.fromLevelItemStack(stack, ID, game);
		if (levelAndXp == null) return null;

		return new BurrowerPick(stack, owner, BurrowerPickBranches.values()[branchIndex], BurrowerPickLevels.values()[levelAndXp.getLeft()], levelAndXp.getRight(), game);
	}
	public static BurrowerPick createItem(Player owner, BurrowerPickLevels level, Game game) {
		BurrowerPick burrowerPick = new BurrowerPick(new ItemStack(level.getMaterial()), owner, BurrowerPickBranches.Pickaxe, level, 0.0, game);
		burrowerPick.initializeItem();

		return burrowerPick;
	}

	protected BurrowerPick(ItemStack stack, Player owner, BurrowerPickBranches branch, @Nullable BurrowerPickLevels level, double xp, Game game) {
		super(stack, owner, branch, level, xp, game);
	}


	@Override
	public String getId() {
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

		int size = 1 + getLevel().getRadius() * 2;
		int depth = getLevel().getDepth() + 1;


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

		int radius = getLevel().getRadius();
		int depth = getLevel().getDepth();

		for (int depthOffset = 0; depthOffset <= depth; depthOffset++) {
			for (int xOffset = -radius; xOffset <= radius; xOffset++) {
				for (int yOffset = -radius; yOffset <= radius; yOffset++) {
					Vector vector = vectorGetter.apply(xOffset, yOffset, isDepthAxisPositive ? -depthOffset : depthOffset);
					Block relativeBlock = block.getRelative(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());

					if (
						ItemUtilities.isBreakable(relativeBlock) &&
						relativeBlock.isPreferredTool(getStack())
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


	@Override
	public BurrowerPickBranches convertToBranch(int levels) {
		return BurrowerPickBranches.values()[levels];
	}
	@Override
	protected BurrowerPickBranches[] getBranches() {
		return BurrowerPickBranches.values();
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


	@Override
	public void setLvl(BurrowerPickLevels level) {
		super.setLvl(level);
		getStack().setType(level.getMaterial());
		getStack().removeEnchantment(Enchantment.DIG_SPEED);
		getStack().removeEnchantment(Enchantment.LOOT_BONUS_BLOCKS);
		getStack().addEnchantments(level.getEnchantments());
	}

	@Override
	public BurrowerPickLevels convertToLevel(int level) {
		return BurrowerPickLevels.values()[level];
	}


	public static class Events extends LevelBranchItem.Events<BurrowerPick, BurrowerPickLevels, BurrowerPickBranches> {

		public Events(Game game) {
			super(game, BurrowerPickLevels.WOODEN, 0);
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
		protected BurrowerPick createItem(Player owner, BurrowerPickLevels level, Game game) {
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

		@Override
		protected BurrowerPick createItem(Player owner, int[] levels, Game game) {
			return BurrowerPick.createItem(owner, BurrowerPickLevels.values()[levels[0]], game);
		}
	}
}
