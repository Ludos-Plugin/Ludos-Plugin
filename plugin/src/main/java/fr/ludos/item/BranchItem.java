package fr.ludos.item;

import org.bukkit.persistence.*;

import fr.ludos.Main;
import fr.ludos.role.Role;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


public abstract class BranchItem<TBranches extends SpecialItemBranches<TBranches>> extends SpecialItem {
	public static final String BRANCH = "branch";
	private NamespacedKey branchKey = new NamespacedKey(Main.getInstance(), BRANCH);

	public static final String LEVELS = "levels";
	private NamespacedKey levelsKey = new NamespacedKey(Main.getInstance(), LEVELS);

	public static final String XP = "xp";
	private NamespacedKey xpKey = new NamespacedKey(Main.getInstance(), XP);

	private static final String MAX_LVL_LABEL = "MAX";



	private TBranches branch;
	private int[] branchLevels;
	private double[] branchXps;

	public TBranches getBranch() {
		return branch;
	}
	public int[] getBranchLevels() {
		return branchLevels;
	}
	public int getCurrentBranchLevel() {
		return branchLevels[branch.index()];
	}
	public double[] getXps() {
		return branchXps;
	}
	public double getCurrentBranchXp() {
		return branchXps[branch.index()];
	}


	public BranchItem(ItemStack stack) throws IllegalArgumentException {
		super(stack);

		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
		if ( ! container.has(branchKey, PersistentDataType.INTEGER) ) {
			throw new IllegalArgumentException("Branch Not found");
		}
		if ( ! container.has(levelsKey, PersistentDataType.INTEGER_ARRAY) ) {
			throw new IllegalArgumentException("Levels Not found");
		}
		if ( ! container.has(xpKey, PersistentDataType.LONG_ARRAY) ) {
			throw new IllegalArgumentException("XPs not found");
		}

		this.branch = convertToBranch(getPersistentData(stack, branchKey, PersistentDataType.INTEGER));
		this.branchLevels = getPersistentData(stack, levelsKey, PersistentDataType.INTEGER_ARRAY);
		this.branchXps = getPersistentData(stack, xpKey, Test.DOUBLE_ARRAY);
	}

	public BranchItem(ItemStack stack, Player owner, TBranches branch, int[] levels, double[] xps) {
		super(stack, owner);
		setBranch(branch);
		setXps(xps);
		setLvls(levels);
	}


	public abstract TBranches convertToBranch(int levels);
	protected abstract TBranches[] getBranches();

	public void setBranch(TBranches level) {
		ItemMeta meta = getStack().getItemMeta();

		this.branch = level;
		meta.getPersistentDataContainer().set(branchKey, PersistentDataType.INTEGER, level.index());
		getStack().setItemMeta(meta);

		updateLore();
	}


	public void setLvls(int[] levels) {
		ItemMeta meta = getStack().getItemMeta();

		branchLevels = levels;
		meta.getPersistentDataContainer().set(levelsKey, PersistentDataType.INTEGER_ARRAY, branchLevels);
		getStack().setItemMeta(meta);

		updateLore();
	}

	public void setLvl(int level) {
		branchLevels[branch.index()] = level;

		setLvls(branchLevels);
	}
	public void addLvl() {
		if (branch.isMax(getCurrentBranchLevel())) {
			return;
		}

		setLvl(getCurrentBranchLevel() + 1);

		if (getOwner() != null) {
			getOwner().sendMessage(ChatColor.GREEN + "Your " + getName() + ChatColor.RESET + ChatColor.GREEN + " has leveled up!"); // TODO: Translate
		}
	}


	public void setXps(double[] xps) {
		ItemMeta meta = getStack().getItemMeta();

		branchXps = xps;
		meta.getPersistentDataContainer().set(xpKey, Test.DOUBLE_ARRAY, branchXps);
		getStack().setItemMeta(meta);

		updateLore();
	}

	public void setXp(double value) {
		double scaledTreshold = branch.getXpThreshold();
		if (value >= scaledTreshold) {
			addLvl();
			value -= scaledTreshold;
		}

		branchXps[branch.index()] = value;

		setXps(branchXps);
	}
	public void addXp(double xp) {
		if (branch.isMax(getCurrentBranchLevel())) {
			return;
		}

		double newXp = getCurrentBranchXp() + xp;
		setXp(newXp);
	}

	@Override
	public List<String> getLore() {
		List<String> lore = super.getLore();
		if (lore == null) {
			lore = new ArrayList<String>();
		}

		int branchCount = getBranches().length;
		if (branchLevels == null || branchXps == null || branchLevels.length < branchCount || branchXps.length < branchCount) {
			return lore;
		}
		int currentLevel = getCurrentBranchLevel();

		String xpFormatted = ChatColor.GRAY + "XP: " + ChatColor.YELLOW;
		if ( branch.isMax(currentLevel) ) {
			xpFormatted += MAX_LVL_LABEL;
		} else {
			String xpRounded = Long.toString(Math.round(getCurrentBranchXp() * 100) / 100);
			xpFormatted += xpRounded + '/' + branch.getXpThreshold();
		}

		String branchFormatted = ChatColor.GRAY + "Type: " + ChatColor.YELLOW + branch.getName();
		if (currentLevel != 0) {
			branchFormatted += " " + Integer.toString(currentLevel + 1);
		}


		lore.add(xpFormatted);
		lore.add(branchFormatted);
		return lore;
	}


	public static abstract class Events<T extends BranchItem<TBranches>, TBranches extends SpecialItemBranches<TBranches>> extends SpecialItem.Events<T> {

		public Events(String roleId) {
			super(roleId);
		}

		private Map<String, int[]> deadPlayerLevels = new HashMap<>();

		@EventHandler
		public void onPlayerDeath(PlayerDeathEvent event) {
			Player player = event.getEntity();
			if ( roleId != null && ! Role.isPlayerRole(player, roleId) ) {
				return;
			}

			T specialItem = SpecialItem.findIn(player.getInventory(), this::getItem);
			if ( specialItem == null ) {
				return;
			}

			deadPlayerLevels.put( player.getName(), Arrays.stream(specialItem.getBranchLevels())
				.map((int lvl) -> lvl == 0 ? 0 : lvl -1)
				.toArray() );
		}

		protected abstract TBranches[] getBranches();
		protected abstract T createItem(Player owner, int[] levels);

		@Override
		protected final T createItem(Player owner) {
			int[] levels = new int[getBranches().length];
			if (owner != null && deadPlayerLevels.containsKey(owner.getName())) {
				levels = deadPlayerLevels.get(owner.getName());
			}

			return createItem(owner, levels);
		}
	}
}
