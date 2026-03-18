package fr.ludos.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.item.BranchItem.Branch;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public abstract class LevelItem<TLevel extends Enum<TLevel> & LevelItem.Level<TLevel>> extends SpecialItem {
	public static final String LEVEL = "level";
	public static final NamespacedKey levelKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), LEVEL);

	public static final String XP = "xp";
	public static final NamespacedKey xpKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), XP);

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
	public double getXp() {
		return levelState.getXp();
	}

	public static LevelState levelFromItemStack(ItemStack stack, String id, Game game) {
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


	public LevelItem(Class<TLevel> levelClass, ItemStack stack, Player owner, LevelState level, Game game) {
		super(stack, owner, game);

		this.levelState = level;
		this.levels = levelClass.getEnumConstants();

		this.levelState.addLevelUpListener( (lvlState) -> {
			this.getLvlObject().onSetLevel(this);
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

		saveLevelState(this, levelState);
	}


	public final void setLvl(int level) {
		LevelItem.LevelState state = getLevelState();

		state.setLevel(level, getMaxLevelIndex());
	}
	public final void addLvl() {
		LevelItem.LevelState state = getLevelState();

		state.addLvl(getMaxLevelIndex());
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

		LevelState state = getLevelState();
		lore.add(getXpLoreField(this));

		return lore;
	}


	public static interface Level<T extends Enum<T> & Level<T>> {
		public Class<T> getLevelClass();
		public double getXpThreshold();

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

		private final List<Consumer<LevelState>> levelUpListeners = new ArrayList<>();

		public void addLevelUpListener(Consumer<LevelState> listener) {
			levelUpListeners.add(listener);
		}

		public void removeLevelUpListener(Consumer<LevelState> listener) {
			levelUpListeners.remove(listener);
		}

		private void notifyLevelUp() {
			for (Consumer<LevelState> listener : levelUpListeners) {
				listener.accept(this);
			}
		}


		private int level;
		public int getLevel() {
			return level;
		}
		public boolean setLevel(int level, int maxLevel) {
			if (level < 0 || (maxLevel >= 0 && level > maxLevel)) return false;
			this.level = level;
			notifyLevelUp();
			return true;
		}
		public boolean addLvl(int maxLevel) {
			return setLevel(level + 1, maxLevel);
		}

		private double xp;
		public double getXp() {
			return xp;
		}
		public boolean setXp(double xp, int maxLevel, Function<Integer, Double> levelToXpThreshold) {
			boolean levelUp = false;
			while (this.level < maxLevel && xp >= levelToXpThreshold.apply(this.level)) {
				xp -= levelToXpThreshold.apply(this.level);
				addLvl(maxLevel);
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
		protected final TLevel baseLevel;
		protected final Map<Player, LevelItem.LevelState> deadPlayerLevels = new HashMap<>();

		protected Events(Game game, TLevel baseLevel, @Nullable Integer slot, boolean canDrop) {
			super(game, slot, canDrop);
			if (baseLevel == null) {
				throw new IllegalArgumentException("Base level cannot be null");
			}

			this.baseLevel = baseLevel;
		}
		protected Events(Game game, TLevel baseLevel, @Nullable Integer slot) {
			this(game, baseLevel, slot, false);
		}
		protected Events(Game game, TLevel baseLevel) {
			this(game, baseLevel, null, false);
		}


		protected abstract T createItem(Player owner, LevelState level, Game game);

		@EventHandler
		public void onPlayerDeath(PlayerDeathEvent event) {
			Player player = event.getEntity();
			if (! canPlayerHaveItem(player)) return;

			T specialItem = SpecialItem.findIn(player.getInventory(), (ItemStack stack) -> getItem(stack, game));
			if ( specialItem == null ) {
				return;
			}

			deadPlayerLevels.put(player, specialItem.getLevelState().copy());
		}

		@Override
		protected T createItem(Player owner, Game game) {
			if (!deadPlayerLevels.containsKey(owner)) {
				return createItem(owner, new LevelState(), game);
			}

			LevelState deadLevels = deadPlayerLevels.get(owner);
			return createItem(owner, deadLevels, game);
		}
	}
}
