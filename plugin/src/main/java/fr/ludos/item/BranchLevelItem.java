package fr.ludos.item;

import org.bukkit.persistence.*;

import fr.ludos.Ludos;
import fr.ludos.game.Game;

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


public abstract class BranchLevelItem<TBranches extends SpecialItemLevelBranches<TBranches>> extends BranchItem<TBranches> {
	public BranchLevelItem(ItemStack stack, Player owner, TBranches branch, Game game) {
		super(stack, owner, branch, game);

		//TODO Auto-generated constructor stub
	}


	public static final String LEVELS = "levels";
	private NamespacedKey levelsKey = new NamespacedKey(getGame().getPlugin(), LEVELS);

	public static final String XP = "xp";
	private NamespacedKey xpKey = new NamespacedKey(getGame().getPlugin(), XP);

	private static final String MAX_LVL_LABEL = "MAX";



	private int[] branchLevels;
	private double[] branchXps;

	public int[] getBranchLevels() {
		return branchLevels;
	}
	public int getCurrentBranchLevel() {
		return branchLevels[getBranch().index()];
	}
	public double[] getXps() {
		return branchXps;
	}
	public double getCurrentBranchXp() {
		return branchXps[getBranch().index()];
	}


	public BranchLevelItem(ItemStack stack, Game game) throws IllegalArgumentException {
		super(stack, game);

		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
		if ( ! container.has(levelsKey, PersistentDataType.INTEGER_ARRAY) ) {
			throw new IllegalArgumentException("Levels Not found");
		}
		if ( ! container.has(xpKey, PersistentDataType.LONG_ARRAY) ) {
			throw new IllegalArgumentException("XPs not found");
		}

		this.branchLevels = getPersistentData(stack, levelsKey, PersistentDataType.INTEGER_ARRAY);
		this.branchXps = getPersistentData(stack, xpKey, DoubleArrayPersistentDataType.INSTANCE);
	}

	public BranchLevelItem(ItemStack stack, Player owner, TBranches branch, int[] levels, double[] xps, Game game) {
		super(stack, owner, branch, game);
		setXps(xps);
		setLvls(levels);
	}


	public void setLvls(int[] levels) {
		ItemMeta meta = getStack().getItemMeta();

		branchLevels = levels;
		meta.getPersistentDataContainer().set(levelsKey, PersistentDataType.INTEGER_ARRAY, branchLevels);
		getStack().setItemMeta(meta);

		updateLore();
	}

	public void setLvl(int level) {
		branchLevels[getBranch().index()] = level;

		setLvls(branchLevels);
	}
	public void addLvl() {
		if (getBranch().isMax(getCurrentBranchLevel())) {
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
		meta.getPersistentDataContainer().set(xpKey, DoubleArrayPersistentDataType.INSTANCE, branchXps);
		getStack().setItemMeta(meta);

		updateLore();
	}

	public void setXp(double value) {
		double scaledTreshold = getBranch().getXpThreshold();
		if (value >= scaledTreshold) {
			addLvl();
			value -= scaledTreshold;
		}

		branchXps[getBranch().index()] = value;

		setXps(branchXps);
	}
	public void addXp(double xp) {
		if (getBranch().isMax(getCurrentBranchLevel())) {
			return;
		}

		double newXp = getCurrentBranchXp() + xp;
		setXp(newXp);
	}

	@Override
	protected List<String> getLore() {
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
		if ( getBranch().isMax(currentLevel) ) {
			xpFormatted += MAX_LVL_LABEL;
		} else {
			String xpRounded = Double.toString(Math.round(getCurrentBranchXp() * 100.0) / 100.0);
			xpFormatted += xpRounded + '/' + getBranch().getXpThreshold();
		}

		String levelFormatted = ChatColor.GRAY + "Level: " + ChatColor.YELLOW + Integer.toString(currentLevel + 1);

		lore.add(xpFormatted);
		lore.add(levelFormatted);
		return lore;
	}


	public static abstract class Events<T extends BranchLevelItem<TBranches>, TBranches extends SpecialItemLevelBranches<TBranches>> extends BranchItem.Events<T, TBranches> {

		public Events(Game game) {
			super(game);

			this.deadPlayerLevels = new HashMap<>();
		}

		private Map<String, int[]> deadPlayerLevels;

		@EventHandler
		public void onPlayerDeath(PlayerDeathEvent event) {
			Player player = event.getEntity();
			if (! canPlayerHaveItem(player)) return;

			T specialItem = SpecialItem.findIn(player.getInventory(), (ItemStack stack) -> getItem(stack, game));
			if ( specialItem == null ) {
				return;
			}

			deadPlayerLevels.put( player.getName(), Arrays.stream(specialItem.getBranchLevels())
				.map((int lvl) -> lvl == 0 ? 0 : lvl -1)
				.toArray() );
		}

		protected abstract TBranches[] getBranches();
		protected abstract T createItem(Player owner, int[] levels, Game game);

		@Override
		protected final T createItem(Player owner, Game game) {
			int[] levels = new int[getBranches().length];
			if (owner != null && deadPlayerLevels != null && deadPlayerLevels.containsKey(owner.getName())) {
				levels = deadPlayerLevels.get(owner.getName());
			}

			return createItem(owner, levels, game);
		}
	}
}
