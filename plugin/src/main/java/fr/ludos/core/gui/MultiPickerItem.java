package fr.ludos.core.gui;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.PersistentEntry;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.gui.SlotElement.ItemSlotElement;
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem;

/**
 *
 * @param <T>
 * @param <C>
 * @param <Content>
 */
public abstract class MultiPickerItem<T, C extends Collection<T>, Content> extends ControlItem<PagedGui<Content>> {
	private final T value;
	private final PersistentEntry<C> entry;
	private final ConfigSectionContext context;
	private final Supplier<C> constructor;

	public MultiPickerItem(T value, PersistentEntry<C> entry, ConfigSectionContext context, Supplier<C> constructor) {
		this.value = Objects.requireNonNull(value);
		this.entry = Objects.requireNonNull(entry);
		this.context = Objects.requireNonNull(context);
		this.constructor = Objects.requireNonNull(constructor);
	}


	@Override
	public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		ConfigurationSection config = context.getConfig(player);

		C collection = entry.getValueOrNull(config);
		if (collection == null) {
			collection = constructor.get();
		}

		if (collection.contains(value)) {
			collection.remove(value);
		} else {
			collection.add(value);
		}

		if (collection.isEmpty()) {
			entry.unsetValue(config);
		} else {
			entry.setValue(collection, config);
		}

		player.playSound(player.getLocation(), Sound.UI_STONECUTTER_SELECT_RECIPE, 0.1f, 0.7f);
		context.saveConfig();

		for (SlotElement element : getGui().getSlotElements()) {
			if (element instanceof ItemSlotElement item) {
				item.getItem().notifyWindows();
			}
		}
	}
}