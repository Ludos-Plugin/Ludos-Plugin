package fr.ludos.item;

import java.util.List;
import java.util.ArrayList;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.persistence.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.ludos.game.Game;


public abstract class BranchItem<TBranches extends SpecialItemBranches<TBranches>> extends SpecialItem {

	public static final String BRANCH = "branch";
	private final NamespacedKey branchKey;


	private TBranches branch;

	public TBranches getBranch() {
		return branch;
	}


	public BranchItem(ItemStack stack, Game game) throws IllegalArgumentException {
		super(stack, game);

		branchKey = new NamespacedKey(game.getPlugin(), BRANCH);

		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
		if ( ! container.has(branchKey, PersistentDataType.INTEGER) ) {
			throw new IllegalArgumentException("Branch Not found");
		}

		this.branch = convertToBranch(getPersistentData(stack, branchKey, PersistentDataType.INTEGER));
	}

	public BranchItem(ItemStack stack, Player owner, TBranches branch, Game game) {
		super(stack, owner, game);

		branchKey = new NamespacedKey(getGame().getPlugin(), BRANCH);

		setBranch(branch);
	}


	public void cycleBranch() {
		setBranch(convertToBranch((getBranch().index() + 1) % getBranches().length));

		Player owner = getOwner();
		owner.playSound(owner.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.25f, 1);
	}


	public abstract TBranches convertToBranch(int levels);
	protected abstract TBranches[] getBranches();

	public void setBranch(TBranches branch) {
		ItemMeta meta = getStack().getItemMeta();

		this.branch = branch;
		meta.getPersistentDataContainer().set(branchKey, PersistentDataType.INTEGER, branch.index());
		getStack().setItemMeta(meta);

		updateLore();
		updateName();

		branch.onSwitchBranch(this);
	}

	protected Component getBranchAnnotation() {
		TBranches branch = getBranch();
		if (branch == null) return null;

		return Component.text("(")
			.append(branch.getName())
			.append(Component.text(")"));
	}

	@Override
	protected List<Component> getLore() {
		List<Component> lore = super.getLore();
		if (lore == null) {
			lore = new ArrayList<Component>();
		}

		lore.add(
			Component.text("Type:").color(TextColor.color(0xAAAAAA)).appendSpace()
			.append(branch.getName().color(TextColor.color(0xFFFF55)))
		);
		return lore;
	}


	public static abstract class Events<T extends BranchItem<TBranches>, TBranches extends SpecialItemBranches<TBranches>> extends SpecialItem.Events<T> {

		public Events(Game game) {
			super(game);
		}

		protected abstract TBranches[] getBranches();
	}
}
