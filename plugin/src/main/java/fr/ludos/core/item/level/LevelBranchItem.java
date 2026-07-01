package fr.ludos.core.item.level;

import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.BranchItem;
import fr.ludos.core.item.BranchItemInterface;
import fr.ludos.core.item.ItemSlot;
import net.kyori.adventure.text.Component;


public abstract class LevelBranchItem<TBranch extends BranchItem.Branch, TLevel extends Enum<TLevel> & LevelItem.Level<TLevel>> extends LevelItem<TLevel> implements BranchItemInterface<TBranch> {
	private TBranch branch;
	public TBranch getBranch() {
		return branch;
	}
	private final TBranch[] branches;
	public TBranch[] getBranches() {
		return branches;
	}
	public void onSetBranch(TBranch branch) {
		this.branch = branch;
	}

	public LevelBranchItem(Class<TBranch> branchClass, TBranch branch, Class<TLevel> levelClass, LevelValue level, ItemStack stack, Player owner, Game game) {
		super(levelClass, level, stack, owner, game);

		this.branch = branch;
		this.branches = branchClass.getEnumConstants();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		switchBranch(branch);
	}



	protected Component getBranchAnnotation() {
		TBranch branch = getBranch();
		return BranchItemInterface.getBranchAnnotation(branch);
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();
		lore.add(BranchItemInterface.getBranchLoreField(getBranch()));

		return lore;
	}


	public static abstract class Events<T extends LevelBranchItem<TBranch, TLevel>, TBranch extends BranchItem.Branch, TLevel extends Enum<TLevel> & LevelItem.Level<TLevel>> extends LevelItem.Events<T, TLevel> {
		protected Events(Game game, @Nullable ItemSlot slot, boolean canDrop) {
			super(game, slot, canDrop);
		}
		public Events(Game game, @Nullable ItemSlot slot) {
			this(game, slot, false);
		}
		public Events(Game game) {
			this(game, null, false);
		}

		protected abstract TBranch[] getBranches();
		public abstract T createItem(Player owner, LevelValue level);

		@Override
		public final T createItem(Player owner) {
			if (!deadPlayerLevels.containsKey(owner)) {
				return createItem(owner, new LevelValue());
			}

			LevelValue deadLevels = deadPlayerLevels.get(owner);
			return createItem(owner, deadLevels);
		}

		@EventHandler
		public void onSwitchItem(PlayerItemHeldEvent event) {
			BranchItem.onSwitchItem(event, this::getItem);
		}
	}
}
