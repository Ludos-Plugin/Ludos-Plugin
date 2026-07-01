package fr.ludos.core.item;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import fr.ludos.core.game.Game;
import net.kyori.adventure.text.Component;


public abstract class BranchItem<TBranch extends BranchItem.Branch> extends SpecialItem implements BranchItemInterface<TBranch> {
	protected TBranch branch;
	@Override
	public TBranch getBranch() {
		return branch;
	}

	protected final Map<String, TBranch> branches;
	private TBranch[] branchesArray;
	@Override
	public boolean switchBranch(TBranch branch) {
		if (! branches.containsValue(branch)) return false;
		BranchItemInterface.setItemBranch(this, branch);
		return true;
	}
	@Override
	public void addBranch(TBranch branch) {
		branches.put(branch.id(), branch);
		branchesArray = null;
	}
	@Override
	public boolean removeBranch(TBranch branch) {
		boolean removed = branches.remove(branch.id(), branch);
		if (removed) {
			branchesArray = null;
		}
		return removed;
	}
	@Override
	public TBranch getNextBranch() {
		if (branchesArray == null) {
			@SuppressWarnings("unchecked")
			TBranch[] array = (TBranch[]) java.lang.reflect.Array.newInstance(branch.getClass(), 0);
			branchesArray = branches.values().toArray(array);
		}

		int currentIndex = -1;
		for (int i = 0; i < branchesArray.length; i++) {
			if (branchesArray[i].equals(branch)) {
				currentIndex = i;
				break;
			}
		}
		if (currentIndex == -1) {
			currentIndex = 0;
		}
		int nextIndex = (currentIndex + 1) % branchesArray.length;

		return branchesArray[nextIndex];
	}

	/**
	 * Utility function to handle branch switching when player switches branches.
	 * @param <TItem> The type of the item, must extend SpecialItem
	 * @param <TBranch> The type of the branch, must be an enum that implements BranchItem.Branch
	 * @param item The item whose branch is being switched
	 * @param newBranch The new branch to switch to
	 */
	public static <TItem extends SpecialItem & BranchItemInterface<TBranch>, TBranch extends Branch> void setItemBranch(TItem item, TBranch newBranch) {
		TBranch oldBranch = item.getBranch();

		ItemStack itemStack = item.getStack();

		ItemMeta meta = itemStack.getItemMeta();
		meta.getPersistentDataContainer().set(branchKey, PersistentDataType.STRING, newBranch.id());
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
	 * @param <TItem> The type of the item, must extend SpecialItem
	 * @param <TBranch> The type of the branch, must be an enum that implements BranchItem.Branch
	 * @param event The PlayerItemHeldEvent to handle
	 * @param getItem An "SpecialItem from ItemStack" function, used to get the item being switched from/to
	 * @param getBranch A "Branch from Item" function, used to get the branch of the item being switched from/to
	 */
	public static <TItem extends SpecialItem & BranchItemInterface<TBranch>, TBranch extends Branch> void onSwitchItem(PlayerItemHeldEvent event, Function<ItemStack, TItem> getItem) {
		Player player = event.getPlayer();

		TItem oldItem = getItem.apply(player.getInventory().getItem(event.getPreviousSlot()));
		TItem newItem = getItem.apply(player.getInventory().getItem(event.getNewSlot()));
		if (oldItem == null && newItem == null) return;

		if (oldItem != null) {
			oldItem.getBranch().onUnequip(oldItem);
		}
		if (newItem != null) {
			newItem.getBranch().onEquip(newItem);
		}
	}


	public BranchItem(Map<String, TBranch> branches, TBranch branch, ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);

		if (branch == null) {
			branch = branches.values().iterator().next();
		}

		this.branch = branch;
		this.branches = branches;
	}
	protected BranchItem(Collection<TBranch> branches, TBranch branch, ItemStack stack, Player owner, Game game) {
		this(branches.stream().collect(Collectors.toMap(b -> b.id(), b -> b)), branch, stack, owner, game);
	}
	public BranchItem(Map<String, TBranch> branches, ItemStack stack, Player owner, Game game) {
		this(branches, null, stack, owner, game);
	}
	protected BranchItem(Collection<TBranch> branches, ItemStack stack, Player owner, Game game) {
		this(branches, null, stack, owner, game);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		switchBranch(branch);
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

	public static abstract class Events<T extends BranchItem<TBranch>, TBranch extends Branch> extends SpecialItem.Events<T> implements Map<String, TBranch> {
		private final Map<String, TBranch> branches;

		private @Nullable TBranch defaultBranch;


		public Map<String, TBranch> getBranches() {
			return Collections.unmodifiableMap(branches);
		}

		public TBranch getDefaultBranch() {
			if (defaultBranch == null) {
				return branches.values().iterator().next();
			}
			if (! branches.containsValue(defaultBranch)) {
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


		protected Events(Map<String, TBranch> branches, Game game, @Nullable ItemSlot slot, boolean canDrop) {
			super(game, slot, canDrop);
			this.branches = branches;
		}
		protected Events(Map<String, TBranch> branches, Game game, @Nullable ItemSlot slot) {
			this(branches, game, slot, false);
		}
		protected Events(Map<String, TBranch> branches, Game game) {
			this(branches, game, null, false);
		}

		protected Events(Collection<TBranch> branches, Game game, @Nullable ItemSlot slot, boolean canDrop) {
			this(branches.stream().collect(Collectors.toMap(b -> b.id(), b -> b)), game, slot, canDrop);
		}
		protected Events(Collection<TBranch> branches, Game game, @Nullable ItemSlot slot) {
			this(branches, game, slot, false);
		}
		protected Events(Collection<TBranch> branches, Game game) {
			this(branches, game, null, false);
		}

		protected Events(Game game, @Nullable ItemSlot slot, boolean canDrop) {
			this(new HashMap<>(), game, slot, canDrop);
		}
		protected Events(Game game, @Nullable ItemSlot slot) {
			this(game, slot, false);
		}
		protected Events(Game game) {
			this(game, null, false);
		}
	}
}
