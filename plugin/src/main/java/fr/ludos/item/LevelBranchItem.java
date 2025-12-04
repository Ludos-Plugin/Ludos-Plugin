package fr.ludos.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import fr.ludos.game.Game;
import net.kyori.adventure.text.Component;


public abstract class LevelBranchItem<TLevel extends LevelItem.Level<TLevel>, TBranch extends BranchItem.Branch<TBranch>> extends LevelItem<TLevel> {
	private TBranch branch;
	public TBranch getBranch() {
		return branch;
	}

	public LevelBranchItem(ItemStack stack, Player owner, TBranch branch, @Nullable TLevel level, double xp, Game game) {
		super(stack, owner, level, xp, game);

		this.branch = branch;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		setBranch(branch);
	}


	public void cycleBranch() {
		setBranch(convertToBranch((getBranch().index() + 1) % getBranches().length));

		Player owner = getOwner();
		owner.playSound(owner.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.25f, 1);
	}

	public void setBranch(TBranch branch) {
		BranchItem.setItemBranch(this, branch, LevelBranchItem::getBranch, (item, newBranch) -> item.branch = newBranch);
	}


	public abstract TBranch convertToBranch(int levels);
	protected abstract TBranch[] getBranches();

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


	public static abstract class Events<T extends LevelBranchItem<TLevel, TBranch>, TLevel extends LevelItem.Level<TLevel>, TBranch extends BranchItem.Branch<TBranch>> extends LevelItem.Events<T, TLevel> {
		private Map<String, int[]> deadPlayerLevels;

		protected Events(Game game, TLevel baseLevel, @Nullable Integer slot, boolean canDrop) {
			super(game, baseLevel, slot, canDrop);

			this.deadPlayerLevels = new HashMap<>();
		}
		public Events(Game game, TLevel baseLevel, @Nullable Integer slot) {
			this(game, baseLevel, slot, false);
		}
		public Events(Game game, TLevel baseLevel) {
			this(game, baseLevel, null, false);
		}

		protected abstract TBranch[] getBranches();
		protected abstract T createItem(Player owner, int[] levels, Game game);

		@Override
		protected final T createItem(Player owner, Game game) {
			int[] levels = new int[getBranches().length];
			if (owner != null && deadPlayerLevels != null && deadPlayerLevels.containsKey(owner.getName())) {
				levels = deadPlayerLevels.get(owner.getName());
			}

			return createItem(owner, levels, game);
		}

		@EventHandler
		public void onSwitchItem(PlayerItemHeldEvent event) {
			BranchItem.onSwitchItem(event, (stack) -> getItem(stack, game), LevelBranchItem::getBranch);
		}
	}
}
