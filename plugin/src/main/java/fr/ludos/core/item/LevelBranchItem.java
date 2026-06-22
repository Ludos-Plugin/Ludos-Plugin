package fr.ludos.core.item;

import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.game.Game;
import net.kyori.adventure.text.Component;


public abstract class LevelBranchItem<TBranch extends Enum<TBranch> & BranchItem.Branch<TBranch>, TLevel extends Enum<TLevel> & LevelItem.Level<TLevel>> extends LevelItem<TLevel> implements BranchItemInterface<TBranch> {
	private TBranch branch;
	public TBranch getBranch() {
		return branch;
	}
	public void setBranch(TBranch branch) {
		this.branch = branch;
	}
	private final TBranch[] branches;
	public TBranch[] getBranches() {
		return branches;
	}

	public LevelBranchItem(Class<TBranch> branchClass, Class<TLevel> levelClass, ItemStack stack, Player owner, TBranch branch, LevelState level, Game game) {
		super(levelClass, stack, owner, level, game);

		this.branch = branch;
		this.branches = branchClass.getEnumConstants();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		switchBranch(branch);
	}


	public void cycleBranch() {
		switchBranch(getBranches()[(getBranch().ordinal() + 1) % getBranches().length]);

		Player owner = getOwner();
		owner.playSound(owner.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.25f, 1);
	}

	public void switchBranch(TBranch branch) {
		BranchItem.setItemBranch(this, branch);
	}


	protected Component getBranchAnnotation() {
		TBranch branch = getBranch();
		return BranchItem.getBranchAnnotation(branch);
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();
		lore.add(BranchItem.getBranchLoreField(getBranch()));

		return lore;
	}


	public static abstract class Events<T extends LevelBranchItem<TBranch, TLevel>, TBranch extends Enum<TBranch> & BranchItem.Branch<TBranch>, TLevel extends Enum<TLevel> & LevelItem.Level<TLevel>> extends LevelItem.Events<T, TLevel> {
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
		public abstract T createItem(Player owner, LevelState level);

		@Override
		public final T createItem(Player owner) {
			if (!deadPlayerLevels.containsKey(owner)) {
				return createItem(owner, new LevelState());
			}

			LevelState deadLevels = deadPlayerLevels.get(owner);
			return createItem(owner, deadLevels);
		}

		@EventHandler
		public void onSwitchItem(PlayerItemHeldEvent event) {
			BranchItem.onSwitchItem(event, this::getItem);
		}
	}
}
