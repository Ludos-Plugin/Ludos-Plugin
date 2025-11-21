package fr.ludos.item;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import fr.ludos.game.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public abstract class BranchLevelItem<TBranches extends SpecialItemLevelBranches<TBranches>> extends BranchItem<TBranches> {
	public static final String LEVELS = "levels";
	private NamespacedKey levelsKey = new NamespacedKey(getGame().getPlugin(), LEVELS);

	public static final String XP = "xp";
	private NamespacedKey xpKey = new NamespacedKey(getGame().getPlugin(), XP);

	private static final String MAX_LVL_LABEL = "MAX";


	private int[] branchLevels;

	public int[] getBranchLevels() {
		return branchLevels;
	}
	public int getCurrentBranchLevel() {
		return branchLevels[getBranch().index()];
	}

	private double[] branchXps;

	public double[] getXps() {
		return branchXps;
	}
	public double getCurrentBranchXp() {
		return branchXps[getBranch().index()];
	}

	public static @Nullable Pair<int[], double[]> levelsFromItemStack(ItemStack stack, String id, Game game) {
		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();

		NamespacedKey levelsKey = new NamespacedKey(game.getPlugin(), LEVELS);
		if ( ! container.has(levelsKey, PersistentDataType.INTEGER_ARRAY) ) return null;

		NamespacedKey xpKey = new NamespacedKey(game.getPlugin(), XP);
		if ( ! container.has(xpKey, DoubleArrayPersistentDataType.INSTANCE) ) return null;

		int[] levels = getPersistentData(stack, levelsKey, PersistentDataType.INTEGER_ARRAY);
		double[] xps = getPersistentData(stack, xpKey, DoubleArrayPersistentDataType.INSTANCE);

		return Pair.of(levels, xps);
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
			getOwner().sendMessage(
				Component.text("Your ")
					.color(NamedTextColor.GREEN)
				.append(getName())
				.append(
					Component.text(" has leveled up!")
					.color(NamedTextColor.GREEN)
				)
			); // TODO: Translate
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
	protected List<Component> getLore() {
		List<Component> lore = super.getLore();

		int branchCount = getBranches().length;
		if (branchLevels == null || branchXps == null || branchLevels.length < branchCount || branchXps.length < branchCount) {
			return lore;
		}
		int currentLevel = getCurrentBranchLevel();

		String xpLabel;
		if ( getBranch().isMax(currentLevel) ) {
			xpLabel = MAX_LVL_LABEL;
		} else {
			String xpRounded = Double.toString(Math.round(getCurrentBranchXp() * 100.0) / 100.0);
			xpLabel = xpRounded + '/' + getBranch().getXpThreshold();
		}

		lore.add(
			Component.text("XP: ")
				.color(NamedTextColor.GRAY)
			.append(Component.text(xpLabel)
				.color(NamedTextColor.YELLOW))
			.decoration(TextDecoration.ITALIC, false)
		);

		lore.add(
			Component.text("Level: ")
				.color(NamedTextColor.GRAY)
			.append(Component.text(Integer.toString(currentLevel + 1))
				.color(NamedTextColor.RED))
			.decoration(TextDecoration.ITALIC, false)
		);

		return lore;
	}


	public static abstract class Events<T extends BranchLevelItem<TBranches>, TBranches extends SpecialItemLevelBranches<TBranches>> extends BranchItem.Events<T, TBranches> {
		private Map<String, int[]> deadPlayerLevels;

		protected Events(Game game, @Nullable Integer slot, boolean canDrop) {
			super(game, slot, canDrop);

			this.deadPlayerLevels = new HashMap<>();
		}
		public Events(Game game, @Nullable Integer slot) {
			this(game, slot, false);
		}
		public Events(Game game) {
			this(game, null, false);
		}

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
