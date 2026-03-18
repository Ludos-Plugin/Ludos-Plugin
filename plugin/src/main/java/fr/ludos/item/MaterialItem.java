package fr.ludos.item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.N;

import fr.ludos.game.Game;
import fr.ludos.item.LevelItem.LevelState;
import fr.ludos.item.trapper.TrapperDagger;
import net.kyori.adventure.text.Component;

public class MaterialItem extends SpecialItem {
	private final String id;
	private final Component name;

	public MaterialItem(ItemStack stack, Player owner, Game game, String id, Component name) {
		super(stack, owner, game);
		this.id = id;
		this.name = name;
	}

	@Override
	protected String getId() {
		return id;
	}

	@Override
	protected Component getName() {
		return name;
	}

	public static class Events extends SpecialItem.Events<MaterialItem> {
		private final static Map<ItemStack, MaterialItem> cachedItems = new HashMap<>();
		private final Consumer<MaterialItem> initializer;
		private final String id;
		private final Component name;
		private final Function<HumanEntity, Boolean> canPlayerHaveItem;

		public Events(Game game, Material material, String id, Component name, int slot,
				@Nullable Consumer<MaterialItem> initializer,
				@Nullable Function<HumanEntity, Boolean> canPlayerHaveItem) {
			super(game, slot);
			this.initializer = initializer;
			this.id = id;
			this.name = name;
			this.canPlayerHaveItem = canPlayerHaveItem;
		}

		public Events(Game game, Material material, String id, Component name, int slot) {
			this(game, material, id, name, slot, null, null);
		}

		@Override
		@Nullable
		protected MaterialItem getItem(ItemStack stack, Game game) {
			MaterialItem cached = cachedItems.get(stack);
			if (cached != null)
				return cached;

			Player owner = SpecialItem.getSpecialItemOwner(stack, id, game);
			if (owner == null)
				return null;

			return new MaterialItem(stack, owner, game, id, name);
		}

		@Override
		protected MaterialItem createItem(Player owner, Game game) {
			MaterialItem item = new MaterialItem(new ItemStack(Material.STONE_SWORD), owner, game, id, name);
			item.initializeItem();
			if (initializer != null) {
				initializer.accept(item);
			}

			return item;
		}

		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			if (canPlayerHaveItem != null) {
				return canPlayerHaveItem.apply(owner);
			}
			return true;
		}
	}
}