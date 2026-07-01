package fr.ludos.core.item;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public interface BranchItemInterface<TBranch extends BranchItem.Branch> extends SpecialItemInterface {
	public static final String BRANCH_KEY = "branch";
	public static final NamespacedKey branchKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), BRANCH_KEY);

	public TBranch getBranch();
	public void onSetBranch(TBranch branch);
	public TBranch getNextBranch();

	public boolean switchBranch(TBranch branch);
	public void addBranch(TBranch branch);
	public boolean removeBranch(TBranch branch);

	public default void cycleBranch() {
		TBranch newBranch = getNextBranch();

		switchBranch(newBranch);

		Player owner = getOwner();
		owner.playSound(owner.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.25f, 1);
	}

	/**
	 * Utility function to handle branch switching when player switches branches.
	 * @param <TItem> The type of the item, must extend SpecialItem
	 * @param <TBranch> The type of the branch, must be an enum that implements BranchItem.Branch
	 * @param item The item whose branch is being switched
	 * @param newBranch The new branch to switch to
	 */
	public static <TItem extends BranchItemInterface<TBranch>, TBranch extends BranchItem.Branch> void setItemBranch(TItem item, TBranch newBranch) {
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
	public static @Nullable String branchFromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();

		if ( ! container.has(branchKey, PersistentDataType.STRING) ) return null;

		return container.get(branchKey, PersistentDataType.STRING);
	}

	public static Component getCycleBranchAnnotation(final @NotNull String keybind) {
		return SpecialItemInterface.getActionAnnotation(keybind, Component.text("Switch Mode"));
	}
	public static <TBranch extends Branch> Component getBranchAnnotation(TBranch branch) {
		if (branch == null) return Component.empty();

		return Component.text("(")
			.append(branch.getName())
			.append(Component.text(")"))
			.decoration(TextDecoration.ITALIC, false);
	}
	public static <TBranch extends Branch> Component getBranchLoreField(TBranch branch) {
		return Component.text("Mode: ")
				.color(NamedTextColor.GRAY)
			.append(branch.getName()
				.color(NamedTextColor.YELLOW))
			.decoration(TextDecoration.ITALIC, false);
	}


	public static interface Branch {
		public String id();
		public Component getName();
		public Component getDescription();

		/**
		 * Called when item is equipped (mainhand) while the branch is selected.
		 * @param item The item being equipped
		 */
		public void onEquip(SpecialItemInterface item);
		/**
		 * Called when item is unequipped (mainhand) while the branch is selected.
		 * @param item The item being unequipped
		 */
		public void onUnequip(SpecialItemInterface item);

		/**
		 * Called when the branch is deselected (another branch is selected).
		 * @param item The item whose branch is being deselected
		 */
		public void onDeselectBranch(SpecialItemInterface item);

		/**
		 * Called when the branch is selected.
		 * @param item The item whose branch is being selected
		 */
		public void onSelectBranch(SpecialItemInterface item);
	}
}