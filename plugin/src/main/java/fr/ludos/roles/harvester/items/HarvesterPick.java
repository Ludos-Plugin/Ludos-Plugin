package fr.ludos.roles.harvester.items;

import java.util.Arrays;
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

import fr.ludos.core.Utility;
import fr.ludos.core.game.Game;
import fr.ludos.core.item.BranchItemInterface;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.ItemUtilities;
import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.core.item.level.LevelBranchItem;
import fr.ludos.core.item.level.LevelItemInterface;
import fr.ludos.core.item.level.LevelValue;
import fr.ludos.core.role.Role;
import fr.ludos.roles.harvester.HarvesterRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public final class HarvesterPick extends LevelBranchItem<HarvesterPickBranch, HarvesterPickLevels> {
	public static final String ID = "harvester_pick";
	public final Events events;

	// private final static Map<UUID, HarvesterPick> cachedItems = new HashMap<>();


	public static HarvesterPick fromItemStack(Events events, ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItemInterface.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// HarvesterPick cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItemInterface.getSpecialItemOwner(stack, game);
		if (owner == null) return null;
		String branchId = BranchItemInterface.branchFromItemStack(stack, game);
		if (branchId == null) return null;
		LevelValue levelValue = LevelItemInterface.levelFromItemStack(stack, game);
		if (levelValue == null) return null;

		HarvesterPick harvesterPick = new HarvesterPick(events, branchId, levelValue, stack, owner, game);
		// cachedItems.put(itemId, harvesterPick);

		return harvesterPick;
	}

	public static HarvesterPick createItem(Events events, String defaultBranchId, LevelValue level, Player owner, Game game) {
		HarvesterPickLevels lvl = events.getLevels().get(0);
		HarvesterPick harvesterPick = new HarvesterPick(events, defaultBranchId, level, new ItemStack(lvl.getMaterial()), owner, game);
		UUID itemId = harvesterPick.initializeItem();

		// cachedItems.put(itemId, harvesterPick);

		return harvesterPick;
	}

	protected HarvesterPick(Events events, String defaultBranchId, LevelValue level, ItemStack stack, Player owner, Game game) {
		super(events.getBranches(), events.getBranches().get(defaultBranchId), events.getLevels(), level, stack, owner, game);
		this.events = events;
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


	public static final class Events extends LevelBranchItem.Events<HarvesterPick, HarvesterPickBranch, HarvesterPickLevels> {
		private static final List<HarvesterPickBranch> DEFAULT_BRANCHES = Arrays.asList(HarvesterPickBranches.values());
		private static final List<HarvesterPickLevels> LEVELS = List.of(HarvesterPickLevels.values());
		public final HarvesterRole role;

		public Events(HarvesterRole role, Game game) {
			super(DEFAULT_BRANCHES, HarvesterPickBranches.Pickaxe, game, new Events.Info(ItemSlot.HOTBAR_2));
			this.role = role;
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
		@Nullable
		public HarvesterPick getItem(ItemStack stack) {
			return HarvesterPick.fromItemStack(this, stack, game);
		}
		@Override
		public HarvesterPick createItem(Player owner, LevelValue level) {
			return HarvesterPick.createItem(this, getDefaultBranch().id(), level, owner, game);
		}
		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return Role.isPlayerRole(owner, HarvesterRole.ID);
		}
	}
}
