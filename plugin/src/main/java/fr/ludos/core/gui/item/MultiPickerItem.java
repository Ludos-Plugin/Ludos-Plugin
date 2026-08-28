package fr.ludos.core.gui.item;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.persistence.PersistentAccessor;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.gui.SlotElement.ItemSlotElement;
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem;

/**
 * Used to pick multiple values at a time, and aggregate in a single collection, to be persisted via a Collection {@link PersistentAccessor}.<br>
 * When clicked, adds
 * @param <T> The type of values to pick.
 * @param <C> The collection type, used to aggregate all selected values.
 * @param <G> The type of Gui.
 */
public abstract class MultiPickerItem<T, C extends Collection<T>, G extends Gui> extends ControlItem<G> {
	private final T value;
	private final PersistentAccessor<C> entry;
	private final Supplier<C> constructor;

	public MultiPickerItem(T value, PersistentAccessor<C> entry, Supplier<C> constructor) {
		this.value = Objects.requireNonNull(value);
		this.entry = Objects.requireNonNull(entry);
		this.constructor = Objects.requireNonNull(constructor);
	}


	@Override
	public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		C collection = entry.getOrNull();
		if (collection == null) {
			collection = constructor.get();
		}

		if (collection.contains(value)) {
			collection.remove(value);
		} else {
			collection.add(value);
		}

		if (collection.isEmpty()) {
			entry.unset();
		} else {
			entry.set(collection);
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