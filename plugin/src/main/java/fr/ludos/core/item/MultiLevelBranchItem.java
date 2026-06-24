package fr.ludos.core.item;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import fr.ludos.core.game.Game;
import fr.ludos.core.persistence.DoubleArrayPersistentDataType;
import net.kyori.adventure.text.Component;


public abstract class MultiLevelBranchItem<TBranch extends Enum<TBranch> & MultiLevelBranchItem.Branch<TBranch>> extends BranchItem<TBranch> implements LevelItemInterface {
	private final LevelItem.LevelState[] levelStates;
	public List<LevelItem.LevelState> getLevelStates() {
		return Collections.unmodifiableList(Arrays.asList(levelStates));
	}

	public static @Nullable LevelItem.LevelState[] levelsFromItemStack(ItemStack stack, String id, Game game) {
		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();

		if ( ! container.has(LevelItem.levelKey, PersistentDataType.INTEGER_ARRAY) ) return null;
		if ( ! container.has(LevelItem.xpKey, DoubleArrayPersistentDataType.INSTANCE) ) return null;

		int[] levels = container.get(LevelItem.levelKey, PersistentDataType.INTEGER_ARRAY);
		double[] xps = container.get(LevelItem.xpKey, DoubleArrayPersistentDataType.INSTANCE);

		LevelItem.LevelState[] levelStates = new LevelItem.LevelState[levels.length];
		for (int i = 0; i < levels.length; i++) {
			levelStates[i] = new LevelItem.LevelState(levels[i], xps[i]);
		}
		return levelStates;
	}
	public static void saveLevelStates(SpecialItem item, LevelItem.LevelState[] levelStates) {
		int[] levels = new int[levelStates.length];
		double[] xps = new double[levelStates.length];

		for (int i = 0; i < levelStates.length; i++) {
			LevelItem.LevelState state = levelStates[i];
			levels[i] = state.getLevel();
			xps[i] = state.getXp();
		}

		ItemMeta meta = item.getStack().getItemMeta();
		meta.getPersistentDataContainer().set(LevelItem.levelKey, PersistentDataType.INTEGER_ARRAY, levels);
		meta.getPersistentDataContainer().set(LevelItem.xpKey, DoubleArrayPersistentDataType.INSTANCE, xps);
		item.getStack().setItemMeta(meta);
	}

	public MultiLevelBranchItem(Class<TBranch> branchClass, ItemStack stack, Player owner, TBranch branch, LevelItem.LevelState[] levels, Game game) {
		super(branchClass, stack, owner, branch, game);

		if (levels.length != getBranches().length) {
			throw new IllegalArgumentException("Levels array length does not match branches length");
		}

		for (int i = 0; i < levels.length; i++) {
			if (levels[i] == null) {
				levels[i] = new LevelItem.LevelState();
			}
		}
		this.levelStates = levels;

		for (LevelItem.LevelState state : levelStates) {
			state.addLevelUpListener( (lvlState, oldLevel) -> {
				if (lvlState.getLevel() <= oldLevel) return;
				getOwner().sendMessage(
					LevelItem.getLevelUpMessage(this)
				);
			} );
			state.addXpChangeListener( (lvlState) -> {
				saveLevelStates(this, levelStates);
				updateLore();
			} );
		}
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		saveLevelStates(this, levelStates);
	}

	public LevelItem.LevelState getLevelState() {
		TBranch currentBranch = getBranch();
		return levelStates[currentBranch.ordinal()];
	}

	public int getLvl() {
		LevelItem.LevelState state = getLevelState();

		return state.getLevel();
	}
	public void setLvl(int level) {
		LevelItem.LevelState state = getLevelState();

		state.setLevel(level, getMaxBranchIndex());
	}
	public void addLvl(int level) {
		LevelItem.LevelState state = getLevelState();

		state.addLvl(level, getMaxBranchIndex());
	}

	public double getXp() {
		LevelItem.LevelState state = getLevelState();

		return state.getXp();
	}
	public void setXp(double value) {
		LevelItem.LevelState state = getLevelState();

		state.setXp(value, getMaxBranchIndex(), (level) -> getBranch().getXpThreshold(level));
	}
	public void addXp(double xp) {
		LevelItem.LevelState state = getLevelState();

		state.addXp(xp, getMaxBranchIndex(), (level) -> getBranch().getXpThreshold(level));
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();

		lore.add(LevelItem.getBranchLevelLoreField(this));
		lore.add(LevelItem.getBranchXpLoreField(this));

		return lore;
	}


	public static interface Branch<T extends Enum<T> & Branch<T>> extends BranchItem.Branch<T> {
		public double getXpThreshold(int level);

		/**
		 * Called when the level is set on the item, including when the item is created with a non-zero level. Should be used to apply the level's effects.
		 * @param item The item on which the level is being set
		 */
		public void onSetLevel(int level, SpecialItem item);
		/**
		 * Called when the level is unset on the item, including when the item is created with a non-zero level. Should be used to remove the level's effects.
		 * @param item The item on which the level is being unset
		 */
		public void onUnsetLevel(int level, SpecialItem item);
	}

	public static abstract class Events<T extends MultiLevelBranchItem<TBranch>, TBranch extends Enum<TBranch> & Branch<TBranch>> extends BranchItem.Events<T, TBranch> {
		private Map<Player, LevelItem.LevelState[]> deadPlayerLevels;

		protected Events(Game game, @Nullable ItemSlot slot, boolean canDrop) {
			super(game, slot, canDrop);

			this.deadPlayerLevels = new HashMap<>();
		}
		public Events(Game game, @Nullable ItemSlot slot) {
			this(game, slot, false);
		}
		public Events(Game game) {
			this(game, null, false);
		}

		@EventHandler
		public void onPlayerDeath(PlayerDeathEvent event) {
			Player player = event.getEntity();
			if (! isPlayerValid(player)) return;

			T specialItem = SpecialItem.findIn(player.getInventory(), this::getItem);
			if ( specialItem == null ) return;

			deadPlayerLevels.put(player,
				specialItem.getLevelStates().stream()
					.map((LevelItem.LevelState state) -> {
						if (state.getLevel() <= 0) {
							return new LevelItem.LevelState(0, 0.0);
						}
						else {
							return new LevelItem.LevelState(state.getLevel() -1, 0.0);
						}
					})
					.toArray(LevelItem.LevelState[]::new)
			);
		}

		protected abstract TBranch[] getBranches();

		public abstract T createItem(Player owner, LevelItem.LevelState[] levels);

		@Override
		public final T createItem(Player owner) {
			LevelItem.LevelState[] levels;
			if (owner != null && deadPlayerLevels != null && deadPlayerLevels.containsKey(owner)) {
				levels = deadPlayerLevels.get(owner);
			}
			else {
				levels = new LevelItem.LevelState[getBranches().length];
			}

			return createItem(owner, levels);
		}

		@EventHandler
		public void onSwitchItem(PlayerItemHeldEvent event) {
			BranchItem.onSwitchItem(event, this::getItem);
		}
	}
}
