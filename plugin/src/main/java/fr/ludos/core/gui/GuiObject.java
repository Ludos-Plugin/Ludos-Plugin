package fr.ludos.core.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.window.Window;

/**
 * Interface for objects which can be represented inside {@link Window}s.
 */
public interface GuiObject extends Named {
	public @NotNull AbstractItemBuilder<?> createItem(Player player);
	public default AbstractItemBuilder<?> displayItem(Player player) {
		AbstractItemBuilder<?> builder = createItem(player);
		builder.setDisplayName(new AdventureComponentWrapper(normalizedDisplayName()));
		builder.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
		return builder;
	}
	public default AbstractItemBuilder<?> displayItem(Player player, GuiContext context) {
		return displayItem(player);
	}
}
