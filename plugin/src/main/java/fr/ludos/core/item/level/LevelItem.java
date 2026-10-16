package fr.ludos.core.item.level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.SpecialItem;
import net.kyori.adventure.text.Component;

/**
 * A {@link LevelItemInterface} wrapper for {@link ItemStack}s.
 * @param <T> self type
 * @param <TLevel> The type of {@link LevelItemInterface.Level} the item uses
 */
public abstract class LevelItem<T extends LevelItem<T, TLevel>, TLevel extends LevelItemInterface.Level<TLevel>> extends SpecialItem<T> implements LevelItemInterface {
	private final LevelState levelState;
	public LevelState levelState() {
		return levelState;
	}
	private final List<TLevel> levels;

	public TLevel lvlObject() {
		return levels.get(levelState.level());
	}
	public TLevel lvlObject(int level) {
		return levels.get(level);
	}

	@Override
	public void onSwitchToLevel(Integer level) {
		lvlObject(level).onSwitchToLevel(this);
	}
	@Override
	public void onSwitchOffLevel(Integer level) {
		lvlObject(level).onSwitchOffLevel(this);
	}

	public LevelItem(LevelItem.ItemData<TLevel> info, Events<T, TLevel> events) {
		super(info.info, events);

		this.levels = info.data.levels;
		this.levelState = LevelItemInterface.initializeLevelState(this, this, levels, info.data.level);
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();
		lore.add(LevelItemInterface.getLevelLoreField(this));
		lore.add(LevelItemInterface.getXpLoreField(this));

		return lore;
	}

	/**
	 * .
	 * @param <TLevel>
	 * @param levels
	 * @param level
	 */
	public static record LevelData<TLevel extends LevelItemInterface.Level<TLevel>>(
		List<TLevel> levels,
		LevelValue level
	) {
		public LevelData(List<TLevel> levels) {
			this(levels, new LevelValue());
		}

		public @Nullable TLevel getCurrentLevel() {
			int levelIndex = level.level();
			return levelIndex < levels.size()
				? levels.get(levelIndex)
				: null;
		}
		public @Nullable TLevel getCurrentLevelOr(TLevel defaultLevel) {
			TLevel found = getCurrentLevel();
			if (found != null) return found;

			return defaultLevel;
		}
	}
	/**
	 * .
	 * @param <TLevel>
	 * @param data
	 * @param info
	 */
	public static record ItemData<TLevel extends LevelItemInterface.Level<TLevel>>(
		LevelData<TLevel> data,
		SpecialItem.ItemData info
	) {}

	/**
	 * Events for {@link LevelItem}.
	 * @param <T> The type of {@link LevelItem}
	 * @param <TLevel> The type of {@link Level} the item uses
	 */
	public static abstract class Events<T extends LevelItem<T, TLevel>, TLevel extends LevelItemInterface.Level<TLevel>> extends SpecialItem.Events<T> {
		protected final Map<Player, LevelValue> deadPlayerLevels = new HashMap<>();

		protected Events(Game game, Events.Info info) {
			super(game, info);
		}

		public abstract List<TLevel> getLevels();

		protected abstract T getItemInternal(LevelItem.ItemData<TLevel> info);
		@Override
		protected final T getItemInternal(SpecialItem.ItemData info) {
			LevelValue levelValue = LevelItemInterface.levelFromItemStack(info.stack(), game);
			if (levelValue == null) return null;

			return getItemInternal(new LevelItem.ItemData<>(new LevelData<>(getLevels(), levelValue), info));
		}

		protected abstract T createItemInternal(LevelItem.LevelData<TLevel> data, Player owner);
		@Override
		protected final T createItemInternal(Player owner) {
			T created = createItemInternal(new LevelItem.LevelData<>(getLevels(), new LevelValue()), owner);
			LevelItemInterface.setItemLevel(created, created, created.levelState().value());

			return created;
		}
	}
}
