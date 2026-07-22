package fr.ludos.core.item.level;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.BranchItem;
import fr.ludos.core.item.BranchItemInterface;
import fr.ludos.core.item.SpecialItem;
import net.kyori.adventure.text.Component;

/**
 * A {@link SpecialItem} implementation with the ability to hold {@link BranchItemInterface.Branch}es, with a single {@link LevelState}, shared between them.
 * @param <T> self type
 * @param <TBranch> The type of {@link BranchItemInterface.Branch} the item uses
 * @param <TLevel> The type of {@link Level} the item uses
 */
public abstract class LevelBranchItem<T extends LevelBranchItem<T, TBranch, TLevel>, TBranch extends BranchItemInterface.Branch, TLevel extends LevelItemInterface.Level<TLevel>> extends BranchItem<T, TBranch> implements LevelItemInterface {
	private final LevelState levelState;
	@Override
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

	public LevelBranchItem(Info<TBranch, TLevel> info, Events<T, TBranch, TLevel> events) {
		super(new BranchItem.ItemData<>(info.branch, info.info), events);

		this.levels = ObjectUtils.requireNonEmpty(info.level.levels());
		this.levelState = LevelItemInterface.initializeLevelState(this, this, this.levels, info.level.level());
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
	 * @param <TBranch>
	 * @param level
	 * @param branch
	 * @param info
	 */
	public static record Info<TBranch extends BranchItemInterface.Branch, TLevel extends LevelItemInterface.Level<TLevel>>(
		BranchItem.BranchData<TBranch> branch,
		LevelItem.LevelData<TLevel> level,
		SpecialItem.ItemData info
	) {}

	/**
	 * Events for {@link LevelBranchItem}s.
	 * @param <T> The type of {@link LevelBranchItem}
	 * @param <TBranch> The type of {@link BranchItemInterface.Branch} the item uses
	 * @param <TLevel> The type of {@link LevelItemInterface.Level} the item uses
	 */
	public static abstract class Events<T extends LevelBranchItem<T, TBranch, TLevel>, TBranch extends BranchItemInterface.Branch, TLevel extends LevelItemInterface.Level<TLevel>> extends BranchItem.Events<T, TBranch> {

		protected Events(Map<String, TBranch> branches, @Nullable TBranch defaultBranch, Game game, Events.Info info) {
			super(branches, defaultBranch, game, info);
		}
		protected Events(Collection<TBranch> branches, @Nullable TBranch defaultBranch, Game game, Events.Info info) {
			this(branches.stream().collect(Collectors.toMap(b -> b.id(), b -> b)), defaultBranch, game, info);
		}
		protected Events(Map<String, TBranch> branches, Game game, Events.Info info) {
			this(branches, null, game, info);
		}
		protected Events(Collection<TBranch> branches, Game game, Events.Info info) {
			this(branches, null, game, info);
		}

		public abstract List<TLevel> getLevels();

		protected abstract T getItemInternal(LevelBranchItem.Info<TBranch, TLevel> info);
		@Override
		protected final T getItemInternal(BranchItem.ItemData<TBranch> info) {
			LevelValue levelValue = LevelItemInterface.levelFromItemStack(info.info().stack(), game);
			if (levelValue == null) return null;

			return getItemInternal(new LevelBranchItem.Info<>(
				info.data(),
				new LevelItem.LevelData<TLevel>(getLevels(), levelValue),
				info.info()
			));
		}

		protected abstract T createItemInternal(BranchItem.BranchData<TBranch> branch, LevelItem.LevelData<TLevel> level, Player owner);
		@Override
		protected T createItemInternal(BranchData<TBranch> data, Player owner) {
			T created = createItemInternal(data, new LevelItem.LevelData<>(getLevels()), owner);
			LevelItemInterface.setItemLevel(created, created, created.levelState().value());

			return created;
		}

		@EventHandler
		public void onSwitchItem(PlayerItemHeldEvent event) {
			BranchItem.onSwitchItem(event, this::getItem);
		}
	}
}
