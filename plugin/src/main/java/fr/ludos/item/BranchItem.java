package fr.ludos.item;

import org.bukkit.persistence.*;

import fr.ludos.Ludos;
import fr.ludos.game.Game;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


import java.util.ArrayList;
import java.util.List;


public abstract class BranchItem<TBranches extends SpecialItemBranches<TBranches>> extends SpecialItem {

	public static final String BRANCH = "branch";
	private NamespacedKey branchKey = new NamespacedKey(getGame().getPlugin(), BRANCH);


	private TBranches branch;

	public TBranches getBranch() {
		return branch;
	}


	public BranchItem(ItemStack stack, Game game) throws IllegalArgumentException {
		super(stack, game);

		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
		if ( ! container.has(branchKey, PersistentDataType.INTEGER) ) {
			throw new IllegalArgumentException("Branch Not found");
		}

		this.branch = convertToBranch(getPersistentData(stack, branchKey, PersistentDataType.INTEGER));
	}

	public BranchItem(ItemStack stack, Player owner, TBranches branch, Game game) {
		super(stack, owner, game);
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

	protected String getBranchAnnotation() {
		TBranches branch = getBranch();
		if (branch == null) return null;

		return "(" + branch.getName() + ChatColor.RESET.toString() + ChatColor.WHITE + ")";
	}

	@Override
	protected List<String> getLore() {
		List<String> lore = super.getLore();
		if (lore == null) {
			lore = new ArrayList<String>();
		}

		String branchFormatted = ChatColor.GRAY + "Type: " + ChatColor.YELLOW + branch.getName();

		lore.add(branchFormatted);
		return lore;
	}


	public static abstract class Events<T extends BranchItem<TBranches>, TBranches extends SpecialItemBranches<TBranches>> extends SpecialItem.Events<T> {

		public Events(Game game) {
			super(game);
		}

		protected abstract TBranches[] getBranches();
	}
}
