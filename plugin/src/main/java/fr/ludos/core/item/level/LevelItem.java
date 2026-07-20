package fr.ludos.core.item.level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.SpecialItem;
import net.kyori.adventure.text.Component;

/**
 * A {@link LevelItemInterface} wrapper for {@link ItemStack}s.
 * @param <TLevel> The type of {@link LevelItemInterface.Level} the item uses
 */
public abstract class LevelItem<TLevel extends LevelItemInterface.Level<TLevel>> extends SpecialItem implements LevelItemInterface {
	private final LevelState levelState;
	public LevelState levelState() {
		return levelState;
	}
	private final List<TLevel> levels;

	public TLevel lvlObject() {
		return levels.get(levelState.level());
	}
	public TLevel lvlObject(int level) {
		return levels.get(level);
	}

	@Override
	public void onSwitchToLevel(Integer level) {
		lvlObject(level).onSwitchToLevel(this);
	}
	@Override
	public void onSwitchOffLevel(Integer level) {
		lvlObject(level).onSwitchOffLevel(this);
	}

	public LevelItem(List<TLevel> levels, LevelValue level, ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);

		this.levels = levels;
		this.levelState = LevelItemInterface.initializeLevelState(this, levels, level);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		LevelItemInterface.setItemLevel(this, levelState().value());
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();
		lore.add(LevelItemInterface.getLevelLoreField(this));
		lore.add(LevelItemInterface.getXpLoreField(this));

		return lore;
	}

	/**
	 * Events for {@link LevelItem}.
	 * @param <T> The type of {@link LevelItem}
	 * @param <TLevel> The type of {@link Level} the item uses
	 */
	public static abstract class Events<T extends LevelItem<TLevel>, TLevel extends Enum<TLevel> & Level<TLevel>> extends SpecialItem.Events<T> {
		protected final Map<Player, LevelValue> deadPlayerLevels = new HashMap<>();

		protected Events(Game game, Events.Info info) {
			super(game, info);
		}

		public abstract List<TLevel> getLevels();


		@EventHandler
		public void onPlayerDeath(PlayerDeathEvent event) {
			Player player = event.getEntity();
			if (! isPlayerValid(player)) return;

			T specialItem = SpecialItem.findIn(player.getInventory(), this::getItem);
			if ( specialItem == null ) {
				return;
			}

			deadPlayerLevels.put(player, specialItem.levelState().value());
		}

		public abstract T createItem(LevelValue level, Player owner);
		@Override
		public T createItem(Player owner) {
			// if (!deadPlayerLevels.containsKey(owner)) {
			// 	return createItem(new LevelValue(), owner);
			// }

			// LevelValue deadLevels = deadPlayerLevels.get(owner);
			return createItem(new LevelValue(), owner);
		}
	}
}
