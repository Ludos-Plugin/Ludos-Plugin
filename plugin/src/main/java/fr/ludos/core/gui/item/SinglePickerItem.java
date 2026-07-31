package fr.ludos.core.gui.item;

import java.util.Objects;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.persistence.PersistentAccessor;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.gui.SlotElement.ItemSlotElement;
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem;

/**
 * Used to pick a single value, to be persisted via a {@link PersistentAccessor}.<br>
 * When clicked, adds
 * @param <T> The type of value to pick.
 * @param <Content> The type of Gui Content.
 */
public abstract class SinglePickerItem<T, Content> extends ControlItem<PagedGui<Content>> {
	private final T value;
	private final PersistentAccessor<T> entry;

	public SinglePickerItem(T value, PersistentAccessor<T> entry) {
		this.value = Objects.requireNonNull(value);
		this.entry = Objects.requireNonNull(entry);
	}


	@Override
	public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		T currentValue = entry.getOrNull();
		if (currentValue == value) {
			entry.unset();
		} else {
			entry.set(value);
		}
		WindowProvider.playClickSound(player);
		entry.save();

		for (SlotElement element : getGui().getSlotElements()) {
			if (element instanceof ItemSlotElement item) {
				item.getItem().notifyWindows();
			}
		}
	}
}