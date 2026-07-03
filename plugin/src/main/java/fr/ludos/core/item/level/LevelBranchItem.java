package fr.ludos.core.item.level;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.BranchItem;
import net.kyori.adventure.text.Component;


public abstract class LevelBranchItem<TBranch extends BranchItem.Branch, TLevel extends Enum<TLevel> & LevelItem.Level<TLevel>> extends BranchItem<TBranch> implements LevelItemInterface {
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

	public LevelBranchItem(Map<String, TBranch> branches, @Nullable TBranch defaultBranch, List<TLevel> levels, LevelValue level, ItemStack stack, Player owner, Game game) {
		super(branches, defaultBranch, stack, owner, game);

		this.levels = ObjectUtils.requireNonEmpty(levels);
		this.levelState = LevelItemInterface.initializeLevelState(this, this.levels, level);
	}
	protected LevelBranchItem(List<TBranch> branches, @Nullable TBranch defaultBranch, List<TLevel> levels, LevelValue defaultLevel, ItemStack stack, Player owner, Game game) {
		this(branches.stream().collect(Collectors.toMap(b -> b.id(), b -> b)), defaultBranch, levels, defaultLevel, stack, owner, game);
	}
	public LevelBranchItem(Map<String, TBranch> branches, @Nullable TBranch defaultBranch, List<TLevel> levels, ItemStack stack, Player owner, Game game) {
		this(branches, defaultBranch, levels, new LevelValue(), stack, owner, game);
	}
	protected LevelBranchItem(List<TBranch> branches, @Nullable TBranch defaultBranch, List<TLevel> levels, ItemStack stack, Player owner, Game game) {
		this(branches.stream().collect(Collectors.toMap(b -> b.id(), b -> b)), defaultBranch, levels, new LevelValue(), stack, owner, game);
	}
	public LevelBranchItem(Map<String, TBranch> branches, List<TLevel> levels, LevelValue defaultLevel, ItemStack stack, Player owner, Game game) {
		this(branches, null, levels, defaultLevel, stack, owner, game);
	}
	protected LevelBranchItem(List<TBranch> branches, List<TLevel> levels, LevelValue defaultLevel, ItemStack stack, Player owner, Game game) {
		this(branches.stream().collect(Collectors.toMap(b -> b.id(), b -> b)), null, levels, defaultLevel, stack, owner, game);
	}
	public LevelBranchItem(Map<String, TBranch> branches, List<TLevel> levels, ItemStack stack, Player owner, Game game) {
		this(branches, null, levels, new LevelValue(), stack, owner, game);
	}
	protected LevelBranchItem(List<TBranch> branches, List<TLevel> levels, ItemStack stack, Player owner, Game game) {
		this(branches.stream().collect(Collectors.toMap(b -> b.id(), b -> b)), null, levels, new LevelValue(), stack, owner, game);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		LevelItemInterface.setItemLevel(this, levelState().value());
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();
		lore.add(LevelItemInterface.getLevelLoreField(this));
		lore.add(LevelItemInterface.getXpLoreField(this));

		return lore;
	}


	public static abstract class Events<T extends LevelBranchItem<TBranch, TLevel>, TBranch extends BranchItem.Branch, TLevel extends Enum<TLevel> & LevelItem.Level<TLevel>> extends BranchItem.Events<T, TBranch> {
		private Map<Player, LevelValue> deadPlayerLevels = new HashMap<>();

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

		public abstract T createItem(Player owner, LevelValue level);
		public abstract List<TLevel> getLevels();

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
