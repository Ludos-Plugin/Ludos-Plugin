package fr.ludos.core.gui.item;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import xyz.xenondevs.invui.item.impl.AbstractItem;

/**
 * {@link AbstractItem} with click-event handler functionality.
 * @param <T> Self type
 */
public abstract class EventItem<T extends EventItem<T>> extends AbstractItem {
	private List<Runnable> clickHandlers;

	public EventItem() { }


	public @NotNull EventItem<T> setClickHandlers(@NotNull List<@NotNull Runnable> clickHandlers) {
		this.clickHandlers = clickHandlers;
		return this;
	}
	public @NotNull EventItem<T> addClickHandler(@NotNull Runnable clickHandler) {
		if (clickHandlers == null)
			clickHandlers = new ArrayList<>();

		clickHandlers.add(clickHandler);
		return this;
	}


	@Override
	public final void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		if (clickHandlers != null) {
			for (Runnable clickHandler : clickHandlers) {
				clickHandler.run();
			}
		}

		handleClickInternal(clickType, player, event);
	}
	public abstract void handleClickInternal(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event);
}
