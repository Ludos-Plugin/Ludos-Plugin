package fr.ludos.core.item.level;

import java.util.function.Function;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import fr.ludos.core.item.MultiLevelBranchItem;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.core.persistence.LevelValuePersistentDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public interface LevelItemInterface extends SpecialItemInterface {
	public static final String LEVEL = "level";
	public static final NamespacedKey levelKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), LEVEL);

	public static final String MAX_LVL_LABEL = "MAX";
	public LevelState levelState();

	public default int level() {
		return levelState().level();
	}
	public default void setLvl(int level) {
		levelState().setLevel(level);
	}
	public default void addLvl(int level) {
		levelState().addLvl(level);
	}

	public default double getXp() {
		return levelState().xp();
	}
	public default void setXp(double value) {
		levelState().setXp(value);
	}
	public default void addXp(double xp) {
		levelState().addXp(xp);
	}

	public static LevelValue levelFromItemStack(ItemStack stack, Game game) {
		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();

		if ( ! container.has(levelKey, LevelValuePersistentDataType.INSTANCE) ) return null;

		return container.get(levelKey, LevelValuePersistentDataType.INSTANCE);
	}
	public static void saveLevelState(SpecialItem item, LevelState levelState) {
		ItemMeta meta = item.getStack().getItemMeta();
		meta.getPersistentDataContainer().set(LevelItem.levelKey, LevelValuePersistentDataType.INSTANCE, levelState.value());
		item.getStack().setItemMeta(meta);
	}

	public static @NotNull TextComponent getLevelUpMessage(SpecialItem item) { // TODO: Translate
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
	public static <TLevel extends Enum<TLevel> & Level<TLevel>> Component getXpLoreField(LevelItem<TLevel> item) {
		int maxLevel = item.levelState().maxLevel();
		int levelNum = item.level();
		TLevel level = item.levelObject(levelNum);

		boolean isMax = maxLevel >= 0 && levelNum >= maxLevel;
		double levelThreshold = isMax ? 0 : level.xpThreshold();

		return getXpLoreField(isMax, levelThreshold, item.levelState().xp());
	}

	public static <TLevel extends Enum<TLevel> & Level<TLevel>> Component getLevelLoreField(int levelIndex) {
		return
			Component.text("Level: ")
				.color(NamedTextColor.GRAY)
			.append(Component.text(levelIndex + 1)
				.color(NamedTextColor.RED))
			.decoration(TextDecoration.ITALIC, false);
	}
	public static <TLevel extends Enum<TLevel> & Level<TLevel>> Component getLevelLoreField(LevelItem<TLevel> item) {
		return getLevelLoreField(item.levelState().level());
	}
	public static <TBranch extends MultiLevelBranchItem.Branch> Component getBranchLevelLoreField(MultiLevelBranchItem<TBranch> item) {
		return getLevelLoreField(item.levelState().level());
	}


	/**
	 * Utility function to handle level switching when player switches leveles.
	 * @param <TItem> The type of the item, must extend SpecialItem
	 * @param <TLevel> The type of the level, must be an enum that implements LevelItem.Level
	 * @param item The item whose level is being switched
	 * @param newLevel The new level to switch to
	 * @param newXp The new XP value to set
	 */
	public static <TItem extends LevelItem<TLevel>, TLevel extends Enum<TLevel> & Level<TLevel>> void setItemLevel(TItem item, LevelValue level) {
		TLevel oldLevel = item.getLvlObject();
		TLevel newLevel = item.levelObject(level.level());

		ItemStack itemStack = item.getStack();

		ItemMeta meta = itemStack.getItemMeta();
		meta.getPersistentDataContainer().set(levelKey, LevelValuePersistentDataType.INSTANCE, level);
		itemStack.setItemMeta(meta);

		item.setLvl(level.level());

		if (oldLevel != null) {
			oldLevel.onUnsetLevel(item);
		}
		newLevel.onSetLevel(item);

		item.update();
	}

	/**
	 * Utility function to handle level switching when player switches items.
	 * @param <TItem> The type of the item, must extend SpecialItem
	 * @param <TLevel> The type of the level, must be an enum that implements LevelItem.Level
	 * @param event The PlayerItemHeldEvent to handle
	 * @param getItem An "SpecialItem from ItemStack" function, used to get the item being switched from/to
	 * @param getLevel A "Level from Item" function, used to get the level of the item being switched from/to
	 */
	public static <TItem extends LevelItem<TLevel>, TLevel extends Enum<TLevel> & LevelItem.Level<TLevel>> void onSwitchItem(PlayerItemHeldEvent event, Function<ItemStack, TItem> getItem) {
		Player player = event.getPlayer();

		TItem oldItem = getItem.apply(player.getInventory().getItem(event.getPreviousSlot()));
		TItem newItem = getItem.apply(player.getInventory().getItem(event.getNewSlot()));
		if (oldItem == null && newItem == null) return;

		if (oldItem != null) {
			oldItem.getLvlObject().onUnequip(oldItem);
		}
		if (newItem != null) {
			newItem.getLvlObject().onEquip(newItem);
		}
	}


	public static interface Level<T extends Enum<T> & Level<T>> {
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
		public void onSetLevel(SpecialItemInterface item);
		/**
		 * Called when the level is unset on the item, including when the item is created with a non-zero level. Should be used to remove the level's effects.
		 * @param item The item on which the level is being unset
		 */
		public void onUnsetLevel(SpecialItemInterface item);
	}
}
