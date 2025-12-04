package fr.ludos.item;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nullable;

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


public abstract class BranchItem<TBranch extends BranchItem.Branch<TBranch>> extends SpecialItem {
	public static final String BRANCH = "branch";
	public static final NamespacedKey branchKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), BRANCH);


	private TBranch branch;
	public TBranch getBranch() {
		return branch;
	}


	public static @Nullable Integer branchFromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();

		if ( ! container.has(branchKey, PersistentDataType.INTEGER) ) return null;

		return getPersistentData(stack, branchKey, PersistentDataType.INTEGER);
	}

	public static Component getCycleBranchAnnotation(final @NotNull String keybind) {
		return getActionAnnotation(keybind, Component.text("Switch Mode"));
	}
	public static <TBranch extends Branch<TBranch>> Component getBranchAnnotation(TBranch branch) {
		if (branch == null) return Component.empty();

		return Component.text("(")
			.append(branch.getName())
			.append(Component.text(")"))
			.decoration(TextDecoration.ITALIC, false);
	}
	public static <TBranch extends Branch<TBranch>> Component getBranchLoreField(TBranch branch) {
		return Component.text("Mode: ")
				.color(NamedTextColor.GRAY)
			.append(branch.getName()
				.color(NamedTextColor.YELLOW))
			.decoration(TextDecoration.ITALIC, false);
	}

	public static <TItem extends SpecialItem, TBranch extends Branch<TBranch>> void setItemBranch(TItem item, TBranch newBranch, Function<TItem, TBranch> getBranch, BiConsumer<TItem, TBranch> setBranch) {
		TBranch oldBranch = getBranch.apply(item);

		ItemStack itemStack = item.getStack();

		ItemMeta meta = itemStack.getItemMeta();
		meta.getPersistentDataContainer().set(branchKey, PersistentDataType.INTEGER, newBranch.index());
		itemStack.setItemMeta(meta);

		setBranch.accept(item, newBranch);

		if (oldBranch != null) {
			oldBranch.onUnequip(item);
		}
		newBranch.onEquip(item);

		item.updateLore();
		item.updateName();
	}

	public static <TItem extends SpecialItem, TBranch extends BranchItem.Branch<TBranch>> void onSwitchItem(PlayerItemHeldEvent event, Function<ItemStack, TItem> getItem, Function<TItem, TBranch> getBranch) {
		Player player = event.getPlayer();

		TItem oldItem = getItem.apply(player.getInventory().getItem(event.getPreviousSlot()));
		TItem newItem = getItem.apply(player.getInventory().getItem(event.getNewSlot()));
		if (oldItem == null && newItem == null) return;

		if (oldItem != null) {
			getBranch.apply(oldItem).onUnequip(oldItem);
		}
		if (newItem != null) {
			getBranch.apply(newItem).onEquip(newItem);
		}
	}


	public BranchItem(ItemStack stack, Player owner, TBranch branch, Game game) {
		super(stack, owner, game);

		this.branch = branch;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		setBranch(branch);
	}


	public void cycleBranch() {
		setBranch(convertToBranch((getBranch().index() + 1) % getBranches().length));

		Player owner = getOwner();
		owner.playSound(owner.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.25f, 1);
	}


	public abstract TBranch convertToBranch(int levels);
	protected abstract TBranch[] getBranches();

	public void setBranch(TBranch branch) {
		setItemBranch(this, branch, BranchItem::getBranch, (item, newBranch) -> item.branch = newBranch);
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


	public static interface Branch<T extends Branch<T>> {
		public Component getName();
		public Component getDescription();

		public int index();

		public void onEquip(SpecialItem item);
		public void onUnequip(SpecialItem item);
	}

	public static abstract class Events<T extends BranchItem<TBranch>, TBranch extends Branch<TBranch>> extends SpecialItem.Events<T> {
		protected Events(Game game, @Nullable Integer slot, boolean canDrop) {
			super(game, slot, canDrop);
		}
		protected Events(Game game, @Nullable Integer slot) {
			this(game, slot, false);
		}
		protected Events(Game game) {
			this(game, null, false);
		}

		protected abstract TBranch[] getBranches();
	}
}
