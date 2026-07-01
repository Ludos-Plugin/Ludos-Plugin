package fr.ludos.core.item.level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import net.kyori.adventure.text.Component;

public abstract class LevelItem<TLevel extends Enum<TLevel> & LevelItem.Level<TLevel>> extends SpecialItem implements LevelItemInterface {
	private final LevelState levelState;
	public LevelState levelState() {
		return levelState;
	}
	private final TLevel[] levels;

	public TLevel getLvlObject() {
		return levels[levelState.level()];
	}
	public TLevel levelObject(int level) {
		return levels[level];
	}

	public LevelItem(Class<TLevel> levelClass, LevelValue level, ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);

		this.levels = levelClass.getEnumConstants();
		this.levelState = new LevelState(level, l -> levelObject(l).xpThreshold(), levels.length - 1);

		this.levelState.addLevelUpListener( (lvlState, oldLevel) -> {
			TLevel oldLevelObj = levels[oldLevel];
			oldLevelObj.onUnsetLevel(this);

			TLevel newLevelObj = getLvlObject();
			newLevelObj.onSetLevel(this);

			if (lvlState.level() <= oldLevel) return;

			getOwner().sendMessage(
				LevelItemInterface.getLevelUpMessage(this)
			);
		} );
		this.levelState.addXpChangeListener( (lvlState) -> {
			LevelItemInterface.saveLevelState(this, lvlState);
			updateLore();
		} );
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

	public static abstract class Events<T extends LevelItem<TLevel>, TLevel extends Enum<TLevel> & Level<TLevel>> extends SpecialItem.Events<T> {
		protected final Map<Player, LevelValue> deadPlayerLevels = new HashMap<>();

		protected Events(Game game, @Nullable ItemSlot slot, boolean canDrop) {
			super(game, slot, canDrop);
		}
		protected Events(Game game, @Nullable ItemSlot slot) {
			this(game, slot, false);
		}
		protected Events(Game game) {
			this(game, null, false);
		}


		public abstract T createItem(Player owner, LevelValue level);

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

		@Override
		public T createItem(Player owner) {
			if (!deadPlayerLevels.containsKey(owner)) {
				return createItem(owner, new LevelValue());
			}

			LevelValue deadLevels = deadPlayerLevels.get(owner);
			return createItem(owner, deadLevels);
		}
	}
}
