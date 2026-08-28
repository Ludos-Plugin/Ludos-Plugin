package fr.ludos.core.gui.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import xyz.xenondevs.invui.item.impl.AbstractItem;

/**
 * {@link AbstractItem} with click-event handler functionality.
 * @param <T> Self type
 */
public abstract class EventItem<T extends EventItem<T>> extends AbstractItem {
	private List<Consumer<ClickType>> clickHandlers;

	public EventItem() { }


	@SuppressWarnings("unchecked")
	@Contract("_ -> this")
	public @NotNull T addClickHandler(@NotNull Runnable clickHandler) {
		if (clickHandlers == null)
			clickHandlers = new ArrayList<>();

		clickHandlers.add(t -> clickHandler.run());
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Contract("_ -> this")
	public @NotNull T setClickHandlers(@NotNull List<@NotNull Consumer<ClickType>> clickHandlers) {
		this.clickHandlers = clickHandlers;
		return (T) this;
	}
	@SuppressWarnings("unchecked")
	@Contract("_ -> this")
	public @NotNull T addClickHandler(@NotNull Consumer<ClickType> clickHandler) {
		if (clickHandlers == null)
			clickHandlers = new ArrayList<>();

		clickHandlers.add(clickHandler);
		return (T) this;
	}


	@Override
	public final void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		if (clickHandlers != null) {
			for (Consumer<ClickType> clickHandler : clickHandlers) {
				clickHandler.accept(clickType);
			}
		}

		handleClickInternal(clickType, player, event);
	}
	public abstract void handleClickInternal(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event);
}
