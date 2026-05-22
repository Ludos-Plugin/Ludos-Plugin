package fr.ludos.item.harvester;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.ludos.Utility;
import fr.ludos.game.Game;
import fr.ludos.item.BranchItem;
import fr.ludos.item.ItemUtilities;
import fr.ludos.item.LevelBranchItem;
import fr.ludos.item.LevelItem;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.HarvesterRole;
import fr.ludos.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public class HarvesterPick extends LevelBranchItem<HarvesterPickBranches, HarvesterPickLevels> {
	private static final String ID = "manhuntHarvesterPick";

	// private final static Map<UUID, HarvesterPick> cachedItems = new HashMap<>();


	public static HarvesterPick fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// HarvesterPick cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;
		Integer branchIndex = BranchItem.branchFromItemStack(stack, game);
		if (branchIndex == null) return null;
		LevelState levelState = LevelItem.levelFromItemStack(stack, game);
		if (levelState == null) return null;

		HarvesterPick harvesterPick = new HarvesterPick(stack, owner, HarvesterPickBranches.values()[branchIndex], levelState, game);
		// cachedItems.put(itemId, harvesterPick);

		return harvesterPick;
	}

	public static HarvesterPick createItem(Player owner, LevelState level, Game game) {
		HarvesterPickLevels lvl = HarvesterPickLevels.values()[level.getLevel()];
		HarvesterPick harvesterPick = new HarvesterPick(new ItemStack(lvl.getMaterial()), owner, HarvesterPickBranches.Pickaxe, level, game);
		UUID itemId = harvesterPick.initializeItem();

		// cachedItems.put(itemId, harvesterPick);

		return harvesterPick;
	}

	protected HarvesterPick(ItemStack stack, Player owner, HarvesterPickBranches branch, LevelState level, Game game) {
		super(HarvesterPickBranches.class, HarvesterPickLevels.class, stack, owner, branch, level, game);
	}


	@Override
	public String getTypeId() {
		return ID;
	}

	@Override
	public Component getName() {
		return Component.text("Harvester's Pick (")
			.append(getBranch().getName())
			.append(Component.text(")"))
			.decoration(TextDecoration.ITALIC, false); // TODO: Translate
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();

		HarvesterPickLevels level = getLvlObject();
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

	public void breakRadius(Block block, BlockFace face, Player breaker) {
		HarvesterPickLevels level = getLvlObject();
		int radius = level.getRadius();
		int depth = level.getDepth();

		float blockHardness = block.getType().getHardness();

		for (Block relativeBlock : Utility.getAllBlocks(
				block, face,
				Pair.of(-radius, radius),
				Pair.of(-radius, radius),
				Pair.of(0, depth)
			)
		) {
			if (
				ItemUtilities.isBreakable(relativeBlock) &&
				relativeBlock.isPreferredTool(getStack()) &&
				relativeBlock.getType().getHardness() == blockHardness
			) {
				HarvesterRole.awardBreak(breaker, relativeBlock, getGame());
				relativeBlock.breakNaturally(getStack(), true);
			}
		}
	}


	public static class Events extends LevelBranchItem.Events<HarvesterPick, HarvesterPickBranches, HarvesterPickLevels> {
		public Events(Game game) {
			super(game, 1);
		}

		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			Action action = event.getAction();
			if (! action.isRightClick()) return;

			Player player = event.getPlayer();
			HarvesterPick pickaxe = getItem(player.getInventory().getItemInMainHand());
			if (pickaxe == null) return;

			if (! pickaxe.refreshUseCooldown()) return;
			event.setCancelled(true);

			pickaxe.cycleBranch();
		}

		@EventHandler
		public void onBlockBreak(BlockBreakEvent event) {
			Player player = event.getPlayer();
			ItemStack mainHandItem = player.getInventory().getItemInMainHand();

			HarvesterPick pick = getItem(mainHandItem);
			if (pick == null) return;

			pick.getBranch().onBreakBlock(pick, event);
		}

		@Override
		@Nullable
		public HarvesterPick getItem(ItemStack stack) {
			return HarvesterPick.fromItemStack(stack, game);
		}
		@Override
		public HarvesterPick createItem(Player owner, LevelState level) {
			return HarvesterPick.createItem(owner, level, game);
		}
		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return Role.isPlayerRole(owner, HarvesterRole.id);
		}

		@Override
		protected HarvesterPickBranches[] getBranches() {
			return HarvesterPickBranches.values();
		}
	}
}
