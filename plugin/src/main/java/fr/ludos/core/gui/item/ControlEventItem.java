package fr.ludos.core.gui.item;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem;

/**
 * {@link AbstractItem} with click-event handler functionality.
 * @param <T> Self type
 * @param <G> The type of Gui
 */
public abstract class ControlEventItem<T extends ControlEventItem<T, G>, G extends Gui> extends ControlItem<G> {
	private List<Runnable> clickHandlers;

	public ControlEventItem() { }


	@SuppressWarnings("unchecked")
	@Contract("_ -> this")
	public @NotNull T setClickHandlers(@NotNull List<@NotNull Runnable> clickHandlers) {
		this.clickHandlers = clickHandlers;
		return (T) this;
	}
	@SuppressWarnings("unchecked")
	@Contract("_ -> this")
	public @NotNull T addClickHandler(@NotNull Runnable clickHandler) {
		if (clickHandlers == null)
			clickHandlers = new ArrayList<>();

		clickHandlers.add(clickHandler);
		return (T) this;
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
