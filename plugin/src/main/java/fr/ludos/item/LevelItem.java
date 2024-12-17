package fr.ludos.item;

import org.bukkit.persistence.*;

import fr.ludos.Ludos;
import fr.ludos.game.Game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public abstract class LevelItem<TLevel extends SpecialItemLevels<TLevel>> extends SpecialItem {
	public static final String LEVEL = "level";
	private NamespacedKey levelKey = new NamespacedKey(Ludos.getInstance(), LEVEL);

	public static final String XP = "xp";
	private NamespacedKey xpKey = new NamespacedKey(Ludos.getInstance(), XP);

	private static final String MAX_LVL_LABEL = "MAX";

	private TLevel level;
	private double xp;

	public TLevel getLevel() {
		return level;
	}
	public double getXp() {
		return xp;
	}

	public abstract TLevel convertToLevel(int level);

	public LevelItem(ItemStack stack) throws IllegalArgumentException {
		super(stack);

		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
		if ( ! container.has(levelKey, PersistentDataType.INTEGER) ) {
			throw new IllegalArgumentException("Level Not found");
		}
		if ( ! container.has(xpKey, PersistentDataType.DOUBLE) ) {
			throw new IllegalArgumentException("XP not found");
		}

		this.level = convertToLevel(getPersistentData(stack, levelKey, PersistentDataType.INTEGER));
		this.xp = getPersistentData(stack, xpKey, PersistentDataType.DOUBLE);
	}

	public LevelItem(ItemStack stack, Player owner, TLevel level) {
		this(stack, owner, level, 0);
	}

	public LevelItem(ItemStack stack, Player owner, TLevel level, double xp) {
		super(stack, owner);

		setLvl(level);
		setXp(xp);
	}


	public void setLvl(TLevel level) {
		ItemMeta meta = getStack().getItemMeta();

		this.level = level;
		meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level.index());
		getStack().setItemMeta(meta);
	}

	public void addLvl() {
		if (getLevel().isMax()) {
			return;
		}

		setLvl(getLevel().getNext());
		if (getOwner() != null) {
			getOwner().sendMessage(ChatColor.GREEN + "Your " + getName() + ChatColor.RESET + ChatColor.GREEN + " has leveled up!"); // TODO: Translate
		}

		updateLore();
	}

	public void setXp(double value) {

		double scaledTreshold = level.getXpThreshold();
		if (value >= scaledTreshold) {
			addLvl();
			value -= scaledTreshold;
		}

		ItemMeta meta = getStack().getItemMeta();

		this.xp = value;
		meta.getPersistentDataContainer().set(xpKey, PersistentDataType.DOUBLE, xp);
		getStack().setItemMeta(meta);

		updateLore();
	}

	public void addXp(double xp) {
		if (level.isMax()) {
			return;
		}

		double newXp = this.xp + xp;
		setXp(newXp);
	}

	@Override
	public List<String> getLore() {
		List<String> lore = super.getLore();
		if (lore == null) {
			lore = new ArrayList<String>();
		}
		String xpFormatted = ChatColor.GRAY + "XP: " + ChatColor.YELLOW;

		if ( level.isMax() ) {
			xpFormatted += MAX_LVL_LABEL;
		} else {
			String xpRounded = Double.toString(Math.round(xp * 100.0) / 100.0);
			xpFormatted += xpRounded + '/' + level.getXpThreshold();
		}

		lore.add(xpFormatted);
		return lore;
	}


	public static abstract class Events<T extends LevelItem<TLevels>, TLevels extends SpecialItemLevels<TLevels>> extends SpecialItem.Events<T> {

		protected final TLevels baseLevel;
		private Map<String, TLevels> deadPlayerLevels = new HashMap<>();


		public Events(Game game, TLevels baseLevel) {
			super(game);
			if (baseLevel == null) {
				throw new IllegalArgumentException("Base level cannot be null");
			}

			this.baseLevel = baseLevel;
			this.deadPlayerLevels = new HashMap<>();
		}


		protected abstract T createItem(Player owner, TLevels level);

		@EventHandler
		public void onPlayerDeath(PlayerDeathEvent event) {
			Player player = event.getEntity();
			if (! canPlayerHaveItem(player)) return;

			T specialItem = SpecialItem.findIn(player.getInventory(), this::getItem);
			if ( specialItem == null ) {
				return;
			}

			deadPlayerLevels.put( player.getName(), specialItem.getLevel().getPrevious() );
		}

		@Override
		protected final T createItem(Player owner) {
			TLevels level = this.baseLevel;

			if (owner != null && deadPlayerLevels != null && deadPlayerLevels.containsKey(owner.getName())) {
				TLevels deadLevels = deadPlayerLevels.get(owner.getName());
				level = deadLevels != null ? deadLevels : level;
			}

			return createItem(owner, level);
		}
	}
}
