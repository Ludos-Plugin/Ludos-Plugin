package fr.ludos.core.item.level;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.core.persistence.pdc.LevelValueMapPersistentDataType;
import fr.ludos.core.persistence.pdc.LevelValuePersistentDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * A {@link SpecialItemInterface} with the ability to store a Level and XP through {@link LevelState}(s).
 */
public interface LevelItemInterface extends SpecialItemInterface {
	public static final String LEVEL_KEY_STRING = "level";
	public static final NamespacedKey LEVEL_KEY = new NamespacedKey(Ludos.NAMESPACE, LEVEL_KEY_STRING);

	public static final String MAX_LVL_LABEL = "MAX";
	public LevelState levelState();
	public void onSwitchOffLevel(Integer level);
	public void onSwitchToLevel(Integer level);

	public default void setValue(LevelValue level) {
		levelState().setValue(level);
	}

	public default int level() {
		return levelState().level();
	}
	public default void setLvl(int level) {
		levelState().setLevel(level);
	}
	public default void addLvl(int level) {
		levelState().addLvl(level);
	}

	public default double xp() {
		return levelState().xp();
	}
	public default void setXp(double value) {
		levelState().setXp(value);
	}
	public default void addXp(double xp) {
		levelState().addXp(xp);
	}


	/**
	 * Utility function to handle level switching when player switches levels.
	 * @param <T> The type of the item, must extend {@link SpecialItem}
	 * @param <TLevel> The type of the level, must implement {@link LevelItemInterface.Level}
	 * @param levelItem
	 * @param item The item whose level is being switched
	 * @param level The new level to switch to
	 */
	public static <T extends SpecialItem<T>, TLevel extends Level<TLevel>> void setItemLevel(LevelItemInterface levelItem, SpecialItem<T> item, LevelValue level) {
		final int oldLevel = levelItem.level();
		final int newLevel = level.level();

		saveLevelValue(item.getStack(), level);

		levelItem.onSwitchOffLevel(oldLevel);
		levelItem.onSwitchToLevel(newLevel);

		item.update();
	}

	public static void saveLevelValue(ItemStack stack, LevelValue levelValue) {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return;

		PersistentDataContainer container = meta.getPersistentDataContainer();

		container.set(LevelItem.LEVEL_KEY, LevelValuePersistentDataType.INSTANCE, levelValue);
		stack.setItemMeta(meta);
	}

	public static void saveLevelValues(ItemStack stack, Map<String, LevelValue> levelValues) {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return;

		PersistentDataContainer container = meta.getPersistentDataContainer();

		container.set(LevelItem.LEVEL_KEY, LevelValueMapPersistentDataType.INSTANCE, levelValues);
		stack.setItemMeta(meta);
	}

	public static LevelValue levelFromItemStack(ItemStack stack, Game game) {
		if (stack == null) return null;

		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return null;

		PersistentDataContainer container = meta.getPersistentDataContainer();

		if ( ! container.has(LEVEL_KEY, LevelValuePersistentDataType.INSTANCE) ) return null;
		return container.get(LEVEL_KEY, LevelValuePersistentDataType.INSTANCE);
	}

	public static <T extends SpecialItem<T>> LevelState initializeLevelState(
		LevelItemInterface levelItem, SpecialItem<T> item,
		LevelState levelState,
		BiConsumer<LevelValue, Integer> onLevelChange,
		BiConsumer<LevelValue, Double> onXpChange
	) {
		levelState.addLevelUpListener( (lvlValue, oldLevel) -> {
			int newLevel = lvlValue.level();
			levelItem.onSwitchOffLevel(oldLevel);
			levelItem.onSwitchToLevel(newLevel);

			if (newLevel == oldLevel) return;
			onLevelChange.accept(lvlValue, oldLevel);
			item.updateLore();

			if (newLevel > oldLevel) {
				item.getOwner().sendMessage(
					LevelItemInterface.getLevelUpMessage(item)
				);
			}
		} );
		levelState.addXpChangeListener( (lvlValue, oldXp) -> {
			double newXp = lvlValue.xp();

			if (newXp == oldXp) return;
			onXpChange.accept(lvlValue, oldXp);
			item.updateLore();
		} );
		return levelState;
	}
	public static <T extends SpecialItem<T>, TLevel extends Level<TLevel>> LevelState initializeLevelState(LevelItemInterface levelItem, SpecialItem<T> item, List<TLevel> levels, LevelValue levelValue) {
		LevelState levelState = LevelState.capped(levelValue, l -> levels.get(l).xpThreshold(), levels.size() - 1);
		return initializeLevelState(
			levelItem, item,
			levelState,
			(lvlValue, oldLevel) -> LevelItemInterface.saveLevelValue(item.getStack(), lvlValue),
			(lvlValue, oldXp) -> LevelItemInterface.saveLevelValue(item.getStack(), lvlValue)
		);
	}

	public static <T extends SpecialItem<T>> @NotNull TextComponent getLevelUpMessage(SpecialItem<T> item) { // TODO: Translate
		return Component.text("Your ")
			.color(NamedTextColor.GREEN)
		.append(item.getName())
		.append(
			Component.text(" has leveled up!")
			.color(NamedTextColor.GREEN)
		);
	}

	public static <TLevel extends Enum<TLevel> & Level<TLevel>> Component getXpLoreField(boolean isMax, double levelThreshold, double xp) {
		String xpLabel;
		if ( isMax ) {
			xpLabel = MAX_LVL_LABEL;
		} else {
			String xpRounded = Double.toString(Math.round(xp * 100.0) / 100.0);
			xpLabel = xpRounded + '/' + levelThreshold;
		}

		return
			Component.text("XP: ")
				.color(NamedTextColor.GRAY)
			.append(Component.text(xpLabel)
				.color(NamedTextColor.RED))
			.decoration(TextDecoration.ITALIC, false);
	}
	public static <TLevel extends Enum<TLevel> & Level<TLevel>> Component getXpLoreField(LevelItemInterface item) {
		int maxLevel = item.levelState().maxLevel();
		int levelNum = item.level();

		boolean isMax = maxLevel >= 0 && levelNum >= maxLevel;
		double levelThreshold = isMax ? 0 : item.levelState().xpThreshold(levelNum);

		return getXpLoreField(isMax, levelThreshold, item.xp());
	}

	public static <TLevel extends Enum<TLevel> & Level<TLevel>> Component getLevelLoreField(int levelIndex) {
		return
			Component.text("Level: ")
				.color(NamedTextColor.GRAY)
			.append(Component.text(levelIndex + 1)
				.color(NamedTextColor.RED))
			.decoration(TextDecoration.ITALIC, false);
	}
	public static <TLevel extends Enum<TLevel> & Level<TLevel>> Component getLevelLoreField(LevelItemInterface item) {
		return getLevelLoreField(item.level());
	}

	/**
	 * Events for {@link LevelItemInterface}.
	 * @param <T> The type of {@link Level} the item uses
	 */
	public static interface Level<T extends Level<T>> {
		public double xpThreshold();

		/**
		 * Called when item is equipped (mainhand) while the level is set.
		 * @param item The item being equipped
		 */
		public void onEquip(SpecialItemInterface item);
		/**
		 * Called when item is unequipped (mainhand) while the level is set.
		 * @param item The item being unequipped
		 */
		public void onUnequip(SpecialItemInterface item);

		/**
		 * Called when the level is set on the item, including when the item is created with a non-zero level. Should be used to apply the level's effects.
		 * @param item The item on which the level is being set
		 */
		public void onSwitchToLevel(SpecialItemInterface item);
		/**
		 * Called when the level is unset on the item, including when the item is created with a non-zero level. Should be used to remove the level's effects.
		 * @param item The item on which the level is being unset
		 */
		public void onSwitchOffLevel(SpecialItemInterface item);
	}
}
