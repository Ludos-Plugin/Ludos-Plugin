package fr.ludos.core.item;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.level.LevelItem;
import fr.ludos.core.item.level.LevelItemInterface;
import fr.ludos.core.item.level.LevelState;
import fr.ludos.core.item.level.LevelValue;
import fr.ludos.core.persistence.LevelValueMapPersistentDataType;
import net.kyori.adventure.text.Component;

/**
 * A {@link SpecialItem} implementation with the ability to hold {@link BranchItemInterface.Branch}es, with each their own {@link LevelState}.
 * @param <T> self type
 * @param <TBranch> The type of {@link BranchItemInterface.Branch} the item uses
 */
public abstract class MultiLevelBranchItem<T extends MultiLevelBranchItem<T, TBranch>, TBranch extends MultiLevelBranchItem.Branch> extends BranchItem<T, TBranch> implements LevelItemInterface {
	private final Map<String, LevelState> levelStates;
	public Map<String, LevelState> getLevelStates() {
		return Collections.unmodifiableMap(levelStates);
	}
	public Map<String, LevelValue> getLevelValues() {
		return levelStates.entrySet().stream()
			.collect(Collectors.toMap(
				e -> e.getKey(),
				e -> e.getValue().value()
			));
	}

	@Override
	public LevelState levelState() {
		TBranch currentBranch = getBranch();
		return levelStates.get(currentBranch.id());
	}

	@Override
	public void onSwitchToLevel(Integer level) {
		getBranch().onSetLevel(level, this);
	}
	@Override
	public void onSwitchOffLevel(Integer level) {
		getBranch().onUnsetLevel(level, this);
	}

	@Override
	public void addBranch(TBranch branch) {
		MultiLevelBranchItem.super.addBranch(branch);
		if ( ! levelStates.containsKey(branch.id()) ) {
			levelStates.put(branch.id(), createLevelStateForBranch(branch, new LevelValue()));
		}
	}
	@Override
	public boolean removeBranch(TBranch branch) {
		if (branch == getBranch()) {
			cycleBranch();
		}

		boolean res = MultiLevelBranchItem.super.removeBranch(branch);
		if (res) {
			levelStates.remove(branch.id());
		}
		return res;
	}

	public static @Nullable Map<String, LevelValue> levelsFromItemStack(ItemStack stack, String id) {
		if (stack == null) return null;

		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return null;

		PersistentDataContainer container = meta.getPersistentDataContainer();

		if ( ! container.has(LevelItem.LEVEL_KEY, LevelValueMapPersistentDataType.INSTANCE) ) return null;

		return container.get(LevelItem.LEVEL_KEY, LevelValueMapPersistentDataType.INSTANCE);
	}
	public static void saveLevelStates(ItemStack stack, Map<String, LevelValue> levelValues) {
		if (stack == null) return;

		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return;

		PersistentDataContainer container = meta.getPersistentDataContainer();

		container.set(LevelItem.LEVEL_KEY, LevelValueMapPersistentDataType.INSTANCE, levelValues);
		stack.setItemMeta(meta);
	}
	public static <T extends MultiLevelBranchItem<T, TBranch>, TBranch extends MultiLevelBranchItem.Branch> Component getBranchXpLoreField(MultiLevelBranchItem<T, TBranch> item, TBranch branch) {
		int maxLevel = branch.maxLevel();
		LevelState level = item.levelState();
		int levelNum = level.level();

		boolean isMax = maxLevel >= 0 && levelNum >= maxLevel;
		double levelThreshold = isMax ? 0 : branch.xpThreshold(levelNum);

		return LevelItemInterface.getXpLoreField(isMax, levelThreshold, level.xp());
	}

	public MultiLevelBranchItem(ItemData<TBranch> info, Events<T, TBranch> events) {
		super(new BranchItem.ItemData<>(info.branch, info.info), events);

		Map<String, LevelValue> initialLevels = info.level.levels;

		this.levelStates = branches.entrySet().stream()
			.collect(Collectors.toMap(
				e -> e.getKey(),
				e -> createLevelStateForBranch(e.getValue(), initialLevels.getOrDefault(e.getKey(), new LevelValue()))
			));
	}

	protected final LevelState createLevelStateForBranch(TBranch branch, @NotNull LevelValue level) {
		LevelState state = LevelState.capped(
			level,
			branch::xpThreshold,
			branch.maxLevel()
		);
		return LevelItemInterface.initializeLevelState(
			this, this,
			state,
			(lvlValue, oldLevel) -> LevelItemInterface.saveLevelValues(getStack(), getLevelValues()),
			(lvlValue, oldXp) -> LevelItemInterface.saveLevelValues(getStack(), getLevelValues())
		);
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();

		lore.add(LevelItemInterface.getLevelLoreField(this));
		lore.add(getBranchXpLoreField(this, getBranch()));

		return lore;
	}

	/**
	 * .
	 * @param levels
	 */
	public static record MultiLevelData(
		Map<String, LevelValue> levels
	) {}

	/**
	 * .
	 * @param <TBranch>
	 * @param level
	 * @param branch
	 * @param info
	 */
	public static record ItemData<TBranch extends BranchItemInterface.Branch>(
		MultiLevelData level,
		BranchData<TBranch> branch,
		SpecialItem.ItemData info
	) {}

	/**
	 * {@link BranchItemInterface.Branch} for {@link MultiLevelBranchItem}s.
	 */
	public static interface Branch extends BranchItemInterface.Branch {
		public int maxLevel();
		public double xpThreshold(@NotNull Integer level);

		/**
		 * Called when the level is set on the item, including when the item is created with a non-zero level. Should be used to apply the level's effects.
		 * @param level The level that is being set
		 * @param item The item on which the level is being set
		 */
		public void onSetLevel(int level, SpecialItemInterface item);
		/**
		 * Called when the level is unset on the item, including when the item is created with a non-zero level. Should be used to remove the level's effects.
		 * @param level The level that is being set
		 * @param item The item on which the level is being unset
		 */
		public void onUnsetLevel(int level, SpecialItemInterface item);
	}

	/**
	 * Events for {@link MultiLevelBranchItem}s.
	 * @param <T> The type of {@link MultiLevelBranchItem}
	 * @param <TBranch> The type of {@link Branch} the item uses
	 */
	public static abstract class Events<T extends MultiLevelBranchItem<T, TBranch>, TBranch extends Branch> extends BranchItem.Events<T, TBranch> {
		private Map<Player, Map<String, LevelValue>> deadPlayerLevels = new HashMap<>();

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

		@EventHandler
		public void onPlayerDeath(PlayerDeathEvent event) {
			Player player = event.getEntity();
			if (! isPlayerValid(player)) return;

			T specialItem = SpecialItem.findOne(player.getInventory(), this::getItem);
			if ( specialItem == null ) return;

			deadPlayerLevels.put(player, specialItem.getLevelValues());
		}


		protected abstract T getItemInternal(ItemData<TBranch> info);
		@Override
		protected final T getItemInternal(BranchItem.ItemData<TBranch> info) {
			final Map<String, LevelValue> levels = MultiLevelBranchItem.levelsFromItemStack(info.info().stack(), getTypeId());
			if (levels == null) return null;

			return getItemInternal(new ItemData<TBranch>(
				new MultiLevelData(levels),
				info.data(),
				info.info()
			));
		}

		protected abstract T createItemInternal(MultiLevelBranchItem.MultiLevelData levels, BranchItem.BranchData<TBranch> data, Player owner);
		@Override
		protected final T createItemInternal(BranchItem.BranchData<TBranch> data, Player owner) {
			T created = createItemInternal(new MultiLevelData(new HashMap<>()), data, owner);
			saveLevelStates(created.getStack(), created.getLevelStates().entrySet().stream()
				.collect(Collectors.toMap(
					e -> e.getKey(),
					e -> e.getValue().value()
				))
			);

			return created;
		}

		@EventHandler
		public void onSwitchItem(PlayerItemHeldEvent event) {
			BranchItem.onSwitchItem(event, this::getItem);
		}
	}
}
