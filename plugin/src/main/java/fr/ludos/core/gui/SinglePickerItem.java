package fr.ludos.core.gui;

import java.util.Objects;

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
 */
public abstract class SinglePickerItem<T, C> extends ControlItem<PagedGui<C>> {
	private final T value;
	private final PersistentEntry<T> entry;
	private final ConfigSectionContext context;

	public SinglePickerItem(T value, PersistentEntry<T> entry, ConfigSectionContext context) {
		this.value = Objects.requireNonNull(value);
		this.entry = Objects.requireNonNull(entry);
		this.context = Objects.requireNonNull(context);
	}


	@Override
	public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		ConfigurationSection config = context.getConfig(player);
		T currentValue = entry.getValueOrNull(config);
		if (currentValue == value) {
			entry.unsetValue(config);
		} else {
			entry.setValue(value, config);
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