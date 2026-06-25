package fr.ludos.core.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public abstract class LevelItem<TLevel extends Enum<TLevel> & LevelItem.Level<TLevel>> extends SpecialItem implements LevelItemInterface {
	public static final String LEVEL = "level";
	public static final NamespacedKey levelKey = new NamespacedKey(Ludos.namespace, LEVEL);

	public static final String XP = "xp";
	public static final NamespacedKey xpKey = new NamespacedKey(Ludos.namespace, XP);

	public static final String MAX_LVL_LABEL = "MAX";


	private final LevelState levelState;
	public LevelState getLevelState() {
		return levelState;
	}
	private final TLevel[] levels;
	public int getMaxLevelIndex() {
		return levels.length - 1;
	}

	public int getLvl() {
		return levelState.getLevel();
	}
	public TLevel getLvlObject() {
		return levels[levelState.getLevel()];
	}
	public TLevel getLvlObject(int level) {
		return levels[level];
	}
	public double getXp() {
		return levelState.getXp();
	}

	public static LevelState levelFromItemStack(ItemStack stack, Game game) {
		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();

		if ( ! container.has(levelKey, PersistentDataType.INTEGER) ) return null;
		if ( ! container.has(xpKey, PersistentDataType.DOUBLE) ) return null;

		int level = getPersistentData(stack, levelKey, PersistentDataType.INTEGER);
		double xp = getPersistentData(stack, xpKey, PersistentDataType.DOUBLE);

		return new LevelState(level, xp);
	}
	public static void saveLevelState(SpecialItem item, LevelItem.LevelState levelState) {
		ItemMeta meta = item.getStack().getItemMeta();
		meta.getPersistentDataContainer().set(LevelItem.levelKey, PersistentDataType.INTEGER, levelState.getLevel());
		meta.getPersistentDataContainer().set(LevelItem.xpKey, PersistentDataType.DOUBLE, levelState.getXp());
		item.getStack().setItemMeta(meta);
	}

	public static @NotNull TextComponent getLevelUpMessage(SpecialItem item) { // TODO: Translate
		return Component.text("Your ")
			.color(NamedTextColor.GREEN)
		.append(item.getName())
		.append(
			Component.text(" has leveled up!")
			.color(NamedTextColor.GREEN)
		);
	}

	public static <TLevel extends Enum<TLevel> & Level<TLevel>> Component getXpLoreField(boolean isMax, double levelThreshold, double xp) {
		String xpLabel;
		if ( isMax ) {
			xpLabel = MAX_LVL_LABEL;
		} else {
			String xpRounded = Double.toString(Math.round(xp * 100.0) / 100.0);
			xpLabel = xpRounded + '/' + levelThreshold;
		}

		return
			Component.text("XP: ")
				.color(NamedTextColor.GRAY)
			.append(Component.text(xpLabel)
				.color(NamedTextColor.RED))
			.decoration(TextDecoration.ITALIC, false);
	}
	public static <TLevel extends Enum<TLevel> & Level<TLevel>> Component getXpLoreField(LevelItem<TLevel> item) {
		int maxLevel = item.getMaxLevelIndex();
		TLevel level = item.getLvlObject();
		int levelNum = level.ordinal();

		boolean isMax = maxLevel >= 0 && levelNum >= maxLevel;
		double levelThreshold = isMax ? 0 : level.getXpThreshold();

		return getXpLoreField(isMax, levelThreshold, item.getLevelState().getXp());
	}
	public static <TBranch extends Enum<TBranch> & MultiLevelBranchItem.Branch<TBranch>> Component getBranchXpLoreField(MultiLevelBranchItem<TBranch> item) {
		int maxLevel = item.getMaxBranchIndex();
		LevelState level = item.getLevelState();
		int levelNum = level.getLevel();

		boolean isMax = maxLevel >= 0 && levelNum >= maxLevel;
		double levelThreshold = isMax ? 0 : item.getBranch().getXpThreshold(levelNum);

		return getXpLoreField(isMax, levelThreshold, level.getXp());
	}

	public static <TLevel extends Enum<TLevel> & Level<TLevel>> Component getLevelLoreField(int levelIndex) {
		return
			Component.text("Level: ")
				.color(NamedTextColor.GRAY)
			.append(Component.text(levelIndex + 1)
				.color(NamedTextColor.RED))
			.decoration(TextDecoration.ITALIC, false);
	}
	public static <TLevel extends Enum<TLevel> & Level<TLevel>> Component getLevelLoreField(LevelItem<TLevel> item) {
		return getLevelLoreField(item.getLevelState().getLevel());
	}
	public static <TBranch extends Enum<TBranch> & MultiLevelBranchItem.Branch<TBranch>> Component getBranchLevelLoreField(MultiLevelBranchItem<TBranch> item) {
		return getLevelLoreField(item.getLevelState().getLevel());
	}


	/**
	 * Utility function to handle level switching when player switches leveles.
	 * @param <TItem> The type of the item, must extend SpecialItem
	 * @param <TLevel> The type of the level, must be an enum that implements LevelItem.Level
	 * @param item The item whose level is being switched
	 * @param newLevel The new level to switch to
	 * @param newXp The new XP value to set
	 */
	public static <TItem extends LevelItem<TLevel>, TLevel extends Enum<TLevel> & Level<TLevel>> void setItemLevel(TItem item, LevelState levelState) {
		TLevel oldLevel = item.getLvlObject();
		TLevel newLevel = item.getLvlObject(levelState.getLevel());

		ItemStack itemStack = item.getStack();

		ItemMeta meta = itemStack.getItemMeta();
		meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, newLevel.ordinal());
		meta.getPersistentDataContainer().set(xpKey, PersistentDataType.DOUBLE, levelState.getXp());
		itemStack.setItemMeta(meta);

		item.setLvl(newLevel.ordinal());

		if (oldLevel != null) {
			oldLevel.onUnsetLevel(item);
		}
		newLevel.onSetLevel(item);

		item.update();
	}

	/**
	 * Utility function to handle level switching when player switches items.
	 * @param <TItem> The type of the item, must extend SpecialItem
	 * @param <TLevel> The type of the level, must be an enum that implements LevelItem.Level
	 * @param event The PlayerItemHeldEvent to handle
	 * @param getItem An "SpecialItem from ItemStack" function, used to get the item being switched from/to
	 * @param getLevel A "Level from Item" function, used to get the level of the item being switched from/to
	 */
	public static <TItem extends LevelItem<TLevel>, TLevel extends Enum<TLevel> & LevelItem.Level<TLevel>> void onSwitchItem(PlayerItemHeldEvent event, Function<ItemStack, TItem> getItem) {
		Player player = event.getPlayer();

		TItem oldItem = getItem.apply(player.getInventory().getItem(event.getPreviousSlot()));
		TItem newItem = getItem.apply(player.getInventory().getItem(event.getNewSlot()));
		if (oldItem == null && newItem == null) return;

		if (oldItem != null) {
			oldItem.getLvlObject().onUnequip(oldItem);
		}
		if (newItem != null) {
			newItem.getLvlObject().onEquip(newItem);
		}
	}

	public LevelItem(Class<TLevel> levelClass, ItemStack stack, Player owner, LevelState level, Game game) {
		super(stack, owner, game);

		this.levelState = level;
		this.levels = levelClass.getEnumConstants();

		this.levelState.addLevelUpListener( (lvlState, oldLevel) -> {
			TLevel oldLevelObj = levels[oldLevel];
			oldLevelObj.onUnsetLevel(this);

			TLevel newLevelObj = getLvlObject();
			newLevelObj.onSetLevel(this);

			if (lvlState.getLevel() <= oldLevel) return;

			getOwner().sendMessage(
				LevelItem.getLevelUpMessage(this)
			);
		} );
		this.levelState.addXpChangeListener( (lvlState) -> {
			saveLevelState(this, lvlState);
			updateLore();
		} );
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		setItemLevel(this, getLevelState());
	}


	public final void setLvl(int level) {
		LevelItem.LevelState state = getLevelState();

		state.setLevel(level, getMaxLevelIndex());
	}
	public final void addLvl(int level) {
		LevelItem.LevelState state = getLevelState();

		state.addLvl(level, getMaxLevelIndex());
	}

	public final void setXp(double value) {
		LevelItem.LevelState state = getLevelState();

		state.setXp(value, getMaxLevelIndex(), (level) -> levels[level].getXpThreshold());
	}
	public final void addXp(double xp) {
		LevelItem.LevelState state = getLevelState();

		state.addXp(xp, getMaxLevelIndex(), (level) -> levels[level].getXpThreshold());
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();
		lore.add(getLevelLoreField(this));
		lore.add(getXpLoreField(this));

		return lore;
	}


	public static interface Level<T extends Enum<T> & Level<T>> {
		public Class<T> getLevelClass();
		public double getXpThreshold();

		/**
		 * Called when item is equipped (mainhand) while the level is set.
		 * @param item The item being equipped
		 */
		public void onEquip(SpecialItem item);
		/**
		 * Called when item is unequipped (mainhand) while the level is set.
		 * @param item The item being unequipped
		 */
		public void onUnequip(SpecialItem item);

		/**
		 * Called when the level is set on the item, including when the item is created with a non-zero level. Should be used to apply the level's effects.
		 * @param item The item on which the level is being set
		 */
		public void onSetLevel(SpecialItem item);
		/**
		 * Called when the level is unset on the item, including when the item is created with a non-zero level. Should be used to remove the level's effects.
		 * @param item The item on which the level is being unset
		 */
		public void onUnsetLevel(SpecialItem item);
	}

	public static class LevelState {
		private final List<Consumer<LevelState>> xpChangeListeners = new ArrayList<>();

		public void addXpChangeListener(Consumer<LevelState> listener) {
			xpChangeListeners.add(listener);
		}

		public void removeXpChangeListener(Consumer<LevelState> listener) {
			xpChangeListeners.remove(listener);
		}

		private void notifyXpChange() {
			for (Consumer<LevelState> listener : xpChangeListeners) {
				listener.accept(this);
			}
		}

		private final List<BiConsumer<LevelState, Integer>> levelUpListeners = new ArrayList<>();

		public void addLevelUpListener(BiConsumer<LevelState, Integer> listener) {
			levelUpListeners.add(listener);
		}

		public void removeLevelUpListener(BiConsumer<LevelState, Integer> listener) {
			levelUpListeners.remove(listener);
		}

		private void notifyLevelUp(int oldLevel) {
			for (BiConsumer<LevelState, Integer> listener : levelUpListeners) {
				listener.accept(this, oldLevel);
			}
		}


		private int level;
		public int getLevel() {
			return level;
		}
		private boolean setLevelNoEvent(int level, int maxLevel) {
			if (level < 0) return false;
			int oldLevel = this.level;
			this.level = Math.min(level, maxLevel);
			notifyLevelUp(oldLevel);
			return true;
		}
		public boolean setLevel(int level, int maxLevel) {
			if (setLevelNoEvent(level, maxLevel)) {
				notifyXpChange();
				return true;
			}
			return false;
		}
		private boolean addLevelNoEvent(int maxLevel) {
			return addLevelNoEvent(1, maxLevel);
		}
		private boolean addLevelNoEvent(int toAdd, int maxLevel) {
			return setLevelNoEvent(level + toAdd, maxLevel);
		}
		public boolean addLvl(int maxLevel) {
			return addLvl(1, maxLevel);
		}
		public boolean addLvl(int toAdd, int maxLevel) {
			if (addLevelNoEvent(toAdd, maxLevel)) {
				notifyXpChange();
				return true;
			}
			return false;
		}

		private double xp;
		public double getXp() {
			return xp;
		}
		public boolean setXp(double xp, int maxLevel, Function<Integer, Double> levelToXpThreshold) {
			boolean levelUp = false;
			while (this.level < maxLevel && xp >= levelToXpThreshold.apply(this.level)) {
				xp -= levelToXpThreshold.apply(this.level);
				addLevelNoEvent(maxLevel);
				levelUp = true;
			}
			this.xp = xp;
			notifyXpChange();
			return levelUp;
		}
		public boolean addXp(double xp, int maxLevel, Function<Integer, Double> levelToXpThreshold) {
			return setXp(this.xp + xp, maxLevel, levelToXpThreshold);
		}

		public LevelState(int level, double xp) {
			this.level = level;
			this.xp = xp;
		}
		public LevelState(int level) {
			this(level, 0.0);
		}
		public LevelState() {
			this(0);
		}

		public LevelState copy() {
			return new LevelState(level, xp);
		}
	}

	public static abstract class Events<T extends LevelItem<TLevel>, TLevel extends Enum<TLevel> & Level<TLevel>> extends SpecialItem.Events<T> {
		protected final Map<Player, LevelItem.LevelState> deadPlayerLevels = new HashMap<>();

		protected Events(Game game, @Nullable ItemSlot slot, boolean canDrop) {
			super(game, slot, canDrop);
		}
		protected Events(Game game, @Nullable ItemSlot slot) {
			this(game, slot, false);
		}
		protected Events(Game game) {
			this(game, null, false);
		}


		public abstract T createItem(Player owner, LevelState level);

		@EventHandler
		public void onPlayerDeath(PlayerDeathEvent event) {
			Player player = event.getEntity();
			if (! isPlayerValid(player)) return;

			T specialItem = SpecialItem.findIn(player.getInventory(), this::getItem);
			if ( specialItem == null ) {
				return;
			}

			deadPlayerLevels.put(player, specialItem.getLevelState().copy());
		}

		@Override
		public T createItem(Player owner) {
			if (!deadPlayerLevels.containsKey(owner)) {
				return createItem(owner, new LevelState());
			}

			LevelState deadLevels = deadPlayerLevels.get(owner);
			return createItem(owner, deadLevels);
		}
	}
}
