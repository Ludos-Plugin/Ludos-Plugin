package fr.ludos.item;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public abstract class BranchItem<TBranch extends Enum<TBranch> & BranchItem.Branch<TBranch>> extends SpecialItem implements BranchItemInterface<TBranch> {
	public static final String BRANCH_KEY = "branch";
	public static final NamespacedKey branchKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), BRANCH_KEY);


	protected TBranch branch;
	public TBranch getBranch() {
		return branch;
	}
	public void setBranch(TBranch branch) {
		this.branch = branch;
	}
	private final TBranch[] branches;
	public TBranch[] getBranches() {
		return branches;
	}
	public int getMaxBranchIndex() {
		return branches.length - 1;
	}


	public static @Nullable Integer branchFromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();

		if ( ! container.has(branchKey, PersistentDataType.INTEGER) ) return null;

		return getPersistentData(stack, branchKey, PersistentDataType.INTEGER);
	}

	public static Component getCycleBranchAnnotation(final @NotNull String keybind) {
		return getActionAnnotation(keybind, Component.text("Switch Mode"));
	}
	public static <TBranch extends Enum<TBranch> & Branch<TBranch>> Component getBranchAnnotation(TBranch branch) {
		if (branch == null) return Component.empty();

		return Component.text("(")
			.append(branch.getName())
			.append(Component.text(")"))
			.decoration(TextDecoration.ITALIC, false);
	}
	public static <TBranch extends Enum<TBranch> & Branch<TBranch>> Component getBranchLoreField(TBranch branch) {
		return Component.text("Mode: ")
				.color(NamedTextColor.GRAY)
			.append(branch.getName()
				.color(NamedTextColor.YELLOW))
			.decoration(TextDecoration.ITALIC, false);
	}

	/**
	 * Utility function to handle branch switching when player switches branches.
	 * @param <TItem> The type of the item, must extend SpecialItem
	 * @param <TBranch> The type of the branch, must be an enum that implements BranchItem.Branch
	 * @param item The item whose branch is being switched
	 * @param newBranch The new branch to switch to
	 */
	public static <TItem extends SpecialItem & BranchItemInterface<TBranch>, TBranch extends Enum<TBranch> & Branch<TBranch>> void setItemBranch(TItem item, TBranch newBranch) {
		TBranch oldBranch = item.getBranch();

		ItemStack itemStack = item.getStack();

		ItemMeta meta = itemStack.getItemMeta();
		meta.getPersistentDataContainer().set(branchKey, PersistentDataType.INTEGER, newBranch.ordinal());
		itemStack.setItemMeta(meta);

		item.setBranch(newBranch);

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
	public static <TItem extends SpecialItem & BranchItemInterface<TBranch>, TBranch extends Enum<TBranch> & BranchItem.Branch<TBranch>> void onSwitchItem(PlayerItemHeldEvent event, Function<ItemStack, TItem> getItem) {
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


	public BranchItem(Class<TBranch> branchClass, ItemStack stack, Player owner, TBranch branch, Game game) {
		super(stack, owner, game);

		if (branch == null) {
			throw new IllegalArgumentException("Branch cannot be null");
		}

		this.branch = branch;
		this.branches = branchClass.getEnumConstants();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		switchBranch(branch);
	}


	public void cycleBranch() {
		TBranch currentBranch = getBranch();
		int oldBranchIndex = currentBranch.ordinal();
		int newBranchIndex = (oldBranchIndex + 1) % branches.length;
		TBranch newBranch = branches[newBranchIndex];
		if (newBranch == currentBranch) return;

		switchBranch(newBranch);

		Player owner = getOwner();
		owner.playSound(owner.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.25f, 1);
	}

	public void switchBranch(TBranch branch) {
		setItemBranch(this, branch);
	}

	protected Component getBranchAnnotation() {
		TBranch branch = getBranch();
		return getBranchAnnotation(branch);
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();
		lore.add(getBranchLoreField(getBranch()));

		return lore;
	}


	public static interface Branch<T extends Enum<T> & Branch<T>> {
		public Component getName();
		public Component getDescription();

		/**
		 * Called when item is equipped (mainhand) while the branch is selected.
		 * @param item The item being equipped
		 */
		public void onEquip(SpecialItem item);
		/**
		 * Called when item is unequipped (mainhand) while the branch is selected.
		 * @param item The item being unequipped
		 */
		public void onUnequip(SpecialItem item);

		/**
		 * Called when the branch is deselected (another branch is selected).
		 * @param item The item whose branch is being deselected
		 */
		public void onDeselectBranch(SpecialItem item);

		/**
		 * Called when the branch is selected.
		 * @param item The item whose branch is being selected
		 */
		public void onSelectBranch(SpecialItem item);
	}

	public static abstract class Events<T extends BranchItem<TBranch>, TBranch extends Enum<TBranch> & Branch<TBranch>> extends SpecialItem.Events<T> {
		protected Events(Game game, @Nullable Integer slot, boolean canDrop) {
			super(game, slot, canDrop);
		}
		protected Events(Game game, @Nullable Integer slot) {
			this(game, slot, false);
		}
		protected Events(Game game) {
			this(game, null, false);
		}
	}
}
