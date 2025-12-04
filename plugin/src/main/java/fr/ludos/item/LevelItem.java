package fr.ludos.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public abstract class LevelItem<TLevel extends LevelItem.Level<TLevel>> extends SpecialItem {
	public static final String LEVEL = "level";
	public static final NamespacedKey levelKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), LEVEL);

	public static final String XP = "xp";
	public static final NamespacedKey xpKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), XP);

	public static final String MAX_LVL_LABEL = "MAX";


	private @Nullable TLevel level;
	public @Nullable TLevel getLevel() {
		return level;
	}

	private double xp;
	public double getXp() {
		return xp;
	}

	public abstract TLevel convertToLevel(int level);

	public static @Nullable Pair<Integer, Double> fromLevelItemStack(ItemStack stack, String id, Game game) {
		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();

		if ( ! container.has(levelKey, PersistentDataType.INTEGER) ) return null;
		if ( ! container.has(xpKey, PersistentDataType.DOUBLE) ) return null;

		int level = getPersistentData(stack, levelKey, PersistentDataType.INTEGER);
		double xp = getPersistentData(stack, xpKey, PersistentDataType.DOUBLE);

		return Pair.of(level, xp);
	}


	public LevelItem(ItemStack stack, Player owner, @Nullable TLevel level, double xp, Game game) {
		super(stack, owner, game);

		this.level = level;
		this.xp = xp;
	}

	public LevelItem(ItemStack stack, Player owner, @Nullable TLevel level, Game game) {
		this(stack, owner, level, 0, game);
	}

	public LevelItem(ItemStack stack, Player owner, Game game) {
		this(stack, owner, null, 0, game);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		setLvl(level != null ? level : convertToLevel(0));
		setXp(xp);
	}


	public void setLvl(TLevel level) {
		ItemMeta meta = getStack().getItemMeta();
		meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level.index());
		getStack().setItemMeta(meta);

		this.level = level;
	}

	public void addLvl() {
		if ( level.getNext() == null ) return;

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

	public void setXp(double xp) {
		double scaledTreshold = level.getXpThreshold();
		while (xp >= scaledTreshold) {
			addLvl();
			xp -= scaledTreshold;
		}

		ItemMeta meta = getStack().getItemMeta();
		meta.getPersistentDataContainer().set(xpKey, PersistentDataType.DOUBLE, xp);
		getStack().setItemMeta(meta);

		this.xp = xp;

		updateLore();
	}

	public void addXp(double xp) {
		if ( level.getNext() == null ) return;

		double newXp = this.xp + xp;
		setXp(newXp);
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();
		lore.add(getLevelLoreField(level, xp));

		return lore;
	}
	public static <TLevel extends Level<TLevel>> Component getLevelLoreField(TLevel level, double xp) {
		String xpLabel;
		if ( level.getNext() == null ) {
			xpLabel = MAX_LVL_LABEL;
		} else {
			String xpRounded = Double.toString(Math.round(xp * 100.0) / 100.0);
			xpLabel = xpRounded + '/' + level.getXpThreshold();
		}

		return
			Component.text("XP: ")
				.color(NamedTextColor.GRAY)
			.append(Component.text(xpLabel)
				.color(NamedTextColor.RED))
			.decoration(TextDecoration.ITALIC, false);
	}

	public static interface Level<T extends Level<T>> {
		public int index();
		public double getXpThreshold();

		public @Nullable T getPrevious();
		public @Nullable T getNext();
	}

	public static abstract class Events<T extends LevelItem<TLevel>, TLevel extends Level<TLevel>> extends SpecialItem.Events<T> {
		protected final TLevel baseLevel;
		private Map<String, TLevel> deadPlayerLevels = new HashMap<>();

		protected Events(Game game, TLevel baseLevel, @Nullable Integer slot, boolean canDrop) {
			super(game, slot, canDrop);
			if (baseLevel == null) {
				throw new IllegalArgumentException("Base level cannot be null");
			}

			this.baseLevel = baseLevel;
			this.deadPlayerLevels = new HashMap<>();
		}
		protected Events(Game game, TLevel baseLevel, @Nullable Integer slot) {
			this(game, baseLevel, slot, false);
		}
		protected Events(Game game, TLevel baseLevel) {
			this(game, baseLevel, null, false);
		}


		protected abstract T createItem(Player owner, TLevel level, Game game);

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
		protected T createItem(Player owner, Game game) {
			TLevel level = this.baseLevel;

			if (owner != null && deadPlayerLevels != null && deadPlayerLevels.containsKey(owner.getName())) {
				TLevel deadLevels = deadPlayerLevels.get(owner.getName());
				level = deadLevels != null ? deadLevels : level;
			}

			return createItem(owner, level, game);
		}
	}
}
