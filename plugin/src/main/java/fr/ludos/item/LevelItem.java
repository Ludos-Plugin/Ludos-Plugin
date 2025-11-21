package fr.ludos.item;

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


public abstract class LevelItem<TLevel extends SpecialItemLevels<TLevel>> extends SpecialItem {

	public LevelItem(ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);
		this.levelKey = new NamespacedKey(game.getPlugin(), LEVEL);
		this.xpKey = new NamespacedKey(game.getPlugin(), XP);
	}

	public static final String LEVEL = "level";
	public static final String XP = "xp";
	private final NamespacedKey levelKey;
	private final NamespacedKey xpKey;

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

	public static @Nullable Pair<Integer, Double> fromLevelItemStack(ItemStack stack, String id, Game game) {
		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();

		NamespacedKey levelKey = new NamespacedKey(game.getPlugin(), LEVEL);
		if ( ! container.has(levelKey, PersistentDataType.INTEGER) ) return null;

		NamespacedKey xpKey = new NamespacedKey(game.getPlugin(), XP);
		if ( ! container.has(xpKey, PersistentDataType.DOUBLE) ) return null;

		int level = getPersistentData(stack, levelKey, PersistentDataType.INTEGER);
		double xp = getPersistentData(stack, xpKey, PersistentDataType.DOUBLE);

		return Pair.of(level, xp);
	}

	public LevelItem(ItemStack stack, Player owner, TLevel level, Game game) {
		this(stack, owner, level, 0, game);
	}

	public LevelItem(ItemStack stack, Player owner, TLevel level, double xp, Game game) {
		super(stack, owner, game);

		this.levelKey = new NamespacedKey(game.getPlugin(), LEVEL);
		this.xpKey = new NamespacedKey(game.getPlugin(), XP);

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
	public List<Component> getLore() {
		List<Component> lore = super.getLore();

		String xpLabel;
		if ( level.isMax() ) {
			xpLabel = MAX_LVL_LABEL;
		} else {
			String xpRounded = Double.toString(Math.round(xp * 100.0) / 100.0);
			xpLabel = xpRounded + '/' + level.getXpThreshold();
		}

		lore.add(
			Component.text("XP: ")
				.color(NamedTextColor.GRAY)
			.append(Component.text(xpLabel)
				.color(NamedTextColor.RED))
			.decoration(TextDecoration.ITALIC, false)
		);

		return lore;
	}


	public static abstract class Events<T extends LevelItem<TLevels>, TLevels extends SpecialItemLevels<TLevels>> extends SpecialItem.Events<T> {
		protected final TLevels baseLevel;
		private Map<String, TLevels> deadPlayerLevels = new HashMap<>();

		protected Events(Game game, TLevels baseLevel, @Nullable Integer slot, boolean canDrop) {
			super(game, slot, canDrop);
			if (baseLevel == null) {
				throw new IllegalArgumentException("Base level cannot be null");
			}

			this.baseLevel = baseLevel;
			this.deadPlayerLevels = new HashMap<>();
		}
		protected Events(Game game, TLevels baseLevel, @Nullable Integer slot) {
			this(game, baseLevel, slot, false);
		}
		protected Events(Game game, TLevels baseLevel) {
			this(game, baseLevel, null, false);
		}


		protected abstract T createItem(Player owner, TLevels level, Game game);

		@EventHandler
		public void onPlayerDeath(PlayerDeathEvent event) {
			Player player = event.getEntity();
			if (! canPlayerHaveItem(player)) return;

			T specialItem = SpecialItem.findIn(player.getInventory(), (ItemStack stack) -> getItem(stack, game));
			if ( specialItem == null ) {
				return;
			}

			deadPlayerLevels.put( player.getName(), specialItem.getLevel().getPrevious() );
		}

		@Override
		protected final T createItem(Player owner, Game game) {
			TLevels level = this.baseLevel;

			if (owner != null && deadPlayerLevels != null && deadPlayerLevels.containsKey(owner.getName())) {
				TLevels deadLevels = deadPlayerLevels.get(owner.getName());
				level = deadLevels != null ? deadLevels : level;
			}

			return createItem(owner, level, game);
		}
	}
}
