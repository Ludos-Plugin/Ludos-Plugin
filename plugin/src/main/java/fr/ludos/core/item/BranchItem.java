package fr.ludos.core.item;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import fr.ludos.core.game.Game;
import net.kyori.adventure.text.Component;

/**
 * Wrapper for {@link BranchItemInterface} over a {@link ItemStack}.
 * @param <T> self type
 * @param <TBranch> The type that a Branch needs to implement/extend to be used as a Branch for this type.
 */
public abstract class BranchItem<T extends BranchItem<T, TBranch>, TBranch extends BranchItemInterface.Branch> extends SpecialItem<T> implements BranchItemInterface<T, TBranch> {
	protected TBranch branch;
	@Override
	public TBranch getBranch() {
		return branch;
	}

	protected final Map<String, TBranch> branches;
	private List<String> branchesList;
	@Override
	public boolean switchBranch(TBranch branch) {
		if (! branches.containsValue(branch)) return false;
		BranchItemInterface.setItemBranch(this, branch);
		return true;
	}
	@Override
	public void addBranch(TBranch branch) {
		branches.put(branch.id(), branch);
		branchesList = null;
	}
	@Override
	public boolean removeBranch(TBranch branch) {
		boolean removed = branches.remove(branch.id(), branch);
		if (removed) {
			branchesList = null;
		}
		return removed;
	}
	@Override
	public TBranch getNextBranch() {
		if (branchesList == null) {
			branchesList = branches.values().stream()
				.map(TBranch::id)
				.sorted()
				.collect(Collectors.toList());
		}
		if (branchesList.size() == 0) return null;

		final String currentBranchId = branch.id();
		final int currentIndex = Math.max(0, branchesList.indexOf(currentBranchId));
		final int nextIndex = (currentIndex + 1) % branchesList.size();
		final String nextBranchId = branchesList.get(nextIndex);

		final TBranch nextBranch = branches.get(nextBranchId);

		return nextBranch;
	}

	/**
	 * Utility function to handle branch switching when player switches branches.
	 * @param <T> The type of the item, must extend SpecialItem
	 * @param <TBranch> The type of the branch, must be an enum that implements {@link BranchItemInterface.Branch}
	 * @param item The item whose branch is being switched
	 * @param newBranch The new branch to switch to
	 */
	public static <T extends BranchItemInterface<T, TBranch>, TBranch extends Branch> void setItemBranch(T item, TBranch newBranch) {
		if (newBranch == null) return;

		TBranch oldBranch = item.getBranch();

		ItemStack itemStack = item.getStack();

		ItemMeta meta = itemStack.getItemMeta();
		meta.getPersistentDataContainer().set(BRANCH_KEY, PersistentDataType.STRING, newBranch.id());
		itemStack.setItemMeta(meta);

		item.onSetBranch(newBranch);

		if (oldBranch != null) {
			oldBranch.onDeselectBranch(item);
		}
		newBranch.onSelectBranch(item);

		item.update();
	}

	/**
	 * Utility function to handle branch switching when player switches items.
	 * @param <T> The type of the item, must extend SpecialItem
	 * @param <TBranch> The type of the branch, must be an enum that implements {@link BranchItemInterface.Branch}
	 * @param event The PlayerItemHeldEvent to handle
	 * @param getItem An "SpecialItem from ItemStack" function, used to get the item being switched from/to
	 */
	public static <T extends BranchItemInterface<T, TBranch>, TBranch extends Branch> void onSwitchItem(PlayerItemHeldEvent event, Function<ItemStack, T> getItem) {
		Player player = event.getPlayer();

		T oldItem = getItem.apply(player.getInventory().getItem(event.getPreviousSlot()));
		T newItem = getItem.apply(player.getInventory().getItem(event.getNewSlot()));
		if (oldItem == null && newItem == null) return;

		if (oldItem != null) {
			oldItem.getBranch().onUnequip(oldItem);
		}
		if (newItem != null) {
			newItem.getBranch().onEquip(newItem);
		}
	}


	public BranchItem(BranchItem.ItemData<TBranch> info, Events<T, TBranch> events) {
		super(info.info, events);

		this.branches = ObjectUtils.requireNonEmpty(info.data.branches);

		this.branch = info.data.branch != null
			? info.data.branch
			: branches.values().iterator().next();
	}

	public void onSetBranch(TBranch branch) {
		this.branch = branch;
	}

	protected Component getBranchAnnotation() {
		TBranch branch = getBranch();
		return BranchItemInterface.getBranchAnnotation(branch);
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();
		lore.add(BranchItemInterface.getBranchLoreField(getBranch()));

		return lore;
	}

	/**
	 * .
	 * @param <TBranch>
	 * @param branches
	 * @param branch
	 */
	public static record BranchData<TBranch extends BranchItemInterface.Branch>(
		Map<String, TBranch> branches,
		TBranch branch
	) {}
	/**
	 * .
	 * @param <TBranch>
	 * @param data
	 * @param info
	 */
	public static record ItemData<TBranch extends BranchItemInterface.Branch>(
		BranchData<TBranch> data,
		SpecialItem.ItemData info
	) {}

	/**
	 * Events for a {@link BranchItem}.
	 * @param <T> The type of {@link BranchItem}
	 * @param <TBranch> The type of {@link BranchItemInterface.Branch} the item uses
	 */
	public static abstract class Events<T extends BranchItem<T, TBranch>, TBranch extends Branch> extends SpecialItem.Events<T> implements Map<String, TBranch> {
		private final Map<String, TBranch> branches;

		private @Nullable TBranch defaultBranch;


		protected Events(Map<String, TBranch> branches, @Nullable TBranch defaultBranch, Game game, Events.Info info) {
			super(game, info);
			this.branches = Objects.requireNonNull(branches);
			this.defaultBranch = defaultBranch;
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

		protected abstract T getItemInternal(BranchItem.ItemData<TBranch> info);
		@Override
		protected T getItemInternal(SpecialItem.ItemData info) {
			final String branchKey = BranchItemInterface.branchFromItemStack(info.stack(), game);
			if (branchKey == null) return null;

			TBranch branch = branches.getOrDefault(branchKey, defaultBranch);

			return getItemInternal(new ItemData<>(new BranchData<>(branches, branch), info));
		}

		protected abstract T createItemInternal(BranchData<TBranch> data, Player owner);
		@Override
		protected T createItemInternal(Player owner) {
			T created = createItemInternal(new BranchData<>(branches, defaultBranch), owner);
			created.switchBranch(created.branch);

			return created;
		}


		public Map<String, TBranch> getBranches() {
			return Collections.unmodifiableMap(branches);
		}

		public TBranch getDefaultBranch() {
			if (defaultBranch == null || ! branches.containsValue(defaultBranch)) {
				return branches.values().iterator().next();
			}
			return defaultBranch;
		}
		public void setDefaultBranch(@Nullable TBranch defaultBranch) {
			this.defaultBranch = defaultBranch;
		}

		@Override
		public void clear() {
			branches.clear();
		}
		@Override
		public boolean containsKey(Object key) {
			return branches.containsKey(key);
		}
		@Override
		public boolean containsValue(Object value) {
			return branches.containsValue(value);
		}
		@Override
		public Set<Entry<String, TBranch>> entrySet() {
			return branches.entrySet();
		}
		@Override
		public TBranch get(Object key) {
			return branches.get(key);
		}
		@Override
		public TBranch put(String key, TBranch value) {
			return branches.put(key, value);
		}
		@Override
		public void putAll(Map<? extends String, ? extends TBranch> m) {
			branches.putAll(m);
		}
		@Override
		public TBranch remove(Object key) {
			return branches.remove(key);
		}
		@Override
		public int size() {
			return branches.size();
		}
		@Override
		public boolean isEmpty() {
			return branches.isEmpty();
		}
		@Override
		public Set<String> keySet() {
			return branches.keySet();
		}
		@Override
		public Collection<TBranch> values() {
			return branches.values();
		}
	}
}
