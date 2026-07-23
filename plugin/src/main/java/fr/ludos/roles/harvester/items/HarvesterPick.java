package fr.ludos.roles.harvester.items;

import java.util.Arrays;
import java.util.List;

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

import fr.ludos.core.Utility;
import fr.ludos.core.game.Game;
import fr.ludos.core.item.BranchItemInterface;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.ItemUtilities;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.level.LevelBranchItem;
import fr.ludos.core.item.level.LevelItem;
import fr.ludos.roles.harvester.HarvesterRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of the Huntsman Pick, for use by any Player with {@link HarvesterRole}.
 */
public final class HarvesterPick extends LevelBranchItem<HarvesterPick, HarvesterPickBranch, HarvesterPickLevels> {
	public static final String ID = "harvester_pick";
	public final Events events;


	protected HarvesterPick(LevelBranchItem.Info<HarvesterPickBranch, HarvesterPickLevels> info, Events events) {
		super(info, events);
		this.events = events;
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

		HarvesterPickLevels level = lvlObject();
		int size = 1 + level.getRadius() * 2;
		int depth = level.getDepth() + 1;


		lore.add(BranchItemInterface.getCycleBranchAnnotation("key.use"));

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
		HarvesterPickLevels level = lvlObject();
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
				events.role.awardBreak(breaker, relativeBlock, getGame());
				relativeBlock.breakNaturally(getStack(), true);
			}
		}
	}

	/**
	 * Events for the {@link HarvesterPick}.
	 */
	public static final class Events extends LevelBranchItem.Events<HarvesterPick, HarvesterPickBranch, HarvesterPickLevels> {
		private static final List<HarvesterPickBranch> DEFAULT_BRANCHES = Arrays.asList(HarvesterPickBranches.values());
		private static final List<HarvesterPickLevels> LEVELS = List.of(HarvesterPickLevels.values());
		public final HarvesterRole role;

		public Events(HarvesterRole role, Game game) {
			super(DEFAULT_BRANCHES, HarvesterPickBranches.Pickaxe, game, new Events.Info(ItemSlot.HOTBAR_2));
			this.role = role;
		}

		@Override
		public String getTypeId() {
			return ID;
		}

		@Override
		public List<HarvesterPickLevels> getLevels() {
			return LEVELS;
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
		protected HarvesterPick getItemInternal(LevelBranchItem.Info<HarvesterPickBranch, HarvesterPickLevels> info) {
			return new HarvesterPick(info, this);
		}
		@Override
		protected HarvesterPick createItemInternal(BranchData<HarvesterPickBranch> branch, LevelItem.LevelData<HarvesterPickLevels> level, Player owner) {
			HarvesterPickLevels levelObject = level.getCurrentLevelOr(HarvesterPickLevels.WOODEN);
			return new HarvesterPick(new LevelBranchItem.Info<>(branch, level, new SpecialItem.ItemData(new ItemStack(levelObject.getMaterial()), owner)), this);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.ludos().getRoleManager().isPlayerRole(owner, HarvesterRole.ID);
		}
	}
}
