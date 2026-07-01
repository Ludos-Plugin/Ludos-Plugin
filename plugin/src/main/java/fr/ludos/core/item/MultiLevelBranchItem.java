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
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.level.LevelItem;
import fr.ludos.core.item.level.LevelItemInterface;
import fr.ludos.core.item.level.LevelState;
import fr.ludos.core.item.level.LevelValue;
import fr.ludos.core.persistence.LevelValueMapPersistentDataType;
import net.kyori.adventure.text.Component;


public abstract class MultiLevelBranchItem<TBranch extends MultiLevelBranchItem.Branch> extends BranchItem<TBranch> implements LevelItemInterface {
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
	public void addBranch(TBranch branch) {
		MultiLevelBranchItem.super.addBranch(branch);
		if ( ! levelStates.containsKey(branch.id()) ) {
			levelStates.put(branch.id(), createLevelStateForBranch(branch));
		}
	}
	@Override
	public boolean removeBranch(TBranch branch) {
		boolean res = MultiLevelBranchItem.super.removeBranch(branch);
		if (res) {
			levelStates.remove(branch.id());
		}
		return res;
	}

	public static @Nullable Map<String, LevelValue> levelsFromItemStack(ItemStack stack, String id, Game game) {
		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();

		if ( ! container.has(LevelItem.levelKey, PersistentDataType.INTEGER_ARRAY) ) return null;

		return container.get(LevelItem.levelKey, LevelValueMapPersistentDataType.INSTANCE);
	}
	public static void saveLevelStates(SpecialItem item, Map<String, LevelValue> levelValues) {
		ItemMeta meta = item.getStack().getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();
		container.set(LevelItem.levelKey, LevelValueMapPersistentDataType.INSTANCE, levelValues);
		item.getStack().setItemMeta(meta);
	}
	public static <TBranch extends MultiLevelBranchItem.Branch> Component getBranchXpLoreField(MultiLevelBranchItem<TBranch> item, TBranch branch) {
		int maxLevel = branch.getMaxLevel();
		LevelState level = item.levelState();
		int levelNum = level.level();

		boolean isMax = maxLevel >= 0 && levelNum >= maxLevel;
		double levelThreshold = isMax ? 0 : branch.getXpThreshold(levelNum);

		return LevelItemInterface.getXpLoreField(isMax, levelThreshold, level.xp());
	}

	public MultiLevelBranchItem(Collection<TBranch> branches, Map<String, LevelValue> levels, ItemStack stack, Player owner, Game game) {
		this(branches.stream().collect(Collectors.toMap(b -> b.id(), b -> b)), branches.iterator().next(), levels, stack, owner, game);
	}
	public MultiLevelBranchItem(Map<String, TBranch> branches, Map<String, LevelValue> levels, ItemStack stack, Player owner, Game game) {
		this(branches, branches.values().iterator().next(), levels, stack, owner, game);
	}
	public MultiLevelBranchItem(Collection<TBranch> branches, TBranch defaultBranch, Map<String, LevelValue> levels, ItemStack stack, Player owner, Game game) {
		this(branches.stream().collect(Collectors.toMap(b -> b.id(), b -> b)), defaultBranch, levels, stack, owner, game);
	}
	public MultiLevelBranchItem(Map<String, TBranch> branches, TBranch defaultBranch, Map<String, LevelValue> levels, ItemStack stack, Player owner, Game game) {
		super(branches, defaultBranch, stack, owner, game);

		this.levelStates = branches.entrySet().stream()
			.collect(Collectors.toMap(
				e -> e.getKey(),
				e -> createLevelStateForBranch(e.getValue())
			));
	}

	protected final LevelState createLevelStateForBranch(TBranch branch) {
		return createLevelStateForBranch(branch, new LevelValue());
	}
	protected final LevelState createLevelStateForBranch(TBranch branch, @NotNull LevelValue level) {
		LevelState state = new LevelState(
			level,
			branch::getXpThreshold,
			branch.getMaxLevel()
		);

		state.addLevelUpListener( (lvlState, oldLevel) -> {
			if (lvlState.level() <= oldLevel) return;
			getOwner().sendMessage(
				LevelItemInterface.getLevelUpMessage(this)
			);
		} );
		state.addXpChangeListener( (lvlState) -> {
			saveLevelStates(this, getLevelValues());
			updateLore();
		} );

		return state;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		saveLevelStates(this, levelStates.entrySet().stream()
			.collect(Collectors.toMap(
				e -> e.getKey(),
				e -> e.getValue().value()
			))
		);
	}

	@Override
	public LevelState levelState() {
		TBranch currentBranch = getBranch();
		return levelStates.get(currentBranch.id());
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();

		lore.add(LevelItemInterface.getBranchLevelLoreField(this));
		lore.add(getBranchXpLoreField(this, getBranch()));

		return lore;
	}


	public static interface Branch extends BranchItem.Branch {
		public int getMaxLevel();
		public double getXpThreshold(@NotNull Integer level);

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

	public static abstract class Events<T extends MultiLevelBranchItem<TBranch>, TBranch extends Branch> extends BranchItem.Events<T, TBranch> {
		private Map<Player, Map<String, LevelValue>> deadPlayerLevels;

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

			deadPlayerLevels.put(player, specialItem.getLevelValues());
		}

		public abstract T createItem(Player owner, Map<String, LevelValue> levels);

		@Override
		public final T createItem(Player owner) {
			return createItem(owner, deadPlayerLevels.get(owner));
		}

		@EventHandler
		public void onSwitchItem(PlayerItemHeldEvent event) {
			BranchItem.onSwitchItem(event, this::getItem);
		}
	}
}
